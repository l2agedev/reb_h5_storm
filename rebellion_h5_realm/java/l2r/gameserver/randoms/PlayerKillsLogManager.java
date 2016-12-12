/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.randoms;

import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Player;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import javolution.util.FastTable;


public class PlayerKillsLogManager extends Object
{
	TIntObjectHashMap<PlayerKillsLog> _logs = new TIntObjectHashMap<PlayerKillsLog>();
	private enum SortBy
	{
		DEFAULT,
		KILLER_KILLED_NOFEED,
		KILLED_NAME,
		KILLED_NAME_ISFEED
	}
	
	public PlayerKillsLogManager()
	{
		// Save data every 15 min
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				storeLog(SortBy.DEFAULT);
				storeLog(SortBy.KILLED_NAME_ISFEED);
				storeLog(SortBy.KILLER_KILLED_NOFEED);
			}
		}, 15*60000, 15*60000);
	}
	
	public void addLog(int objId, KillLog log)
	{
		if (!_logs.contains(objId))
			_logs.put(objId, new PlayerKillsLog());
		
		_logs.get(objId).addLog(log);
	}
	
	public String toString(SortBy sort)
	{
		StringBuilder sb = new StringBuilder();
		
		if (sort == SortBy.KILLER_KILLED_NOFEED)
		{
			TreeMap<String, PlayerKillsLog> sortedMap = new TreeMap<String, PlayerKillsLog>();
			
			for (PlayerKillsLog pkl : _logs.valueCollection())
				sortedMap.put(pkl.ownerName, pkl);
			
			for (PlayerKillsLog pkl : sortedMap.values())
				sb.append(pkl.toString(SortBy.KILLER_KILLED_NOFEED));
		}
		else
		{
			for (PlayerKillsLog pkl : _logs.values(new PlayerKillsLog[_logs.size()]))
				sb.append(pkl.toString(sort));
		}
		
		return sb.toString();
	}
	
	private void storeLog(SortBy sort)
	{
		try
		{
			File file = new File(Config.DATAPACK_ROOT, "log/feedlog/"+sort.toString()+"/");
			file.mkdirs();
			
			file = new File(file+"/"+getDate()+".txt");
			if (file.exists())
				file.delete();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
			bw.write(toString(sort));
			bw.newLine();
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	

	private String getDate()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	private final class PlayerKillsLog extends FastTable<KillLog>
	{
		private static final long serialVersionUID = 1L;
		private String ownerName;

		public boolean addLog(KillLog log)
		{
			if (ownerName == null)
				ownerName = log.killer;
			return super.add(log);
		}
		
		@SuppressWarnings("unused")
		public boolean removeLog(KillLog log)
		{
			return super.remove(log);
		}
		
		public String toString(SortBy sort)
		{
			StringBuilder sb = new StringBuilder();
			List<KillLog> tmp = new FastTable<KillLog>();
			tmp.addAll(this);
			
			// Get only today's logs
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			for (KillLog kl : tmp)
			{
				if (kl.killDate.before(cal))
					tmp.remove(kl);
			}
			
			switch (sort)
			{
				case KILLED_NAME:
					Collections.sort(tmp, new Comparator<KillLog>()
					{
						@Override
						public int compare(KillLog o1, KillLog o2)
						{
							String o1v = o1.killed;
							String o2v = o2.killed;
							
							return o1v.compareTo(o2v);
						}
					});
					break;
				case KILLER_KILLED_NOFEED:
					for (KillLog kl : tmp)
					{
						if (kl.isFeed)
							tmp.remove(kl);
					}
					Collections.sort(tmp, new Comparator<KillLog>()
					{
						@Override
						public int compare(KillLog o1, KillLog o2)
						{
							String o1v = o1.killed;
							String o2v = o2.killed;
							
							return o1v.compareTo(o2v);
						}
					});
					break;
				case KILLED_NAME_ISFEED: //Non-feeds first
					Collections.sort(tmp, new Comparator<KillLog>()
					{
						@Override
						public int compare(KillLog o1, KillLog o2)
						{
							if (o1.isFeed && !o2.isFeed)
								return 1;
							else if (!o1.isFeed && o2.isFeed)
								return 1;
							else
								return o1.killed.compareTo(o2.killed);
						}
					});
					break;
			}
			
			for (KillLog kl : tmp)
			{
				sb.append(kl);
				sb.append("\n");
			}
			
			return sb.toString();
		}
	}
	
	public final class KillLog
	{
		String killer;
		String killed;
		Calendar killDate = Calendar.getInstance();
		Calendar lastKillDate = Calendar.getInstance();
		boolean isFeed;
		boolean atEvent;
		boolean isPvP;
		Calendar killDatesDiff = Calendar.getInstance();
		
		public KillLog(Player $killer, Player $killed, long $killTime, long $lastKillTime, boolean $isFeed, boolean $atEvent, boolean $isPvP)
		{
			killer = $killer.getName();
			killed = $killed.getName();
			killDate.setTimeInMillis($killTime);
			lastKillDate.setTimeInMillis($lastKillTime == 0 ? $killTime : $lastKillTime);
			isFeed = $isFeed;
			atEvent = $atEvent;
			isPvP = $isPvP;
			killDatesDiff.set(Calendar.MILLISECOND, (int) ($killTime - $lastKillTime));
		}
		
		@Override
		public String toString()
		{
			String diff = (killDatesDiff.get(Calendar.MINUTE) > 0 ? (killDatesDiff.get(Calendar.MINUTE) + " minutes")
				: killDatesDiff.get(Calendar.SECOND) > 0 ? (killDatesDiff.get(Calendar.SECOND) + " seconds")
				: killDatesDiff.get(Calendar.MILLISECOND) > 0 ? (killDatesDiff.get(Calendar.MILLISECOND) + " miliseconds")
				: "0");
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			sb.append(killer);
			sb.append(" killed ");
			sb.append(killed);
			sb.append("]");
			sb.append("[ "+ (isPvP ? "PVP" : "PK"));
			sb.append(" ]");
			sb.append("\t\t\t");
			sb.append("[ at: ");
			sb.append(killDate.getTime().toString());
			sb.append(" (last: ");
			sb.append(lastKillDate.getTime().toString());
			sb.append(") ");
			sb.append("- diff: ");
			sb.append(diff);
			sb.append("]");
			sb.append("\t\t\t");
			
			sb.append("counted as: ");
			sb.append(isFeed ? "FEED" : "NOT FEED");
			if (atEvent)
				sb.append("(at event)");
			
			
			return sb.toString();
			
		}
	}
	
	public static final PlayerKillsLogManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final PlayerKillsLogManager _instance = new PlayerKillsLogManager();
	}
}