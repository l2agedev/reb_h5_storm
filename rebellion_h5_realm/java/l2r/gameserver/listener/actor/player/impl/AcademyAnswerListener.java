package l2r.gameserver.listener.actor.player.impl;

import l2r.commons.lang.reference.HardReference;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.model.AcademyList;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Request;
import l2r.gameserver.network.serverpackets.components.ChatType;

public class AcademyAnswerListener implements OnAnswerListener
{
	private final HardReference<Player> _activeChar;
	private final HardReference<Player> _academyChar;
	
	public AcademyAnswerListener(Player activeChar, Player academyChar)
	{
		_activeChar = activeChar.getRef();
		_academyChar = academyChar.getRef();
	}
	
	@Override
	public void sayYes()
	{
		Player activeChar = _activeChar.get();
		Player academyChar = _academyChar.get();
		if (activeChar == null || academyChar == null)
			return;
		
		AcademyList.inviteInAcademy(activeChar, academyChar);
	}
	
	@Override
	public void sayNo()
	{
		Player activeChar = _activeChar.get();
		Player academyChar = _academyChar.get();
		if (activeChar == null || academyChar == null)
			return;
		
		Request req = activeChar.getRequest();
		if (req != null && req.isInProgress())
			req.cancel();
		
		activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.BATTLEFIELD.ordinal(), "Academy", "Player " + academyChar.getName() + " refused to join!");
	}
}
