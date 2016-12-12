package npc.model.residences.clanhall;

import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.residence.ClanHall;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.TimeUtils;

/**
 * @author VISTALL
 * @date 18:16/04.03.2011
 */
public class BrakelInstance  extends NpcInstance
{
	public BrakelInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		ClanHall clanhall = ResidenceHolder.getInstance().getResidence(ClanHall.class, 21);
		if(clanhall == null)
			return;
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("residence2/clanhall/partisan_ordery_brakel001.htm");
		html.replace("%next_siege%", TimeUtils.toSimpleFormat(clanhall.getSiegeDate().getTimeInMillis()));
		player.sendPacket(html);
	}
}
