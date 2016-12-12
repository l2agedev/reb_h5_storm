package services;

import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.dao.PremiumAccountsTable.PremiumTemplate;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ConfirmDlg;
import l2r.gameserver.network.serverpackets.ExBR_PremiumState;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PremiumAccount extends Functions
{
	public static final Logger _log = LoggerFactory.getLogger(PremiumAccount.class);
	
	public void buy(String[] param)
	{
		String actualCommand = param[0]; // Get actual command
		
		Player activeChar = getSelf();
		if (activeChar == null)
			return;
		
		if (actualCommand.equalsIgnoreCase("bbsgetpremium"))
		{
			int premiumId = Integer.parseInt(param[1]);
			String premiumTime = param[2];
			
			PremiumTemplate currenttemplate = PremiumAccountsTable.getPremiumAccount(activeChar).getTemplate();
			
			if (currenttemplate != PremiumAccountsTable.DEFAULT_PREMIUM_TEMPLATE)
				askHim(activeChar, premiumId, premiumTime, currenttemplate);
			else
			{
				try
				{
					long premiumDuration = 0;
					int premiumCost = 0;
					
					PremiumTemplate template = PremiumAccountsTable.getPremiumTemplate(premiumId);

					if (template == PremiumAccountsTable.DEFAULT_PREMIUM_TEMPLATE)
					{
						activeChar.sendMessage("There is a error with premium system please contact the server administrator.");
						_log.error("There was an error with premium templates: id " + premiumId + " , does not exsists...");
						return;
					}
					
					switch (premiumTime)
					{
						case "week":
							premiumDuration = 604800000L; // 7 days
							premiumCost = template.costWeek;
							break;
						case "month":
							premiumDuration = 2592000000L; // 30 days
							premiumCost = template.costMonth;
					}
					
					if (template.cost <= 0 || template.costWeek <= 0  || template.costMonth <= 0 || premiumDuration <= 0 || premiumId <= 0)
					{
						activeChar.sendMessage(new CustomMessage("scripts.services.premiumaccount.err_code1", activeChar));
						return;
					}
					
					if (activeChar.getInventory().destroyItemByItemId(template.cost, premiumCost))
					{
						long endtime = System.currentTimeMillis() + premiumDuration;
						PremiumAccountsTable.savePremium(activeChar.getAccountName(), premiumId, endtime);
						activeChar.sendChatMessage(0, ChatType.BATTLEFIELD.ordinal(),"Premium" ,"You have purchased " + template.name + " premium status for 1 " + premiumTime + ".");
						activeChar.sendPacket(new ExBR_PremiumState(activeChar, true));
						Log.addDonation("Character " + activeChar + " has buyed premium: " + template.name  + " ID(" + premiumId + ") ", "premiumsystem");
					}
					else
						activeChar.sendMessage(new CustomMessage("scripts.services.premiumaccount.cost", activeChar, premiumCost));
				}
				catch (Exception e)
				{
					activeChar.sendMessage(new CustomMessage("scripts.services.premiumaccount.err_code2", activeChar));
				}
			}
		}
	}
	
	private static void askHim(final Player activeChar, final int premiumId, final String premiumTime, final PremiumTemplate currentTemplate)
	{
		if (activeChar != null)
		{
			final PremiumTemplate template = PremiumAccountsTable.getPremiumTemplate(premiumId);
			
			if (template == PremiumAccountsTable.DEFAULT_PREMIUM_TEMPLATE)
			{
				activeChar.sendMessage("There is a error with premium system please contact the server administrator.");
				_log.error("There was an error with premium templates: id " + premiumId + " , does not exsists...");
				return;
			}
			
			activeChar.ask(new ConfirmDlg(SystemMsg.S1, 10000).addString("You have a " + currentTemplate.name + " premium account, do you want to continue?"), new OnAnswerListener()
			{
				@Override
				public void sayYes()
				{
					try
					{
						long premiumDuration = 0;
						int premiumCost = 0;
						
						switch (premiumTime)
						{
							case "week":
								premiumDuration = 604800000L; // 7 days
								premiumCost = template.costWeek;
								break;
							case "month":
								premiumDuration = 2592000000L; // 30 days
								premiumCost = template.costMonth;
						}
						
						if (template.cost <= 0 || template.costWeek <= 0  || template.costMonth <= 0 || premiumDuration <= 0 || premiumId <= 0)
						{
							activeChar.sendMessage(new CustomMessage("scripts.services.premiumaccount.err_code1", activeChar));
							return;
						}
						
						if (Util.getPay(activeChar, template.cost, premiumCost, true))
						{
							long endtime = System.currentTimeMillis() + premiumDuration;
							PremiumAccountsTable.savePremium(activeChar.getAccountName(), premiumId, endtime);
							activeChar.sendChatMessage(0, ChatType.BATTLEFIELD.ordinal(),"Premium" ,"You have purchased " + template.name + " premium status for 1 " + premiumTime + ".");
							activeChar.sendPacket(new ExBR_PremiumState(activeChar, true));
							Log.addDonation("Character " + activeChar + " has buyed premium: " + template.name  + " ID(" + premiumId + ") ", "premiumsystem");
						}
						else
							activeChar.sendMessage(new CustomMessage("scripts.services.premiumaccount.cost", activeChar, premiumCost));
					}
					catch (Exception e)
					{
						activeChar.sendMessage(new CustomMessage("scripts.services.premiumaccount.err_code2", activeChar));
					}
				}

				@Override
				public void sayNo()
				{
					//
				}
			});
		}
	}
	
}