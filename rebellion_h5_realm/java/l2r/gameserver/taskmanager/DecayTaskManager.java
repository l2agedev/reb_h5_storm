package l2r.gameserver.taskmanager;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.threading.SteppingRunnableQueueManager;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Creature;

import java.util.concurrent.Future;

public class DecayTaskManager extends SteppingRunnableQueueManager
{
	private static final DecayTaskManager _instance = new DecayTaskManager();
	public static final DecayTaskManager getInstance()
	{
		return _instance;
	}

	private DecayTaskManager()
	{
		super(500L);

		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 500L, 500L);

		//Очистка каждую минуту
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				DecayTaskManager.this.purge();
			}

		}, 60000L, 60000L);
	}

	public Future<?> addDecayTask(final Creature actor, long delay)
	{
		return schedule(new RunnableImpl(){

			@Override
			public void runImpl() throws Exception
			{
				actor.doDecay();
			}

		}, delay);
	}
}