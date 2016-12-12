package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.utils.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminPremiumAccount implements IAdminCommandHandler
{
	private static final Logger _log = LoggerFactory.getLogger(AdminPremiumAccount.class);
	
	private static enum Commands
	{
		admin_premium,
		admin_addpremium,
		admin_removepremium,
		admin_removepremiumaccount
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_premium:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/premium.htm"));
				break;
			case admin_addpremium:
			{
				try
				{
					if(wordList.length < 4)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpremiumaccount.message1", activeChar));
						return false;
					}

					String account = String.valueOf(wordList[1]);
					String templateName = String.valueOf(wordList[2]);
					String premiumTime = String.valueOf(wordList[3]);
					
					long premiumDuration = 0;
					int premiumTemplateId = 0;
					
					switch (templateName)
					{
						case "silver":
							premiumTemplateId = 2;
							switch (premiumTime)
							{
								case "week":
									premiumDuration = 604800000L; // 7 days
									break;
								case "twoweek":
									premiumDuration = 1209600000L; // 14 days
									break;
								case "month":
									premiumDuration = 2592000000L; // 30 days
							}
							break;
						case "gold":
							premiumTemplateId = 3;
							switch (premiumTime)
							{
								case "week":
									premiumDuration = 604800000L; // 7 days
									break;
								case "twoweek":
									premiumDuration = 1209600000L; // 14 days
									break;
								case "month":
									premiumDuration = 2592000000L; // 30 days
							}
							break;
						case "platinum":
							premiumTemplateId = 4;
							switch (premiumTime)
							{
								case "week":
									premiumDuration = 604800000L; // 7 days
									break;
								case "twoweek":
									premiumDuration = 1209600000L; // 14 days
									break;
								case "month":
									premiumDuration = 2592000000L; // 30 days
							}
							break;
						case "world":
							premiumTemplateId = 5;
							switch (premiumTime)
							{
								case "week":
									premiumDuration = 604800000L; // 7 days
									break;
								case "twoweek":
									premiumDuration = 1209600000L; // 14 days
									break;
								case "month":
									premiumDuration = 2592000000L; // 30 days
							}
							break;
					}
					
					if (premiumTemplateId <= 0 || premiumDuration <= 0)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpremiumaccount.message2", activeChar));
						return false;
					}
					
					if (CharacterDAO.getInstance().accountCharNumber(account) <= 0)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpremiumaccount.message3", activeChar));
						return false;
					}
					
					long endtime = System.currentTimeMillis() + premiumDuration;
					PremiumAccountsTable.savePremium(account, premiumTemplateId, endtime);
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpremiumaccount.message4", activeChar, account, templateName, (Util.formatTime((int) (endtime - System.currentTimeMillis()) / 1000))));
					_log.info("Gm : " + activeChar.getName() + " have added premium to account:  " + account + " . Template  " + templateName + " , premium will expire after : " + Util.formatTime((int) (endtime - System.currentTimeMillis()) / 1000));
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpremiumaccount.message5", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/premium.htm"));;
				break;
			}
			case admin_removepremium:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/premiumremove.htm"));
				break;
			case admin_removepremiumaccount:
			{
				try
				{
					if(wordList.length < 2)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpremiumaccount.message6", activeChar));
						return false;
					}

					String account = String.valueOf(wordList[1]);
					
					if (CharacterDAO.getInstance().accountCharNumber(account) <= 0)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpremiumaccount.message7", activeChar));
						return false;
					}
					PremiumAccountsTable.removePremium(account);
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpremiumaccount.message8", activeChar, account));
					_log.info("Gm : " + activeChar.getName() + " has removed premium for account : " + account);
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpremiumaccount.message9", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/premiumremove.htm"));;
				break;
			}
		}
		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}