package services;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author PaInKiLlEr
 */
public class Account extends Functions implements ScriptFile
{
	
	public void CharToAcc()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if (!Config.ACC_MOVE_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		
		String append_ru = "Перенос персонажей между аккаунтами.<br>";
		append_ru += "Цена: " + Config.ACC_MOVE_PRICE + " " + ItemHolder.getInstance().getTemplate(Config.ACC_MOVE_ITEM).getName() + ".<br>";
		append_ru += "Внимание !!! При переносе персонажа на другой аккаунт, убедитесь что персонажей там меньше чем 7, иначе могут возникнуть непредвиденные ситуации за которые Администрация не отвечает.<br>";
		append_ru += "Внимательно вводите логин куда переносите, администрация не возвращает персонажей.";
		append_ru += "Вы переносите персонажа " + player.getName() + ", на какой аккаунт его перенести ?";
		append_ru += "<edit var=\"new_acc\" width=150>";
		append_ru += "<button value=\"Перенести\" action=\"bypass -h scripts_services.Account:NewAccount $new_acc\" width=150 height=15><br>";
		
		String append_en = "Transfer characters between accounts.<br>";
		append_en += "Price: " + Config.ACC_MOVE_PRICE + " " + ItemHolder.getInstance().getTemplate(Config.ACC_MOVE_ITEM).getName() + ".<br>";
		append_en += "Attention! When you transfer a character to another account, make sure that the characters there is less than 7, or there might be unforeseen situations for which the Administration is not responsible.<br>";
		append_en += "Carefully enter the username to transfer, Administration does not return characters.";
		append_en += "You transfer your character " + player.getName() + ", on which account to transfer it?";
		append_en += "<edit var=\"new_acc\" width=150>";
		append_en += "<button value=\"Transfer\" action=\"bypass -h scripts_services.Account:NewAccount $new_acc\" width=150 height=15><br>";
		
		show(player.isLangRus() ? append_ru : append_en, player, null);
		
	}

	public void NewAccount(String[] name)
	{
		Player player = getSelf();
		if(player == null)
			return;
		if (!Config.ACC_MOVE_ENABLED)
		{
			show(player.isLangRus() ? "Сервис отключен." : "Service is disabled.", player);
			return;
		}
		if(player.getInventory().getCountOf(Config.ACC_MOVE_ITEM) < Config.ACC_MOVE_PRICE)
		{
			player.sendMessage(new CustomMessage("scripts.services.account.noadena", player, Config.ACC_MOVE_PRICE, ItemHolder.getInstance().getTemplate(Config.ACC_MOVE_ITEM)));
			CharToAcc();
			return;
		}
		String _name = name[0];
		Connection con = null;
        Connection conGS = null;
		PreparedStatement offline = null;
        Statement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT `login` FROM `accounts` WHERE `login` = ?");
			offline.setString(1, _name);
			rs = offline.executeQuery();
			if(rs.next())
			{
				removeItem(player, Config.ACC_MOVE_ITEM, Config.ACC_MOVE_PRICE);
                conGS = DatabaseFactory.getInstance().getConnection();
			    statement = conGS.createStatement();
				statement.executeUpdate("UPDATE `characters` SET `account_name` = '" + _name + "' WHERE `char_name` = '" + player.getName() + "'");
				player.sendMessage(new CustomMessage("scripts.services.account.char_transferred", player));
				player.logout();
			}
			else
			{
				player.sendMessage(new CustomMessage("scripts.services.account.acc_not_found", player));
				CharToAcc();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
            DbUtils.closeQuietly(conGS, statement);
		}
	}

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}