package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.stats.Calculator;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.stats.funcs.Func;
import l2r.gameserver.stats.funcs.FuncSet;
import l2r.gameserver.utils.Util;

/**
 * This class handles following admin commands: - gm = turns gm mode on/off
 */
public class AdminPcCondOverride implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_exceptions,
		admin_set_exception,
		admin_setparam,
		admin_unsetparam,
		admin_listparam
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch (command) // command
		{
			case admin_exceptions:
			{
				NpcHtmlMessage msg = new NpcHtmlMessage(5);
				msg.setFile("admin/cond_override.htm");
				StringBuilder sb = new StringBuilder();
				sb.append("<table cellspacing=5 width=275 bgcolor=333333>");
				for (PcCondOverride ex : PcCondOverride.values())
					sb.append("<tr><td fixwidth=180><font color=" + (activeChar.canOverrideCond(ex) ? "33FF99" : "BB2233") + ">" + ex.getDescription() + ":</font></td><td><a action=\"bypass -h admin_set_exception " + ex.ordinal() + "\">" + (activeChar.canOverrideCond(ex) ? "Disable" : "Enable") + "</a></td></tr>");	
				sb.append("</table>");
				msg.replace("%cond_table%", sb.toString());
				activeChar.sendPacket(msg);
				break;
			}
			case admin_set_exception:
			{
				if (wordList.length > 1)
				{
					String condValue = wordList[1];
					if (Util.isDigit(condValue))
					{
						PcCondOverride ex = PcCondOverride.getCondOverride(Integer.valueOf(condValue));
						if (ex != null)
						{
							if (activeChar.canOverrideCond(ex))
							{
								activeChar.removeOverridedCond(ex);
								activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpccondoverride.message1", activeChar, ex.getDescription()));
							}
							else
							{
								int duration = 0;
								if (wordList.length > 2 && Util.isDigit(wordList[2]))
									duration = Integer.parseInt(wordList[2]);
								activeChar.addOverrideCond(duration, ex);
								activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpccondoverride.message2", activeChar, ex.getDescription()));
							}
						}
					}
					else
					{
						switch (condValue)
						{
							case "enable_all":
							{
								for (PcCondOverride ex : PcCondOverride.values())
								{
									if (!activeChar.canOverrideCond(ex))
										activeChar.addOverrideCond(ex);
								}
								activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpccondoverride.message3", activeChar));
								break;
							}
							case "disable_all":
							{
								for (PcCondOverride ex : PcCondOverride.values())
								{
									if (activeChar.canOverrideCond(ex))
										activeChar.removeOverridedCond(ex);
								}
								activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminpccondoverride.message4", activeChar));
								break;
							}
						}
					}
					useAdminCommand(Commands.admin_exceptions, null , "", activeChar);
				}
				break;
			}
			case admin_setparam:
			{
				try
				{
					Creature target = (Creature) (activeChar.getTarget() != null ? activeChar.getTarget() : null);
					if (target == null)
						target = activeChar;
					
					for (int i = 1; i < wordList.length; i+=2)
					{
						boolean fail = true;
						String statName = wordList[i];
						double value = Double.parseDouble(wordList[i+1]);
						for(Stats stat : Stats.values())
						{
							if(stat.getValue().equalsIgnoreCase(statName))
							{
								fail = false;
								Calculator calc = target.getCalculators()[stat.ordinal()];
								
								// Remove old param.
								if (calc != null)
								{
									for (Func func : calc.getFunctions())
									{
										if (func.order == 0x90)
											calc.removeFunc(func);
									}
								}
								
								target.addStatFunc(new FuncSet(stat, 0x90, target, value));
								activeChar.sendMessage("Stat: " + stat + "(" + value + ") set to " + target.getName());
								activeChar.updateStats();
								break;
							}
						}
						
						if (fail)
							activeChar.sendMessage("Incorrect stat name: " + statName + " with value: " + value);
					}
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Usage: //setparam stat value; //setparam stat1 value1 stat2 value2 stat3 value3...");
					activeChar.sendMessage("Error: " + e.getMessage());
				}
				
				break;
			}
			case admin_unsetparam:
			{
				Creature target = (Creature) (activeChar.getTarget() != null ? activeChar.getTarget() : null);
				if (target == null)
					target = activeChar;
				
				if (wordList.length == 1)
				{
					for (Calculator calc : target.getCalculators())
					{
						if (calc == null)
							continue;
						
						for (Func func : calc.getFunctions())
						{
							if (func.order == 0x90)
							{
								calc.removeFunc(func);
								activeChar.sendMessage("Stat: " + func.stat + "(" + func.value + ") removed from " + target.getName());
							}
						}
					}
					
					activeChar.updateStats();
					
					return true;
				}
				
				try
				{
					for (String param : wordList)
					{
						if (param == wordList[0])
							continue;
						
						Stats stat = null;
						for(Stats s : Stats.values())
						{
							if(s.getValue().equalsIgnoreCase(param))
								stat = s;
						}
						
						if (stat != null)
						{
							Calculator calc = target.getCalculators()[stat.ordinal()];
							if (calc != null)
							{
								for (Func func : calc.getFunctions())
								{
									if (func.order == 0x90)
									{
										calc.removeFunc(func);
										activeChar.sendMessage("Stat: " + func.stat + "(" + func.value + ") removed from " + target.getName());
									}
								}
								
								activeChar.updateStats();
							}
							else
								activeChar.sendMessage("Calculator is null for stat: " + stat);
						}
						else
							activeChar.sendMessage("Stat with name: " + param + " not found.");
					}
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Usage: //unsetparam stat; Call '//unsetparam' only to remove all set params.");
					activeChar.sendMessage("Error: " + e.getMessage());
				}
				
				break;
			}
			case admin_listparam:
			{
				try
				{
					Creature target = (Creature) (activeChar.getTarget() != null ? activeChar.getTarget() : null);
					if (target == null)
						target = activeChar;
					
					if (wordList.length == 1)
					{
						for (Calculator calc : target.getCalculators())
						{
							if (calc == null)
								continue;
							
							for (Func func : calc.getFunctions())
							{
								if (func.order == 0x90)
									activeChar.sendMessage("Stat: " + func.stat + "(" + func.value + ")");
							}
						}
						
						return true;
					}
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Error: " + e.getMessage());
				}
				
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