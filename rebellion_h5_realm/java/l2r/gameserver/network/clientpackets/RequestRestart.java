package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2r.gameserver.network.GameClient.GameClientState;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.CharacterSelectionInfo;
import l2r.gameserver.network.serverpackets.RestartResponse;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;

public class RequestRestart extends L2GameClientPacket
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

		if(activeChar.isInObserverMode())
		{
			activeChar.sendPacket(SystemMsg.OBSERVERS_CANNOT_PARTICIPATE, RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}

		if(!activeChar.isGM() && activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_RESTART_WHILE_IN_COMBAT, RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2, RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}

		if(activeChar.isInJail())
		{
			activeChar.standUp();
			activeChar.unblock();
		}
		else if(activeChar.isBlocked()) // Разрешаем выходить из игры если используется сервис HireWyvern. Вернет в начальную точку.
		{
			if(!activeChar.isFlying())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.clientpackets.RequestRestart.OutOfControl", activeChar));
				activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
				return;
			}
		}

		if(activeChar.getVar("isPvPevents") != null)
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можите выйти во время участия в ивенте!" : "You can follow any responses did not leave while participating in the event!");
			activeChar.sendActionFailed();
			return;
		}

		if (NexusEvents.isInEvent(activeChar) && !activeChar.isGM())
		{
			activeChar.sendMessage("A superior power doesn't allow you to leave the event");
			activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}
		
		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.clientpackets.Logout.Olympiad", activeChar));
			activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}

		if(activeChar.isInStoreMode() && !activeChar.isInZone(Zone.ZoneType.offshore) && Config.SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE)
		{
			activeChar.sendMessage(new CustomMessage("trade.OfflineNoTradeZoneOnlyOffshore", activeChar));
			activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}
		// Prevent player from restarting if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if(activeChar.isFestivalParticipant())
			if(SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.clientpackets.RequestRestart.Festival", activeChar));
				activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
				return;
			}

		if(getClient() != null)
			getClient().setState(GameClientState.AUTHED);
		activeChar.restart(false);
		// send char list
		CharacterSelectionInfo cl = new CharacterSelectionInfo(getClient().getLogin(), getClient().getSessionKey().playOkID1);
		sendPacket(RestartResponse.OK, cl);
		getClient().setCharSelection(cl.getCharInfo());
	}
}