package events.Cataclizm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;

import l2r.commons.dbutils.DbUtils;
import l2r.commons.geometry.Polygon;
import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Territory;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author PaInKiLlEr - Настройка евента катаклизма в Aden городе - ВНИМАНИЕ: ни в коем случае не должно произойти так что бы ID гвардов и статуй совпадали с ID других городов - ВНИМАНИЕ: не ставить ID статуй/гвардов которые участвуют стандартно в спавне (гремленов сменить тоже)
 */
public class AdenCataclizm extends Cataclizm
{
	private static boolean _cycle = false;
	private static final Logger _log = LoggerFactory.getLogger(Cataclizm.class);
	private static boolean _active = false;
	
	private static ScheduledFuture<?> _startTask;
	private static ScheduledFuture<?> _cataclizmTask;
	private static ScheduledFuture<?> _cataclizm2Task;
	private static ScheduledFuture<?> _cataclizmTimeTask;
	
	private static Zone _zone1 = ZoneHolder.getZone("[aden_town_peace1]");
	private static Zone _zone2 = ZoneHolder.getZone("[aden_town_peace2]");
	private static Zone _zone3 = ZoneHolder.getZone("[aden_town_peace3]");
	private static Zone _zone4 = ZoneHolder.getZone("[aden_town_peace4]");
	private static Zone _zone5 = ZoneHolder.getZone("[aden_town_peace5]");
	private static ZoneListener _zoneListener = new ZoneListener();
	
	// Если включено то город после захвата будет захвачен пока игроки не отобьют его
	private boolean _cycleEnable = false;
	// Через какое время город автоматически оживится (_cycleEnable должно быть выключено), по умолчанию 300 минут = 5 часов
	private int _cycleTime = 30;
	
	// Сколько времени нужно продержать в живых мирную статую что бы город не захватили монстрами (по умолчанию 5 минут)
	private int _cycleZashita = 5;
	
	// Время в которое будет происходить захват города
	private String timer = "7,21,00";
	
	// Сколько времени будет происходить захват (в минутах), по умолчанию 30 минут
	private int timer_siege = 30;
	
	// Включить захват города? true - включен захват города
	public boolean isSiege = false;
	
	// Статуя которая спавнится при захвате, по умолчанию спавнится гремлин
	public int statuya1 = 36803;
	
	// Статуя которая спавнится если город захватили мобы, по умолчанию спавнится гремлин
	public int statuya2 = 36804;
	
	// Статуя которая спавнится если убита первая статуя (на эту статую агрятся все гварды и её нужно защищать)
	public int statuya3 = 36805;
	
	// Ид,Кол-во;Ид,Кол-во (можно продолжать), по умолчанию спавнятся гремлины
	public String[] guards1 = new String[]
	{
		"18342,1;18342,1"
	}; // Первые ворота
	public String[] guards2 = new String[]
	{
		"18342,1;18342,1"
	}; // Вторые ворота
	public String[] guards3 = new String[]
	{
		"18342,1;18342,1"
	}; // Третьи ворота
	public String[] guards4 = new String[]
	{
		"18342,1;18342,1"
	}; // 4 ворота
	
	// 5 список пропускаем!!!
	
	public String[] guards6 = new String[]
	{
		"18342,1;18342,1"
	}; // Возле телепорта
	
	public String[] guards7 = new String[]
	{
		"18342,1;18342,1"
	}; // Возле первого КХ
	public String[] guards8 = new String[]
	{
		"18342,1;18342,1"
	}; // Возле второго КХ
	public String[] guards9 = new String[]
	{
		"18342,1;18342,1"
	}; // Возле третьего КХ
	public String[] guards10 = new String[]
	{
		"18342,1;18342,1"
	}; // Возле четвертого КХ
	public String[] guards11 = new String[]
	{
		"18342,1;18342,1"
	}; // Возле 5 КХ
	public String[] guards12 = new String[]
	{
		"18342,1;18342,1"
	}; // Возле 6 КХ
	
	// 13 список пропускаем!!!
	
	public static String[] guards14 = new String[]
	{
		"18342,1;18342,1"
	}; // Первая стенка мобов к статуе
	public static String[] guards15 = new String[]
	{
		"18342,1;18342,1"
	}; // Вторая стенка мобов к статуе
	public static String[] guards16 = new String[]
	{
		"18342,1;18342,1"
	}; // Вторая стенка мобов к статуе
	public static String[] guards17 = new String[]
	{
		"18342,1;18342,1"
	}; // Вторая стенка мобов к статуе
	public static String[] guards18 = new String[]
	{
		"18342,1;18342,1"
	}; // Вторая стенка мобов к статуе
	public static String[] guards19 = new String[]
	{
		"18342,1;18342,1"
	}; // Третья стенка мобов к статуе
	public static String[] guards20 = new String[]
	{
		"18342,1;18342,1"
	}; // Третья стенка мобов к статуе
	public static String[] guards21 = new String[]
	{
		"18342,1;18342,1"
	}; // Третья стенка мобов к статуе
	public static String[] guards22 = new String[]
	{
		"18342,1;18342,1"
	}; // Третья стенка мобов к статуе
	
	public class CataclizmTimeTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			// Мы не успели убить мирную статую
			for (NpcInstance n : GameObjectsStorage.getAllNpcs())
			{
				if (n != null && !n.isDead() && (n.getNpcId() == getStatuya1() || n.getNpcId() == getStatuya2() || n.getNpcId() == getStatuya3()))
					n.deleteMe();
			}
			
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeFinishAden", null);
			deleteTown();
			life = false;
			despawning(getGuardsMobs1());
			despawning(getGuardsMobs2());
			despawning(getGuardsMobs3());
			despawning(getGuardsMobs4());
			despawning(getGuardsMobs5());
			despawning(getGuardsMobs6());
			despawning(getGuardsMobs7());
			despawning(getGuardsMobs8());
			despawning(getGuardsMobs9());
			despawning(getGuardsMobs10());
			despawning(getGuardsMobs11());
			despawning(getGuardsMobs12());
			despawning(getGuardsMobs13());
			despawning(getGuardsMobs14());
			despawning(getGuardsMobs15());
			despawning(getGuardsMobs16());
			despawning(getGuardsMobs17());
			despawning(getGuardsMobs18());
			despawning(getGuardsMobs19());
			despawning(getGuardsMobs20());
			despawning(getGuardsMobs21());
			despawning(getGuardsMobs22());
			
			// Запускаем таймер евента
			activate();
		}
	}
	
	@Override
	public void onLoad()
	{
		_zone1.addListener(_zoneListener);
		_zone2.addListener(_zoneListener);
		_zone3.addListener(_zoneListener);
		_zone4.addListener(_zoneListener);
		_zone5.addListener(_zoneListener);
		
		_active = ServerVariables.getString("CataclizmAden", "off").equalsIgnoreCase("on");
		_cycle = ServerVariables.getString("CataclizmAdenCycle", "off").equalsIgnoreCase("on");
		
		if (_active)
		{
			if (_cycle)
			{
				NpcTemplate template = NpcHolder.getInstance().getTemplate(getStatuya2());
				MonsterInstance statuya = new MonsterInstance(IdFactory.getInstance().getNextId(), template);
				statuya.setCurrentHpMp(statuya.getMaxHp(), statuya.getMaxMp(), true);
				statuya.setLoc(getStatuyaLoc(), false);
				statuya.spawnMe();
				
				if (!_cycleEnable)
					_cataclizmTimeTask = ThreadPoolManager.getInstance().schedule(new CataclizmTimeTask(), _cycleTime * 60000);
			}
			else
			{
				NpcTemplate template = NpcHolder.getInstance().getTemplate(getStatuya1());
				MonsterInstance statuya = new MonsterInstance(IdFactory.getInstance().getNextId(), template);
				statuya.setCurrentHpMp(statuya.getMaxHp(), statuya.getMaxMp(), true);
				statuya.setLoc(getStatuyaLoc(), false);
				statuya.spawnMe();
				
				_cataclizmTask = ThreadPoolManager.getInstance().schedule(new CataclizmTask(), getTimeSiege() * 60000);
			}
			spawning(getGuardsMobs1(), getGuardsMobsTerritory1());
			spawning(getGuardsMobs2(), getGuardsMobsTerritory2());
			spawning(getGuardsMobs3(), getGuardsMobsTerritory3());
			spawning(getGuardsMobs4(), getGuardsMobsTerritory4());
			spawning(getGuardsMobs5(), getGuardsMobsTerritory5());
			spawning(getGuardsMobs6(), getGuardsMobsTerritory6());
			spawning(getGuardsMobs7(), getGuardsMobsTerritory7());
			spawning(getGuardsMobs8(), getGuardsMobsTerritory8());
			spawning(getGuardsMobs9(), getGuardsMobsTerritory9());
			spawning(getGuardsMobs10(), getGuardsMobsTerritory10());
			spawning(getGuardsMobs11(), getGuardsMobsTerritory11());
			spawning(getGuardsMobs12(), getGuardsMobsTerritory12());
			spawning(getGuardsMobs13(), getGuardsMobsTerritory13());
			spawning(getGuardsMobs14(), getGuardsMobsTerritory14());
			spawning(getGuardsMobs15(), getGuardsMobsTerritory15());
			spawning(getGuardsMobs16(), getGuardsMobsTerritory16());
			spawning(getGuardsMobs17(), getGuardsMobsTerritory17());
			spawning(getGuardsMobs18(), getGuardsMobsTerritory18());
			spawning(getGuardsMobs19(), getGuardsMobsTerritory19());
			spawning(getGuardsMobs20(), getGuardsMobsTerritory20());
			spawning(getGuardsMobs21(), getGuardsMobsTerritory21());
			spawning(getGuardsMobs22(), getGuardsMobsTerritory22());
		}
		else
			activate();
		
		_log.info("Loaded Event: Cataclizm Aden");
	}
	
	@Override
	public void onReload()
	{
		_zone1.removeListener(_zoneListener);
		_zone2.removeListener(_zoneListener);
		_zone3.removeListener(_zoneListener);
		_zone4.removeListener(_zoneListener);
		_zone5.removeListener(_zoneListener);
		if (_startTask != null)
		{
			_startTask.cancel(false);
			_startTask = null;
		}
		if (_cataclizmTask != null)
		{
			_cataclizmTask.cancel(false);
			_cataclizmTask = null;
		}
		if (_cataclizm2Task != null)
		{
			_cataclizm2Task.cancel(false);
			_cataclizm2Task = null;
		}
		if (_cataclizmTimeTask != null)
		{
			_cataclizmTimeTask.cancel(false);
			_cataclizmTimeTask = null;
		}
	}
	
	public class StartTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			sayToAll("scripts.events.Cataclizm.AnnounceEventStartedAden", null);
			ServerVariables.set("CataclizmAden", "on");
			
			// Спавним статую
			NpcTemplate template = NpcHolder.getInstance().getTemplate(getStatuya1());
			MonsterInstance statuya = new MonsterInstance(IdFactory.getInstance().getNextId(), template);
			statuya.setCurrentHpMp(statuya.getMaxHp(), statuya.getMaxMp(), true);
			statuya.setLoc(getStatuyaLoc(), false);
			statuya.spawnMe();
			
			// Спавним охрану вокруг города
			spawning(getGuardsMobs1(), getGuardsMobsTerritory1());
			spawning(getGuardsMobs2(), getGuardsMobsTerritory2());
			spawning(getGuardsMobs3(), getGuardsMobsTerritory3());
			spawning(getGuardsMobs4(), getGuardsMobsTerritory4());
			spawning(getGuardsMobs5(), getGuardsMobsTerritory5());
			spawning(getGuardsMobs6(), getGuardsMobsTerritory6());
			spawning(getGuardsMobs7(), getGuardsMobsTerritory7());
			spawning(getGuardsMobs8(), getGuardsMobsTerritory8());
			spawning(getGuardsMobs9(), getGuardsMobsTerritory9());
			spawning(getGuardsMobs10(), getGuardsMobsTerritory10());
			spawning(getGuardsMobs11(), getGuardsMobsTerritory11());
			spawning(getGuardsMobs12(), getGuardsMobsTerritory12());
			spawning(getGuardsMobs13(), getGuardsMobsTerritory13());
			spawning(getGuardsMobs14(), getGuardsMobsTerritory14());
			spawning(getGuardsMobs15(), getGuardsMobsTerritory15());
			spawning(getGuardsMobs16(), getGuardsMobsTerritory16());
			spawning(getGuardsMobs17(), getGuardsMobsTerritory17());
			spawning(getGuardsMobs18(), getGuardsMobsTerritory18());
			spawning(getGuardsMobs19(), getGuardsMobsTerritory19());
			spawning(getGuardsMobs20(), getGuardsMobsTerritory20());
			spawning(getGuardsMobs21(), getGuardsMobsTerritory21());
			spawning(getGuardsMobs22(), getGuardsMobsTerritory22());
			
			// Захват начался, запускаем таймер осады городов
			_cataclizmTask = ThreadPoolManager.getInstance().schedule(new CataclizmTask(), getTimeSiege() * 60000);
		}
	}
	
	private boolean life = false;
	
	public class CataclizmTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			// Мы не успели убить мирную статую
			for (NpcInstance n : GameObjectsStorage.getAllNpcs())
			{
				if (n != null && !n.isDead() && n.getNpcId() == getStatuya1())
				{
					life = true;
					break;
				}
			}
			
			if (life)
			{
				ServerVariables.set("CataclizmAdenCycle", "on");
				sayToAll("scripts.events.Cataclizm.AnnounceCataclysmFinishAden", null);
				addTown();
				// Спавним вторую статую
				NpcTemplate template2 = NpcHolder.getInstance().getTemplate(getStatuya2());
				MonsterInstance statuya2 = new MonsterInstance(IdFactory.getInstance().getNextId(), template2);
				statuya2.setCurrentHpMp(statuya2.getMaxHp(), statuya2.getMaxMp(), true);
				statuya2.setLoc(getStatuyaLoc(), false);
				statuya2.spawnMe();
				
				// А теперь удаляем старую охрану города
				despawning(getGuardsMobs1());
				despawning(getGuardsMobs2());
				despawning(getGuardsMobs3());
				despawning(getGuardsMobs4());
				despawning(getGuardsMobs5());
				despawning(getGuardsMobs6());
				despawning(getGuardsMobs7());
				despawning(getGuardsMobs8());
				despawning(getGuardsMobs9());
				despawning(getGuardsMobs10());
				despawning(getGuardsMobs11());
				despawning(getGuardsMobs12());
				despawning(getGuardsMobs13());
				despawning(getGuardsMobs14());
				despawning(getGuardsMobs15());
				despawning(getGuardsMobs16());
				despawning(getGuardsMobs17());
				despawning(getGuardsMobs18());
				despawning(getGuardsMobs19());
				despawning(getGuardsMobs20());
				despawning(getGuardsMobs21());
				despawning(getGuardsMobs22());
				
				// И спавним заного
				spawning(getGuardsMobs1(), getGuardsMobsTerritory1());
				spawning(getGuardsMobs2(), getGuardsMobsTerritory2());
				spawning(getGuardsMobs3(), getGuardsMobsTerritory3());
				spawning(getGuardsMobs4(), getGuardsMobsTerritory4());
				spawning(getGuardsMobs5(), getGuardsMobsTerritory5());
				spawning(getGuardsMobs6(), getGuardsMobsTerritory6());
				spawning(getGuardsMobs7(), getGuardsMobsTerritory7());
				spawning(getGuardsMobs8(), getGuardsMobsTerritory8());
				spawning(getGuardsMobs9(), getGuardsMobsTerritory9());
				spawning(getGuardsMobs10(), getGuardsMobsTerritory10());
				spawning(getGuardsMobs11(), getGuardsMobsTerritory11());
				spawning(getGuardsMobs12(), getGuardsMobsTerritory12());
				spawning(getGuardsMobs13(), getGuardsMobsTerritory13());
				spawning(getGuardsMobs14(), getGuardsMobsTerritory14());
				spawning(getGuardsMobs15(), getGuardsMobsTerritory15());
				spawning(getGuardsMobs16(), getGuardsMobsTerritory16());
				spawning(getGuardsMobs17(), getGuardsMobsTerritory17());
				spawning(getGuardsMobs18(), getGuardsMobsTerritory18());
				spawning(getGuardsMobs19(), getGuardsMobsTerritory19());
				spawning(getGuardsMobs20(), getGuardsMobsTerritory20());
				spawning(getGuardsMobs21(), getGuardsMobsTerritory21());
				spawning(getGuardsMobs22(), getGuardsMobsTerritory22());
				
				if (!_cycleEnable)
					_cataclizmTimeTask = ThreadPoolManager.getInstance().schedule(new CataclizmTimeTask(), _cycleTime * 60000);
			}
			else
			{
				sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeFinishAden", null);
				deleteTown();
				life = false;
				ServerVariables.unset("CataclizmAden");
				ServerVariables.unset("CataclizmAdenCycle");
				
				// Мирная статуя мертва или исчезла, удаляем всех гвардов
				despawning(getGuardsMobs1());
				despawning(getGuardsMobs2());
				despawning(getGuardsMobs3());
				despawning(getGuardsMobs4());
				despawning(getGuardsMobs5());
				despawning(getGuardsMobs6());
				despawning(getGuardsMobs7());
				despawning(getGuardsMobs8());
				despawning(getGuardsMobs9());
				despawning(getGuardsMobs10());
				despawning(getGuardsMobs11());
				despawning(getGuardsMobs12());
				despawning(getGuardsMobs13());
				despawning(getGuardsMobs14());
				despawning(getGuardsMobs15());
				despawning(getGuardsMobs16());
				despawning(getGuardsMobs17());
				despawning(getGuardsMobs18());
				despawning(getGuardsMobs19());
				despawning(getGuardsMobs20());
				despawning(getGuardsMobs21());
				despawning(getGuardsMobs22());
				
				// Запускаем таймер евента
				activate();
			}
		}
	}
	
	private void loadSql(Player player)
	{
		Connection con = null;
		PreparedStatement stmt = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			stmt = con.prepareStatement("UPDATE cataclysm SET `player_name`=? WHERE town = Aden");
			stmt.setString(1, player.getName());
			stmt.setString(2, "Aden");
			stmt.execute();
		}
		catch (Exception e)
		{
			_log.info("Exception: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, stmt);
		}
	}
	
	public void activate()
	{
		if (isSiege())
		{
			String[] time = getTimer().split(",");
			Calendar c = Calendar.getInstance();
			int weekDay = Integer.valueOf(time[0]) + 1;
			if (weekDay == 8)
				weekDay = 1;
			
			c.set(Calendar.DAY_OF_WEEK, weekDay);
			c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time[1]));
			c.set(Calendar.MINUTE, Integer.valueOf(time[2]));
			long init = c.getTime().getTime() - System.currentTimeMillis();
			long delay = 604800000;
			if (init < 0)
				init = delay + init;
			
			_startTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new StartTask(), init, delay);
		}
	}
	
	private static class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			if (cha == null)
				return;
			
			Player player = cha.getPlayer();
			
			if (_cycle)
				player.sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, "Город Аден захвачен монстрами, пожалуйста покиньте город для вашей же безопасности!"));
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
		}
	}
	
	public String getTimer()
	{
		return timer;
	}
	
	public int getTimeSiege()
	{
		return timer_siege;
	}
	
	public boolean isSiege()
	{
		return isSiege;
	}
	
	public Territory getGuardsMobsTerritory1()
	{
		return new Territory().add(new Polygon().add(152064, 25355).setZmin(-2128).setZmax(-2028).add(152387, 25402).setZmin(-2128).setZmax(-2028).add(152455, 24858).setZmin(-2136).setZmax(-2036).add(152096, 24796).setZmin(-2128).setZmax(-2028));
	}
	
	public Territory getGuardsMobsTerritory2()
	{
		return new Territory().add(new Polygon().add(147260, 19815).setZmin(-1984).setZmax(-1884).add(147668, 19801).setZmin(-1984).setZmax(-1884).add(147669, 19453).setZmin(-1944).setZmax(-1844).add(147223, 19480).setZmin(-1944).setZmax(-1844));
	}
	
	public Territory getGuardsMobsTerritory3()
	{
		return new Territory().add(new Polygon().add(142534, 26059).setZmin(-2392).setZmax(-2292).add(142114, 26009).setZmin(-2376).setZmax(-2276).add(142135, 26584).setZmin(-2408).setZmax(-2308).add(142524, 26647).setZmin(-2392).setZmax(-2292));
	}
	
	public Territory getGuardsMobsTerritory4()
	{
		return new Territory().add(new Polygon().add(147148, 32180).setZmin(-2472).setZmax(-2372).add(147112, 32629).setZmin(-2464).setZmax(-2364).add(147761, 32644).setZmin(-2456).setZmax(-2356).add(147774, 32192).setZmin(-2472).setZmax(-2372));
	}
	
	public Territory getGuardsMobsTerritory5()
	{
		return null;
	}
	
	public Territory getGuardsMobsTerritory6()
	{
		return new Territory().add(new Polygon().add(146417, 25943).setZmin(-2008).setZmax(-1908).add(146435, 25696).setZmin(-2008).setZmax(-1908).add(147293, 25667).setZmin(-2008).setZmax(-1908).add(147266, 25898).setZmin(-2008).setZmax(-1908));
	}
	
	public Territory getGuardsMobsTerritory7()
	{
		return new Territory().add(new Polygon().add(149518, 23268).setZmin(-2136).setZmax(-2036).add(149734, 23264).setZmin(-2136).setZmax(-2036).add(149720, 23043).setZmin(-2136).setZmax(-2036).add(149508, 23028).setZmin(-2136).setZmax(-2036));
	}
	
	public Territory getGuardsMobsTerritory8()
	{
		return new Territory().add(new Polygon().add(150257, 23533).setZmin(-2136).setZmax(-2036).add(150017, 23526).setZmin(-2136).setZmax(-2036).add(150011, 23787).setZmin(-2136).setZmax(-2036).add(150279, 23779).setZmin(-2136).setZmax(-2036));
	}
	
	public Territory getGuardsMobsTerritory9()
	{
		return new Territory().add(new Polygon().add(150379, 26360).setZmin(-2264).setZmax(-2164).add(150208, 26377).setZmin(-2264).setZmax(-2164).add(150212, 26712).setZmin(-2264).setZmax(-2164).add(150377, 26706).setZmin(-2264).setZmax(-2164));
	}
	
	public Territory getGuardsMobsTerritory10()
	{
		return new Territory().add(new Polygon().add(145329, 25157).setZmin(-2136).setZmax(-2036).add(145165, 25183).setZmin(-2136).setZmax(-2036).add(145169, 25375).setZmin(-2136).setZmax(-2036).add(145339, 25465).setZmin(-2136).setZmax(-2036));
	}
	
	public Territory getGuardsMobsTerritory11()
	{
		return new Territory().add(new Polygon().add(144349, 26934).setZmin(-2264).setZmax(-2164).add(144600, 26909).setZmin(-2264).setZmax(-2164).add(144598, 27264).setZmin(-2264).setZmax(-2164).add(144343, 27282).setZmin(-2264).setZmax(-2164));
	}
	
	public Territory getGuardsMobsTerritory12()
	{
		return new Territory().add(new Polygon().add(144383, 28019).setZmin(-2264).setZmax(-2164).add(144581, 28040).setZmin(-2264).setZmax(-2164).add(144604, 28366).setZmin(-2264).setZmax(-2164).add(144366, 28409).setZmin(-2264).setZmax(-2164));
	}
	
	public Territory getGuardsMobsTerritory13()
	{
		return null;
	}
	
	public Territory getGuardsMobsTerritory14()
	{
		return new Territory().add(new Polygon().add(147628, 27978).setZmin(-2264).setZmax(-2164).add(147294, 27977).setZmin(-2264).setZmax(-2164).add(147289, 28302).setZmin(-2264).setZmax(-2164).add(147609, 28307).setZmin(-2264).setZmax(-2164));
	}
	
	public Territory getGuardsMobsTerritory15()
	{
		return new Territory().add(new Polygon().add(147693, 28313).setZmin(-2264).setZmax(-2164).add(147835, 28307).setZmin(-2264).setZmax(-2164).add(147804, 27809).setZmin(-2264).setZmax(-2164).add(147646, 27796).setZmin(-2264).setZmax(-2164));
	}
	
	public Territory getGuardsMobsTerritory16()
	{
		return new Territory().add(new Polygon().add(147771, 27782).setZmin(-2264).setZmax(-2164).add(147757, 27876).setZmin(-2264).setZmax(-2164).add(147201, 27894).setZmin(-2264).setZmax(-2164).add(147185, 27761).setZmin(-2264).setZmax(-2164));
	}
	
	public Territory getGuardsMobsTerritory17()
	{
		return new Territory().add(new Polygon().add(147211, 27844).setZmin(-2264).setZmax(-2164).add(147046, 27836).setZmin(-2264).setZmax(-2164).add(147110, 28346).setZmin(-2264).setZmax(-2164).add(147250, 28368).setZmin(-2264).setZmax(-2164));
	}
	
	public Territory getGuardsMobsTerritory18()
	{
		return new Territory().add(new Polygon().add(147111, 28550).setZmin(-2264).setZmax(-2164).add(147728, 28541).setZmin(-2264).setZmax(-2164).add(147732, 28332).setZmin(-2264).setZmax(-2164).add(147158, 28349).setZmin(-2264).setZmax(-2164));
	}
	
	public Territory getGuardsMobsTerritory19()
	{
		return new Territory().add(new Polygon().add(147634, 29059).setZmin(-2264).setZmax(-2164).add(147274, 29077).setZmin(-2264).setZmax(-2164).add(147267, 28703).setZmin(-2264).setZmax(-2164).add(147659, 28707).setZmin(-2264).setZmax(-2164));
	}
	
	public Territory getGuardsMobsTerritory20()
	{
		return new Territory().add(new Polygon().add(147873, 28417).setZmin(-2264).setZmax(-2164).add(147886, 27811).setZmin(-2264).setZmax(-2164).add(148170, 27790).setZmin(-2264).setZmax(-2164).add(148197, 28424).setZmin(-2264).setZmax(-2164));
	}
	
	public Territory getGuardsMobsTerritory21()
	{
		return new Territory().add(new Polygon().add(147739, 27271).setZmin(-2200).setZmax(-2100).add(147745, 26956).setZmin(-2200).setZmax(-2100).add(147174, 26961).setZmin(-2200).setZmax(-2100).add(147182, 27251).setZmin(-2200).setZmax(-2100));
	}
	
	public Territory getGuardsMobsTerritory22()
	{
		return new Territory().add(new Polygon().add(147026, 28447).setZmin(-2264).setZmax(-2164).add(146709, 28474).setZmin(-2264).setZmax(-2164).add(146668, 27816).setZmin(-2264).setZmax(-2164).add(147084, 27755).setZmin(-2264).setZmax(-2164));
	}
	
	public String[] getGuardsMobs1()
	{
		return guards1;
	}
	
	public String[] getGuardsMobs2()
	{
		return guards2;
	}
	
	public String[] getGuardsMobs3()
	{
		return guards3;
	}
	
	public String[] getGuardsMobs4()
	{
		return guards4;
	}
	
	public String[] getGuardsMobs5()
	{
		return null;
	}
	
	public String[] getGuardsMobs6()
	{
		return guards6;
	}
	
	public String[] getGuardsMobs7()
	{
		return guards7;
	}
	
	public String[] getGuardsMobs8()
	{
		return guards8;
	}
	
	public String[] getGuardsMobs9()
	{
		return guards9;
	}
	
	public String[] getGuardsMobs10()
	{
		return guards10;
	}
	
	public String[] getGuardsMobs11()
	{
		return guards11;
	}
	
	public String[] getGuardsMobs12()
	{
		return guards12;
	}
	
	public String[] getGuardsMobs13()
	{
		return null;
	}
	
	public String[] getGuardsMobs14()
	{
		return guards14;
	}
	
	public String[] getGuardsMobs15()
	{
		return guards15;
	}
	
	public String[] getGuardsMobs16()
	{
		return guards16;
	}
	
	public String[] getGuardsMobs17()
	{
		return guards17;
	}
	
	public String[] getGuardsMobs18()
	{
		return guards18;
	}
	
	public String[] getGuardsMobs19()
	{
		return guards19;
	}
	
	public String[] getGuardsMobs20()
	{
		return guards20;
	}
	
	public String[] getGuardsMobs21()
	{
		return guards21;
	}
	
	public String[] getGuardsMobs22()
	{
		return guards22;
	}
	
	public int getStatuya1()
	{
		return statuya1;
	}
	
	public int getStatuya2()
	{
		return statuya2;
	}
	
	public int getStatuya3()
	{
		return statuya3;
	}
	
	public Location getStatuyaLoc()
	{
		return new Location(147471, 28129, -2264);
	}
	
	@Override
	public void onShowChat(NpcInstance actor)
	{
		if (life)
		{
			if (actor.isInZone(_zone1) || actor.isInZone(_zone2) || actor.isInZone(_zone3) || actor.isInZone(_zone4) || actor.isInZone(_zone5))
				return;
		}
	}
	
	public class Cataclysm2Task extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			boolean zashita = false;
			
			// Мы не успели убить мирную статую
			for (NpcInstance n : GameObjectsStorage.getAllNpcs())
			{
				if (n != null && !n.isDead() && n.getNpcId() == getStatuya3())
				{
					n.deleteMe();
					zashita = true;
					break;
				}
			}
			
			if (zashita)
			{
				sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeFinishAden", null);
				deleteTown();
				life = false;
				ServerVariables.unset("CataclizmAden");
				ServerVariables.unset("CataclizmAdenCycle");
				despawning(getGuardsMobs1());
				despawning(getGuardsMobs2());
				despawning(getGuardsMobs3());
				despawning(getGuardsMobs4());
				despawning(getGuardsMobs5());
				despawning(getGuardsMobs6());
				despawning(getGuardsMobs7());
				despawning(getGuardsMobs8());
				despawning(getGuardsMobs9());
				despawning(getGuardsMobs10());
				despawning(getGuardsMobs11());
				despawning(getGuardsMobs12());
				despawning(getGuardsMobs13());
				despawning(getGuardsMobs14());
				despawning(getGuardsMobs15());
				despawning(getGuardsMobs16());
				despawning(getGuardsMobs17());
				despawning(getGuardsMobs18());
				despawning(getGuardsMobs19());
				despawning(getGuardsMobs20());
				despawning(getGuardsMobs21());
				despawning(getGuardsMobs22());
				
				// Запускаем таймер евента
				activate();
			}
			else
			{
				life = true;
				if (_cataclizmTask != null)
				{
					_cataclizmTask.cancel(false);
					_cataclizmTask = null;
				}
				
				_cataclizmTask = ThreadPoolManager.getInstance().schedule(new CataclizmTask(), 1000);
			}
		}
	}
	
	@Override
	public void onDecay(NpcInstance actor)
	{
		if (actor.getNpcId() == getStatuya1())
		{
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeStatuya1Aden", null);
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeStatuya3Aden", null);
			
			if (_cataclizmTask != null)
			{
				_cataclizmTask.cancel(false);
				_cataclizmTask = null;
			}
			
			NpcTemplate template = NpcHolder.getInstance().getTemplate(getStatuya3());
			MonsterInstance statuya = new MonsterInstance(IdFactory.getInstance().getNextId(), template);
			statuya.setCurrentHpMp(statuya.getMaxHp(), statuya.getMaxMp(), true);
			statuya.setLoc(getStatuyaLoc(), false);
			statuya.spawnMe();
			
			_cataclizm2Task = ThreadPoolManager.getInstance().schedule(new Cataclysm2Task(), _cycleZashita * 60000);
		}
		else if (actor.getNpcId() == getStatuya2())
		{
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeStatuya2Aden", null);
			
			if (_cataclizmTimeTask != null)
			{
				_cataclizmTimeTask.cancel(false);
				_cataclizmTimeTask = null;
			}
			
			deleteTown();
			life = false;
			despawning(getGuardsMobs1());
			despawning(getGuardsMobs2());
			despawning(getGuardsMobs3());
			despawning(getGuardsMobs4());
			despawning(getGuardsMobs5());
			despawning(getGuardsMobs6());
			despawning(getGuardsMobs7());
			despawning(getGuardsMobs8());
			despawning(getGuardsMobs9());
			despawning(getGuardsMobs10());
			despawning(getGuardsMobs11());
			despawning(getGuardsMobs12());
			despawning(getGuardsMobs13());
			despawning(getGuardsMobs14());
			despawning(getGuardsMobs15());
			despawning(getGuardsMobs16());
			despawning(getGuardsMobs17());
			despawning(getGuardsMobs18());
			despawning(getGuardsMobs19());
			despawning(getGuardsMobs20());
			despawning(getGuardsMobs21());
			despawning(getGuardsMobs22());
			
			// Запускаем таймер евента
			activate();
		}
	}
	
	@Override
	public void onDeath(Creature actor, Creature killer)
	{
		if (actor.isNpc())
		{
			Player player = killer.getPlayer();
			if (player != null)
			{
				if (actor.getNpcId() == rewards(getGuardsMobs14(), (NpcInstance) actor))
				{
					for (String str : rewards1)
					{
						String[] str2 = str.split(";");
						for (String str3 : str2)
						{
							String[] str4 = str3.split(",");
							int id = Integer.parseInt(str4[0]);
							int count = Integer.parseInt(str4[1]);
							player.getInventory().addItem(id, count);
						}
					}
				}
				else if (actor.getNpcId() == rewards(getGuardsMobs15(), (NpcInstance) actor) || actor.getNpcId() == rewards(getGuardsMobs16(), (NpcInstance) actor) || actor.getNpcId() == rewards(getGuardsMobs17(), (NpcInstance) actor) || actor.getNpcId() == rewards(getGuardsMobs18(), (NpcInstance) actor))
				{
					for (String str : rewards2)
					{
						String[] str2 = str.split(";");
						for (String str3 : str2)
						{
							String[] str4 = str3.split(",");
							int id = Integer.parseInt(str4[0]);
							int count = Integer.parseInt(str4[1]);
							player.getInventory().addItem(id, count);
						}
					}
				}
				else if (actor.getNpcId() == rewards(getGuardsMobs19(), (NpcInstance) actor) || actor.getNpcId() == rewards(getGuardsMobs20(), (NpcInstance) actor) || actor.getNpcId() == rewards(getGuardsMobs21(), (NpcInstance) actor) || actor.getNpcId() == rewards(getGuardsMobs22(), (NpcInstance) actor))
				{
					for (String str : rewards3)
					{
						String[] str2 = str.split(";");
						for (String str3 : str2)
						{
							String[] str4 = str3.split(",");
							int id = Integer.parseInt(str4[0]);
							int count = Integer.parseInt(str4[1]);
							player.getInventory().addItem(id, count);
						}
					}
				}
				else if (actor.getNpcId() == getStatuya1() || actor.getNpcId() == getStatuya2())
				{
					for (String str : rewards4)
					{
						String[] str2 = str.split(";");
						for (String str3 : str2)
						{
							String[] str4 = str3.split(",");
							int id = Integer.parseInt(str4[0]);
							int count = Integer.parseInt(str4[1]);
							player.getInventory().addItem(id, count);
						}
					}
					
					loadSql(player);
				}
			}
		}
	}
}