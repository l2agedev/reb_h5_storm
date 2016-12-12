package npc.model;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.npc.NpcTemplate;

import java.util.StringTokenizer;

import bosses.BelethManager;

/**
 * @author pchayka
 */

public final class BelethCoffinInstance extends NpcInstance
{
	private static final int RING = 10314;

	public BelethCoffinInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		StringTokenizer st = new StringTokenizer(command);
		if(st.nextToken().equals("request_ring"))
		{
			if(!BelethManager.isRingAvailable())
			{
				player.sendPacket(new NpcHtmlMessage(player, this).setHtml("Stone Coffin:<br><br>Ring is not available. Get lost!"));
				return;
			}
			if(player.getParty() == null)
			{
				player.sendPacket(new NpcHtmlMessage(player, this).setHtml("Stone Coffin:<br><br>You are not allowed to take the ring. Only the leader of party can."));
				return;
			}
			if(!player.getParty().isLeader(player))
			{
				player.sendPacket(new NpcHtmlMessage(player, this).setHtml("Stone Coffin:<br><br>You are not leader of this party."));
				return;
			}

			Player party = player.getParty().getLeader();

			Functions.addItem(player, RING, 1);

			SystemMessage smsg = new SystemMessage(SystemMessage.S1_HAS_OBTAINED_S2);
			smsg.addString(player.getName());
			smsg.addItemName(RING);
			party.sendPacket(smsg);

			BelethManager.setRingAvailable(false);
			deleteMe();

		}
		else
			super.onBypassFeedback(player, command);
	}
}