package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.pledge.Alliance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.ClanTable;

public class RequestDismissAlly extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Clan clan = activeChar.getClan();
		if(clan == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if (Config.SECURITY_ENABLED && Config.SECURITY_CLAN_ALLY_ALL && activeChar.getSecurity())
		{
			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			return;
		}
		
		Alliance alliance = clan.getAlliance();
		if(alliance == null)
		{
			activeChar.sendPacket(SystemMsg.YOU_ARE_NOT_CURRENTLY_ALLIED_WITH_ANY_CLANS);
			return;
		}

		if(!activeChar.isAllyLeader())
		{
			activeChar.sendPacket(SystemMsg.THIS_FEATURE_IS_ONLY_AVAILABLE_TO_ALLIANCE_LEADERS);
			return;
		}

		if(alliance.getMembersCount() > 1)
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_DISSOLVE_THE_ALLIANCE);
			return;
		}

		ClanTable.getInstance().dissolveAlly(activeChar);
	}
}