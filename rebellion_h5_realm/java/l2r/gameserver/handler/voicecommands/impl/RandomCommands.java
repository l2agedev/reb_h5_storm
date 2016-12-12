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
package l2r.gameserver.handler.voicecommands.impl;

import l2r.commons.dao.JdbcEntityState;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.instancemanager.AutoHuntingManager;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.model.Spawner;
import l2r.gameserver.model.World;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.entity.residence.ClanHall;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.serverpackets.ExGetPremiumItemList;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.RadarControl;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.randoms.CharacterEmails;
import l2r.gameserver.randoms.TradesHandler;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.SpawnTable;
import l2r.gameserver.templates.npc.AbsorbInfo;
import l2r.gameserver.templates.npc.AbsorbInfo.AbsorbType;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.TimeUtils;
import l2r.gameserver.utils.Util;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javolution.util.FastTable;
import javolution.util.FastMap;

import org.apache.commons.lang3.math.NumberUtils;


public class RandomCommands extends Functions implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"help",
		"openatod",
		"oatod",
		"combinetalismans",
		"talisman",
		"ct",
		"npcspawn",
		"whereis",
		"referral",
		"getsms",
		"smsreward",
		"report",
		"findparty",
		"fp",
		"gearscore",
		"sc",
		"scinfo",
		"tradelist",
		"exp"
	};
	
	private static final int[] TALISMAN_IDS = Config.TALISMAN_IDS;
	private static final int[] CLANHALL_NPC_IDS = Config.CLANHALL_NPC_IDS;
	
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if (command.equalsIgnoreCase("smsreward"))
		{
			if(activeChar.getPremiumItemList().isEmpty())
			{
				activeChar.sendPacket(Msg.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND);
				return true;
			}
			
			activeChar.sendPacket(new ExGetPremiumItemList(activeChar));
		}
		else if (command.equalsIgnoreCase("gearscore") && activeChar.isGM())
		{
			int gearScore = Util.getGearPoints(activeChar);
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message1", activeChar, gearScore));
		}
		else if (command.equalsIgnoreCase("sc") || command.equalsIgnoreCase("scinfo") && Config.ENABLE_SC_INFO_COMMAND)
		{
			int page = 1;
			if (args != null && !args.isEmpty() && NumberUtils.isNumber(args))
				page = Integer.parseInt(args);
			
			int blockforvisual = 0;
			int all = 0;
			boolean pagereached = false;
			
			StringBuilder html = new StringBuilder();
			html.append("<html><title>Soul Crystal Information</title><body><table height=350>");
			
			for (NpcTemplate tmpl : NpcHolder.getInstance().getAll())
			{
				if (tmpl != null && !tmpl.getAbsorbInfo().isEmpty())
				{
					boolean nameAppended = false;
					for (AbsorbInfo ai : tmpl.getAbsorbInfo())
					{
						if (ai == null || ai.getMaxLevel() <= 10)
							continue;
						
						all++;
						if (page == 1 && blockforvisual > 10)
							continue;
						if (!pagereached && all > page * 10)
							continue;
						if (!pagereached && all <= (page - 1) * 10)
							continue;
						
						blockforvisual++;
						
						if (!nameAppended)
						{
							html.append("<tr><td><font color=\"07AE23\">").append(tmpl.getName()).append("</font></td></tr>");
							nameAppended = true;
						}
						
						int chance = ai.getChance();
						
						if (Config.LEVEL_UP_CRY_EXTRA_CHANCE > 0)
							chance += Config.LEVEL_UP_CRY_EXTRA_CHANCE;
						
						if (chance > 100)
							chance = 100;
						
						html.append("<tr><td><table><tr><td width=80>[").append(ai.getMinLevel() == ai.getMaxLevel() ? ai.getMinLevel() : (ai.getMinLevel() + "-" + ai.getMaxLevel())).append("]</td><td width=200>").append(getAbsorbType(ai.getAbsorbType())).append("</td><td width=50>").append(chance).append("%</td></tr></table></td></tr>");
					}
				}
			}
			
			
			int totalPages = (int) Math.round(all / 10.0 + 1);
			
			if (page > totalPages)
				return false;
			
			if(page == 1)
			{
				
				html.append("<tr><td width=210>&nbsp;</td>");
				html.append("<td width=50><button value=\">>\" action=\"bypass -h user_sc " + (page + 1) + "\" width=60 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			}
			else if(page > 1)
				if(totalPages == page)
				{
					html.append("<tr><td width=210>&nbsp;</td>");
					html.append("<td width=50><button value=\"<<\" action=\"bypass -h user_sc " + (page - 1) + "\" width=60 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
				}
				else
				{
					html.append("<tr><td width=210><button value=\"<<\" action=\"bypass -h user_sc " + (page - 1) + "\" width=60 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
					html.append("<td width=50><button value=\">>\" action=\"bypass -h user_sc " + (page + 1) + "\" width=60 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
				}
			
			html.append("</table></body></html>");
			activeChar.sendPacket(new NpcHtmlMessage(0).setHtml(html.toString()));
		}
		else if (command.equalsIgnoreCase("findparty") || command.equalsIgnoreCase("fp") && Config.PARTY_SEARCH_COMMANDS)
		{
			if(activeChar.isInParty() && !activeChar.getParty().isLeader(activeChar))
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message2", activeChar));
				return false;
			}

			if (!activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS) && !activeChar.antiFlood.canFindParty())
			{
				activeChar.sendChatMessage(0, ChatType.PARTY.ordinal(), "FINDPARTY", "Anti flood protection. Please try again later.");
				return false;
			}
			
			if (activeChar.isInJail())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message3", activeChar));
				return false;
			}
			
			int currmembers = activeChar.getParty() != null ? activeChar.getParty().size() : 0;
			if (args == null || args.isEmpty())
			{	
				if(NexusEvents.isInEvent(activeChar))
				{
					for(Player player : GameObjectsStorage.getAllPlayersForIterate())
						if(!player.isInBlockList(activeChar.getName()) && !player.isInParty() && !player.isInOfflineMode() && !player.isInOlympiadMode() && activeChar.getEventInfo().getTeamId() == player.getEventInfo().getTeamId())
							player.sendPacket(new Say2(activeChar.getObjectId(), ChatType.BATTLEFIELD, activeChar.getName(), "	Type=1 	ID=" + activeChar.getObjectId() + " Color=0 	Underline=0 	Title=[EVENT]Free slots (" + currmembers + "/9) "));
				}
				else
				{
					for(Player player : GameObjectsStorage.getAllPlayersForIterate())
						if(!player.isInBlockList(activeChar.getName()) && !player.isInParty() && !player.isInOfflineMode() && !player.isInOlympiadMode() && !player.getVarB("findparty"))
							player.sendPacket(new Say2(activeChar.getObjectId(), ChatType.PARTY, activeChar.getName(), "	Type=1 	ID=" + activeChar.getObjectId() + " Color=0 	Underline=0 	Title=[PARTY]Free slots (" + currmembers + "/9) "));
				}
			}
			else
			{
				for(String s : Config.TRADE_WORDS)
					if(args.contains(s))
					{
						activeChar.sendChatMessage(0, ChatType.PARTY.ordinal(), "FINDPARTY", "Dont use party find command for trade!");
						return false;
					}
				
				if(args.length() > 22)
					args = args.substring(0, 22);
				
				if(NexusEvents.isInEvent(activeChar))
				{
					for(Player player : GameObjectsStorage.getAllPlayersForIterate())
						if(!player.isInBlockList(activeChar.getName()) && !player.isInParty() && !player.isInOfflineMode() && !player.isInOlympiadMode() && activeChar.getEventInfo().getTeamId() == player.getEventInfo().getTeamId())
							player.sendPacket(new Say2(activeChar.getObjectId(), ChatType.BATTLEFIELD, activeChar.getName(), "	Type=1 	ID=" + activeChar.getObjectId() + " Color=0 	Underline=0 	Title=[EVENT]Free slots (" + currmembers + "/9)  for " + args + ""));
				}
				else
				{
					for(Player player : GameObjectsStorage.getAllPlayersForIterate())
						if(!player.isInBlockList(activeChar.getName()) && !player.isInParty() && !player.isInOfflineMode() && !player.isInOlympiadMode() && !player.getVarB("findparty"))
							player.sendPacket(new Say2(activeChar.getObjectId(), ChatType.PARTY, activeChar.getName(), "	Type=1 	ID=" + activeChar.getObjectId() + " Color=0 	Underline=0 	Title=[PARTY]Free slots (" + currmembers + "/9)  for " + args + ""));
				}
			}
			
				
			activeChar.setPartyFindValid(true);
		}
		else if (command.equalsIgnoreCase("report")) //TODO: Config ...
		{
			String htmlreport = HtmCache.getInstance().getNotNull("command/report.htm", activeChar);
			
			if (args == null || args == "" || args.isEmpty())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setHtml(htmlreport);
				html.replace("%reported%", activeChar.getTarget() == null ? "Please select target to report or type his name." : activeChar.getTarget().isPlayer() ? activeChar.getTarget().getName() : "You can report only players.");
				activeChar.sendPacket(html);
				return false;
			}
			
			String[] paramSplit = args.split(" ");
			if (paramSplit[0].equalsIgnoreCase("Bot") && paramSplit.length != 1)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message4", activeChar));
				return false;
			}
			
			StringBuilder sb = new StringBuilder();
			for (String other : paramSplit)
			{
				other = other.replace("Bot", "");
				other = other.replace("Abuse", "");
				other = other.replace("FakeShop", "");
				other = other.replace("\n", " ");
				
				sb.append(other + " ");
			}
			
			String fullMsg = sb.toString();
			
			if (fullMsg.length() > 150)
			{
				activeChar.sendMessage("You have exceeded maximum allowed characters for report. Maximum lenght: 150 characters.");
				return false;
			}
			
			botReportcommand(activeChar, paramSplit[0], sb.toString());
		}
		else if (command.equalsIgnoreCase("referral") && Config.ENABLE_REFERRAL_SYSTEM)
		{
			CharacterEmails.showReferralHtml(activeChar);
		}
		else if (command.equalsIgnoreCase("help") && Config.ENABLE_HELP_COMMAND)
		{
			String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/help.htm", activeChar);
			ShowBoard.separateAndSend(html, activeChar);
		}
		else if (command.equalsIgnoreCase("npcspawn") && Config.ENABLE_NPCSPAWN_COMMAND)
		{
			if (args == null || args == "" || args.isEmpty())
			{
				String msg = showClanhallNpcSpawnWindow(activeChar);
				if (msg != null)
					activeChar.sendMessage(msg);
				
				return true;
			}
			
			String[] paramSplit = args.split(" ");
			if (paramSplit.length != 2)
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message5", activeChar));
			else
			{
				if (paramSplit[0].equalsIgnoreCase("spawn"))
				{
					String npcId = paramSplit[1];
					if (Util.isDigit(npcId))
					{
						String msg = spawnClanhallNpc(activeChar, Integer.parseInt(npcId));
						if (msg != null)
							activeChar.sendMessage(msg);
					}
				}
				else if (paramSplit[0].equalsIgnoreCase("unspawn"))
				{
					String npcObjId = paramSplit[1];
					if (Util.isDigit(npcObjId))
					{
						String msg = unspawnClanhallNpc(activeChar, Integer.parseInt(npcObjId));
						if (msg != null)
							activeChar.sendMessage(msg);
					}
				}
			}
		}
		else if(command.equalsIgnoreCase("whereis") && Config.ENABLE_WHEREIS_COMMAND)
			return whereis(command, activeChar, args);
		else if (command.equalsIgnoreCase("combinetalismans") || command.equalsIgnoreCase("talisman") || command.equalsIgnoreCase("ct") && Config.ENABLE_COMBINE_TALISMAN_COMMAND)
		{
			try
			{
				// TalismanId, List<TalismansWithThisId>
				Map<Integer, List<ItemInstance>> talismans = new FastMap<>();
				
				for (ItemInstance item : activeChar.getInventory().getItems())
				{
					if (item == null || !item.isShadowItem()) // Talismans are shadow items.
						continue;
					
					int itemId = item.getItemId();
					// Get only the talismans.
					if (!Util.contains(TALISMAN_IDS, itemId))
						continue;
					
					if (!talismans.containsKey(itemId))
						talismans.put(itemId, new FastTable<ItemInstance>());
					
					talismans.get(itemId).add(item);
				}
				activeChar.sendMessage("----------------------------------------------------");
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message6", activeChar));
				
				// Now same talismans are under 1 list. Loop this list to combine them.
				for (Entry<Integer, List<ItemInstance>> n3 : talismans.entrySet())
				{
					List<ItemInstance> sameTalismans = n3.getValue();
					if (sameTalismans.size() <= 1) // We need at least 2 talismans.
						continue;
					
					List<ItemInstance> talismansToCharge = new FastTable<>(); // The talisman(s) that isnt(arent) going to be destroyed, but charged.
					
					// First, find the equipped talisman, it is with charge priority.
					for (ItemInstance talisman : sameTalismans)
					{
						if (talisman.isEquipped())
						{
							talismansToCharge.add(talisman); // Add to the chargable talismans.
							sameTalismans.remove(talisman); // and remove it from the list, because we will loop it again and we dont want that item there.
						}
					}
					
					if (talismansToCharge.isEmpty())
						talismansToCharge.add(sameTalismans.remove(0));
					
					// Second loop, charge the talismans.
					int index = 0;
					ItemInstance lastTalisman = null;
					for (ItemInstance talisman : sameTalismans)
					{
						if (index >= talismansToCharge.size())
							index = 0;
						
						ItemInstance talismanToCharge = talismansToCharge.get(index++);
						int chargeMana = talisman.getLifeTime() + talismanToCharge.getLifeTime();
						if (activeChar.getInventory().destroyItem(talisman))
							talismanToCharge.setLifeTime(chargeMana);
						
						lastTalisman = talismanToCharge;
					}
					
					if (lastTalisman != null)
					{
						if(lastTalisman.getJdbcState().isSavable())
						{
							lastTalisman.save();
						}
						else
						{
							lastTalisman.setJdbcState(JdbcEntityState.UPDATED);
							lastTalisman.update();
						}
						
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message8", activeChar, lastTalisman.getName()));
						InventoryUpdate iu = new InventoryUpdate().addModifiedItem(lastTalisman);
						activeChar.sendPacket(iu);
					}
				}
				
				activeChar.sendMessage("----------------------------------------------------");
			}
			catch (Exception e)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message9", activeChar, TimeUtils.getDateString(new Date(System.currentTimeMillis()))));
				_log.warn("Error while combining talismans: ", e);
			}
		}
		else if (command.equalsIgnoreCase("openatod") || command.equalsIgnoreCase("oatod") && Config.ENABLE_OPENATOD_COMMAND)
		{
			if (args == null)
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message10", activeChar));
			else
			{
				int num = 0;
				try {num = Integer.parseInt(args);}
				catch (NumberFormatException nfe) 
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message11", activeChar));
					return false;
				}
				
				if (num == 0)
					return false;
				else if (activeChar.getInventory().getCountOf(9599) >= num)
				{
					int a=0, b=0, c=0, d=0, rnd;
					for (int i=0; i<num;i++)
					{
						rnd = Rnd.get(100);
						// 40% Chance for hidden first page
						if (rnd <= 99 && rnd > 59)
							a++;
						// 50% chance for hidden second page
						else if (rnd <= 59 && rnd > 9)
							b++;
						else if (rnd <= 9)
							c++;
						else d++;
					}
					if (activeChar.getInventory().destroyItemByItemId(9599, a+b+c+d))
					{
						if (a>0)
							Functions.addItem(activeChar, 9600, a, true);
							//activeChar.getInventory().addItem(9600, a);
						if (b>0)
							Functions.addItem(activeChar, 9601, b, true);
							//activeChar.getInventory().addItem(9601, b);
						if (c>0)
							Functions.addItem(activeChar, 9602, c, true);
							//activeChar.getInventory().addItem(9602, c);
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message12", activeChar, d));
					}
					else activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message13", activeChar));
				}
				else activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message14", activeChar));
			}
		}
		else if (Config.ENABLE_TRADELIST_VOICE && command.equalsIgnoreCase("tradelist"))
		{
			TradesHandler.display(activeChar, args);
		}
		else if(command.equalsIgnoreCase("exp") && Config.ENABLE_EXP_COMMAND)
		{
			NumberFormat df = NumberFormat.getNumberInstance();
			df.setMaximumFractionDigits(2);
			
			if(activeChar.getLevel() >= (activeChar.isSubClassActive() ? Experience.getMaxSubLevel() : Experience.getMaxLevel()))
				show("Maximum level!", activeChar);
			else
			{
				long exp = Experience.LEVEL[activeChar.getLevel() + 1] - activeChar.getExp();
				double count = 0;
				String ret = "Exp left: " + exp;
				if(count > 0)
					ret += "<br>Monsters left: " + df.format(count);
				show(ret, activeChar);
			}
		}
		
		return true;
	}

	private boolean whereis(String command, Player activeChar, String args)
	{
		Player friend = World.getPlayer(args);
		
		if (!PremiumAccountsTable.getWhereisVoiced(activeChar))
		{
			activeChar.sendChatMessage(0, ChatType.PARTY.ordinal() , "[Compass]", (activeChar.isLangRus() ? "У вас нет разрешения на использование этой команды." : "You do not have permission to use this command."));
			return false;
		}
		
		if(friend == null)
		{
			activeChar.sendChatMessage(0, ChatType.PARTY.ordinal() , "[Compass]", (activeChar.isLangRus() ? "Кажется, что игрок вы ищете в сети." : "It seems the player you are searching for is offline."));
			return false;
		}

		if(activeChar.getFriendList().getList().containsKey(friend.getObjectId()) || friend.getParty() == activeChar.getParty() || friend.getClan() == activeChar.getClan())
		{
			RadarControl rc = new RadarControl(0, 2, friend.getLoc());
			activeChar.sendPacket(rc);
			return true;
		}

		activeChar.sendChatMessage(0, ChatType.PARTY.ordinal() , "[Compass]", (activeChar.isLangRus() ? "Вы можете использовать этот звонкий команду, чтобы найти друзей, члены партии или члены клана." : "You can only use this voiced command to find friends, party members or clan members."));
		return false;
	}
	
	private String showClanhallNpcSpawnWindow(Player player)
	{
		if (!((player.getClanPrivileges() & Clan.CP_CH_SET_FUNCTIONS) == Clan.CP_CH_SET_FUNCTIONS) && !player.isGM())
			return "Only clan clan members with privilegies to set clanhall functions can spawn NPCs in their clanhall.";
		else if (!player.isGM() && player.getClan().getHasHideout() == 0)
			return "You do not own a clanhall.";
		else if (!player.isInZone(ZoneType.RESIDENCE))
			return "You are not located in a clanhall.";
		else if (player.getClanHall() == null)
			return "You do not have a clanhall";
		
		ClanHall zone = ResidenceHolder.getInstance().getResidenceByCoord(ClanHall.class, player.getX(), player.getY(), player.getZ(), player.getReflection());
		if (zone == null)
			return "You are not located in a clanhall.";
		else if (!player.isGM() && player.getClan().getHasHideout() != zone.getId())
			return "This clanhall doesn't belong to your clan.";
		
		// Add the clanhall NPCs in the list.
		Map<Integer, Integer> _npcIdOid = new FastMap<>();
		for (int npcId : CLANHALL_NPC_IDS)
		{
			if (!_npcIdOid.containsKey(npcId))
				_npcIdOid.put(npcId, 0);
		}
		
		// Now fill the spawned NPCs
		for (NpcInstance cha : zone.getZone().getInsideNpcs())
		{
			if (!cha.isNpc() || !Util.contains(CLANHALL_NPC_IDS, cha.getNpcId()))
				continue;
			
			_npcIdOid.put(cha.getNpcId(), cha.getObjectId());
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body><title>Clanhall NPC Manage:</title>");
		sb.append("<font color=LEVEL>In this menu you can spawn/unspawn the given NPCs. If you are spawning an NPC, it will be spawned at your char's location and heading. </font><br>");
		sb.append("<table width=280>");
		for (Entry<Integer, Integer> npcIdOid : _npcIdOid.entrySet())
		{
			int npcId = npcIdOid.getKey();
			int objId = npcIdOid.getValue();
			boolean isSpawned = objId != 0;
			sb.append("<tr><td width=160>");
			sb.append("<font color=" + (isSpawned ? "00FF00" : "FF0000") + ">" + getNpcName(npcId) + "</font>");
			sb.append("</td><td align=right>");
			sb.append("<button value=\"" + (isSpawned ? "Unspawn" : "Spawn") + "\" action=\"bypass -h user_npcspawn " + (isSpawned ? ("unspawn " + objId) : ("spawn " + npcId)) + "\" width=100 height=30 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			sb.append("</td></tr>");
		}
		sb.append("</table></body></html>");
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		player.sendPacket(html);
		
		return null;
	}
	
	private String spawnClanhallNpc(Player player, int npcId)
	{
		if (!((player.getClanPrivileges() & Clan.CP_CH_SET_FUNCTIONS) == Clan.CP_CH_SET_FUNCTIONS) && !player.isGM())
			return "Only clan clan members with privilegies to set clanhall functions can spawn NPCs in their clanhall.";
		else if (!player.isGM() && player.getClan().getHasHideout() == 0)
			return "You do not own a clanhall.";
		else if (!player.isInZone(ZoneType.RESIDENCE))
			return "You are not located in a clanhall.";
		
		ClanHall zone = ResidenceHolder.getInstance().getResidenceByCoord(ClanHall.class, player.getX(), player.getY(), player.getZ(), player.getReflection());
		if (zone == null)
			return "You are not located in a clanhall.";
		else if (!player.isGM() && player.getClan().getHasHideout() != zone.getId())
			return "This clanhall doesn't belong to your clan.";
		
		for (NpcInstance cha : zone.getZone().getInsideNpcs())
		{
			if (cha.isNpc() && cha.getNpcId() == npcId)
				return "This NPC is already spawned in your clanhall.";
		}
		
		try
		{
			NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
			SimpleSpawner spawn = new SimpleSpawner(template);
			//spawn.setCustom(true);
			spawn.setLocx(player.getX());
			spawn.setLocy(player.getY());
			spawn.setLocz(player.getZ());
			spawn.setAmount(1);
			spawn.setHeading(player.getHeading());
			spawn.setRespawnDelay(0);
			spawn.setReflection(player.getReflection()); // cant understand what this does, need to test...
			spawn.stopRespawn();
			SpawnTable.getInstance().addNewSpawn(spawn);
			spawn.init();
			
			String msg = showClanhallNpcSpawnWindow(player);
			if (msg != null)
				player.sendMessage(msg);
			
			return "You have spawned " + template.getName();
		}
		catch (Exception e)
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOUR_TARGET_CANNOT_BE_FOUND));
		}
		
		return "There has been a problem while spawning the NPC.";
	}
	
	private String unspawnClanhallNpc(Player player, int npcObjId)
	{
		if (!player.isClanLeader() && !player.isGM())
			return "Only clan leaders can unspawn NPCs from their clanhall.";
		
		GameObject obj = GameObjectsStorage.findObject(npcObjId);
		if (obj == null || !obj.isNpc())
			return "NPC not found.";
		
		NpcInstance npc = (NpcInstance) obj;
		if (!Util.contains(CLANHALL_NPC_IDS, npc.getNpcId()))
			return "You cannot unspawn this NPC.";
		ClanHall zone = ResidenceHolder.getInstance().getResidenceByCoord(ClanHall.class, player.getX(), player.getY(), player.getZ(), player.getReflection());
		if (zone == null)
			return "The selected NPC is not in a clanhall.";
		else if (!player.isGM() && player.getClan().getHasHideout() != zone.getId())
			return "The selected NPC is not in your clanhall.";
		
		Spawner spawn = npc.getSpawn();
		if (spawn != null)
		{
			spawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(npc.getSpawnedLoc(), npc.getNpcId());
			npc.deleteMe();
			
			String msg = showClanhallNpcSpawnWindow(player);
			if (msg != null)
				player.sendMessage(msg);
			
			return "Deleted " + npc.getName() + " from your clanhall.";
		}
		
		return "Something is wrong while trying to unspawn the NPC.";
	}
	
	private String getNpcName(int npcId)
	{
		NpcTemplate tmpl = NpcHolder.getInstance().getTemplate(npcId);
		if (tmpl == null)
		{
			_log.warn("Npc template is null for NPC ID: "+npcId);
			return "Unknown";
		}
		
		return tmpl.getName();
	}
	
	private void botReportcommand(Player activeChar, String typeofreport, String moreinfo)
	{
		if (Config.ENABLE_AUTO_HUNTING_REPORT)
		{
			if (activeChar.getTarget() != null && activeChar.getTarget().isPlayer() && !typeofreport.isEmpty() && typeofreport != null)
			{
				Player reported = activeChar.getTarget().getPlayer();
				
				if (reported == null)
					return;
				
				if (!AutoHuntingManager.getInstance().validateBot(reported, activeChar))
					return;
				
				if (!AutoHuntingManager.getInstance().validateReport(activeChar))
					return;

				try
				{
					AutoHuntingManager.getInstance().reportBot(reported, activeChar, typeofreport, moreinfo);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message15", activeChar));
		}
		else
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.randomcommands.message16", activeChar));
	}
	
	public String getAbsorbType(AbsorbType type)
	{
		if (type == null)
			return "Missing data";
		
		switch(type)
		{
			case LAST_HIT:
				return "Last Hit";
			case PARTY_ONE:
				return "One from Party";
			case PARTY_ALL:
				return "Whole Party";
			case PARTY_RANDOM:
				return "Random from Party";
		}
		
		return "None";
	}
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}