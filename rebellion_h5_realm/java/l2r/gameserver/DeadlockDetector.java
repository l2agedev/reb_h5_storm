package l2r.gameserver;

import l2r.gameserver.utils.GArray;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DeadlockDetector extends Thread
{
	private static final Logger _log = LoggerFactory.getLogger(DeadlockDetector.class);
	private static final ThreadMXBean _mbean = ManagementFactory.getThreadMXBean();

	public DeadlockDetector()
	{
		super();
		setDaemon(true);
		setPriority(MIN_PRIORITY);
	}

	@Override
	public void run()
	{
		for (;;)
		{
			if (Config.DEADLOCKCHECK_INTERVAL <= 0)
				break;
			
			try
			{
				if (checkForDeadlocks())
				{
					Thread.sleep(1000);
					continue;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			try
			{
				Thread.sleep(Config.DEADLOCKCHECK_INTERVAL);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private static final GArray<Long> all_locked_ids = new GArray<Long>();
	private static long[] locked_ids;
	private static ThreadInfo[] locked_infos;
	private static Thread[] locked_threads;

	public static boolean checkForDeadlocks()
	{
		if ((locked_ids = _mbean.findDeadlockedThreads()) == null)
		{
			return false;
		}
		locked_infos = _mbean.getThreadInfo(locked_ids, true, true);
		locked_threads = getThreads(locked_infos);
		int locks_count = logDeadlocks();
		//fixDeadlocks();
		return locks_count > 0;
	}

	@SuppressWarnings("deprecation")
	public static boolean fixDeadlocks()
	{
		if (locked_threads == null || locked_threads.length == 0)
			return false;
		
		_log.warn("Killing thread " + locked_threads[0] + " via NullPointerException");
		locked_threads[0].stop(new NullPointerException("This thread is deadlocked!"));
		_log.warn("Killed thread " + locked_threads[0] + " via NullPointerException");
		
		return true;
	}

	public static int logDeadlocks()
	{
		int ret = 0;
		ThreadInfo ti;
		String logstr = String.format("Deadlock detected [total %d / %d locked threads]...\r\n", locked_infos.length, locked_ids.length);
		for (int i = 0; i < locked_infos.length; i++)
		{
			ti = locked_infos[i];
			if (all_locked_ids.contains(ti.getThreadId()))
			{
				continue;
			}
			all_locked_ids.add(ti.getThreadId());
			ret++;
			logstr += "=========== DEADLOCK # " + i + " ============\r\n";
			do
			{
				logstr += formatLockThreadInfo(ti);
				ti = _mbean.getThreadInfo(new long[] { ti.getLockOwnerId() }, true, true)[0];
			}
			while (ti.getThreadId() != locked_infos[i].getThreadId());
		}
		_log.error(logstr);
		return ret;
	}

	private static String formatLockThreadInfo(ThreadInfo t)
	{
		String ret = String.format("\t\t%s is waiting to lock %s which is held by %s\r\n", t.getThreadName(), t.getLockInfo().toString(), t.getLockOwnerName());
		for (StackTraceElement trace : t.getStackTrace())
		{
			ret += String.format("\t\t\t at %s\r\n", trace.toString());
		}
		return ret;
	}

	private static Thread[] getThreads(ThreadInfo[] infos)
	{
		Thread[] result = new Thread[infos.length];
		Set<Thread> all = Thread.getAllStackTraces().keySet();
		long id;
		for (int i = 0; i < infos.length; i++)
		{
			result[i] = null;
			id = infos[i].getThreadId();
			for (Thread thread : all)
			{
				if (thread.getId() == id)
				{
					result[i] = thread;
					break;
				}
			}
		}
		return result;
	}
}