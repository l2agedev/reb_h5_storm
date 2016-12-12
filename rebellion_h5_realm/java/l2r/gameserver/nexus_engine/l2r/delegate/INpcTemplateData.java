/**
 * 
 */
package l2r.gameserver.nexus_engine.l2r.delegate;

import l2r.gameserver.nexus_interface.delegate.NpcData;

/**
 * @author hNoke
 *
 */
public interface INpcTemplateData
{
	public void setSpawnName(String name);
	public void setSpawnTitle(String title);
	
	public boolean exists();
	
	public NpcData doSpawn(int x, int y, int z, int ammount, int reflection);
	public NpcData doSpawn(int x, int y, int z, int ammount, int heading, int reflection);
	public NpcData doSpawn(int x, int y, int z, int ammount, int heading, int respawn, int reflection);
}
