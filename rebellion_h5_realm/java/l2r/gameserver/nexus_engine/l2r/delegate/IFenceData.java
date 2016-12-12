/**
 * 
 */
package l2r.gameserver.nexus_engine.l2r.delegate;

import l2r.gameserver.utils.Location;


/**
 * @author hNoke
 *
 */
public interface IFenceData
{
	public void deleteMe();
	public void setLoc(int x, int y, int z);
	public Location getLoc();
}
