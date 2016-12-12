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
 * @author PaInKiLlEr - Настройка евента катаклизма в Dion городе - ВНИМАНИЕ: ни в коем случае не должно произойти так что бы ID гвардов и статуй совпадали с ID других городов - ВНИМАНИЕ: не ставить ID статуй/гвардов которые участвуют стандартно в спавне (гремленов сменить тоже)
 */
public class DionCataclizm extends Cataclizm
{
	private static boolean _cycle = false;
	private static final Logger _log = LoggerFactory.getLogger(Cataclizm.class);
	private static boolean _active = false;
	
	private static ScheduledFuture<?> _startTask;
	private static ScheduledFuture<?> _cataclizmTask;
	private static ScheduledFuture<?> _cataclizm2Task;
	private static ScheduledFuture<?> _cataclizmTimeTask;
	private static Zone _zone1 = ZoneHolder.getZone("[dion_town_peace1]");
	private static Zone _zone2 = ZoneHolder.getZone("[dion_town_peace2]");
	private static Zone _zone3 = ZoneHolder.getZone("[dion_town_peace3]");
	private static Zone _zone4 = ZoneHolder.getZone("[dion_town_peace4]");
	private static Zone _zone5 = ZoneHolder.getZone("[dion_town_peace5]");
	private static ZoneListener _zoneListener = new ZoneListener();
	
	// Если включено то город после захвата будет захвачен пока игроки не отобьют его
	private boolean _cycleEnable = false;
	// Через какое время город автоматически оживится (_cycleEnable должно быть выключено), по умолчанию 300 минут = 5 часов
	private int _cycleTime = 300;
	
	// Сколько времени нужно продержать в живых мирную статую что бы город не захватили монстрами (по умолчанию 5 минут)
	private int _cycleZashita = 5;
	
	// Время в которое будет происходить захват города
	private String timer = "7,21,00";
	
	// Сколько времени будет происходить захват (в минутах), по умолчанию 30 минут
	private int timer_siege = 30;
	
	// Включить захват города? true - включен захват города
	public boolean isSiege = false;
	
	// Статуя которая спавнится при захвате, по умолчанию спавнится гремлин
	public int statuya1 = 18342;
	
	// Статуя которая спавнится если город захватили мобы, по умолчанию спавнится гремлин
	public int statuya2 = 18342;
	
	// Статуя которая спавнится если убита первая статуя (на эту статую агрятся все гварды и её нужно защищать)
	public int statuya3 = 18342;
	
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
	}; // Четвертые ворота
	
	// Пятый список пропускаем!!!
	
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
	
	// 10 список пропускаем!!!
	// 11 список пропускаем!!!
	// 12 список пропускаем!!!
	// 13 список пропускаем!!!
	
	public String[] guards14 = new String[]
	{
		"18342,1;18342,1"
	}; // Первая стенка мобов к статуе
	public String[] guards15 = new String[]
	{
		"18342,1;18342,1"
	}; // Вторая стенка мобов к статуе
	public String[] guards16 = new String[]
	{
		"18342,1;18342,1"
	}; // Вторая стенка мобов к статуе
	public String[] guards17 = new String[]
	{
		"18342,1;18342,1"
	}; // Вторая стенка мобов к статуе
	public String[] guards18 = new String[]
	{
		"18342,1;18342,1"
	}; // Вторая стенка мобов к статуе
	public String[] guards19 = new String[]
	{
		"18342,1;18342,1"
	}; // Третья стенка мобов к статуе
	public String[] guards20 = new String[]
	{
		"18342,1;18342,1"
	}; // Третья стенка мобов к статуе
	public String[] guards21 = new String[]
	{
		"18342,1;18342,1"
	}; // Третья стенка мобов к статуе
	public String[] guards22 = new String[]
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
			
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeFinishDion", null);
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
		
		_active = ServerVariables.getString("CataclizmDion", "off").equalsIgnoreCase("on");
		_cycle = ServerVariables.getString("CataclizmDionCycle", "off").equalsIgnoreCase("on");
		
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
		
		_log.info("Loaded Event: Cataclizm Dion");
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
			sayToAll("scripts.events.Cataclizm.AnnounceEventStartedDion", null);
			ServerVariables.set("CataclizmDion", "on");
			
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
				ServerVariables.set("CataclizmDionCycle", "on");
				sayToAll("scripts.events.Cataclizm.AnnounceCataclysmFinishDion", null);
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
				sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeFinishDion", null);
				deleteTown();
				life = false;
				ServerVariables.unset("CataclizmDion");
				ServerVariables.unset("CataclizmDionCycle");
				
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
			stmt = con.prepareStatement("UPDATE cataclysm SET `player_name`=? WHERE town = Dion");
			stmt.setString(1, player.getName());
			stmt.setString(2, "Dion");
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
				player.sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, "Город Дион захвачен монстрами, пожалуйста покиньте город для вашей же безопасности!"));
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
		return new Territory().add(new Polygon().add(15684, 144653).setZmin(-3080).setZmax(-2980).add(15580, 144371).setZmin(-3064).setZmax(-2964).add(16429, 143556).setZmin(-2848).setZmax(-2748).add(16935, 143916).setZmin(-3008).setZmax(-2908));
	}
	
	public Territory getGuardsMobsTerritory2()
	{
		return new Territory().add(new Polygon().add(19149, 142341).setZmin(-3048).setZmax(-2948).add(19059, 142046).setZmin(-3104).setZmax(-3004).add(18650, 142158).setZmin(-3072).setZmax(-2972).add(18757, 142443).setZmin(-3048).setZmax(-2948));
	}
	
	public Territory getGuardsMobsTerritory3()
	{
		return new Territory().add(new Polygon().add(21565, 145985).setZmin(-3152).setZmax(-3052).add(21848, 146036).setZmin(-3192).setZmax(-3092).add(21950, 145642).setZmin(-3168).setZmax(-3068).add(21521, 145516).setZmin(-3128).setZmax(-3028));
	}
	
	public Territory getGuardsMobsTerritory4()
	{
		return new Territory().add(new Polygon().add(17113, 147459).setZmin(-3112).setZmax(-3012).add(17034, 147768).setZmin(-3160).setZmax(-3060).add(17624, 147987).setZmin(-3208).setZmax(-3108).add(17681, 147673).setZmin(-3112).setZmax(-3012));
	}
	
	public Territory getGuardsMobsTerritory5()
	{
		return null;
	}
	
	public Territory getGuardsMobsTerritory6()
	{
		return new Territory().add(new Polygon().add(15457, 142760).setZmin(-2688).setZmax(-2588).add(14839, 142750).setZmin(-2656).setZmax(-2556).add(14871, 143392).setZmin(-2656).setZmax(-2556).add(15625, 143403).setZmin(-2736).setZmax(-2636).add(16245, 143103).setZmin(-2704).setZmax(-2604).add(15891, 142880).setZmin(-2688).setZmax(-2588).add(15540, 142805).setZmin(-2696).setZmax(-2596));
	}
	
	public Territory getGuardsMobsTerritory7()
	{
		return new Territory().add(new Polygon().add(17716, 145316).setZmin(-3048).setZmax(-2948).add(17553, 145503).setZmin(-3064).setZmax(-2964).add(17382, 145404).setZmin(-3056).setZmax(-2956).add(17481, 145166).setZmin(-3048).setZmax(-2948));
	}
	
	public Territory getGuardsMobsTerritory8()
	{
		return new Territory().add(new Polygon().add(20009, 145930).setZmin(-3120).setZmax(-3020).add(19992, 145715).setZmin(-3128).setZmax(-3028).add(20274, 145640).setZmin(-3112).setZmax(-3012).add(20352, 145934).setZmin(-3120).setZmax(-3020));
	}
	
	public Territory getGuardsMobsTerritory9()
	{
		return new Territory().add(new Polygon().add(18947, 143457).setZmin(-3024).setZmax(-2924).add(19187, 143413).setZmin(-3048).setZmax(-2948).add(19217, 143126).setZmin(-3040).setZmax(-2940).add(18913, 143142).setZmin(-3024).setZmax(-2924));
	}
	
	public Territory getGuardsMobsTerritory10()
	{
		return null;
	}
	
	public Territory getGuardsMobsTerritory11()
	{
		return null;
	}
	
	public Territory getGuardsMobsTerritory12()
	{
		return null;
	}
	
	public Territory getGuardsMobsTerritory13()
	{
		return null;
	}
	
	public Territory getGuardsMobsTerritory14()
	{
		return new Territory().add(new Polygon().add(18664, 145449).setZmin(-3120).setZmax(-3020).add(18950, 145427).setZmin(-3112).setZmax(-3012).add(18881, 145135).setZmin(-3120).setZmax(-3020).add(18581, 145209).setZmin(-3112).setZmax(-3012));
	}
	
	public Territory getGuardsMobsTerritory15()
	{
		return new Territory().add(new Polygon().add(18466, 145193).setZmin(-3096).setZmax(-2996).add(18424, 145110).setZmin(-3088).setZmax(-2988).add(18888, 144954).setZmin(-3120).setZmax(-3020).add(18957, 145056).setZmin(-3120).setZmax(-3020));
	}
	
	public Territory getGuardsMobsTerritory16()
	{
		return new Territory().add(new Polygon().add(18870, 144984).setZmin(-3120).setZmax(-3020).add(18950, 144960).setZmin(-3112).setZmax(-3012).add(19115, 145407).setZmin(-3104).setZmax(-3004).add(18990, 145494).setZmin(-3112).setZmax(-3012));
	}
	
	public Territory getGuardsMobsTerritory17()
	{
		return new Territory().add(new Polygon().add(19081, 145445).setZmin(-3112).setZmax(-3012).add(19093, 145550).setZmin(-3096).setZmax(-2996).add(18646, 145659).setZmin(-3104).setZmax(-3004).add(18575, 145488).setZmin(-3120).setZmax(-3020));
	}
	
	public Territory getGuardsMobsTerritory18()
	{
		return new Territory().add(new Polygon().add(18722, 145583).setZmin(-3096).setZmax(-2996).add(18620, 145617).setZmin(-3104).setZmax(-3004).add(18341, 145248).setZmin(-3088).setZmax(-2988).add(18459, 145147).setZmin(-3096).setZmax(-2996));
	}
	
	public Territory getGuardsMobsTerritory19()
	{
		return new Territory().add(new Polygon().add(18377, 145600).setZmin(-3112).setZmax(-3012).add(18379, 145095).setZmin(-3072).setZmax(-2972).add(18113, 145085).setZmin(-3048).setZmax(-2948).add(17990, 145547).setZmin(-3080).setZmax(-2980));
	}
	
	public Territory getGuardsMobsTerritory20()
	{
		return new Territory().add(new Polygon().add(18038, 145961).setZmin(-3104).setZmax(-3004).add(18164, 145655).setZmin(-3088).setZmax(-2988).add(18961, 145538).setZmin(-3112).setZmax(-3012).add(18932, 145878).setZmin(-3080).setZmax(-2980));
	}
	
	public Territory getGuardsMobsTerritory21()
	{
		return new Territory().add(new Polygon().add(19027, 145776).setZmin(-3080).setZmax(-2980).add(19425, 145755).setZmin(-3088).setZmax(-2988).add(19422, 145309).setZmin(-3096).setZmax(-2996).add(19119, 145210).setZmin(-3104).setZmax(-3004));
	}
	
	public Territory getGuardsMobsTerritory22()
	{
		return new Territory().add(new Polygon().add(19291, 144849).setZmin(-3096).setZmax(-2996).add(19268, 144477).setZmin(-3096).setZmax(-2996).add(18782, 144252).setZmin(-3064).setZmax(-2964).add(18490, 144561).setZmin(-3056).setZmax(-2956));
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
		return null;
	}
	
	public String[] getGuardsMobs11()
	{
		return null;
	}
	
	public String[] getGuardsMobs12()
	{
		return null;
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
		return new Location(18770, 145341, -3120);
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
				sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeFinishDion", null);
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
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeStatuya1Dion", null);
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeStatuya3Dion", null);
			
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
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeStatuya2Dion", null);
			
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