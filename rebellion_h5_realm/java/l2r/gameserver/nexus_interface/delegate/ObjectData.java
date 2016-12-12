package l2r.gameserver.nexus_interface.delegate;


import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.instances.FenceInstance;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.nexus_engine.l2r.delegate.IObjectData;

/**
 * @author hNoke
 *
 */
public class ObjectData implements IObjectData
{
	protected GameObject _owner;
	
	public ObjectData(GameObject cha)
	{
		_owner = cha;
	}
	
	public GameObject getOwner()
	{
		return _owner;
	}
	
	@Override
	public int getObjectId()
	{
		return _owner.getObjectId();
	}
	
	@Override
	public boolean isPlayer()
	{
		return _owner instanceof Player;
	}
	
	@Override
	public boolean isSummon()
	{
		return _owner instanceof Summon;
	}
	
	@Override
	public boolean isFence()
	{
		return _owner instanceof FenceInstance;
	}
	
	@Override
	public FenceData getFence()
	{
		if(!isFence()) return null;
		return new FenceData((FenceInstance)_owner);
	}
	
	@Override
	public NpcData getNpc()
	{
		return new NpcData((NpcInstance)_owner);
	}
	
	@Override
	public boolean isNpc()
	{
		return _owner instanceof NpcInstance;
	}
}
