package l2r.gameserver.templates.mapregion;

import l2r.gameserver.model.Territory;

public class DomainArea implements RegionData
{
	private final int _id;
	private String _name;
	private final Territory _territory;

	public DomainArea(int id, Territory territory)
	{
		_id = id;
		_territory = territory;
	}

	public DomainArea(int id, Territory territory, String name)
	{
		_id = id;
		_territory = territory;
		_name = name;
	}
	
	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}
	
	@Override
	public Territory getTerritory()
	{
		return _territory;
	}
}
