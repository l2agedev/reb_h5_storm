package l2r.gameserver.instancemanager;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Skill.SkillType;
import l2r.gameserver.model.World;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.base.EnchantSkillLearn;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.tables.SkillTreeTable;
import l2r.gameserver.utils.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author Infern0
 */
public class OfflineBufferManager
{
	protected static final Logger _log = Logger.getLogger(OfflineBufferManager.class.getName());
	
	private static final int MAX_INTERACT_DISTANCE = 100;
	
	private final Map<Integer, BufferData> _buffStores = new ConcurrentHashMap<>();
	
	public void processBypass(Player player, String bypass)
	{
		StringTokenizer str = new StringTokenizer(bypass, " ");
		String command = str.nextToken();
		
		if (command.equalsIgnoreCase("showSellerMenu"))
		{
			StringTokenizer st = new StringTokenizer(bypass, " ");
			st.nextToken();
			boolean isSchemeMenu = (st.hasMoreTokens() ? st.nextToken().equalsIgnoreCase("scheme") : false);
			int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
			
 			// check if can open offline buffer.
			if (!canSetOfflineBuffer(player))
				return;
			
			BufferData buffer = null;
			if (getBuffStores().containsKey(player.getObjectId()))
				buffer = getBuffStores().get(player.getObjectId());
			else
			{
				buffer = new BufferData(player);
				
				List<Skill> buffList = getBuffList(player);
				for (Skill skill : buffList)
				{
					if (skill == null)
						continue;
					
					if (!Config.SELL_BUFF_SKILL_LIST.containsKey(skill.getId()))
						continue;
					
					buffer.getBuffs(true).put(skill.getId(), 0L);
					buffer.getBuffs(false).put(skill.getId(), 0L);
				}
				
				_buffStores.put(player.getObjectId(), buffer);
			}
			
			if (buffer == null)
				return;
			
			//buffer.getBuffs().clear();
			//_buffStores.clear();
			
			showStoreWindow(player, buffer, isSchemeMenu, page);
		}
		if (command.equalsIgnoreCase("showBuyerMenu"))
		{
			StringTokenizer st = new StringTokenizer(bypass, " ");
			st.nextToken();
			int playerId = Integer.parseInt(st.nextToken());
			boolean isPlayer = (st.hasMoreTokens() ? st.nextToken().equalsIgnoreCase("player") : true);
			boolean isSchemeMenu = (st.hasMoreTokens() ? st.nextToken().equalsIgnoreCase("scheme") : false);
			int page = (st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1);
			
			// Check if the buffer exists
			final BufferData buffer = _buffStores.get(playerId);
			if (buffer == null)
				return;
			
			Player plr = World.getPlayer(buffer.getOwner().getObjectId());
			
			// plr should be vissible as offline buffer
			if (plr == null)
				return;
			
			// Check if the player is in the right distance from the buffer
			if (player.getDistance(plr) > MAX_INTERACT_DISTANCE)
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You must be closer to the buffer!");
				return;
			}
			
			// Check if the player has a summon before buffing
			if (!isPlayer && player.getPet() == null)
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "No pet has been found....");
				
				// Send window again
				showBuyerWindow(player, buffer, !isPlayer, isSchemeMenu, page);
				return;
			}
			
			showBuyerWindow(player, buffer, isPlayer, isSchemeMenu, page);
		}
		if (command.equalsIgnoreCase("addBuffstoSell"))
		{
			String[] cm = bypass.split(" ");
			if (cm.length != 5)
				return;
			
			int skillid = Integer.valueOf(cm[1]);
			int page = Integer.valueOf(cm[2]);
			long price = Long.parseLong(cm[3]);
			boolean isScheme = cm[4].equalsIgnoreCase("scheme");
			
			if (price > 0 && price <= Config.MINIMUM_PRICE_FOR_OFFLINE_BUFF)
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "Too low price. Min is " + Util.formatAdena(Config.MINIMUM_PRICE_FOR_OFFLINE_BUFF) + " adena.");
				processBypass(player, "showSellerMenu " + cm[4] + " " + page);
				return;
			}
			
			if (price > 2000000000)
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "Too big price. Max is 2 000 000 000 adena.");
				processBypass(player, "showSellerMenu " + cm[4] + " " + page);
				return;
			}
			
			BufferData buffer = null;
			if (getBuffStores().containsKey(player.getObjectId()))
				buffer = getBuffStores().get(player.getObjectId());
			else
			{
				buffer = new BufferData(player);
				
				List<Skill> buffList = getBuffList(player);
				for (Skill skill : buffList)
				{
					if (skill == null)
						continue;
					
					if (!Config.SELL_BUFF_SKILL_LIST.containsKey(skill.getId()))
						continue;
					
					buffer.getBuffs(true).put(skill.getId(), 0L);
					buffer.getBuffs(false).put(skill.getId(), 0L);
				}
				
				_buffStores.put(player.getObjectId(), buffer);
			}
			
			if (buffer == null)
				return;
			
			buffer.setBuffPrice(skillid, price, isScheme);
			
			showStoreWindow(player, buffer, isScheme, page);
		}
		if (command.equalsIgnoreCase("purchaseScheme"))
		{
			StringTokenizer st = new StringTokenizer(bypass, " ");
			st.nextToken();
			
			int playerId = Integer.parseInt(st.nextToken());
			boolean isPlayer = st.hasMoreTokens() ? st.nextToken().equalsIgnoreCase("player") : true;
			
			// Check if the buffer exists
			final BufferData buffer = _buffStores.get(playerId);
			if (buffer == null)
				return;
			
			// seller should be visible as offline buffer.
			Player seller = World.getPlayer(buffer.getOwner().getObjectId());
			if (seller == null)
				return;
			
			// Check if the player is in the right distance from the buffer
			if (player.getDistance(seller) > MAX_INTERACT_DISTANCE)
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You must be closer to the buffer!");
				player.setTarget(null);
				return;
			}
			
			if (!seller.isSellBuff())
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You cant buy buffs now!");
				return;
			}
			
			if (player.getReflectionId() != 0)
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You cannot buy buffs in instance.");
				return;
			}
			
			if (player.isInOlympiadMode() || Olympiad.isRegistered(player))
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "Cannot buy buffs while you are regged for Olympiad.");
				return;
			}
			
			// Check if the player has a summon before buffing
			if (!isPlayer && player.getPet() == null)
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "No pet has been found....");
				
				// Send window again
				showBuyerWindow(player, buffer, !isPlayer, true, 1);
				return;
			}
			
			// Check buffing conditions
			if (player.getPvpFlag() > 0 || player.isInCombat() || player.getKarma() > 0 || player.isAlikeDead() || player.isInJail() || player.isInOlympiadMode() || player.isCursedWeaponEquipped() || player.isInStoreMode() || player.isInTrade() || player.getEnchantScroll() != null || player.isFishing())
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You don't meet the required conditions to use the buffer right now!");
				return;
			}
			
			long schemePrice = 0;
			
			for (Integer buffId : buffer.getBuffs(true).keySet())
			{
				Skill buff = SkillTable.getInstance().getInfo(buffId, getSkillLevel(seller, buffId));
				if (buff == null)
					continue;
				
				if (!Config.SELL_BUFF_SKILL_LIST.containsKey(buff.getId()))
					continue;
				
				long price = buffer.getBuffPrice(buff.getId(), true);
				
				if (price <= 0)
					continue;
				
				schemePrice += price;
			}
			
			if (schemePrice <= 0)
				return;
			
			int tax = (int) (schemePrice / 10); // 10 % going for treasure..
			
			if (schemePrice > 0 && !player.reduceAdena(schemePrice, true))
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You dont have enought adena!");
				return;
			}
			
			// If it have castle near the seller and its owned by a clan, add tax 10% to the treasury.
			// Else add the whole quantity to the seller.
			Castle nearestCastle = ResidenceHolder.getInstance().getResidenceByCoord(Castle.class, player.getX(), player.getY(), player.getZ(), player.getReflection());
			if (nearestCastle != null && nearestCastle.getOwner() != null)
			{
				seller.addAdena(schemePrice - tax);
				nearestCastle.addToTreasuryNoTax((long) tax, false, false);
			}
			else	
				seller.addAdena(schemePrice);
			
			for (Integer buffId : buffer.getBuffs(true).keySet())
			{
				Skill buff = SkillTable.getInstance().getInfo(buffId, getSkillLevel(seller, buffId));
				if (buff == null)
					continue;
				
				if (!Config.SELL_BUFF_SKILL_LIST.containsKey(buff.getId()))
					continue;
				
				long price = buffer.getBuffPrice(buff.getId(), true);
				
				if (price <= 0)
					continue;
				
				int timeSec = 0;
				if(Config.SELL_BUFF_SKILL_LIST.containsKey(buff.getId()))
					timeSec = Config.SELL_BUFF_SKILL_LIST.get(buff.getId());
				
				// give buffs to buyer
				if (isPlayer)
					buff.getEffects(player, player, false, false, timeSec * 1000, false);
				else
					buff.getEffects(player.getPet(), player.getPet(), false, false, timeSec * 1000, false);
			}
			
			// Send message
			player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You purchased scheme buffs from : " + seller.getName() + " for " + Util.formatAdena(schemePrice) + " adena.");
			
			// Send the buff list again after buffing, exactly where it was before
			showBuyerWindow(player, buffer, isPlayer, true, 1);
		}
		if (command.equalsIgnoreCase("purchaseBuff"))
		{
			StringTokenizer st = new StringTokenizer(bypass, " ");
			st.nextToken();
			
			int playerId = Integer.parseInt(st.nextToken());
			boolean isPlayer = st.hasMoreTokens() ? st.nextToken().equalsIgnoreCase("player") : true;
			int buffId = Integer.parseInt(st.nextToken());
			int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
			
			// Check if the buffer exists
			final BufferData buffer = _buffStores.get(playerId);
			if (buffer == null)
				return;
			
			// Check if the buffer has this buff
			if (!buffer.getBuffs(false).containsKey(buffId))
				return;
			
			
			Player seller = World.getPlayer(buffer.getOwner().getObjectId());
			if (seller == null)
				return;
			
			// Check if the player is in the right distance from the buffer
			if (player.getDistance(seller) > MAX_INTERACT_DISTANCE)
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You must be closer to the buffer!");
				player.setTarget(null);
				return;
			}
			
			if (!seller.isSellBuff())
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You cant buy buffs now!");
				return;
			}
			
			if (player.getReflectionId() != 0)
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You cannot buy buffs in instance.");
				return;
			}
			
			if (player.isInOlympiadMode() || Olympiad.isRegistered(player))
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "Cannot buy buffs while you are regged for Olympiad.");
				return;
			}
			
			if (!enoughSpiritOreForBuff(player, buffId))
			{
				if (seller.isOnline())
					seller.sendChatMessage(seller.getObjectId(), ChatType.TELL.ordinal(), "Server", "You can't sell buff, you haven't Spirit Ore for use skill!");
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You can't buff now. Seller dont have Spirit Ore :(");
				
				showBuyerWindow(player, buffer, isPlayer, false, page);
				return;
			}
			
			// Check if the player has a summon before buffing
			if (!isPlayer && player.getPet() == null)
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "No pet has been found....");
				
				// Send window again
				showBuyerWindow(player, buffer, !isPlayer, false, page);
				return;
			}
			
			// Check buffing conditions
			if (player.getPvpFlag() > 0 || player.isInCombat() || player.getKarma() > 0 || player.isAlikeDead() || player.isInJail() || player.isInOlympiadMode() || player.isCursedWeaponEquipped() || player.isInStoreMode() || player.isInTrade() || player.getEnchantScroll() != null || player.isFishing())
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You don't meet the required conditions to use the buffer right now!");
				return;
			}
			
			// get buff data
			Skill skill = SkillTable.getInstance().getInfo(buffId, getSkillLevel(seller, buffId));
			if (skill == null)
				return;
			
			// single skill mp consume
			if (Config.SELL_BUFF_SKILL_MP_ENABLED)
			{
				if (seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER >= (skill.getMpConsume() + 1))
				{
					seller.setCurrentMp(Math.round(seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER - skill.getMpConsume()) / Config.SELL_BUFF_SKILL_MP_MULTIPLIER);
				}
				else
				{
					player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "Buffer has no Mana Points (" + Math.round(seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER) + " / " + Math.round(seller.getMaxMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER) + ")");
					showBuyerWindow(player, buffer, isPlayer, false, page);
					return;
				}
			}
			
			long buffPrice = buffer.getBuffPrice(buffId, false);
			
			int tax = (int) (buffPrice / 10); // 10 % going for treasure..
			
			if (buffPrice > 0 && !player.reduceAdena(buffPrice, true))
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You dont have enought adena!");
				return;
			}
			
			// If it have castle near the seller and its owned by a clan, add tax 10% to the treasury.
			// Else add the whole quantity to the seller.
			Castle nearestCastle = ResidenceHolder.getInstance().getResidenceByCoord(Castle.class, player.getX(), player.getY(), player.getZ(), player.getReflection());
			if (nearestCastle != null && nearestCastle.getOwner() != null)
			{
				seller.addAdena(buffPrice - tax);
				nearestCastle.addToTreasuryNoTax((long) tax, false, false);
			}
			else	
				seller.addAdena(buffPrice);
			
			int timeSec = 0;
			if(Config.SELL_BUFF_SKILL_LIST.containsKey(skill.getId()))
				timeSec = Config.SELL_BUFF_SKILL_LIST.get(skill.getId());
			
			// give buffs to buyer
			if (isPlayer)
				skill.getEffects(player, player, false, false, timeSec * 1000, false);
			else
				skill.getEffects(player.getPet(), player.getPet(), false, false, timeSec * 1000, false);
			
			// Send message
			player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You bought: " + skill.getName() + " for " + Util.formatAdena(buffPrice) + " adena.");
			
			// Send the buff list again after buffing, exactly where it was before
			showBuyerWindow(player, buffer, isPlayer, false, page);
		}
		if (command.equalsIgnoreCase("startOfflineBuffer"))
		{
			if (canSetOfflineBuffer(player))
			{
				boolean canSetup = false;
				BufferData buff = getBuffStores().get(player.getObjectId());
				if (buff != null)
				{
					for (Long price : buff.getBuffs(false).values())
					{
						if (price > 0)
						{
							canSetup = true;
							break;
						}
					}
					
					for (Long price : buff.getBuffs(true).values())
					{
						if (price > 0)
						{
							canSetup = true;
							break;
						}
					}
				}
				
				if (!canSetup)
				{
					player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Server", "You must have atleast 1 Buff to go in Offline Buffer Mode.");
					return;
				}
				
				player.sitDown(null);
				player.setSellBuff(true);
				player.offlineBuffer();
			}
		}
	}
	
	/**
	 * Sends the to the player the buffer store window with all the buffs and info
	 * @param player
	 * @param buffer
	 * @param isForPlayer
	 * @param page
	 */
	private void showStoreWindow(Player player, BufferData buffer, boolean isScheme, int page)
	{
		String html = HtmCache.getInstance().getNotNull("scripts/services/offlinebuffer/Buffer.htm", player);
		
		int all = 0;
		int pageloop = 0;
		boolean pagereached = false;
		int totalpages = 0;
		int idloop = 0;
		StringBuilder buffList = new StringBuilder();
		boolean changeColor = false;
		long schemePrice = 0;
		
		for (Integer buffId : buffer.getBuffs(isScheme).keySet())
		{
			Skill buff = SkillTable.getInstance().getInfo(buffId, getSkillLevel(player, buffId));
			if (buff == null)
				continue;
			
			if (!Config.SELL_BUFF_SKILL_LIST.containsKey(buff.getId()))
				continue;
			
			all++;
			if(page == 1 && pageloop > 5)
				continue;
			if(!pagereached && all > page * 5)
				continue;
			if(!pagereached && all <= (page - 1) * 5)
				continue;
			pageloop++;
			
			int skillId = buff.getId();
			
			// if summoners skills (exception)
			if (skillId == 4699 || skillId == 4700)
				skillId = 1331;
			
			if (skillId == 4702 || skillId == 4703)
				skillId = 1332;
			
			String tmp_skillId = "" + skillId;
			if (skillId < 1000)
				tmp_skillId = "0" + skillId;
			
			long price = buffer.getBuffPrice(buff.getId(), isScheme);
			
			schemePrice += price;
			
			buffList.append("<tr>");
			buffList.append("<td fixwidth=300>");
			buffList.append("<table height=36 cellspacing=-1 border=0 bgcolor=" + (changeColor ? "171612" : "23221e") + ">");
			buffList.append("<tr>");
			buffList.append("<td width=42 height=32 valign=top><img width=32 height=32 src=\"icon.skill" + tmp_skillId + "\"></td>");
			
			String skillInfo = String.valueOf(buff.getLevel());
			
			Skill newSkill = SkillTable.getInstance().getInfo(buff.getId(), buff.getLevel());
			EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(newSkill.getId(), buff.getDisplayLevel());
			if (sl != null)
				skillInfo = sl.getType();
			
			buffList.append("<td fixwidth=240>" + buff.getName());
			buffList.append(" <font color=ffd969>" + skillInfo + "</font></td></tr>");
			buffList.append("</table>");
			
			buffList.append("<table width=275 cellspacing=-1 bgcolor=" + (changeColor ? "171612" : "23221e") + ">");
			buffList.append("<tr>");
			
			
			if (price == 0)
			{
				buffList.append("<td width=50 height=20><a action=\"bypass -h BuffStore addBuffstoSell " + buff.getId() + " " + page + " -1 " + (isScheme ? "scheme" : "single") + "\">Set Price</a></td>");
			}
			else if (price == -1)
			{
				// dumb but working...
				if (price == -1 && idloop++ < 2)
					buffer.setBuffPrice(skillId, 0L, isScheme);
				
				buffList.append("<td fixwidth=150><edit var=\"box\" width=\"100\" height=13 type=\"number\" length=11></td>");
				buffList.append("<td width=40 align=left><button action=\"bypass -h BuffStore addBuffstoSell " + buff.getId() + " " + page + " $box " + (isScheme ? "scheme" : "single") + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\"></td>");
			}
			else
			{
				buffList.append("<td height=10 fixwidth=150> Price: <font color=50DEDE>" + Util.formatAdena(price) + " adena.</font></td>");
				buffList.append("<td height=32 width=40 align=left><button action=\"bypass -h BuffStore addBuffstoSell " + buff.getId() + " " + page + " 0 " + (isScheme ? "scheme" : "single") + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red_Over\" fore=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red\"></td>");
			}
			
			buffList.append("</tr>");
			
			buffList.append("</table>");
			buffList.append("</td>");
			buffList.append("</tr>");
			
			buffList.append("<tr>");
			buffList.append("<td height=10></td>");
			buffList.append("</tr>");
			
			changeColor = !changeColor;
		}
		
		// Make the arrows buttons
		String previousPageButton = "&nbsp;";
		String nextPageButton = "&nbsp;";
		totalpages = all / 5 + 1;
		if(page == 1)
		{
			if(totalpages == 1)
				previousPageButton = "&nbsp;";
			else
				nextPageButton = "<button value=\"[NEXT]\" action=\"bypass -h BuffStore showSellerMenu " + (isScheme ? "scheme" : "single") + " " + (page + 1) + "\" width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				nextPageButton = "&nbsp;";
				previousPageButton = "<button value=\"[PREV]\" action=\"bypass -h BuffStore showSellerMenu " + (isScheme ? "scheme" : "single") + " " + (page - 1) + "\" width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			}
			else
			{
				
				previousPageButton = "<button value=\"[PREV]\" action=\"bypass -h BuffStore showSellerMenu " + (isScheme ? "scheme" : "single") + " " + (page - 1) + "\" width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
				nextPageButton = "<button value=\"[NEXT]\" action=\"bypass -h BuffStore showSellerMenu " + (isScheme ? "scheme" : "single") + " " + (page + 1) + "\" width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			}
		}
		
		if (schemePrice < 0)
			schemePrice = 0;
		
		if (isScheme)
		{
			html = html.replace("%singleButton%", "<button value=\"Single Buff\" action=\"bypass -h BuffStore showSellerMenu single 1\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			html = html.replace("%schemeButton%", "<font color=3ADF00 name=hs10>[Scheme Menu]</font>");
			html = html.replace("%schemeTotalPrice%", "Scheme price will be " + Util.formatAdena(schemePrice) + " adena.");
		}
		else
		{
			html = html.replace("%singleButton%", "<font color=2ECCFA name=hs10>[Single Buff Menu]</font>");
			html = html.replace("%schemeButton%", "<button value=\"Scheme Buffs\" action=\"bypass -h BuffStore showSellerMenu scheme 1\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			html = html.replace("%schemeTotalPrice%", "&nbsp;");
		}
		
		html = html.replace("%page%", "" + page);
		html = html.replace("%buffs%", buffList.toString());
		html = html.replace("%previousPageButton%", "" + previousPageButton);
		html = html.replace("%nextPageButton%", "" + nextPageButton);
		html = html.replace("%pageCount%", (page) + "/" + (totalpages));
		html = html.replace("%title%", "Offline Buffer Store Menu");
		
		buffList = new StringBuilder();
		
		buffList.append("<tr>");
		buffList.append("<td fixwidth=275 align=center>");
		buffList.append("<font color=07F24A name=hs10>If you feel ready...</font>");
		buffList.append("</td>");
		buffList.append("</tr>");
		buffList.append("<tr>");
		buffList.append("<td fixwidth=275 align=center>");
		buffList.append("<button value=\"Start Offline Buff Store\" action=\"bypass -h BuffStore startOfflineBuffer\" width=200 height=31 back=L2UI_CT1.OlympiadWnd_DF_Watch_Down fore=L2UI_CT1.OlympiadWnd_DF_Watch>");
		buffList.append("</td>");
		buffList.append("</tr>");
		
		html = html.replace("%info%", buffList.toString());
		
		NpcHtmlMessage npchtm = new NpcHtmlMessage(0);
		npchtm.setHtml(html);
		player.sendPacket(npchtm);
	}
	
	private void showBuyerWindow(Player player, BufferData buffer, boolean isForPlayer, boolean isScheme, int page)
	{
		String html = HtmCache.getInstance().getNotNull("scripts/services/offlinebuffer/Buffer.htm", player);
		
		int all = 0;
		int pageloop = 0;
		boolean pagereached = false;
		int totalpages = 0;
		long schemePrice = 0;
		
		Player seller = World.getPlayer(buffer.getOwner().getObjectId());
		// seller should be vissible as offline buffer
		if (seller == null)
			return;
		
		StringBuilder buffList = new StringBuilder();
		boolean changeColor = false;
		
		for (Integer buffId : buffer.getBuffs(isScheme).keySet())
		{
			Skill buff = SkillTable.getInstance().getInfo(buffId, getSkillLevel(seller, buffId));
			if (buff == null)
				continue;
			
			if (!Config.SELL_BUFF_SKILL_LIST.containsKey(buff.getId()))
				continue;
			
			long price = buffer.getBuffPrice(buff.getId(), isScheme);
			if (price == 0)
				continue;
			
			schemePrice += price;
			
			all++;
			if(page == 1 && pageloop > (isScheme ? 10 : 5))
				continue;
			if(!pagereached && all > page * (isScheme ? 10 : 5))
				continue;
			if(!pagereached && all <= (page - 1) * (isScheme ? 10 : 5))
				continue;
			pageloop++;
			
			int skillId = buff.getId();
			
			// if summoners skills (exception)
			if (skillId == 4699 || skillId == 4700)
				skillId = 1331;
			
			if (skillId == 4702 || skillId == 4703)
				skillId = 1332;
			
			String tmp_skillId = "" + skillId;
			if (skillId < 1000)
				tmp_skillId = "0" + skillId;

			if (isScheme)
			{
				buffList.append("<tr>");
				buffList.append("<td fixwidth=300>");
				buffList.append("<table height=36 cellspacing=-1 border=0 bgcolor=" + (changeColor ? "01A9DB" : "58D3F7") + ">");
				buffList.append("<tr>");
				buffList.append("<td width=42 height=32 valign=top><img width=32 height=32 src=\"icon.skill" + tmp_skillId + "\"></td>");
				
				String skillInfo = String.valueOf(buff.getLevel());
				
				Skill newSkill = SkillTable.getInstance().getInfo(buff.getId(), buff.getLevel());
				EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(newSkill.getId(), buff.getDisplayLevel());
				if (sl != null)
					skillInfo = sl.getType();
				
				buffList.append("<td fixwidth=240>" + buff.getName() + " <font color=ffd969>" + skillInfo + "</font></td>");
				buffList.append("</tr>");
				buffList.append("</table>");
				
				buffList.append("<table width=275 cellspacing=-1 bgcolor=" + (changeColor ? "171612" : "23221e") + ">");
				buffList.append("<tr>");
					
				buffList.append("<td height=10 fixwidth=150> Price: <font color=FF7575>" + Util.formatAdena(price) + " adena.</font></td>");

				buffList.append("</tr>");
				
				buffList.append("</table>");
				buffList.append("</td>");
				buffList.append("</tr>");
				
				buffList.append("<tr>");
				buffList.append("<td height=10></td>");
				buffList.append("</tr>");
			}
			else
			{
				buffList.append("<tr>");
				buffList.append("<td fixwidth=300>");
				buffList.append("<table height=36 cellspacing=-1 border=0 bgcolor=" + (changeColor ? "171612" : "23221e") + ">");
				buffList.append("<tr>");
				buffList.append("<td width=42 height=32 valign=top><img width=32 height=32 src=\"icon.skill" + tmp_skillId + "\"></td>");
				
				String skillInfo = String.valueOf(buff.getLevel());
				
				Skill newSkill = SkillTable.getInstance().getInfo(buff.getId(), buff.getLevel());
				EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(newSkill.getId(), buff.getDisplayLevel());
				if (sl != null)
					skillInfo = sl.getType();
				
				buffList.append("<td fixwidth=240>" + buff.getName() + " <font color=ffd969>" + skillInfo + "</font></td>");
				buffList.append("</tr>");
				buffList.append("</table>");
				
				buffList.append("<table width=275 cellspacing=-1 bgcolor=" + (changeColor ? "171612" : "23221e") + ">");
				buffList.append("<tr>");
				
				String whotobuff = "player";
				if (!isForPlayer)
					whotobuff = "pet";
					
				buffList.append("<td height=10 fixwidth=150> Price: <font color=FF7575>" + Util.formatAdena(price) + " adena.</font></td>");
				buffList.append("<td height=32 width=40 align=left><button action=\"bypass -h BuffStore purchaseBuff " + seller.getObjectId() + " " + whotobuff + " " + buff.getId() + " " + page + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\"></td>");

				buffList.append("</tr>");
				
				buffList.append("</table>");
				buffList.append("</td>");
				buffList.append("</tr>");
				
				buffList.append("<tr>");
				buffList.append("<td height=10></td>");
				buffList.append("</tr>");
			}
			
			changeColor = !changeColor;
		}
		
		// Make the arrows buttons
		String previousPageButton = "&nbsp;";
		String nextPageButton = "&nbsp;";
		totalpages = all / (isScheme ? 10 : 5) + 1;
		if(page == 1)
		{
			if(totalpages == 1)
				previousPageButton = "&nbsp;";
			else
				nextPageButton = "<button value=\"[NEXT]\" action=\"bypass -h BuffStore showBuyerMenu " + seller.getObjectId() + " player " + (isScheme ? "scheme" : "single") + " " + (page + 1) + "\" width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
		}
		else if(page > 1)
		{
			if(totalpages <= page)
			{
				nextPageButton = "&nbsp;";
				previousPageButton = "<button value=\"[PREV]\" action=\"bypass -h BuffStore showBuyerMenu " + seller.getObjectId() + " player " + (isScheme ? "scheme" : "single") + " " + (page - 1) + "\" width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			}
			else
			{
				
				previousPageButton = "<button value=\"[PREV]\" action=\"bypass -h BuffStore showBuyerMenu " + seller.getObjectId() + " player " + (isScheme ? "scheme" : "single") + " " + (page - 1) + "\" width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
				nextPageButton = "<button value=\"[NEXT]\" action=\"bypass -h BuffStore showBuyerMenu " + seller.getObjectId() + " player " + (isScheme ? "scheme" : "single") + " " + (page + 1) + "\" width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
			}
		}
		
		if (schemePrice < 0)
			schemePrice = 0;
		
		if (isScheme)
		{
			html = html.replace("%singleButton%", "<button value=\"Single Buff\" action=\"bypass -h BuffStore showBuyerMenu " + seller.getObjectId() + " player single 1\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			html = html.replace("%schemeButton%", "<font color=3ADF00 name=hs10>[Scheme Menu]</font>");
			if (schemePrice > 0)
				html = html.replace("%schemeTotalPrice%", "<font name=hs10>Scheme Price is " + Util.formatAdena(schemePrice) + " adena.</font> &nbsp;&nbsp; <button value=\"Buy\" action=\"bypass -h BuffStore purchaseScheme " + seller.getObjectId() + " player\" width=50 height=18 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			else
				html = html.replace("%schemeTotalPrice%", "&nbsp;");
		}
		else
		{
			html = html.replace("%singleButton%", "<font color=2ECCFA name=hs10>[Single Buff Menu]</font>");
			html = html.replace("%schemeButton%", "<button value=\"Scheme Buffs\" action=\"bypass -h BuffStore showBuyerMenu " + seller.getObjectId() + " player scheme 1\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			html = html.replace("%schemeTotalPrice%", "&nbsp;");
		}
		
		if (all == 0)
		{
			buffList.append("<tr>");
			buffList.append("<td fixwidth=300>");
			buffList.append("<table height=30 cellspacing=5 border=0 bgcolor=\"FE2E64\">");
			buffList.append("<tr>");
			buffList.append("<td align=center height=30 fixwidth=300>No " + (isScheme ? "scheme" : "single") + " buffs has been found!</td>");
			buffList.append("</tr>");
			buffList.append("</table>");
			buffList.append("</td>");
			buffList.append("</tr>");
		}
		
		html = html.replace("%target%", isForPlayer ? "Player" : "Pet");
		html = html.replace("%page%", "" + page);
		html = html.replace("%buffs%", buffList.toString());
		html = html.replace("%previousPageButton%", "" + previousPageButton);
		html = html.replace("%nextPageButton%", "" + nextPageButton);
		html = html.replace("%pageCount%", (page) + "/" + (totalpages));
		html = html.replace("%title%", "Offline Buffer Shop");
		
		buffList = new StringBuilder();

		buffList.append("<tr>");
		buffList.append("<td fixwidth=275 align=center>");
		buffList.append("<center>Hello, <font color=\"LEVEL\" name=hs12>" + player.getName() + "</font><br1>");
		buffList.append("I'm <font color=\"00BFFF\" name=hs12>" + Util.getFullClassName(seller.getClassId().getId()) + "</font> (" + seller.getLevel() + ") <br1>");
		
		buffList.append("<br>");
		buffList.append("</td>");
		buffList.append("</tr>");
		
		html = html.replace("%info%", buffList.toString());
		
		NpcHtmlMessage npchtm = new NpcHtmlMessage(0);
		npchtm.setHtml(html);
		player.sendPacket(npchtm);
	}
	public static OfflineBufferManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final OfflineBufferManager _instance = new OfflineBufferManager();
	}
	
	public static class BufferData
	{
		private final Player _owner;
		private final Map<Integer, Long> _buffs = new HashMap<Integer, Long>();
		private final Map<Integer, Long> _schemebuffs = new HashMap<Integer, Long>();
		
		public BufferData(Player player, int skillid, long price, boolean isScheme)
		{
			_owner = player;
			
			if (isScheme)
				_schemebuffs.put(skillid, price);
			else
				_buffs.put(skillid, price);
		}

		public BufferData(Player player)
		{
			_owner = player;
		}
		
		public Player getOwner()
		{
			return _owner;
		}
		
		public long getBuffPrice(int skillid, boolean forScheme)
		{
			if (forScheme)
				return _schemebuffs.containsKey(skillid) ? _schemebuffs.get(skillid) : -1;
			else
				return _buffs.containsKey(skillid) ? _buffs.get(skillid) : -1;
		}
		
		public void setBuffPrice(int skillid, long price, boolean isScheme)
		{
			if (isScheme)
				_schemebuffs.put(skillid, price);
			else
				_buffs.put(skillid, price);
		}
		
		public void removeBuff(int skillid, boolean fromScheme)
		{
			if (fromScheme)
				_schemebuffs.remove(skillid);
			else
				_buffs.remove(skillid);
		}
		
		public Map<Integer, Long> getBuffs(boolean fromScheme)
		{
			if (fromScheme)
				return _schemebuffs;
			else
				return _buffs;
		}
	}
	
	private boolean canSetOfflineBuffer(Player activeChar)
	{
		if (activeChar == null)
			return false;
		
		if (!Config.ENABLE_OFFLINE_BUFFERS)
			return false;
		
		if (activeChar.isDead() || activeChar.isAlikeDead())
		{
			activeChar.sendActionFailed();
			return false;
		}
		else if (activeChar.getOlympiadObserveGame() != null || activeChar.getOlympiadGame() != null || Olympiad.isRegisteredInComp(activeChar) || activeChar.getKarma() > 0 || activeChar.isInJail())
		{
			activeChar.sendActionFailed();
			return false;
		}
		else if (activeChar.isInStoreMode())
		{
			activeChar.sendActionFailed();
			return false;
		}
		else if (activeChar.getNoChannelRemained() > 0)
		{
			activeChar.sendActionFailed();
			return false;
		}
		if (activeChar.getReflectionId() != 0 || activeChar.isInZone(ZoneType.epic))
		{
			activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Server", activeChar.isLangRus() ? "Невозможно использовать в данных зонах!" : "Can not be used in these areas!");
			return false;
		}
		else if (NexusEvents.isInEvent(activeChar) || NexusEvents.isRegistered(activeChar))
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.offlinebuff.message1", activeChar));
			return false;
		}
		else if (!activeChar.isInPeaceZone())
		{
			activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Server", "You are not in peacefull zone , you can sell only in peacefull zones.");
			return false;
		}
		else if (Config.OFFLINE_SELLBUFF_ONLY_IN_ZONE && !activeChar.isInZone(ZoneType.offbuff) && (!activeChar.isInZone(ZoneType.RESIDENCE) || !activeChar.isInPeaceZone()))
		{
			activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Server", "You can set offline buffer only in a specific zone. Check forum for more info.");
			return false;
		}
		else if (activeChar.getPvpFlag() > 0 || activeChar.isInCombat() || activeChar.getKarma() > 0)
		{
			activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Server", "You are in combat mode , you can't sell at the moment.");
			return false;
		}
		else if (!Config.SELL_BUFF_CLASS_LIST.contains(Integer.toString(activeChar.getClassId().getId())))
		{
			activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Server", "Your class can't sell buffs.");
			return false;
		}
		else if (activeChar.getLevel() < Config.SELL_BUFF_MIN_LVL)
		{
			activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Server", "You can sell buffs on " + Config.SELL_BUFF_MIN_LVL + " level.");
			return false;
		}
		// summoner classes exception, buffs allowed from 56 level.
		else if (activeChar.getClassId().getId() == 96 || activeChar.getClassId().getId() == 14 || activeChar.getClassId().getId() == 104 || activeChar.getClassId().getId() == 28)
		{
			if (activeChar.getLevel() < 56)
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "Server", "You can sell buffs on level 56.");
				return false;
			}
		}
		else if (Config.SERVICES_TRADE_ONLY_FAR)
		{
			boolean tradenear = false;
			for (Player p : World.getAroundPlayers(activeChar, Config.SERVICES_TRADE_RADIUS, 200))
				if (p.isInStoreMode())
				{
					tradenear = true;
					break;
				}
			
			if (World.getAroundNpc(activeChar, Config.SERVICES_TRADE_RADIUS + 100, 200).size() > 0 && !activeChar.isInZone(ZoneType.RESIDENCE))
				tradenear = true;
			
			if (tradenear)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.offlinebuff.message2", activeChar));
				return false;
			}
		}
		
		return true;
	}
	
	public List<Skill> getBuffList(Player seller)
	{
		Collection<Skill> skills = seller.getAllSkills();
		List<Skill> ba = new ArrayList<Skill>();
		
		for (Skill s : skills)
		{
			if (s == null)
				continue;
			
			if ((seller.getClassId().getId() != 96 && seller.getClassId().getId() != 14 && seller.getClassId().getId() != 104 && seller.getClassId().getId() != 28) && ((s.getSkillType() == SkillType.BUFF || s.getSkillType() == SkillType.HEAL_PERCENT || s.getSkillType() == SkillType.COMBATPOINTHEAL) && s.isActive()))
			{
				if (Config.SELL_BUFF_FILTER_ENABLED)
				{
					if (seller.getClassId().getId() == 115 || seller.getClassId().getId() == 51)
					{
						if (s.getId() != 1002 && s.getId() != 1006 && s.getId() != 1007 && s.getId() != 1009)
							ba.add(s);
					}
					else if (seller.getClassId().getId() == 116 || seller.getClassId().getId() == 52)
					{
						if (s.getId() != 1003 && s.getId() != 1005)
							ba.add(s);
					}
					else if (seller.getClassId().getId() == 97 || seller.getClassId().getId() == 16)
					{
						if (s.getId() == 1353 || s.getId() == 1307 || s.getId() == 1311)
							ba.add(s);
					}
					else
						ba.add(s);
				}
				else
					ba.add(s);
			}
			else
			{
				Skill skill = null;
				if (s.getId() == 1332)
				{
					skill = SkillTable.getInstance().getInfo(4702, getSkillLevel(seller, 1332));
					ba.add(skill);
					skill = SkillTable.getInstance().getInfo(4703, getSkillLevel(seller, 1332));
					ba.add(skill);
				}
				if (s.getId() == 1331)
				{
					skill = SkillTable.getInstance().getInfo(4699, getSkillLevel(seller, 1331));
					ba.add(skill);
					skill = SkillTable.getInstance().getInfo(4700, getSkillLevel(seller, 1331));
					ba.add(skill);
				}
			}
		}
		return ba;
	}
	
	/**
	 * Special summoners skill level or normal skill level.
	 * @param seller
	 * @param skillId
	 * @return
	 */
	private int getSkillLevel(Player seller, int skillId)
	{
		
		if (seller.getClassId().getId() != 96 && seller.getClassId().getId() != 14 && seller.getClassId().getId() != 104 && seller.getClassId().getId() != 28)
			return seller.getSkillLevel(skillId);
		
		if (seller.getLevel() >= 56 && seller.getLevel() <= 57)
			return 5;
		if (seller.getLevel() >= 58 && seller.getLevel() <= 67)
			return 6;
		if (seller.getLevel() >= 68 && seller.getLevel() <= 73)
			return 7;
		if (seller.getLevel() >= 74)
			return 8;
		
		return 1;
	}
	
	public String getBuffsAsString(Player seller, boolean forScheme)
	{
		StringBuilder sb = new StringBuilder();
		
		BufferData data = _buffStores.get(seller.getObjectId());
		for (Entry<Integer, Long> buffs : data.getBuffs(forScheme).entrySet())
		{
			if (buffs == null)
				continue;
			
			int skillId = buffs.getKey();
			long price = buffs.getValue();
			
			if (price <= 0)
				continue;
			
			sb.append(skillId + "," + price);
			sb.append(";");
		}
		
		return sb.toString();
	}
	
	public void restoreOfflineBuffer(Player player, boolean scheme)
	{
		if (player == null)
			return;
		
		String[] data = null;
		
		if (scheme)
			data = player.getVar("offlinebufferschemeprice").split(";");
		else
			data = player.getVar("offlinebufferprice").split(";");
		
		BufferData buffer = null;
		
		if (_buffStores.containsKey(player.getObjectId()))
			buffer = _buffStores.get(player.getObjectId());
		
		for (String buffs : data)
		{
			if (buffs == null || buffs.isEmpty())
				continue;
			
			String[] buff = buffs.split(",");
			
			int skillid = Integer.valueOf(buff[0]);
			long price = Long.parseLong(buff[1]);
			if (buffer == null)
				buffer = new BufferData(player);
			
			buffer.getBuffs(scheme).put(skillid, price);
			
			List<Skill> buffList = getBuffList(player);
			for (Skill skill : buffList)
			{
				if (skill == null)
					continue;
				
				if (!Config.SELL_BUFF_SKILL_LIST.containsKey(skill.getId()))
					continue;
				
				if (!buffer.getBuffs(scheme).containsKey(skill.getId()))
					buffer.getBuffs(scheme).put(skill.getId(), 0L);
			}
			
			_buffStores.put(player.getObjectId(), buffer);
		}
		
		
	}
	
	private boolean enoughSpiritOreForBuff(Player seller, int skillId)
	{
		if (Config.SELL_BUFF_SKILL_ITEM_CONSUME_ENABLED)
		{
			if (getSkillConsumeSOCount(skillId, seller.getSkillLevel(skillId)) > 0)
			{
				// 3031 == Spirit Ore ID
				if (seller.getInventory().getItemByItemId(3031) == null || seller.getInventory().getItemByItemId(3031).getCount() < getSkillConsumeSOCount(skillId, seller.getSkillLevel(skillId)))
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns skill consume count (Spirit Ore count).
	 * @param skillId
	 * @param skillLvl
	 * @return
	 */
	private int getSkillConsumeSOCount(int skillId, int skillLvl)
	{
		// Buffs what consume items like Spirit Ore
		// {skillId, skillLvl, itemConsumeId, itemConsumeCount}
		int[][] skillsData =
		{
			{
				1388,
				1,
				3031,
				1
			},
			{
				1388,
				2,
				3031,
				2
			},
			{
				1388,
				3,
				3031,
				3
			},
			{
				1389,
				1,
				3031,
				1
			},
			{
				1389,
				2,
				3031,
				2
			},
			{
				1389,
				3,
				3031,
				3
			},
			{
				1356,
				1,
				3031,
				10
			},
			{
				1397,
				1,
				3031,
				1
			},
			{
				1397,
				2,
				3031,
				2
			},
			{
				1397,
				3,
				3031,
				3
			},
			{
				1355,
				1,
				3031,
				10
			},
			{
				1357,
				1,
				3031,
				10
			},
			{
				1416,
				1,
				3031,
				20
			},
			{
				1414,
				1,
				3031,
				40
			},
			{
				1391,
				1,
				3031,
				4
			},
			{
				1391,
				2,
				3031,
				8
			},
			{
				1391,
				3,
				3031,
				12
			},
			{
				1390,
				1,
				3031,
				4
			},
			{
				1390,
				2,
				3031,
				8
			},
			{
				1390,
				3,
				3031,
				12
			},
			{
				1363,
				1,
				3031,
				40
			},
			{
				1413,
				1,
				3031,
				40
			},
			{
				1323,
				1,
				3031,
				5
			}
		};
		
		for (int i = 0; i < skillsData.length; i++)
		{
			if (skillsData[i][0] == skillId && skillsData[i][1] == skillLvl)
			{
				return skillsData[i][3];
			}
		}
		return 0;
	}
	

	public Map<Integer, BufferData> getBuffStores()
	{
		return _buffStores;
	}
	
}
