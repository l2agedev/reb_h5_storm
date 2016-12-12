package l2r.gameserver.nexus_interface.delegate;

import l2r.gameserver.model.instances.DoorInstance;
import l2r.gameserver.nexus_engine.l2r.delegate.IDoorData;

/**
 * @author hNoke
 *
 */
public class DoorData extends CharacterData implements IDoorData
{
	protected DoorInstance _owner;
	
	public DoorData(DoorInstance d)
	{
		super(d);
		_owner = d;
	}
	
	@Override
	public DoorInstance getOwner()
	{
		return _owner;
	}
	
	@Override
	public int getDoorId()
	{
		return _owner.getDoorId();
	}
	
	@Override
	public boolean isOpened()
	{
		return _owner.isOpen();
	}
	
	@Override
	public void openMe()
	{
		if (!isOpened())
			_owner.openMe();
	}
	
	@Override
	public void closeMe()
	{
		if (isOpened())
			_owner.closeMe();
	}
}
