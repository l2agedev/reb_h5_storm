package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.skills.EffectType;
import l2r.gameserver.skills.skillclasses.Call;
import l2r.gameserver.utils.Location;

import java.util.List;

public class Relocate extends Functions implements IVoicedCommandHandler
{

	private final String[] _commandList = new String[] { "km-all-to-me"	};

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
	    if (!Config.ENABLE_KM_ALL_TO_ME) 
	      return false;
	    
		if(command.equalsIgnoreCase("km-all-to-me"))
		{
			if (!activeChar.isClanLeader())
			{
				activeChar.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
				return false;
			}
			if (activeChar.isInOlympiadMode())
			{
				activeChar.sendPacket(Msg.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD);
				return false;
			}
			SystemMessage2 msg = Call.canSummonHere(activeChar);
			if (msg != null)
			{
				activeChar.sendPacket(msg);
				return false;
			}
			if (activeChar.isInZone(ZoneType.epic))
			{
				activeChar.sendPacket(Msg.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
				return false;
			}
			if (activeChar.isAlikeDead())
			{
				activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Relocate.Dead", activeChar, new Object[0]));
				return false;
			}
			List<Player> players = activeChar.getClan().getOnlineMembers(activeChar.getObjectId());
			for (Player player : players)
			{
				Effect effect = player.getEffectList().getEffectByType(EffectType.Meditation);
				if (effect == null)
				{
					if (Call.canBeSummoned(player) == null)
						player.summonCharacterRequest(activeChar, Location.findAroundPosition(activeChar, 100, 150), 5);
				}
			}
			return true;
		}
		return false;
	}
}