package l2r.gameserver.network.clientpackets;

import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.QuestList;

public class RequestQuestAbort extends L2GameClientPacket
{
	private int _questID;

	@Override
	protected void readImpl()
	{
		_questID = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		Quest quest = QuestManager.getQuest(_questID);
		if(activeChar == null || quest == null)
			return;

		if(!quest.canAbortByPacket())
		{
			activeChar.sendPacket(new QuestList(activeChar));
			return;
		}

		QuestState qs = activeChar.getQuestState(quest.getClass());
		if(qs != null && !qs.isCompleted())
			qs.abortQuest();
	}
}