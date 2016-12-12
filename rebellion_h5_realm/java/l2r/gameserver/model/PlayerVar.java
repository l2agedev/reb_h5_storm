package l2r.gameserver.model;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Akumu
 * @date 14:53/29.04.12
 */
public class PlayerVar
{
	private Player owner;
	private String name;
	private String value;
	private long expire_time;

	@SuppressWarnings("rawtypes")
	private ScheduledFuture task;

	public PlayerVar(Player owner, String name, String value, long expire_time)
	{
		this.owner = owner;
		this.name = name;
		this.value = value;
		this.expire_time = expire_time;

		if(expire_time > 0) // if expires schedule expiration
		{
			task = ThreadPoolManager.getInstance().schedule(new PlayerVarExpireTask(this), expire_time - System.currentTimeMillis());
		}
	}

	public String getName()
	{
		return name;
	}

	public Player getOwner()
	{
		return owner;
	}

	public boolean hasExpired()
	{
		return task == null || task.isDone();
	}

	public long getTimeToExpire()
	{
		return expire_time - System.currentTimeMillis();
	}

	/**
	 *
	 * @return возвращает значение переменной
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 *
	 * @return возвращает значение в виде логической переменной
	 */
	public boolean getValueBoolean()
	{
		return value.equals("1") || value.equalsIgnoreCase("true");
	}

	public void setValue(String val)
	{
		value = val;
	}

	public void stopExpireTask()
	{
		if(task != null && !task.isDone())
		{
			task.cancel(true);
		}
	}

	private static class PlayerVarExpireTask extends RunnableImpl
	{
		private PlayerVar _pv;

		public PlayerVarExpireTask(PlayerVar pv)
		{
			_pv = pv;
		}

		@Override
		public void runImpl()
		{
			Player pc = _pv.getOwner();
			if (pc == null)
			{
				return;
			}

			pc.unsetVar(_pv.getName());
		}
	}
}
