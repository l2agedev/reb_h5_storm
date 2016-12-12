package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.SkillList;

public final class RequestSkillList extends L2GameClientPacket
{
	private static final String _C__50_REQUESTSKILLLIST = "[C] 50 RequestSkillList";

	@Override
	protected void readImpl()
	{
		// this is just a trigger packet. it has no content
	}

	@Override
	protected void runImpl()
	{
		Player cha = getClient().getActiveChar();

		if(cha != null)
			cha.sendPacket(new SkillList(cha));
	}

	@Override
	public String getType()
	{
		return _C__50_REQUESTSKILLLIST;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
