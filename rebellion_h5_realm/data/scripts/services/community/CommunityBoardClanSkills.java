package services.community;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.UserInfo;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.tables.SkillTable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunityBoardClanSkills implements ScriptFile, ICommunityBoardHandler
{
	
	private static final Logger _log = LoggerFactory.getLogger(CommunityBoardClanSkills.class);
	
	private static final String[] commands = new String[]
	{
		"_bbs_skillSeller;",
		"_bbs_sellSkillGroup;"
	};
	
	private static final int PRICE_ITEM_ID = Config.COMMUNITY_CLAN_SKILL_SELLER_ITEM;
	
	private static final Map<Integer, SellableSkillGroup> TEACHABLE_SKILL_GROUPS = new HashMap<Integer, SellableSkillGroup>();
	static 
	{
		TEACHABLE_SKILL_GROUPS.put(1, new SellableSkillGroup(1750,
				new SellableSkill(370, 3, true),new SellableSkill(379, 3, true),new SellableSkill(373, 3, true),new SellableSkill(391, 1, true),new SellableSkill(376, 3, true),
				new SellableSkill(377, 3, true),new SellableSkill(374, 3, true),new SellableSkill(383, 3, true),new SellableSkill(371, 3, true),new SellableSkill(390, 3, true),
				new SellableSkill(380, 3, true),new SellableSkill(384, 3, true),new SellableSkill(385, 3, true),new SellableSkill(387, 3, true),new SellableSkill(386, 3, true),
				new SellableSkill(388, 3, true),new SellableSkill(382, 3, true),new SellableSkill(378, 3, true),new SellableSkill(372, 3, true),new SellableSkill(389, 3, true),
				new SellableSkill(375, 3, true),new SellableSkill(381, 3, true),new SellableSkill(615, 3, true),new SellableSkill(616, 3, true),new SellableSkill(614, 3, true),
				new SellableSkill(611, 1, true),new SellableSkill(612, 3, true),new SellableSkill(613, 3, true)));
	}
	
	@Override
	public void onBypassCommand(Player player, String command)
	{
		
		final String[] commands = command.split(";");
		if (player.getClanId() < 1 || !player.isClanLeader())
		{
			HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/clanskills/skillseller-error.htm", player);
			
			return;
		}
		
		if (command.equalsIgnoreCase("_bbs_skillSeller;"))
		{
			ShowBoard.separateAndSend(getSkillSellerHtml(player), player);
		}
		else if (commands[0].equalsIgnoreCase("_bbs_sellSkillGroup"))
		{
			
			int groupId = Integer.parseInt(commands[1]);
			boolean cont = true;
			final String boughtSkillGroups = player.getVar("boughtSkillGroups");
			if (!StringUtils.isEmpty(boughtSkillGroups))
			{
				
				String[] boughtGroupIds = boughtSkillGroups.split(",");
				
				for (String boughtGroupId : boughtGroupIds)
				{
					if (Integer.parseInt(boughtGroupId) == groupId)
					{
						player.sendMessage("You already have this skill pack.");
						cont = false;
					}
				}
				
			}
			if (cont)
			{
				sellSkills(player, groupId);
			}
			
		}
		
	}
	
	public void sellSkills(final Player player, final int groupId)
	{
		final SellableSkillGroup group = TEACHABLE_SKILL_GROUPS.get(groupId);
		boolean canBuySkill = takePrice(player, PRICE_ITEM_ID, group.price);
		if (!canBuySkill)
		{
			return;
		}
		
		teachSkills(player, group);
		
		String boughtSkillGroups = player.getVar("boughtSkillGroups");
		if (StringUtils.isEmpty(boughtSkillGroups))
			boughtSkillGroups = String.valueOf(groupId);
		else
			boughtSkillGroups = boughtSkillGroups + "," + groupId;
		
		player.setVar("boughtSkillGroups", boughtSkillGroups, -1);
	}
	
	private static boolean takePrice(Player player, int itemId, int count)
	{
		ItemInstance pay = player.getInventory().getItemByItemId(itemId);
		
		if (pay != null && pay.getCount() >= count)
		{
			player.getInventory().destroyItem(pay, count);
			player.sendPacket(new SystemMessage(SystemMsg.S1_HAS_DISAPPEARED).addItemName(itemId));
			
			return true;
		}
		
		return false;
	}
	
	private static void teachSkills(Player player, SellableSkillGroup group)
	{
		for (SellableSkill skill : group.sellableSkills)
			if (skill.clanSkill)
			{
				if (player.getClan() != null)
				{
					teachSkill(player, skill.id, skill.level);
					player.sendPacket(new SkillList(player), new UserInfo(player));
				}
			}
			else
			{
				teachSkill(player, skill.id, skill.level);
				player.sendPacket(new SkillList(player), new UserInfo(player));
			}
	}
	
	private static void teachSkill(Player player, int skillId, int skillLevel)
	{
		final Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
		
		if (skill == null)
		{
			throw new IllegalArgumentException("SkillSeller: Not existing skill " + String.valueOf(skillId));
		}
		
		player.getClan().addSkill(skill, true);
		player.sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_EARNED_S1).addSkillName(skillId, skillLevel));
	}
	
	private static String getSkillSellerHtml(Player player)
	{
		
		String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/clanskills/skillSeller.htm", player);
		
		StringBuilder builder = new StringBuilder();
		for (final Entry<Integer, SellableSkillGroup> group : TEACHABLE_SKILL_GROUPS.entrySet())
		{
			builder.append("<center>");
			builder.append("<button action=\"bypass _bbs_sellSkillGroup;" + group.getKey() + "\" value=\"");
			appendButtonHtml(builder, group);
			builder.append("\" width=235 height=40 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			builder.append("</center>");
		}
		
		html = html.replace("%skillSeller_buttons%", builder.toString());
		
		return html;
	}
	
	private static StringBuilder appendButtonHtml(StringBuilder builder, Entry<Integer, SellableSkillGroup> group)
	{
		return builder.append("Купить все скиллы за ").append(group.getValue().price).append(" " + ItemHolder.getInstance().getTemplate(PRICE_ITEM_ID).getName());
	}
	
	private static class SellableSkillGroup
	{
		final int price;
		final SellableSkill[] sellableSkills;
		
		public SellableSkillGroup(int price, SellableSkill... sellableSkills)
		{
			this.price = price;
			this.sellableSkills = sellableSkills;
		}
	}
	
	private static class SellableSkill
	{
		final int id;
		final int level;
		final boolean clanSkill;
		
		SellableSkill(int id, int level, boolean clanSkill)
		{
			this.id = id;
			this.level = level;
			this.clanSkill = clanSkill;
		}
	}
	
	@Override
	public String[] getBypassCommands()
	{
		
		return commands;
	}
	
	@Override
	public void onLoad()
	{
		if (!Config.ALLOW_COMMUNITY_CLAN_SKILLS_SELLER)
			return;
		
		CommunityBoardManager.getInstance().registerHandler(this);
		_log.info("CommunityBoard: Clan SKills Seller Loaded.");
	}
	
	@Override
	public void onReload()
	{
		if (!Config.ALLOW_COMMUNITY_CLAN_SKILLS_SELLER)
			return;
		
		CommunityBoardManager.getInstance().removeHandler(this);
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		
	}
	
}