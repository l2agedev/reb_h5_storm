package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;

public class AdminVitality implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_set_vitality_points,
		admin_set_vitality_level,
		admin_full_vitality,
		admin_empty_vitality,
		admin_get_vitality
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if (!Config.ALT_VITALITY_ENABLED)
		{
			activeChar.sendMessage("Vitality is not enabled on the server!");
			return false;
		}
		
		if (activeChar == null)
			return false;
		
		int level = 0;
		int vitality = 0;
		
		switch (command)
		{
			case admin_set_vitality_points:
			{
				if (wordList.length != 2)
				{
					activeChar.sendMessage("Specific Vitality points to set...");
					return false;
				}
				
				GameObject obj = activeChar.getTarget();
				if (obj != null && obj.isPlayer())
				{
					Player plr = obj.getPlayer();
					try
					{
						vitality = Integer.parseInt(wordList[1]);
					}
					catch (Exception e)
					{
						activeChar.sendMessage("Incorrect vitality");
						return false;
					}
					
					plr.setVitality(vitality);
					activeChar.sendMessage("You have set " + vitality + " vitality points to " + plr.getName());
				}
				else
					activeChar.sendMessage("You dont have target or its not a Player...");
				break;
			}
			case admin_set_vitality_level:
			{
				if (wordList.length != 2)
				{
					activeChar.sendMessage("Specific Vitality level to set...");
					return false;
				}
				
				GameObject obj = activeChar.getTarget();
				if (obj != null && obj.isPlayer())
				{
					Player plr = obj.getPlayer();
					try
					{
						level = Integer.parseInt(wordList[1]);
					}
					catch (Exception e)
					{
						activeChar.sendMessage("Incorrect vitality");
						return false;
					}
					
					if (level >= 0 && level <= 4)
					{
						if (level == 0)
							vitality = 1;
						else
							vitality = Config.VITALITY_LEVELS[level - 1];
						
						plr.setVitality(vitality);
						activeChar.sendMessage("You have set level "  + level + " vitality to char " + plr.getName());
					}
					else
					{
						activeChar.sendMessage("Incorrect vitality level (0-4)");
						return false;
					}
				}
				else
					activeChar.sendMessage("You dont have target or its not a Player...");
				break;
			}
			case admin_full_vitality:
			{
				GameObject obj = activeChar.getTarget();
				if (obj != null && obj.isPlayer())
				{
					Player plr = obj.getPlayer();
					
					plr.setVitality(Config.VITALITY_LEVELS[4]);
					activeChar.sendMessage("You have set MAXIMUM vitality to " + plr.getName());
				}
				else
					activeChar.sendMessage("You dont have target or its not a Player...");
				break;
			}
			case admin_empty_vitality:
			{
				GameObject obj = activeChar.getTarget();
				if (obj != null && obj.isPlayer())
				{
					Player plr = obj.getPlayer();
					
					plr.setVitality(1);
					activeChar.sendMessage("You have set MINIMUM vitality to " + plr.getName());
				}
				else
					activeChar.sendMessage("You dont have target or its not a Player...");
				break;
			}
			case admin_get_vitality:
			{
				GameObject obj = activeChar.getTarget();
				if (obj != null && obj.isPlayer())
				{
					Player plr = obj.getPlayer();
					
					plr.getVitality();
					
					level = plr.getVitalityLevel(false);
					vitality = (int) plr.getVitality();
					
					activeChar.sendMessage("Player vitality level: " + level);
					activeChar.sendMessage("Player vitality points: " + vitality);
				}
				else
					activeChar.sendMessage("You dont have target or its not a Player...");
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