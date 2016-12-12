package l2r.gameserver.taskmanager;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.threading.SteppingRunnableQueueManager;
import l2r.commons.util.Rnd;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.PhantomPlayerAI;
import l2r.gameserver.model.Player;

import java.util.concurrent.Future;

public class AutoSaveManager extends SteppingRunnableQueueManager
{
	private static final AutoSaveManager _instance = new AutoSaveManager();

	public static final AutoSaveManager getInstance()
	{
		return _instance;
	}

	private AutoSaveManager()
	{
		super(10000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 10000L, 10000L);
		//Очистка каждые 60 секунд
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				AutoSaveManager.this.purge();
			}

		}, 60000L, 60000L);
	}

	public Future<?> addAutoSaveTask(final Player player)
	{
		long delay = Rnd.get(180, 360) * 1000L;

		return scheduleAtFixedRate(new RunnableImpl(){

			@Override
			public void runImpl() throws Exception
			{
				if (player == null || !player.isOnline())
					return;

				player.store(true);
				
				// Kick stuck phantoms.
				if (player.isPhantom() && player.getAI().isPhantomPlayerAI())
				{
					long stuckTime = (System.currentTimeMillis() - ((PhantomPlayerAI) player.getAI()).getLastAiResponse());
					if (stuckTime > 10000)
					{
						player.kick();
						_log.info("Kicking stuck phantom player: " + player + " stuck time is " + stuckTime + "ms.");
					}
				}
			}

		}, delay, delay);
	}
}