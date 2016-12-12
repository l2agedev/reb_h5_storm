/**
 * 
 */
package l2r.gameserver.nexus_engine.l2r.delegate;


/**
 * @author hNoke
 *
 */
public interface IDoorData
{
	public int getDoorId();
	
	public boolean isOpened();
	public void openMe();
	public void closeMe();
}
