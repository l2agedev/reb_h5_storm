package ai.PaganTemplete;

import l2r.gameserver.ai.DefaultAI;
import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.model.instances.DoorInstance;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.scripts.Functions;

/**
 *         - AI для нпц Altar Gatekeeper (32051).
 *         - Контроллеры дверей.
 *         - AI проверен и работает.
 */
public class AltarGatekeeper extends DefaultAI
{
	private boolean _firstTime = true;

	public AltarGatekeeper(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		// Двери на балкон
		DoorInstance door1 = ZoneHolder.getDoor(19160014);
		DoorInstance door2 = ZoneHolder.getDoor(19160015);
		// Двери к алтарю
		DoorInstance door3 = ZoneHolder.getDoor(19160016);
		DoorInstance door4 = ZoneHolder.getDoor(19160017);

		// Кричим 4 раза (т.к. актор заспавнен в 4х местах) как на оффе о том что двери открылись
		if( !door1.isOpen() && !door2.isOpen() && door3.isOpen() && door4.isOpen() && _firstTime)
		{
			_firstTime = false;
			Functions.npcSay(actor, NpcString.THE_DOOR_TO_THE_3RD_FLOOR_OF_THE_ALTAR_IS_NOW_OPEN);
		}

		return true;
	}
}