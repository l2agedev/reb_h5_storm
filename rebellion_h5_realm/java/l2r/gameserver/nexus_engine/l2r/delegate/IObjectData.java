/**
 * 
 */
package l2r.gameserver.nexus_engine.l2r.delegate;

import l2r.gameserver.nexus_interface.delegate.FenceData;
import l2r.gameserver.nexus_interface.delegate.NpcData;

/**
 * @author hNoke
 *
 */
public interface IObjectData
{
	public int getObjectId();
	
	public boolean isPlayer();
	public boolean isSummon();
	
	public boolean isFence();
	public FenceData getFence();
	
	public boolean isNpc();
	public NpcData getNpc();
}
