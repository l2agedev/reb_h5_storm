package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.events.impl.DominionSiegeEvent;

/**
 * @author VISTALL
 * @date 12:08/05.03.2011
 */
public class ExDominionWarStart extends L2GameServerPacket
{
	private int _objectId;
	private int _territoryId, _disguisedTerritoryId;
	private boolean _isDisguised;

	public ExDominionWarStart(Player player)
	{
		_objectId = player.getObjectId();
		
		DominionSiegeEvent siegeEvent = player.getEvent(DominionSiegeEvent.class);
		if(siegeEvent != null)
		{
			_territoryId = siegeEvent.isInProgress() ? siegeEvent.getId() : 0;
			_isDisguised = siegeEvent.getObjects(DominionSiegeEvent.DISGUISE_PLAYERS).contains(_objectId);
			if(_isDisguised)
				_disguisedTerritoryId = siegeEvent.getId();
		}
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0xA3);
		writeD(_objectId);
		writeD(1);
		writeD(_territoryId);
		writeD(_isDisguised);
		writeD(_disguisedTerritoryId);
	}
}
