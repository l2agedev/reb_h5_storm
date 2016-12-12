package l2r.gameserver.taskmanager;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.threading.SteppingRunnableQueueManager;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;

public class EffectTaskManager extends SteppingRunnableQueueManager
{
	private final static long TICK = 250L;

	private final static EffectTaskManager[] _instances = new EffectTaskManager[Config.EFFECT_TASK_MANAGER_COUNT];
	static
	{
		for(int i =0; i < _instances.length; i++)
			_instances[i] = new EffectTaskManager();
	}

	private static int randomizer;

	public final static EffectTaskManager getInstance()
	{
		return _instances[randomizer++ & _instances.length - 1];
	}

	private EffectTaskManager()
	{
		super(TICK);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, Rnd.get(TICK), TICK);
		//Очистка каждые 30 секунд
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				EffectTaskManager.this.purge();
			}

		}, 30000L + 1000L * randomizer++, 30000L);
	}

	public CharSequence getStats(int num)
	{
		return _instances[num].getStats();
	}
}