package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.RestartType;
import l2r.gameserver.model.entity.events.GlobalEvent;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.nexus_interface.NexusEvents;

import java.util.HashMap;
import java.util.Map;


public class Die extends L2GameServerPacket
{
	private int _objectId;
	private boolean _fake;
	@SuppressWarnings("unused")
	private boolean _sweepable, isPvPevents;

	private Map<RestartType, Boolean> _types = new HashMap<RestartType, Boolean>(RestartType.VALUES.length);

	public Die(Creature cha)
	{
		_objectId = cha.getObjectId();
		_fake = !cha.isDead();

		if(cha.isMonster())
			_sweepable = ((MonsterInstance) cha).isSweepActive();
		else if (cha != null && cha.isPlayer() && NexusEvents.isInEvent(cha.getPlayer()))
		{
			if (!NexusEvents.canShowToVillageWindow(cha.getPlayer()))
			{
				put(RestartType.TO_FLAG, false);
				put(RestartType.TO_VILLAGE, false);
				put(RestartType.TO_CLANHALL, false);
				put(RestartType.TO_CASTLE, false);
				put(RestartType.TO_FORTRESS, false);
			}
		}
		else if(cha.isPlayer() && !cha.getPlayer().isInEvent())
		{
			Player player = (Player) cha;
			put(RestartType.FIXED, player.getAccessLevel().allowFixedRes() || ((player.getInventory().getCountOf(10649) > 0 || player.getInventory().getCountOf(13300) > 0) && !player.isOnSiegeField() && !player.isInOlympiadMode()));
			put(RestartType.AGATHION, player.isAgathionResAvailable());
			put(RestartType.TO_VILLAGE, true);

			Clan clan = null;
			if(get(RestartType.TO_VILLAGE))
				clan = player.getClan();
			if(clan != null)
			{
				put(RestartType.TO_CLANHALL, clan.getHasHideout() > 0);
				put(RestartType.TO_CASTLE, clan.getCastle() > 0 );
				put(RestartType.TO_FORTRESS, clan.getHasFortress() > 0 );
			}

			for(GlobalEvent e : cha.getEvents())
				e.checkRestartLocs(player, _types);
			if(player.getVar("isPvPevents") != null)
				isPvPevents = true;
		}
		else if (cha != null && cha.isPlayer())
		{
			put(RestartType.TO_FLAG, true);
			put(RestartType.TO_VILLAGE, false);
			put(RestartType.TO_CLANHALL, false);
			put(RestartType.TO_CASTLE, false);
			put(RestartType.TO_FORTRESS, false);
		}
	}

	@Override
	protected final void writeImpl()
	{
		if(_fake)
			return;

		writeC(0x00);
		writeD(_objectId);
		writeD(get(RestartType.TO_VILLAGE)); // to nearest village
		writeD(get(RestartType.TO_CLANHALL)); // to hide away
		writeD(get(RestartType.TO_CASTLE)); // to castle
		writeD(get(RestartType.TO_FLAG));// to siege HQ
		writeD(_sweepable ? 0x01 : 0x00); // sweepable  (blue glow)
		writeD(get(RestartType.FIXED));// FIXED
		writeD(get(RestartType.TO_FORTRESS));// fortress
		writeC(0); //show die animation
		writeD(get(RestartType.AGATHION));//agathion ress button
		writeD(0x00); //additional free space
	}

	private void put(RestartType t, boolean b)
	{
		_types.put(t, b);
	}

	private boolean get(RestartType t)
	{
		Boolean b = _types.get(t);
		return b != null && b;
	}
}