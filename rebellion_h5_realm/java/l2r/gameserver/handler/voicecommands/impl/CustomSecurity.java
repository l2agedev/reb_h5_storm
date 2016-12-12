package l2r.gameserver.handler.voicecommands.impl;

import l2r.commons.util.Base64;
import l2r.gameserver.Config;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2r.gameserver.instancemanager.BetaServer;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.GameClient.GameClientState;
import l2r.gameserver.network.serverpackets.CharacterSelectionInfo;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.RestartResponse;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.randoms.CharacterIntro;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.AutoBan;
import l2r.gameserver.utils.TimeUtils;

import gov.nasa.worldwind.util.StringUtil;

import java.security.MessageDigest;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;

import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;

public class CustomSecurity extends Functions implements IVoicedCommandHandler
{
	private static Session SESSION = null;
	static
	{
		Properties props = new Properties();
		props.put("mail.smtp.host", Config.SMTP_SERVER);
		props.put("mail.smtp.port", Config.SMTP_SERVER_PORT);
		props.put("mail.smtp.auth", Config.SMTP_SERVER_AUTH);
		props.put("mail.smtp.timeout", Config.SMTP_SERVER_TIMEOUT);
		props.put("mail.smtp.connectiontimeout", Config.SMTP_SERVER_CONNECTION_TIMEOUT);
		
		switch (Config.SMTP_SERVER_SECUIRTY)
		{
			case "TLS":
			{
				props.put("mail.smtp.starttls.enable", "true");
				props.put("mail.smtp.socketFactory.port", Config.SMTP_SERVER_PORT);
				break;
			}
			case "SSL":
			{
				props.put("mail.smtp.ssl", "true");
				props.put("mail.smtp.socketFactory.port", Config.SMTP_SERVER_PORT);
				props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				break;
			}
		}
		
		if (Config.SMTP_SERVER_AUTH)
		{
			SESSION = Session.getDefaultInstance(props, new javax.mail.Authenticator()
			{
				@Override
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(Config.SMTP_USERNAME, Config.SMTP_PASSWORD);
				}
			});
		}
	}
	
	private static final String[] VOICED_COMMANDS =
	{
		"security",
		"security_auth",
		"security_set_passwd",
		"security_unset",
		"security_change_passwd"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if (!Config.SECURITY_ENABLED)
			return false;
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		if (activeChar.getSecurityPassword() == null)
			html.setFile("mods/CustomSecurityMissing.htm");
		else if (activeChar.getSecurity())
			html.setFile("mods/CustomSecurityMainIdenty.htm");
		else
			html.setFile("mods/CustomSecurityMain.htm");
		
		html.replace("%playername%", activeChar.getName());
		
		if (command.equalsIgnoreCase("security"))
		{	
			if (activeChar.getSecurityPassword() == null)
				html.setFile("mods/CustomSecurityMissing.htm");
			else if (activeChar.getSecurity())
				html.setFile("mods/CustomSecurityMainIdenty.htm");
			else
				html.setFile("mods/CustomSecurityMain.htm");
				

			html.replace("%playername%", activeChar.getName());
			activeChar.sendPacket(html);
			return true;
		}
		else if (command.startsWith("security_auth"))
		{
			if (args == null)
			{
				activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароль не может быть пустым. Если вы хотите удалить пароль полностью - пожалуйста, отключите .security!" : "The password cannot be empty. If you wish to remove the password completely - please disable .security!"));
				activeChar.sendPacket(html);
			}
			else 
			{
				MessageDigest md;
				try
				{
					md = MessageDigest.getInstance("SHA");
					byte[] raw = args.getBytes("UTF-8");
					raw = md.digest(raw);
					String curpassEnc = Base64.encodeBytes(raw);
					if (activeChar.getSecurityPassword() == null)
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Ваше поле пароля пусто!" : "Your password field is empty !"));
						activeChar.sendPacket(html);
						return false;
					}
					else 
					{
						if (activeChar.getSecurityPassword().equalsIgnoreCase(curpassEnc))
						{
							activeChar.setSecurity(false);
							activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Теперь вы определили!" : "You are now identified!"));
							activeChar.setSecurityRemainingTries((byte) 5); // Successful auth, set tries left to 5 again.
							
							// For beta test servers
							boolean betaTest = BetaServer.isBetaServerAvailable() && BetaServer.canAccessGmshop();
							if (betaTest)
							{
								IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("gmshop");
								if(vch != null)
									vch.useVoicedCommand("gmshop", activeChar, "");
							}
							return true;
						}
						else
						{
							byte remaining = activeChar.getSecurityRemainingTries();
							if (--remaining <= 0) // Lower by 1, because failed.
							{
								activeChar.setSecurityRemainingTries((byte) 5); // update sql and give another 5 tries.
								
								activeChar.getClient().setState(GameClientState.AUTHED);
								activeChar.unsetVar("noCarrier");
								//activeChar.restart(false);
								
								// send char list
								CharacterSelectionInfo cl = new CharacterSelectionInfo(activeChar.getClient().getLogin(), activeChar.getClient().getSessionKey().playOkID1);
								activeChar.sendPacket(RestartResponse.OK, cl);
								activeChar.getClient().setCharSelection(cl.getCharInfo());
								
								AutoBan.Banned(activeChar, 0, 0 , 5, "Failed security authorization on character " + activeChar.getName() + "", "Server");
								String htmlban = HtmCache.getInstance().getNotNull("baninfo.htm", activeChar);
								String bannedby = "Server";
								String reason = "Failed security authorization on character " + activeChar.getName();
								String enddate = TimeUtils.convertDateToString(System.currentTimeMillis() + 5 * 60 * 1000);
								if (enddate.isEmpty() || enddate == null)
									enddate = "Bad Date";
								
								html = new NpcHtmlMessage(0);
								html.setHtml(htmlban);
								html.replace("%bannedby%", bannedby);
								html.replace("%endDate%", enddate);
								html.replace("%reason%", reason);
								activeChar.sendPacket(html);
								
								//activeChar.banHWID("[" + new Date().toString() + "] Failed security authorization on character " + activeChar.getName());
							}
							else
							{
								activeChar.setSecurityRemainingTries(remaining);
								activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Вы получили " + remaining + " попытки оставить успешно идентифицировать себя перед запрещены." : "You got " + remaining + " attempts left to successfully identify yourself before being banned."));
								activeChar.sendPacket(html);
							}
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					// Show the clan notice to player.
					activeChar.showClanNotice();
				}
			}
			return true;
		}
		else if (command.startsWith("security_set_passwd"))
		{
			if (args == null)
			{
				activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пустой пароль не приемлемо!" : "Empty password is not acceptable!"));
				activeChar.sendPacket(html);
				return false;
			}
			
			if (activeChar.getSecurityPassword() != null)
			{
				activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "У вас уже есть пароль безопасности!" : "You already have a security password!"));
				activeChar.sendPacket(html);
				return false;
			}
			
			StringTokenizer st = new StringTokenizer(args);
			try
			{
				String newpass = null, repeatnewpass = null;
				if (st.hasMoreTokens()) 
					newpass = st.nextToken();
				if (st.hasMoreTokens()) 
					repeatnewpass = st.nextToken();
				
				if (!(newpass == null || repeatnewpass == null))
				{
					if (!newpass.equals(repeatnewpass))
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароли не совпадают! Пожалуйста, введите его снова." : "Passwords don't match! Please, re-enter."));
						activeChar.sendPacket(html);
						return false;
					}
					
					if (newpass.startsWith("1234") || newpass.startsWith("1111") || newpass.startsWith("0000"))
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Ваш пароль слишком легко угадать. Пожалуйста, используйте более сложную пароль!" : "Your password is too easy to guess. Please use more difficult password!"));
						activeChar.sendPacket(html);
						return false;
					}
					
					if (newpass.equalsIgnoreCase(activeChar.getAccountName()))
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароль безопасность не может быть таким же, как ваш счет ID." : "The security password cannot be the same as your account ID."));
						activeChar.sendPacket(html);
						return false;
					}
					
					if (newpass.equalsIgnoreCase(activeChar.getName()))
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароль безопасность не может быть таким же, как имя персонажа." : "The security password cannot be the same as your character name."));
						activeChar.sendPacket(html);
						return false;
					}
					
					if (newpass.length() < 4)
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароль должен быть длиннее 4 символов! Пожалуйста, используйте более длинный пароль." : "The password has to be longer than 4 chars! Please use longer password."));
						activeChar.sendPacket(html);
						return false;
					}
					
					if (newpass.length() > 16)
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароль не может быть длиннее, чем 16 символов. Пожалуйста, используйте более короткий пароль." : "The password cannot be longer than 16 chars. Please use a shorter password."));
						activeChar.sendPacket(html);
						return false;
					}
					
					MessageDigest md = MessageDigest.getInstance("SHA");
					
					byte[] raw = newpass.getBytes("UTF-8");
					raw = md.digest(raw);
					String newpassEnc = Base64.encodeBytes(raw);
					
					String accPassword = CharacterDAO.getInstance().getAccountPassword(activeChar.getAccountName());
					
					String passwordHash = encrypt(newpass);
					
					if (accPassword.equals(passwordHash))
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", "The security password cannot match your login password.");
						activeChar.sendPacket(html);
						return false;
					}
				
					activeChar.setSecurityPassword(newpassEnc);
					activeChar.saveSecurity();
					activeChar.setSecurity(false);
					

					activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Вы успешно установить пароль!" : "You have successfully set your character password!"));
					
					activeChar.sendPacket(html.setFile("mods/CustomSecurityMain.htm"));
					
					// Send E-mail about the security password.
					if (Config.ENABLE_ON_SECURITY_PASSWORD_CHANGE)
					{
						String email = CharacterIntro.getEmail(activeChar);
						
						if (email == null)
							email = "";
						
						if (!email.equals(StringUtils.EMPTY))
						{
							try
							{
								StringBuilder sb = new StringBuilder();
								sb.append("This is an automated notification regarding the recent change(s) made to your character: " + activeChar.getName() + " \n\n");
								
								sb.append("You have enabled password protection for character " + activeChar.getName() + " \n\n");
								
								sb.append("L2-Rain \n");
								
								Message message = new MimeMessage(SESSION);
								message.setFrom(new InternetAddress(Config.SMTP_EMAIL_ADDR_SENDER));
								message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
								message.setSubject("L2-Rain - Character Security Password Notice");
								message.setText(sb.toString());
								
								Transport.send(message);
							}
							catch (MessagingException e)
							{
								throw new RuntimeException(e);
							}
						}
					}
				}
				else
				{
					activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Что-то пошло не так. Пожалуйста, попробуйте еще раз и заполнить все поля!" : "Something went wrong. Please try again and fill all the boxes!"));
					activeChar.sendPacket(html);
					return false;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (command.startsWith("security_change_passwd"))
		{
			if (args == null)
			{
				activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Поле пароля пусто! Пожалуйста, попробуйте еще раз!" : "The password box is empty! Please try again!"));
				activeChar.sendPacket(html);
				return false;
			}
			
			if (activeChar.getSecurityPassword() == null)
			{
				activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Ваша безопасность в настоящее время отключена. Пожалуйста, включите его." : "Your security is currently disabled. Please re-enable it."));
				activeChar.sendPacket(html);
				return false;
			}
			
			if (activeChar.getSecurity())
			{
				activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того чтобы изменить пароль безопасности, вы должны быть определены первым!" : "In order to change security password, you have to be identified first!"));
				activeChar.sendPacket(html);
				return false;
			}
			
			StringTokenizer st = new StringTokenizer(args);
			try
			{
				String curpass = null, newpass = null, repeatnewpass = null;
				if (st.hasMoreTokens()) curpass = st.nextToken();
				if (st.hasMoreTokens()) newpass = st.nextToken();
				if (st.hasMoreTokens()) repeatnewpass = st.nextToken();
				
				if (!(curpass == null || newpass == null || repeatnewpass == null))
				{
					if (!newpass.equals(repeatnewpass))
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароли не совпадают! Пожалуйста, введите его снова." : "Passwords don't match! Please, re-enter."));
						activeChar.sendPacket(html);
						return false;
					}
					
					if (newpass.startsWith("1234") || newpass.startsWith("1111") || newpass.startsWith("0000"))
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Ваш пароль слишком легко угадать. Пожалуйста, используйте более сложную пароль!" : "Your password is too easy to guess. Please use more difficult password!"));
						activeChar.sendPacket(html);
						return false;
					}
					
					if (newpass.equalsIgnoreCase(activeChar.getAccountName()))
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароль безопасность не может быть таким же, как ваш счет ID." : "The security password cannot be the same as your account ID."));
						activeChar.sendPacket(html);
						return false;
					}
					
					if (newpass.equalsIgnoreCase(activeChar.getName()))
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароль безопасность не может быть таким же, как имя персонажа." : "The security password cannot be the same as your character name."));
						activeChar.sendPacket(html);
						return false;
					}
					
					if (newpass.length() < 5)
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароль должен быть длиннее 5 символов! Пожалуйста, используйте более длинный пароль." : "The password has to be longer than 5 chars! Please use longer password."));
						activeChar.sendPacket(html);
						return false;
					}
					
					if (newpass.length() > 30)
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароль не может быть длиннее, чем 30 символов. Пожалуйста, используйте более короткий пароль." : "The password cannot be longer than 30 chars. Please use a shorter password."));
						activeChar.sendPacket(html);
						return false;
					}
					
					String curpassEnc = Base64.encodeBytes(MessageDigest.getInstance("SHA").digest(curpass.getBytes("UTF-8")));
					
					String accPassword = CharacterDAO.getInstance().getAccountPassword(activeChar.getAccountName());
					
					String passwordHash = encrypt(newpass);
					
					if (accPassword.equals(passwordHash))
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", "The security password cannot match your login password.");
						activeChar.sendPacket(html);
						return false;
					}
					
					if (activeChar.getSecurityPassword().equals(curpassEnc))
					{
						String newpassEnc = Base64.encodeBytes(MessageDigest.getInstance("SHA").digest(newpass.getBytes("UTF-8")));
						activeChar.setSecurityPassword(newpassEnc);
						activeChar.saveSecurity();
						
						
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Ваш пароль успешно установлен!" : "Your password is set successfully!"));
						activeChar.sendPacket(html);
						
						// Send E-mail about the security password.
						if (Config.ENABLE_ON_SECURITY_PASSWORD_CHANGE)
						{
							String email = CharacterIntro.getEmail(activeChar);
							
							if (email == null)
								email = "";
							
							if (!email.equals(StringUtil.EMPTY))
							{
								try
								{
									StringBuilder sb = new StringBuilder();
									sb.append("This is an automated notification regarding the recent change(s) made to your character: " + activeChar.getName() + " \n\n");
									
									sb.append("Your security password for character " + activeChar.getName() + " has recently been modified. \n");
									
									sb.append("If you made this password change, please disregard this notification. If you did not change your password, please contact the administrator. This action was performed from IP: " + activeChar.getClient().getIpAddr() + ".\n");
									
									sb.append("L2-Rain \n");
									
									Message message = new MimeMessage(SESSION);
									message.setFrom(new InternetAddress(Config.SMTP_EMAIL_ADDR_SENDER));
									message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
									message.setSubject("L2-Rain - Character Security Password Notice");
									message.setText(sb.toString());
									Transport.send(message);
								}
								catch (MessagingException e)
								{
									throw new RuntimeException(e);
								}
							}
						}
					}
					else
					{
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароли не совпадают! Пожалуйста, введите заново." : "Passwords don't match! Please re-enter."));
						activeChar.sendPacket(html);
						return false;
					}
					
				}
				else
				{
					activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Что-то пошло не так. Пожалуйста, попробуйте еще раз и заполнить все поля!" : "Something went wrong. Please try again and fill all the boxes!"));
					activeChar.sendPacket(html);
					return false;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (command.startsWith("security_unset"))
		{
			if (activeChar.getSecurity())
			{
				activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Чтобы отменить пароль (отключение защиты), вы должны быть определены первым!" : "In order to unset password (disable security), you have to be identified first!"));
				activeChar.sendPacket(html);
				return false;
			}
			
			activeChar.setSecurityPassword(null);
			activeChar.saveSecurity();

			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", activeChar.isLangRus() ? "Вы успешно отключили безопасность на этого персонажа." : "You have successfully disabled Security on this Character.");

			
			// Send E-mail about the security password.
			if (Config.ENABLE_ON_SECURITY_PASSWORD_CHANGE)
			{
				String email = CharacterIntro.getEmail(activeChar);
				
				if (email == null)
					email = "";
				
				if (!email.equals(StringUtil.EMPTY))
				{
					try
					{
						StringBuilder sb = new StringBuilder();
						sb.append("This is an automated notification regarding the recent change(s) made to your character: " + activeChar.getName() + " \n\n");
						
						sb.append("Your security password for character " + activeChar.getName() + " has been removed. \n");
						
						sb.append("If you made this password change, please disregard this notification. If you did not change your password, please contact the administrator. This action was performed from IP: " + activeChar.getClient().getIpAddr() + ".\n");
						
						sb.append("L2-Rain \n");

						Message message = new MimeMessage(SESSION);
						message.setFrom(new InternetAddress(Config.SMTP_EMAIL_ADDR_SENDER));
						message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
						message.setSubject("L2-Rain - Character Security Password Notice");
						message.setText(sb.toString());
						
						Transport.send(message);
					}
					catch (MessagingException e)
					{
						throw new RuntimeException(e);
					}
				}
			}
		}
		
		/*
		else if (command.startsWith("close_tut_wnd"))
		{
			activeChar.sendPacket(new TutorialCloseHtml());
		}
		*/
		
		return false;
	}
	
	private String encrypt(String password) throws Exception
	{
		AbstractChecksum checksum = JacksumAPI.getChecksumInstance("whirlpool2");
		checksum.setEncoding("BASE64");
		checksum.update(password.getBytes());
		return checksum.format("#CHECKSUM");
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}