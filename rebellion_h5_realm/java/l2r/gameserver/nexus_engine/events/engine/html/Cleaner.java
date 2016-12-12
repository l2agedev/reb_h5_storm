/**
 * 
 */
package l2r.gameserver.nexus_engine.events.engine.html;

import l2r.gameserver.nexus_engine.l2r.CallBack;

/**
 * @author hNoke
 *
 */
public class Cleaner implements Runnable
{
	Cleaner()
	{
		CallBack.getInstance().getOut().scheduleGeneralAtFixedRate(this, 900000, 900000);
	}
	
	@Override
	public void run()
	{
		long time = System.currentTimeMillis();
		for(PartyRecord party : PartyMatcher.parties)
		{
			if(party.canBeRemoved(time))
			{
				party.leader.sendMessage("Your party room has expired and has been deleted from the party matching system.");
				PartyMatcher.parties.remove(party);
				continue;
			}
		}
	}
}