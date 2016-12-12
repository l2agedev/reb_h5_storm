package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.instancemanager.PetitionManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.AdminTable;

/**
 * <p>Format: (c) d
 * <ul>
 * <li>d: Unknown</li>
 * </ul></p>
 *
 * @author n0nam3
 */
public final class RequestPetitionCancel extends L2GameClientPacket
{
	//private int _unknown;

	@Override
	protected void readImpl()
	{
		//_unknown = readD(); This is pretty much a trigger packet.
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(PetitionManager.getInstance().isPlayerInConsultation(activeChar))
		{
			if(activeChar.isGM())
				PetitionManager.getInstance().endActivePetition(activeChar);
			else
				activeChar.sendPacket(new SystemMessage2(SystemMsg.YOUR_PETITION_IS_BEING_PROCESSED));
		}
		else if(PetitionManager.getInstance().isPlayerPetitionPending(activeChar))
		{
			if(PetitionManager.getInstance().cancelActivePetition(activeChar))
			{
				int numRemaining = Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar);

				SystemMessage sm;
				sm = new SystemMessage(SystemMessage.THE_PETITION_WAS_CANCELED_YOU_MAY_SUBMIT_S1_MORE_PETITIONS_TODAY);
				sm.addNumber(numRemaining);
				activeChar.sendPacket(sm);

				// Notify all GMs that the player's pending petition has been cancelled.
				String msgContent = activeChar.getName() + " has cancelled a pending petition.";
				AdminTable.broadcastToGMs(new Say2(activeChar.getObjectId(), ChatType.HERO_VOICE, "Petition System", msgContent));
			}
			else
				activeChar.sendPacket(new SystemMessage2(SystemMsg.FAILED_TO_CANCEL_PETITION));
		}
		else
			activeChar.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_NOT_SUBMITTED_A_PETITION));
	}
}
