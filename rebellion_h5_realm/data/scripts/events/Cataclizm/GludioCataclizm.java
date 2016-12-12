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
import l2r.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author PaInKiLlEr - Настройка евента катаклизма в Gludio городе - ВНИМАНИЕ: ни в коем случае не должно произойти так что бы ID гвардов и статуй совпадали с ID других городов - ВНИМАНИЕ: не ставить ID статуй/гвардов которые участвуют стандартно в спавне (гремленов сменить тоже)
 */
public class GludioCataclizm extends Cataclizm
{
	// TODO почему то ненашёл в оверской сборке зоны глудио
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
	public String[] guards10 = new String[]
	{
		"18342,1;18342,1"
	}; // Возле четвертого КХ
	
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
			
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeFinishGludio", null);
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
		
		_active = ServerVariables.getString("CataclizmGludio", "off").equalsIgnoreCase("on");
		_cycle = ServerVariables.getString("CataclizmGludioCycle", "off").equalsIgnoreCase("on");
		
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
		
		_log.info("Loaded Event: Cataclizm Gludio");
	}
	
	@Override
	public void onReload()
	{
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
			sayToAll("scripts.events.Cataclizm.AnnounceEventStartedGludio", null);
			ServerVariables.set("CataclizmGludio", "on");
			
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
				ServerVariables.set("CataclizmGludioCycle", "on");
				sayToAll("scripts.events.Cataclizm.AnnounceCataclysmFinishGludio", null);
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
				sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeFinishGludio", null);
				deleteTown();
				life = false;
				ServerVariables.unset("CataclizmGludio");
				ServerVariables.unset("CataclizmGludioCycle");
				
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
			stmt = con.prepareStatement("UPDATE cataclysm SET `player_name`=? WHERE town = Gludio");
			stmt.setString(1, player.getName());
			stmt.setString(2, "Gludio");
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
			
			if(_cycle)
				player.sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, false, "Город Глудио захвачен монстрами, пожалуйста покиньте город для вашей же безопасности!"));
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
		return new Territory().add(new Polygon().add(-16649, 124421).setZmin(-3112).setZmax(-3012).add(-16963, 124548).setZmin(-3136).setZmax(-3036).add(-16964, 123912).setZmin(-3128).setZmax(-3028).add(-16676, 123919).setZmin(-3112).setZmax(-3012));
	}
	
	public Territory getGuardsMobsTerritory2()
	{
		return new Territory().add(new Polygon().add(-14348, 120963).setZmin(-2984).setZmax(-2884).add(-14375, 120647).setZmin(-3000).setZmax(-2900).add(-14957, 120775).setZmin(-3024).setZmax(-2924).add(-14817, 121019).setZmin(-2976).setZmax(-2876));
	}
	
	public Territory getGuardsMobsTerritory3()
	{
		return new Territory().add(new Polygon().add(-14396, 126556).setZmin(-3144).setZmax(-3044).add(-14392, 126851).setZmin(-3160).setZmax(-3060).add(-13860, 126873).setZmin(-3168).setZmax(-3068).add(-13953, 126549).setZmin(-3144).setZmax(-3044));
	}
	
	public Territory getGuardsMobsTerritory4()
	{
		return new Territory().add(new Polygon().add(-11906, 123414).setZmin(-3088).setZmax(-2988).add(-11558, 123340).setZmin(-3072).setZmax(-2972).add(-11566, 123858).setZmin(-3056).setZmax(-2956).add(-11897, 123864).setZmin(-3072).setZmax(-2972));
	}
	
	public Territory getGuardsMobsTerritory5()
	{
		return null;
	}
	
	public Territory getGuardsMobsTerritory6()
	{
		return new Territory().add(new Polygon().add(-14613, 124055).setZmin(-3120).setZmax(-3020).add(-14586, 123882).setZmin(-3120).setZmax(-3020).add(-14343, 123873).setZmin(-3112).setZmax(-3012).add(-14335, 124095).setZmin(-3112).setZmax(-3012));
	}
	
	public Territory getGuardsMobsTerritory7()
	{
		return new Territory().add(new Polygon().add(-15802, 123793).setZmin(-3112).setZmax(-3012).add(-16049, 123876).setZmin(-3112).setZmax(-3012).add(-15988, 124100).setZmin(-3112).setZmax(-3012).add(-15711, 124005).setZmin(-3120).setZmax(-3020));
	}
	
	public Territory getGuardsMobsTerritory8()
	{
		return new Territory().add(new Polygon().add(-14707, 125700).setZmin(-3136).setZmax(-3036).add(-14455, 125717).setZmin(-3144).setZmax(-3044).add(-14439, 125404).setZmin(-3136).setZmax(-3036).add(-14688, 125417).setZmin(-3136).setZmax(-3036));
	}
	
	public Territory getGuardsMobsTerritory9()
	{
		return new Territory().add(new Polygon().add(-14160, 125777).setZmin(-3136).setZmax(-3036).add(-14366, 125791).setZmin(-3144).setZmax(-3044).add(-14389, 125494).setZmin(-3136).setZmax(-3036).add(-14165, 125394).setZmin(-3136).setZmax(-3036));
	}
	
	public Territory getGuardsMobsTerritory10()
	{
		return new Territory().add(new Polygon().add(-12467, 123838).setZmin(-3112).setZmax(-3012).add(-12472, 123551).setZmin(-3112).setZmax(-3012).add(-12803, 123552).setZmin(-3112).setZmax(-3012).add(-12782, 123828).setZmin(-3112).setZmax(-3012));
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
		return new Territory().add(new Polygon().add(-14241, 122925).setZmin(-3104).setZmax(-3004).add(-14202, 123201).setZmin(-3112).setZmax(-3012).add(-14480, 123240).setZmin(-3120).setZmax(-3020).add(-14509, 122957).setZmin(-3104).setZmax(-3004));
	}
	
	public Territory getGuardsMobsTerritory15()
	{
		return new Territory().add(new Polygon().add(-14544, 122852).setZmin(-3112).setZmax(-3012).add(-14623, 122852).setZmin(-3112).setZmax(-3012).add(-14602, 123304).setZmin(-3112).setZmax(-3012).add(-14504, 123307).setZmin(-3112).setZmax(-3012));
	}
	
	public Territory getGuardsMobsTerritory16()
	{
		return new Territory().add(new Polygon().add(-14558, 123272).setZmin(-3112).setZmax(-3012).add(-14543, 123350).setZmin(-3112).setZmax(-3012).add(-14131, 123331).setZmin(-3112).setZmax(-3012).add(-14148, 123227).setZmin(-3112).setZmax(-3012));
	}
	
	public Territory getGuardsMobsTerritory17()
	{
		return new Territory().add(new Polygon().add(-14173, 123283).setZmin(-3112).setZmax(-3012).add(-14085, 123270).setZmin(-3112).setZmax(-3012).add(-14092, 122863).setZmin(-3112).setZmax(-3012).add(-14200, 122856).setZmin(-3112).setZmax(-3012));
	}
	
	public Territory getGuardsMobsTerritory18()
	{
		return new Territory().add(new Polygon().add(-14142, 122902).setZmin(-3112).setZmax(-3012).add(-14153, 122797).setZmin(-3112).setZmax(-3012).add(-14580, 122814).setZmin(-3112).setZmax(-3012).add(-14598, 122938).setZmin(-3112).setZmax(-3012));
	}
	
	public Territory getGuardsMobsTerritory19()
	{
		return new Territory().add(new Polygon().add(-14767, 123437).setZmin(-3112).setZmax(-3012).add(-14761, 123621).setZmin(-3112).setZmax(-3012).add(-14021, 123576).setZmin(-3112).setZmax(-3012).add(-14033, 123346).setZmin(-3112).setZmax(-3012));
	}
	
	public Territory getGuardsMobsTerritory20()
	{
		return new Territory().add(new Polygon().add(-14025, 123346).setZmin(-3112).setZmax(-3012).add(-14048, 123079).setZmin(-3112).setZmax(-3012).add(-13867, 123065).setZmin(-3112).setZmax(-3012).add(-13837, 122744).setZmin(-3112).setZmax(-3012).add(-14185, 122710).setZmin(-3112).setZmax(-3012));
	}
	
	public Territory getGuardsMobsTerritory21()
	{
		return new Territory().add(new Polygon().add(-14009, 122557).setZmin(-3112).setZmax(-3012).add(-14621, 122563).setZmin(-3112).setZmax(-3012).add(-14741, 122664).setZmin(-3112).setZmax(-3012).add(-14575, 122878).setZmin(-3112).setZmax(-3012).add(-14315, 122887).setZmin(-3104).setZmax(-3004));
	}
	
	public Territory getGuardsMobsTerritory22()
	{
		return new Territory().add(new Polygon().add(-14746, 122631).setZmin(-3112).setZmax(-3012).add(-14573, 122654).setZmin(-3112).setZmax(-3012).add(-14644, 123328).setZmin(-3112).setZmax(-3012).add(-14748, 123307).setZmin(-3120).setZmax(-3020));
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
		return new Location(-14356, 123060, -3112);
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
				sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeFinishGludio", null);
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
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeStatuya1Gludio", null);
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeStatuya3Gludio", null);
			
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
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeStatuya2Gludio", null);
			
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