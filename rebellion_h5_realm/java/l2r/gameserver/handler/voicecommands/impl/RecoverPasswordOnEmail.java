package l2r.gameserver.handler.voicecommands.impl;

import l2r.commons.dbutils.DbUtils;
import l2r.commons.util.Base64;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.gspackets.ChangePassword;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.randoms.CharacterIntro;
import l2r.gameserver.scripts.Functions;

import gov.nasa.worldwind.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javolution.util.FastMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecoverPasswordOnEmail extends Functions implements IVoicedCommandHandler
{
	private static final Logger _log = LoggerFactory.getLogger(RecoverPasswordOnEmail.class);
	
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
	
	private final String[] _commandList =
	{
		"recoverpassword",
		"recover",
		"recoveraccount",
		"recoversecurity",
		"enterTypeToRecover",
		"checkcode"
	};
	
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
	
	private FastMap<String, String> accountAndCode = new FastMap<String, String>().shared();
	private FastMap<String, String> securityAndCode = new FastMap<String, String>().shared();
	
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		
		if (!Config.ENABLE_PASSWORD_RECOVERY)
			return false;

		if (command.equals("recoverpassword") || command.equals("recover"))
		{
			String htmContent = HtmCache.getInstance().getNotNull("mods/recoverpassword/Intro.htm", activeChar);
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
			npcHtmlMessage.setHtml(htmContent);
			activeChar.sendPacket(npcHtmlMessage);
			
			return true;
		}
		else if (command.equals("recoveraccount"))
		{
			String htmContent = HtmCache.getInstance().getNotNull("mods/recoverpassword/recoverAcc.htm", activeChar);
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
			npcHtmlMessage.setHtml(htmContent);
			activeChar.sendPacket(npcHtmlMessage);
			
			return true;
		}
		else if (command.equals("recoversecurity"))
		{
			String htmContent = HtmCache.getInstance().getNotNull("mods/recoverpassword/recoverSecurity.htm", activeChar);
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
			npcHtmlMessage.setHtml(htmContent);
			activeChar.sendPacket(npcHtmlMessage);
			
			return true;
		}
		else if (command.equals("enterTypeToRecover"))
		{
			StringTokenizer st = new StringTokenizer(args);	
			if (!st.hasMoreTokens())
				return false;
			
			final String type = st.nextToken();
			final String value = st.nextToken();
			
			if (type.equals("account"))
			{
				if (accountAndCode.containsKey(value))
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.recoverpasswordonemail.message1", activeChar));
					return false;
				}
				
				String email = CharacterIntro.getEmail(value);

				if (email == null)
					email = "";
				
				if (!email.equals(StringUtil.EMPTY))
				{
					String generatedCode = RandomStringUtils.random(5, true, true);
					
					try
					{
						StringBuilder sb = new StringBuilder();
						sb.append("Dear Player, \n\n");
						sb.append("You have requested Account password recovery for the following Account: " + value + " \n");
						sb.append("The random generated code for the password recovery is: " + generatedCode + " It will be valid for 15 minutes.\n");
						sb.append("If you haven't requested this action, please IGNORE this message! \n\n");
						sb.append("L2-Rain - Community \n\n");
						sb.append("This is an automated message, please do not reply.");
						
						Message message = new MimeMessage(SESSION);
						message.setFrom(new InternetAddress(Config.SMTP_EMAIL_ADDR_SENDER));
						message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
						message.setSubject("Account Password Recovery");
						message.setText(sb.toString());
						
						Transport.send(message);
						
						_log.info("Password Recovery E-Mail with code " + generatedCode + " was sent to " + email + " for account: " + value);
					}
					catch (MessagingException e)
					{
						throw new RuntimeException(e);
					}
					
					accountAndCode.put(value, generatedCode);
					ThreadPoolManager.getInstance().schedule(new Runnable()
					{
						public void run()
						{
							accountAndCode.remove(value);
						}
					}, 15 * 60000);
					
					String htmContent = HtmCache.getInstance().getNotNull("mods/recoverpassword/entercode.htm", activeChar);
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					npcHtmlMessage.replace("%type%", type);
					npcHtmlMessage.setHtml(htmContent);
					activeChar.sendPacket(npcHtmlMessage);
				}
				else
				{
					String htmContent = HtmCache.getInstance().getNotNull("mods/recoverpassword/wrongaccount.htm", activeChar);
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					npcHtmlMessage.replace("%type%", type);
					npcHtmlMessage.replace("%string%", "Account");
					npcHtmlMessage.setHtml(htmContent);
					activeChar.sendPacket(npcHtmlMessage);
				}
				
				return true;
			}
			else if (type.equals("security"))
			{
				if (securityAndCode.containsKey(value))
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.recoverpasswordonemail.message2", activeChar));
					return false;
				}
				
				if (!haveSecurity(value))
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.recoverpasswordonemail.message3", activeChar));
					return false;
				}
				
				String accName = CharacterDAO.getInstance().getAccountName(value);
				
				if (!accName.equals(StringUtil.EMPTY))
				{	
					String email = CharacterIntro.getEmail(accName);
					
					if (email == null)
						email = "";
					
					if (email.length() < 5 || email.length() > 40)
						email = "";
					
					if (!email.equals(StringUtil.EMPTY))
					{
						String generatedCode = RandomStringUtils.random(5, true, true);
						
						try
						{
							StringBuilder sb = new StringBuilder();
							sb.append("Dear Player, \n\n");
							sb.append("You have requested Security password recovery for the following Character: " + value + " \n");
							sb.append("The random generated code for the password recovery is: " + generatedCode + " It will be valid for 15 minutes.\n");
							sb.append("If you haven't requested this action, please IGNORE this message! \n\n");
							sb.append("L2-Rain - Community \n\n");
							sb.append("This is an automated message, please do not reply.");
							
							Message message = new MimeMessage(SESSION);
							message.setFrom(new InternetAddress(Config.SMTP_EMAIL_ADDR_SENDER));
							message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
							message.setSubject("Security Password Recovery");
							message.setText(sb.toString());
							
							Transport.send(message);
							
							_log.info("Password Recovery E-Mail with code " +generatedCode + " was sent to " + email + " for character: " + value);
						}
						catch (MessagingException e)
						{
							throw new RuntimeException(e);
						}
						
						securityAndCode.put(value, generatedCode);
						ThreadPoolManager.getInstance().schedule(new Runnable()
						{
							public void run()
							{
								securityAndCode.remove(value);
							}
						}, 15 * 60000);
						
						String htmContent = HtmCache.getInstance().getNotNull("mods/recoverpassword/entercode.htm", activeChar);
						NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
						npcHtmlMessage.replace("%type%", type);
						npcHtmlMessage.setHtml(htmContent);
						activeChar.sendPacket(npcHtmlMessage);
					}
					else
					{
						String htmContent = HtmCache.getInstance().getNotNull("mods/recoverpassword/wrongaccount.htm", activeChar);
						NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
						npcHtmlMessage.replace("%type%", type);
						npcHtmlMessage.replace("%string%", "Character");
						npcHtmlMessage.setHtml(htmContent);
						activeChar.sendPacket(npcHtmlMessage);
					}
					
					return true;
				}
				
			}
			
			return true;
		}
		else if (command.equals("checkcode"))
		{
			StringTokenizer st = new StringTokenizer(args);	
			if (!st.hasMoreTokens())
				return false;
			
			String type = st.nextToken();
			String randomcode = st.nextToken();
			
			if (type.equals("account"))
			{
				if (accountAndCode.containsValue(randomcode))
				{		
					
					String randompassword = RandomStringUtils.random(5, false, true);
					String accountName = "";
					for (Entry<String, String> n3 : accountAndCode.entrySet())
					{
						if (n3.getValue().equalsIgnoreCase(randomcode))
							accountName = n3.getKey();
					}
					
					if (accountName.isEmpty())
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.recoverpasswordonemail.message4", activeChar));
						return false;
					}
					
					String email = CharacterIntro.getEmail(accountName);
					if (email.equals(StringUtil.EMPTY) || email == null)
						return false;
					
					try
					{
						StringBuilder sb = new StringBuilder();
						sb.append("Dear Player, \n\n");
						sb.append("You have requested Account password recovery for the following Account: " + accountName + " \n");
						sb.append("Your new account password is: " + randompassword + " \n\n");
						sb.append("L2-Rain - Community \n\n");
						sb.append("This is an automated message, please do not reply.");
						
						Message message = new MimeMessage(SESSION);
						message.setFrom(new InternetAddress(Config.SMTP_EMAIL_ADDR_SENDER));
						message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
						message.setSubject("Account Password Recovery");
						message.setText(sb.toString());
						
						Transport.send(message);
						
						_log.info("Password Recovery E-Mail with new password: " +randompassword + " was sent to " + email + " for account: " + accountName);
					}
					catch (MessagingException e)
					{
						throw new RuntimeException(e);
					}
					
					AuthServerCommunication.getInstance().sendPacket(new ChangePassword(accountName, "", randompassword, "null"));
					
					String htmContent = HtmCache.getInstance().getNotNull("mods/recoverpassword/passwordsend.htm", activeChar);
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					npcHtmlMessage.setHtml(htmContent);
					activeChar.sendPacket(npcHtmlMessage);
				}
				else
				{
					String htmContent = HtmCache.getInstance().getNotNull("mods/recoverpassword/wrongcode.htm", activeChar);
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					npcHtmlMessage.replace("%type%", type);
					npcHtmlMessage.setHtml(htmContent);
					activeChar.sendPacket(npcHtmlMessage);
				}
				
				return true;
			}
			else if (type.equals("security"))
			{
				if (securityAndCode.containsValue(randomcode))
				{		
					String randompassword = RandomStringUtils.random(5, false, true);
					
					String charName = "";
					
					for (Entry<String, String> n3 : securityAndCode.entrySet())
					{
						if (n3.getValue().equalsIgnoreCase(randomcode))
							charName = n3.getKey();
					}
					
					if (charName.isEmpty())
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.recoverpasswordonemail.message5", activeChar));
						return false;
					}
					
					String accName = CharacterDAO.getInstance().getAccountName(charName);
					
					if (accName.equals(StringUtil.EMPTY))
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.recoverpasswordonemail.message6", activeChar));
						return false;
					}
					
					String email = CharacterIntro.getEmail(accName);
					if (email.equals(StringUtil.EMPTY) || email == null)
						return false;
					
					try
					{
						StringBuilder sb = new StringBuilder();
						sb.append("Dear Player, \n\n");
						sb.append("You have requested Security password recovery for the following character: " + charName + " \n");
						sb.append("Your new security password is: " + randompassword + " \n\n");
						sb.append("L2-Rain - Community \n\n");
						sb.append("This is an automated message, please do not reply.");
						
						Message message = new MimeMessage(SESSION);
						message.setFrom(new InternetAddress(Config.SMTP_EMAIL_ADDR_SENDER));
						message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
						message.setSubject("Security Password Recovery");
						message.setText(sb.toString());
						
						Transport.send(message);
						
						_log.info("Password Recovery E-Mail with new Security password: " +randompassword + " was sent to " + email + " for character: " + charName);
					}
					catch (MessagingException e)
					{
						throw new RuntimeException(e);
					}
					
					try
					{
						byte[] raw = randompassword.getBytes("UTF-8");
						raw = MessageDigest.getInstance("SHA").digest(raw);
						String newpassEnc = Base64.encodeBytes(raw);
						saveSecurity(charName, newpassEnc);
					}
					catch (UnsupportedEncodingException e) {e.printStackTrace();}
					catch (NoSuchAlgorithmException e) {e.printStackTrace();}

					String htmContent = HtmCache.getInstance().getNotNull("mods/recoverpassword/passwordsend.htm", activeChar);
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					npcHtmlMessage.setHtml(htmContent);
					activeChar.sendPacket(npcHtmlMessage);
				}
				else
				{
					String htmContent = HtmCache.getInstance().getNotNull("mods/recoverpassword/wrongcode.htm", activeChar);
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					npcHtmlMessage.replace("%type%", type);
					npcHtmlMessage.setHtml(htmContent);
					activeChar.sendPacket(npcHtmlMessage);
				}
				
				return true;
			}
			
			return true;
		}
		return false;
	}
	
	private void saveSecurity(String charname, String password)
	{
		Connection con = null;
		PreparedStatement statement = null;
		
		int charObjId = CharacterDAO.getInstance().getObjectIdByName(charname);
		
		if (charObjId <= 0)
		{
			_log.error("Password Recovery : Error while fetch the obj id for char: " + charname);
			return;
		}
		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			if (haveSecurity(charname))
			{
				statement = con.prepareStatement("UPDATE `character_security` SET `password`=?, `changeDate`=?, `changeHWID`=?, `remainingTries`=? WHERE `charId`=?");
				statement.setString(1, password);
				statement.setLong(2, System.currentTimeMillis());
				statement.setString(3, null);
				statement.setInt(4, 3); // 3 tries left on successful change
				statement.setInt(5, charObjId);
				statement.executeUpdate();
			}
			
		}
		catch (Exception e)
		{
			_log.warn("Could not store security password: " + e.getMessage() + " for " + charname + "objId - " + charObjId, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	private boolean haveSecurity(String charname)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		
		int charObjId = CharacterDAO.getInstance().getObjectIdByName(charname);
		
		if (charObjId <= 0)
			return false;
		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT password FROM character_security WHERE charId = ?");
			statement.setInt(1, charObjId);
			rset = statement.executeQuery();
			
			if (rset.next())
			{
				if (rset.getString("password") == null || rset.getString("password").length() < 1)
					return false;
					
				return true;
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not fetch security password: " + e.getMessage() + " for " + charname + " : objId - " + charObjId, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return false;
	}
}