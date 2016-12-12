/**
 * 
 */
package l2r.gameserver.nexus_engine.events.engine.main.pvpzone;

import l2r.gameserver.nexus_engine.events.Configurable;
import l2r.gameserver.nexus_engine.events.EventGame;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

import java.util.concurrent.ScheduledFuture;

/**
 * @author hNoke
 *
 */
public abstract class PvpZone implements EventGame, Configurable
{
	public PvpZone()
	{
		
	}
	
	public abstract String getName();
	public abstract void start();
	public abstract void end();
	
	public abstract boolean canRegister(PlayerEventInfo player);
	public abstract boolean canUnregister(PlayerEventInfo player);
	public abstract void addPlayer(PlayerEventInfo player);
	public abstract void removePlayer(PlayerEventInfo player);
	public abstract void setPlayerToUnregister(PlayerEventInfo player);
	
	public abstract int getPlayersCountForHtml();
	public abstract String getStateNameForHtml();
	
	public abstract void scheduledCheck();
	
	protected Checker _checker;
	
	public class Checker implements Runnable
	{
		int duration = 0;
		boolean enabled;
		
		public int tick = 0;
		
		ScheduledFuture<?> _future;
		
		public Checker(int repeatDuration)
		{
			this.duration = repeatDuration;
			enabled = false;
		}
		
		public void setDuration(int duration)
		{
			this.duration = duration; 
		}
		
		public void start()
		{
			enabled = true;
			scheduleNextCheck();
		}
		
		private void scheduleNextCheck()
		{
			if(enabled)
				_future = CallBack.getInstance().getOut().scheduleGeneral(this, duration);
		}
		
		public void disable()
		{
			if(_future != null)
			{
				_future.cancel(false);
				_future = null;
			}
			
			enabled = false;
		}
		
		@Override
		public void run()
		{
			try
			{
				tick ++;
				scheduledCheck();
				scheduleNextCheck();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
