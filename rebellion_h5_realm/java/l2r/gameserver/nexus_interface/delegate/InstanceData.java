package l2r.gameserver.nexus_interface.delegate;

import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.nexus_engine.l2r.delegate.IInstanceData;

/**
 * @author hNoke
 *
 */
public class InstanceData implements IInstanceData
{
	protected Reflection _instance;
	
	public InstanceData(Reflection i)
	{
		_instance = i;
	}
	
	public Reflection getOwner()
	{
		return _instance;
	}
	
	@Override
	public int getId()
	{
		return _instance.getId();
	}
	
	@Override
	public Reflection getReflection()
	{
		return _instance;
	}
	
	@Override
	public String getName()
	{
		return _instance.getName();
	}
}
