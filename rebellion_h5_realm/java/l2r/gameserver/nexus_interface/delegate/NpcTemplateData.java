package l2r.gameserver.nexus_interface.delegate;


import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.nexus_engine.l2r.delegate.INpcTemplateData;
import l2r.gameserver.templates.npc.NpcTemplate;

/**
 * @author hNoke
 *
 */
public class NpcTemplateData implements INpcTemplateData
{
	private NpcTemplate _template;
	
	private String _spawnName = null;
	private String _spawnTitle = null;
	
	public NpcTemplateData(int id)
	{
		_template = NpcHolder.getInstance().getTemplate(id);
	}
	
	@Override
	public void setSpawnName(String name)
	{
		_spawnName = name;
	}
	
	@Override
	public void setSpawnTitle(String title)
	{
		_spawnTitle = title;
	}
	
	@Override
	public boolean exists()
	{
		return _template != null;
	}
	
	@Override
	public NpcData doSpawn(int x, int y, int z, int ammount, int instance)
	{
		return doSpawn(x, y, z, ammount, 0, instance);
	}
	
	@Override
	public NpcData doSpawn(int x, int y, int z, int ammount, int heading, int instance)
	{
		return doSpawn(x, y, z, ammount, heading, 0, instance);
	}
	
	@Override
	public NpcData doSpawn(int x, int y, int z, int ammount, int heading, int respawn, int instance)
	{
		if(_template == null)
			return null;
		
		SimpleSpawner spawn;
		try
		{
			spawn = new SimpleSpawner(_template);
			
			spawn.setLocx(x);
			spawn.setLocy(y);
			spawn.setLocz(z);
			spawn.setAmount(1);
			spawn.setHeading(heading);
			spawn.setRespawnDelay(respawn);
			
			Reflection ref = ReflectionManager.getInstance().get(instance);
			spawn.setReflection(ref);
			
			NpcInstance npc = spawn.doSpawn(true);
			NpcData npcData = new NpcData(npc);
			
			boolean update = false;
			if(_spawnName != null)
			{
				npc.setName(_spawnName);
				update = true;
			}
			if(_spawnTitle != null)
			{
				npc.setTitle(_spawnTitle);
				update = true;
			}
			
			if(update)
				npcData.broadcastNpcInfo();
			
			return npcData;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
}
