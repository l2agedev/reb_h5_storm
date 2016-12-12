package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.CharMoveToLocation;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.Location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// cdddddd(d)
public class MoveBackwardToLocation extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(MoveBackwardToLocation.class);
	
	private Location _targetLoc = new Location();
	private Location _originLoc = new Location();
	private int _moveMovement;

	/**
	 * packet type id 0x0f
	 */
	@Override
	protected void readImpl()
	{
		_targetLoc.x = readD();
		_targetLoc.y = readD();
		_targetLoc.z = readD();
		_originLoc.x = readD();
		_originLoc.y = readD();
		_originLoc.z = readD();
		if(_buf.hasRemaining())
			_moveMovement = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		try
		{
			activeChar.setActive();

			if(_moveMovement == 0 && (!Config.ALLOW_KEYBOARD_MOVE || activeChar.getReflectionId() > 0))
			{
				activeChar.sendActionFailed();
				return;
			}
			
			if(System.currentTimeMillis() - activeChar.getLastMovePacket() < Config.MOVE_PACKET_DELAY)
			{
				activeChar.sendActionFailed();
				return;
			}

			//long lastMoveTime = activeChar.getLastMovePacket();
			activeChar.setLastMovePacket();

			if(activeChar.isTeleporting())
			{
				activeChar.sendActionFailed();
				return;
			}

			if(activeChar.isFrozen())
			{
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFail.STATIC);
				return;
			}
			
			if(activeChar.isInObserverMode())
			{
				if(activeChar.getOlympiadObserveGame() == null)
					activeChar.sendActionFailed();
				else
					activeChar.sendPacket(new CharMoveToLocation(activeChar.getObjectId(), _originLoc, _targetLoc));
				return;
			}

			if(activeChar.isOutOfControl())
			{
				activeChar.sendActionFailed();
				return;
			}

			if(activeChar.getTeleMode() > 0)
			{
				if(activeChar.getTeleMode() == 1)
					activeChar.setTeleMode(0);
				activeChar.sendActionFailed();
				activeChar.teleToLocation(_targetLoc);
				return;
			}
			
			if(activeChar.isInFlyingTransform())
				_targetLoc.z = Math.min(5950, Math.max(50, _targetLoc.z)); // В летающей трансформе нельзя летать ниже, чем 0, и выше, чем 6000

			/*
			if (lastMoveTime == 0)
				lastMoveTime = activeChar.getLastMovePacket();
			
			LocationStorage.addLocation(activeChar.getObjectId(), _targetLoc, (int)(activeChar.getLastMovePacket() - lastMoveTime));
			*/
			
			activeChar.moveToLocation(_targetLoc, 0, _moveMovement != 0 && !activeChar.getVarB("no_pf"));
		}
		catch (Exception e)
		{
			_log.warn("Possible geodata error for player " + activeChar + " from location " + _originLoc + " to location " + _targetLoc, e);

		}
	}
}