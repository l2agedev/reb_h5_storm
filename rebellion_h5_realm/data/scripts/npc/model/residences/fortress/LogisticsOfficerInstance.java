package npc.model.residences.fortress;

import l2r.commons.dao.JdbcEntityState;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.residence.Fortress;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Location;

/**
 * @author VISTALL
 */
public class LogisticsOfficerInstance extends FacilityManagerInstance
{
	private static final int[] SUPPLY_NPC = new int[]
	{
		35665,
		35697,
		35734,
		35766,
		35803,
		35834
	};

	private static final int ITEM_ID = 9910; // Blood Oath

	public LogisticsOfficerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		Fortress fortress = getFortress();

		if(!player.isClanLeader() || fortress.getOwnerId() != player.getClanId())
		{
			showChatWindow(player, "residence2/fortress/fortress_not_authorized.htm");
			return;
		}

		if(command.equalsIgnoreCase("guardInfo"))
		{
			if(fortress.getContractState() != Fortress.CONTRACT_WITH_CASTLE)
			{
				showChatWindow(player, "residence2/fortress/fortress_supply_officer005.htm");
				return;
			}

			showChatWindow(player, "residence2/fortress/fortress_supply_officer002.htm", "%guard_buff_level%", fortress.getFacilityLevel(Fortress.GUARD_BUFF));
		}
		else if(command.equalsIgnoreCase("supplyInfo"))
		{
			if(fortress.getContractState() != Fortress.CONTRACT_WITH_CASTLE)
			{
				showChatWindow(player, "residence2/fortress/fortress_supply_officer005.htm");
				return;
			}

			showChatWindow(player, "residence2/fortress/fortress_supply_officer009.htm", "%supply_count%", fortress.getSupplyCount());
		}
		else if(command.equalsIgnoreCase("rewardInfo"))
		{
			showChatWindow(player, "residence2/fortress/fortress_supply_officer010.htm", "%blood_oaths%", fortress.getRewardCount());
		}
		else if(command.equalsIgnoreCase("receiveSupply"))
		{
			String filename;
			if(fortress.getSupplySpawn() + 3600000 < System.currentTimeMillis())
			{
				filename = "residence2/fortress/fortress_supply_officer016.htm";
				fortress.setSupplySpawn(System.currentTimeMillis());
				fortress.setJdbcState(JdbcEntityState.UPDATED);
				fortress.update();

				NpcInstance npc = NpcHolder.getInstance().getTemplate(SUPPLY_NPC[fortress.getSupplyCount() - 1]).getNewInstance();
				npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
				npc.spawnMe(new Location(getX() - 23, getY() + 41, getZ()));
				fortress.setSupplyCount(fortress.getSupplyCount() - 1);
				fortress.setJdbcState(JdbcEntityState.UPDATED);
				fortress.update();
			}
			else
				filename ="residence2/fortress/fortress_supply_officer017.htm";

			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile(filename);
			player.sendPacket(html);
		}
		else if(command.equalsIgnoreCase("receiveRewards"))
		{
			String filename;
			int count = fortress.getRewardCount();
			if(count > 0)
			{
				filename = "residence2/fortress/fortress_supply_officer013.htm";
				fortress.setRewardCount(0);
				fortress.setJdbcState(JdbcEntityState.UPDATED);
				fortress.update();

				Functions.addItem(player, ITEM_ID, count);
			}
			else
				filename ="residence2/fortress/fortress_supply_officer014.htm";

			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile(filename);
			player.sendPacket(html);
		}
		else if(command.equalsIgnoreCase("toLevel1"))
			buyFacility(player, Fortress.GUARD_BUFF, 1, 100000);
		else if(command.equalsIgnoreCase("toLevel2"))
			buyFacility(player, Fortress.GUARD_BUFF, 2, 150000);
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		showChatWindow(player, "residence2/fortress/fortress_supply_officer001.htm");
	}
}