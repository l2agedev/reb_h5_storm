package l2r.gameserver.nexus_interface.delegate;

import l2r.gameserver.model.World;
import l2r.gameserver.model.WorldRegion;
import l2r.gameserver.model.instances.FenceInstance;
import l2r.gameserver.nexus_engine.l2r.delegate.IFenceData;
import l2r.gameserver.utils.Location;

/**
 * @author hNoke
 *
 */
public class FenceData extends ObjectData implements IFenceData
{
	private FenceInstance _owner;
	private Location _loc;
	public FenceData(FenceInstance cha)
	{
		super(cha);
		_owner = cha;
	}
	
	@Override
	public FenceInstance getOwner()
	{
		return _owner;
	}
	
	@Override
	public void setLoc(int x, int y, int z)
	{
		_loc = new Location(x, y, z);
	}
	
	@Override
	public Location getLoc()
	{
		return _loc;
	}
	
	@Override
	public void deleteMe()
	{
		WorldRegion region = _owner.getCurrentRegion();
		_owner.decayMe();
		
		if (region != null)
			region.removeObject(_owner);
		
		
		World.removeObjectFromPlayers(_owner);
	}
}
