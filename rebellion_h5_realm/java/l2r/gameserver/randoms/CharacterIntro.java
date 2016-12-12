/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.randoms;

import l2r.commons.dbutils.DbUtils;
import l2r.commons.util.Base64;
import l2r.gameserver.Config;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2r.gameserver.instancemanager.BetaServer;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.mail.Mail;
import l2r.gameserver.network.serverpackets.ConfirmDlg;
import l2r.gameserver.network.serverpackets.ExNoticePostArrived;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.TutorialCloseHtml;
import l2r.gameserver.network.serverpackets.TutorialShowHtml;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.ItemFunctions;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.util.FastMap;
import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;

/**
 * Character intro menu.
 * @author Infern0
 */
public class CharacterIntro
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterIntro.class);
	private static String EMAIL_VERIFICATION_HTML_PATH = "mods/EmailValidation.htm";

	// from here start the intro script.
	private static void showTutorialHTML(String html, Player player)
	{
		String text = HtmCache.getInstance().getNotNull("mods/characterintro/" + html + ".htm", player);
		text = text.replaceAll("%playername%", "" + player.getName());
		text = text.replaceAll("%palyerOID%", "" + player.getObjectId());
		player.sendPacket(new TutorialShowHtml(text));
	}
	
	public static void sendIntro(Player activeChar)
	{
		switch (getAccountStep(activeChar))
		{
			case "":
			case "intro":
				setAccountStep(activeChar, "intro");
				showTutorialHTML("terms", activeChar);
				break;
			case "terms":
				showTutorialHTML("security", activeChar);
				break;
			case "security":
				showTutorialHTML("emailvalidation", activeChar);
				break;
			case "emailvalidation":
				showTutorialHTML("referral", activeChar);
				break;
		}
	}
	
	public static void bypassIntro(Player activeChar, String bypass)
	{
		if (bypass.startsWith("terms"))
		{
			setAccountStep(activeChar, "terms");
			
			if (activeChar.getSecurityPassword() != null)
			{
				setAccountStep(activeChar, "emailvalidation");
				showTutorialHTML("emailvalidation", activeChar);
			}
			else
				showTutorialHTML("security", activeChar);
		}
		else if (bypass.startsWith("security"))
		{
			setSecurity(activeChar, bypass);
		}
		else if (bypass.startsWith("decline"))
		{
			notAcceptTerms(activeChar);
		}
		else if (bypass.startsWith("skipsecurity"))
		{
			setAccountStep(activeChar, "emailvalidation");
			showTutorialHTML("emailvalidation", activeChar);
			ExShowScreenMessage sm = new ExShowScreenMessage("Warning: Your character is unprotected. To protect it type .security", 3000, ScreenMessageAlign.MIDDLE_CENTER, true);
			activeChar.sendPacket(sm);
		}
		else if (bypass.startsWith("skipreferral"))
		{
			setAccountStep(activeChar, "finished");
			activeChar.sendPacket(TutorialCloseHtml.STATIC);
			activeChar.unblock();
			activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.BATTLEFIELD.ordinal(), "Server", "You have compelted our intro, have fun!.");
			
			// For beta test servers
			boolean betaTest = BetaServer.isBetaServerAvailable() && BetaServer.canAccessGmshop();
			if (betaTest)
			{
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("gmshop");
				if(vch != null)
					vch.useVoicedCommand("gmshop", activeChar, "");
			}
		}
		else if (bypass.startsWith("showrules"))
		{
			String text = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/rules.htm", activeChar);
			
			ShowBoard.separateAndSend(text, activeChar);
		}
		
	}
	
	private static void notAcceptTerms(final Player activeChar)
	{
		if (activeChar != null)
		{
			activeChar.ask(new ConfirmDlg(SystemMsg.S1, 10000).addString("Confirm that you do not accept our terms.."), new OnAnswerListener()
			{
				@Override
				public void sayYes()
				{
					activeChar.logout();
				}

				@Override
				public void sayNo()
				{
					showTutorialHTML("terms", activeChar);
				}
			});
		}
	}
	
	public static void checkAndSendIntro(Player activeChar)
	{
		if (activeChar == null)
			return;
		
		if (getAccountStep(activeChar).equalsIgnoreCase("finished"))
			return;
		
		activeChar.block();
		activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.BATTLEFIELD.ordinal(), "Server", "Hello, " + activeChar.getName() + " you character is blocked until you fill out required blank.");
		activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.BATTLEFIELD.ordinal(), "Server", "The required blank is the tutorial window that popup, if you do not see it please enable tutorial window from the game settings.");
		activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.BATTLEFIELD.ordinal(), "Server", "For problems or questions, please contact server staff!");
		sendIntro(activeChar);
	}
	
	public static void insertAccountData(String accountName, String var, String value)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO account_data VALUES (?,?,?)");
			statement.setString(1, accountName);
			statement.setString(2, var);
			statement.setString(3, value);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.error("Could not save email:", e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static String getAccountValue(String accountName, String var)
	{
		String data = "";
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT value FROM account_data WHERE account_name=? AND var=?");
			statement.setString(1, accountName);
			statement.setString(2, var);
			rset = statement.executeQuery();
			while(rset.next())
			{
				data = rset.getString(1);
			}
		}
		catch (Exception e)
		{
			_log.error("Could not get value data:", e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return data;
	}
	
	public static String getAccountVar(String accountName)
	{
		String data = "";
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT var FROM account_data WHERE account_name=?");
			statement.setString(1, accountName);
			rset = statement.executeQuery();
			while(rset.next())
			{
				data = rset.getString(1);
			}
		}
		catch (Exception e)
		{
			_log.error("Could not get var data:", e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return data;
	}
	
	public static void setSecurity(Player activeChar, String bypass)
	{
		if (bypass.length() <= 9)
		{
			showTutorialHTML("security", activeChar);
			return;
		}
		
		String arg = bypass.substring(9); // command = security args
		
		if (arg == null || arg.isEmpty())
		{
			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароль не может быть пустым!" : "Password cannot be empty!"));
			showTutorialHTML("security", activeChar);
			return;
		}
		
		if (activeChar.getSecurityPassword() != null)
		{
			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "У вас уже есть пароль!" : "You already have a password!"));
			showTutorialHTML("security", activeChar);
			return;
		}
		
		StringTokenizer st = new StringTokenizer(arg);
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
					activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Новый пароль не совпадает с повторным один!" : "The new password doesn't match with the repeated one!"));
					showTutorialHTML("security", activeChar);
					return;
				}
				
				if (newpass.startsWith("1234") || newpass.startsWith("1111") || newpass.startsWith("0000"))
				{
					activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Ваш пароль слишком легко угадать. Пожалуйста, используйте более сложную пароль!" : "Your password is too easy to guess. Please use more difficult password!"));
					showTutorialHTML("security", activeChar);
					return;
				}
				
				if (newpass.equalsIgnoreCase(activeChar.getAccountName()))
				{
					activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароль безопасность не может быть таким же, как ваш счет ID." : "The security password cannot be the same as your account ID."));
					showTutorialHTML("security", activeChar);
					return;
				}
				
				if (newpass.equalsIgnoreCase(activeChar.getName()))
				{
					activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароль безопасность не может быть таким же, как имя персонажа." : "The security password cannot be the same as your character name."));
					showTutorialHTML("security", activeChar);
					return;
				}
				
				if (newpass.length() < 5) // Minimum of 5 characters for better security
				{
					activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Новый пароль короче 5 символов! Пожалуйста, попробуйте более длинный." : "The new password is shorter than 5 chars! Please try with a longer one."));
					showTutorialHTML("security", activeChar);
					return;
				}
				
				if (newpass.length() > 30)
				{
					activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Новый пароль длиннее 30 символов! Пожалуйста, попробуйте с более короткой." : "The new password is longer than 30 chars! Please try with a shorter one."));
					showTutorialHTML("security", activeChar);
					return;	
				}
				
				MessageDigest md = MessageDigest.getInstance("SHA");
				
				byte[] raw = newpass.getBytes("UTF-8");
				raw = md.digest(raw);
				String newpassEnc = Base64.encodeBytes(raw);
				
				String accPassword = CharacterDAO.getInstance().getAccountPassword(activeChar.getAccountName());
				
				String passwordHash = encrypt(newpass);
				
				if (accPassword.equals(passwordHash))
				{
					activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Пароль безопасности не могут совпадать ваш логин пароль!" : "The security password cannot match your login password!"));
					showTutorialHTML("security", activeChar);
					return;
				}
				
				activeChar.setSecurityPassword(newpassEnc);
				activeChar.saveSecurity();
				
				activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Вы успешно установить пароль!" : "You have successfully set your password!"));
				showTutorialHTML("emailvalidation", activeChar);
				setAccountStep(activeChar, "security");
				activeChar.setSecurity(false);
				activeChar.setSecurityRemainingTries((byte) 5); // Successful auth, set tries left to 5 again.
			}
			else
			{
				activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Неверные данные паролем! Вы должны заполнить все поля." : "Invalid password data! You have to fill all boxes."));
				showTutorialHTML("security", activeChar);
				return;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// below is email validation script.
	public static void sendEmailVerificationHtml(Player activeChar)
	{
		String html = HtmCache.getInstance().getNotNull(EMAIL_VERIFICATION_HTML_PATH, activeChar);
		activeChar.sendPacket(new TutorialShowHtml(html));
	}
	
	public static void verifyEmail(Player activeChar)
	{
		if (!hasEmail(activeChar))
			sendEmailVerificationHtml(activeChar);
	}
	
	public static void setAccountStep(Player activeChar, String value)
	{
		if (activeChar == null)
			return;
		
		insertAccountData(activeChar.getAccountName(), "intro_step", value);
	}
	
	public static String getAccountStep(Player activeChar)
	{
		if (activeChar == null)
			return "";
		
		try
		{
			return getAccountValue(activeChar.getAccountName(), "intro_step");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}
	
	public static void setEmail(Player activeChar, String email)
	{
		if (activeChar == null)
			return;
		
		insertAccountData(activeChar.getAccountName(), "email_addr", email);
	}
	
	public static boolean hasEmail(Player activeChar)
	{
		if (activeChar == null)
			return false;
		
		return getEmail(activeChar.getAccountName()) != null;
	}
	
	public static String getEmail(Player activeChar)
	{
		if (activeChar == null)
			return null;
		
		return getEmail(activeChar.getAccountName());
	}
	
	
	public static String getEmail(String accountName)
	{
		String email = null;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT value FROM account_data WHERE account_name=? AND var='email_addr'");
			statement.setString(1, accountName);
			rset = statement.executeQuery();
			while(rset.next())
			{
				email = rset.getString(1);
			}
		}
		catch (Exception e)
		{
			_log.error("Could not get email:", e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return email;
	}
	
	public static void sendEmailValidation(Player player, String email, String email2)
	{
		
		if (player == null)
			return;
		
		if (!email.isEmpty() && email.contains("@") && email.contains(".") && email.length() <= 50 && email.length() >= 5 && email.length() <= 40)
		{
			if (email.equalsIgnoreCase(email2))
			{
				CharacterEmails.setEmail(player, email);
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Email", (player.isLangRus() ? "Адрес электронной почты вашего аккаунта в настоящее время:" : "Your account's e-mail is now: ") + email);
				setAccountStep(player, "emailvalidation");
				showTutorialHTML("referral", player);
			}
			else
			{
				showTutorialHTML("emailvalidation", player);
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Email", (player.isLangRus() ? "Ввели электронную почту в ящиках не совпадают." : "The typed e-mails in the boxes do not match."));
			}
				
		}
		else
		{
			showTutorialHTML("emailvalidation", player);
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Email", (player.isLangRus() ? "Типизированных электронной почты является недопустимым." : "The typed e-mail is invalid."));
		}
	}
	
	public static void referralSystem(Player player, String referralName)
	{
		if (!referralName.isEmpty())
		{
			if (player.getLevel() >= 20)
			{
				showTutorialHTML("referral", player);
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Referral", (player.isLangRus() ? "Вы должны быть не ниже 20 уровня использования справочной системы. Ваша Приведи будут игнорироваться." : "You must be below level 20 to use referral system. Your refer will be ignored."));
				return;
			}
			else if (referralName.length() > 16 ||  CharacterDAO.getInstance().getObjectIdByName(referralName) <= 0)
			{
				showTutorialHTML("referral", player);
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Referral", (player.isLangRus() ? "Имя персонажа реферала не существует. Оставьте поле имени реферала пустым, если вы хотите продолжить, не устанавливая его." : "The referral's character name doesn't exist. Leave the referral's name field blank if you wish to continue without setting it."));
				return;
			}
			else if (referralName.equalsIgnoreCase(player.getName()))
			{
				showTutorialHTML("referral", player);
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Referral", (player.isLangRus() ? "Вы не можете использовать себя в качестве направления." : "You cannot use yourself as the referral."));
				return;
			}
			
			if (!player.hasHWID())
			{
				showTutorialHTML("referral", player);
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Referral", (player.isLangRus() ? "Что-то не так случилось, пожалуйста, свяжитесь с администратором хромой." : "Something wrong happend, please contact the lame admin."));
				_log.warn("Referral System: Error while check character HWID!");
				return;
			}
			
			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rset = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT * FROM referral_system WHERE senderHWID=?");
				statement.setString(1, player.getHWID());
				rset = statement.executeQuery();
				if (rset.next())
				{
					showTutorialHTML("referral", player);
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Referral", (player.isLangRus() ? "Вы либо не новый игрок или вы уже установили направление." : "You are either not a new player or you have already set a referral."));
					return;
				}
			}
			catch (Exception e)
			{	
				_log.error("Could not get referral system settings:" + e);
				e.printStackTrace();
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rset);
			}
			
		
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("INSERT INTO referral_system VALUES (?,?,?,?,?,?,?)");
				statement.setString(1, player.getAccountName());
				statement.setString(2, player.getHWID());
				statement.setString(3, player.getName());
				statement.setInt(4, 0); //senderReward
				statement.setString(5, referralName);
				statement.setDate(6, new Date(System.currentTimeMillis()));
				statement.setInt(7, 0);
				statement.executeUpdate();
				
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Referral", (player.isLangRus() ? "Вы успешно установить направление. Как только вы достигаете 85 уровня, ваше направление будет вознагражден." : "You have successfully set your referral. Once you reach Level 85, your referral will be rewarded."));
				
				setAccountStep(player, "finished");
				player.unblock();
				player.sendPacket(TutorialCloseHtml.STATIC);
				player.sendChatMessage(player.getObjectId(), ChatType.BATTLEFIELD.ordinal(), "Server", "You have compelted our intro, have fun!.");
				
				// For beta test servers
				boolean betaTest = BetaServer.isBetaServerAvailable() && BetaServer.canAccessGmshop();
				if (betaTest)
				{
					IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("gmshop");
					if(vch != null)
						vch.useVoicedCommand("gmshop", player, "");
				}
			}
			catch (Exception e)
			{
				_log.error("Could not save referral system settings:" + e);
				e.printStackTrace();
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rset);
			}
		}
		
		setAccountStep(player, "finished");
	}
	
	public static void newcomersReward(Player activeChar, int amount, int levelPassed)
	{
		if (!activeChar.hasHWID())
		{
			_log.error("CharacterIntro: newcomersReward() error getting player hwid! Player: " + activeChar.getName());
			return;
		}
		
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM referral_system WHERE senderHWID=? OR senderAccount=?");
			statement2 = con.prepareStatement("UPDATE `referral_system` SET `senderReward`=? WHERE (`senderHWID`=?) OR (`senderAccount`=?)");
			statement.setString(1, activeChar.getHWID());
			statement.setString(2, activeChar.getAccountName());
			rset = statement.executeQuery();
			while (rset.next())
			{
				int sndRwd = rset.getInt("senderReward");
				if (sndRwd >= 1000) // 1000 is the current max reward.
					return;
				
				if (levelPassed == 20 && sndRwd >= 250)
					return;
				else if (levelPassed == 40 && sndRwd >= 500)
					return;
				else if (levelPassed == 60 && sndRwd >= 750)
					return;
				else if (levelPassed == 80 && sndRwd >= 1000)
					return;
				
				statement2.setInt(1, amount + sndRwd);
				statement2.setString(2, activeChar.getHWID());
				statement2.setString(3, activeChar.getAccountName());
				int updated = statement2.executeUpdate();
				if (updated > 0)
				{
					Map<Integer, Long> item = new FastMap<Integer, Long>();
					item.put(13693, (long) amount);
					
					Functions.sendSystemMail(activeChar, "Newscomers' Gift", "Hey newcomer, I see that you are making a good progress. Here is a little reward to help you along your way :P", item);
					_log.info("Referral System: Player " + activeChar.getName() + " has reached level " + levelPassed + " . And recived " + amount + " coins.");
				}
				
				return;
			}
			
		}
		catch (Exception e)
		{
			_log.error("Could not get referral system settings:", e);
			e.printStackTrace();
		}
		finally
		{	
			DbUtils.closeQuietly(con, statement, rset);
			DbUtils.closeQuietly(statement2);
		}
	}
	
	public static void referralReward(Player activeChar)
	{
		if (!activeChar.hasHWID())
		{
			_log.error("CharacterIntro: referralReward() error getting player hwid! Player: " + activeChar.getName());
			return;
		}
		
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		//PreparedStatement statement3 = null;
		ResultSet rset = null;
		//ResultSet rset3 = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `referralName`, `rewarded` FROM `referral_system` WHERE `senderHWID` = ?");
			statement.setString(1, activeChar.getHWID());
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				String referralName = rset.getString("referralName");
				int rewarded = rset.getInt("rewarded");
				if (rewarded == 1)
					return;
				
				int updated = 0;
				statement2 = con.prepareStatement("UPDATE `referral_system` SET `rewarded` = '1' WHERE `senderHWID` = ?");
				statement2.setString(1, activeChar.getHWID());
				updated = statement2.executeUpdate();
				
				/* disabled till understand why does not take the count..
				int rewardedCount = 0;
				statement3 = con.prepareStatement("SELECT COUNT(*) as rewarded FROM `referral_system` WHERE `referralName` = ? AND `rewarded` = '1' LIMIT 0, 30;");
				statement3.setString(1, referralName);
				rset3 = statement.executeQuery();
				if (rset3.next())
					rewardedCount = rset3.getInt("rewarded");
				*/
				
				int referralOID = CharacterDAO.getInstance().getObjectIdByName(referralName);
				Player referralplayer = World.getPlayer(referralOID);
				if (referralOID > 0)
				{
					if (updated > 0)
					{
						int rewardAmount = 100;
						/*
						if (rewardedCount >= 30)
							rewardAmount = 150;
							*/
						
						Mail mail = new Mail();
						mail.setSenderId(1);
						mail.setSenderName("Admin");
						mail.setReceiverId(referralOID);
						mail.setReceiverName(referralName);
						mail.setTopic("Referral System");
						mail.setBody(referralName + " has reached level 85. You will be rewarded for inviting him to the server.");
						ItemInstance item = ItemFunctions.createItem(13693);
						item.setLocation(ItemInstance.ItemLocation.MAIL);
						item.setCount(rewardAmount);
						item.save();
						mail.addAttachment(item);
						mail.setType(Mail.SenderType.NEWS_INFORMER);
						mail.setUnread(true);
						mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
						mail.save();
						
						if(referralplayer != null)
						{
							referralplayer.sendPacket(ExNoticePostArrived.STATIC_TRUE);
							referralplayer.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
						}
						
						//_log.info("Referral System: Player " + activeChar.getName() + " has reached max level with referral " + referralName + " . Reward sended.");
					}
				}
				return;
			}
		}
		catch (Exception e)
		{
			_log.error("Could not get referral system settings:", e);
			e.printStackTrace();
		}
		finally
		{	
			DbUtils.closeQuietly(con, statement, rset);
			DbUtils.closeQuietly(statement2);
		}
	}
	
	public static void showReferralHtml(Player activeChar)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM referral_system WHERE referralName=?");
			statement.setString(1, activeChar.getName());
			rset = statement.executeQuery();
			
			StringBuilder sb = new StringBuilder(500);
			sb.append("<html><title>Referral System</title><body> List of people which you have invited to the server:<br><table width=280>");
			sb.append("<tr><td width=100>");
			sb.append("<font color=LEVEL>Character</font>");
			sb.append("</td><td width=80>");
			sb.append("<font color=LEVEL>Date</font>");
			sb.append("</td><td width=80>");
			sb.append("<font color=LEVEL>Status</font>");
			sb.append("</td></tr>");
			while (rset.next())
			{
				String senderCharacter = rset.getString("senderCharacter");
				boolean rewarded = rset.getInt("rewarded") == 1;
				Date date = rset.getDate("date");
				String font = rewarded ? "<font color=00FF00>" : "<font color=FF0000>";
				sb.append("<tr><td>");
				sb.append(font + senderCharacter + "</font>");
				sb.append("</td><td>");
				sb.append(font + date.toString() + "</font>");
				sb.append("</td><td>");
				sb.append(font + (rewarded ? "aproved" : "unaproved") + "</font>");
				sb.append("</td></tr>");
			}
			
			sb.append("</table></body></html>");
			
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setHtml(sb.toString());
			activeChar.sendPacket(html);
		}
		catch (Exception e)
		{
			_log.error("Could not get referral system settings:", e);
			e.printStackTrace();
		}
		finally
		{	
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	private static String encrypt(String password) throws Exception
	{
		AbstractChecksum checksum = JacksumAPI.getChecksumInstance("whirlpool2");
		checksum.setEncoding("BASE64");
		checksum.update(password.getBytes());
		return checksum.format("#CHECKSUM");
	}
}
