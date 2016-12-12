package l2r.gameserver.network.clientpackets;

import l2r.commons.lang.ArrayUtils;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.RestartType;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.events.GlobalEvent;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.model.entity.residence.ClanHall;
import l2r.gameserver.model.entity.residence.Fortress;
import l2r.gameserver.model.entity.residence.ResidenceFunction;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.Die;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;

import org.apache.commons.lang3.tuple.Pair;

public class RequestRestartPoint extends L2GameClientPacket
{
	private RestartType _restartType;

	@Override
	protected void readImpl()
	{
		_restartType = ArrayUtils.valid(RestartType.VALUES, readD());
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(_restartType == null || activeChar == null)
			return;

		if(activeChar.isFakeDeath())
		{
			activeChar.breakFakeDeath();
			return;
		}

		if (activeChar.isInJail())
		{
			_restartType = RestartType.JAIL;
		}
		
		if(!activeChar.isDead() && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}

		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendActionFailed();
			return;
		}
		
		if(activeChar.isFestivalParticipant())
		{
			activeChar.doRevive();
			return;
		}

		if(NexusEvents.isInEvent(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}
		
		switch(_restartType)
		{
			case JAIL:
				if (!activeChar.isInJail()) 
					return;
				activeChar.teleToLocation(-114356, -249645, -2984);
				break;
			case AGATHION:
				if(activeChar.isAgathionResAvailable())
					activeChar.doRevive(100);
				else
					activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
				break;
			case FIXED:
				if(activeChar.getAccessLevel().allowFixedRes())
					activeChar.doRevive(100);
				else if(ItemFunctions.removeItem(activeChar, 13300, 1, true) == 1)
				{
					activeChar.sendPacket(SystemMsg.YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT);
					activeChar.doRevive(100);
				}
				else if(ItemFunctions.removeItem(activeChar, 10649, 1, true) == 1)
				{
					activeChar.sendPacket(SystemMsg.YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT);
					activeChar.doRevive(100);
				}
				else
					activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
				break;
			default:
				Location loc = null;
				Reflection ref = activeChar.getReflection();

				if(ref == ReflectionManager.DEFAULT)
					for(GlobalEvent e : activeChar.getEvents())
						loc = e.getRestartLoc(activeChar, _restartType);

				if(loc == null)
					loc = defaultLoc(_restartType, activeChar);
				
				if(loc != null)
				{
					Pair<Integer, OnAnswerListener> ask = activeChar.getAskListener(false);
					if(ask != null && ask.getValue() instanceof ReviveAnswerListener && !((ReviveAnswerListener) ask.getValue()).isForPet())
						activeChar.getAskListener(true);

					activeChar.setPendingRevive(true);
					activeChar.teleToLocation(loc, ReflectionManager.DEFAULT);
				}
				else
					activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
				break;
		}
	}

	//FIXME [VISTALL] вынести куда то?
	// телепорт к флагу, не обрабатывается, по дефалту
	public static Location defaultLoc(RestartType restartType, Player activeChar)
	{
		Location loc = null;
		Clan clan = activeChar.getClan();

		switch(restartType)
		{
			case TO_CLANHALL:
				if(clan != null && clan.getHasHideout() != 0)
				{
					ClanHall clanHall = activeChar.getClanHall();
					loc = Location.getRestartLocation(activeChar, RestartType.TO_CLANHALL);
					if(clanHall.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(clanHall.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
				}
				break;
			case TO_CASTLE:
				if(clan != null && clan.getCastle() != 0)
				{
					Castle castle = activeChar.getCastle();
					loc = Location.getRestartLocation(activeChar, RestartType.TO_CASTLE);
					if(castle.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(castle.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
				}
				break;
			case TO_FORTRESS:
				if(clan != null && clan.getHasFortress() != 0)
				{
					Fortress fort = activeChar.getFortress();
					loc = Location.getRestartLocation(activeChar, RestartType.TO_FORTRESS);
					if(fort.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(fort.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
				}
				break;
			case TO_FLAG:
				break;
			case TO_VILLAGE:
			default:
				loc = Location.getRestartLocation(activeChar, RestartType.TO_VILLAGE);
				break;
		}
		return loc;
	}
}
