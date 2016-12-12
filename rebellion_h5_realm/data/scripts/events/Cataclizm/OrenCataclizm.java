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
 * @author PaInKiLlEr - Настройка евента катаклизма в Oren городе - ВНИМАНИЕ: ни в коем случае не должно произойти так что бы ID гвардов и статуй совпадали с ID других городов - ВНИМАНИЕ: не ставить ID статуй/гвардов которые участвуют стандартно в спавне (гремленов сменить тоже)
 */
public class OrenCataclizm extends Cataclizm
{
	private static boolean _cycle = false;
	private static final Logger _log = LoggerFactory.getLogger(Cataclizm.class);
	private static boolean _active = false;
	
	private static ScheduledFuture<?> _startTask;
	private static ScheduledFuture<?> _cataclizmTask;
	private static ScheduledFuture<?> _cataclizm2Task;
	private static ScheduledFuture<?> _cataclizmTimeTask;
	private static Zone _zone1 = ZoneHolder.getZone("[oren_town_peace1]");
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
	
	// 4 список пропускаем!!!
	// 5 список пропускаем!!!
	
	public String[] guards6 = new String[]
	{
		"18342,1;18342,1"
	}; // Возле телепорта
	
	// 7 список пропускаем!!!
	// 8 список пропускаем!!!
	// 9 список пропускаем!!!
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
			
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeFinishOren", null);
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
		
		_active = ServerVariables.getString("CataclizmOren", "off").equalsIgnoreCase("on");
		_cycle = ServerVariables.getString("CataclizmOrenCycle", "off").equalsIgnoreCase("on");
		
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
		
		_log.info("Loaded Event: Cataclizm Oren");
	}
	
	@Override
	public void onReload()
	{
		_zone1.removeListener(_zoneListener);
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
			sayToAll("scripts.events.Cataclizm.AnnounceEventStartedOren", null);
			ServerVariables.set("CataclizmOren", "on");
			
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
				ServerVariables.set("CataclizmOrenCycle", "on");
				sayToAll("scripts.events.Cataclizm.AnnounceCataclysmFinishOren", null);
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
				sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeFinishOren", null);
				deleteTown();
				life = false;
				ServerVariables.unset("CataclizmOren");
				ServerVariables.unset("CataclizmOrenCycle");
				
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
			stmt = con.prepareStatement("UPDATE cataclysm SET `player_name`=? WHERE town = Oren");
			stmt.setString(1, player.getName());
			stmt.setString(2, "Oren");
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
				player.sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, "Город Орен захвачен монстрами, пожалуйста покиньте город для вашей же безопасности!"));
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
		return new Territory().add(new Polygon().add(81183, 52873).setZmin(-1576).setZmax(-1476).add(81177, 52435).setZmin(-1720).setZmax(-1620).add(80877, 52451).setZmin(-1712).setZmax(-1612).add(80871, 52900).setZmin(-1568).setZmax(-1468));
	}
	
	public Territory getGuardsMobsTerritory2()
	{
		return new Territory().add(new Polygon().add(79520, 54117).setZmin(-1560).setZmax(-1460).add(79056, 54121).setZmin(-1696).setZmax(-1596).add(79038, 54438).setZmin(-1712).setZmax(-1612).add(79510, 54427).setZmin(-1560).setZmax(-1460));
	}
	
	public Territory getGuardsMobsTerritory3()
	{
		return new Territory().add(new Polygon().add(81376, 56361).setZmin(-1528).setZmax(-1428).add(81369, 57047).setZmin(-1712).setZmax(-1612).add(81696, 57041).setZmin(-1712).setZmax(-1612).add(81696, 56375).setZmin(-1528).setZmax(-1428));
	}
	
	public Territory getGuardsMobsTerritory4()
	{
		return null;
	}
	
	public Territory getGuardsMobsTerritory5()
	{
		return null;
	}
	
	public Territory getGuardsMobsTerritory6()
	{
		return new Territory().add(new Polygon().add(83179, 53288).setZmin(-1488).setZmax(-1388).add(83173, 53101).setZmin(-1488).setZmax(-1388).add(82806, 53123).setZmin(-1488).setZmax(-1388).add(82821, 53302).setZmin(-1488).setZmax(-1388));
	}
	
	public Territory getGuardsMobsTerritory7()
	{
		return null;
	}
	
	public Territory getGuardsMobsTerritory8()
	{
		return null;
	}
	
	public Territory getGuardsMobsTerritory9()
	{
		return null;
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
		return new Territory().add(new Polygon().add(82287, 55731).setZmin(-1520).setZmax(-1420).add(82291, 56035).setZmin(-1520).setZmax(-1420).add(82644, 56024).setZmin(-1520).setZmax(-1420).add(82683, 55728).setZmin(-1520).setZmax(-1420));
	}
	
	public Territory getGuardsMobsTerritory15()
	{
		return new Territory().add(new Polygon().add(82234, 55657).setZmin(-1520).setZmax(-1420).add(82099, 55655).setZmin(-1520).setZmax(-1420).add(82110, 56092).setZmin(-1520).setZmax(-1420).add(82226, 56084).setZmin(-1520).setZmax(-1420));
	}
	
	public Territory getGuardsMobsTerritory16()
	{
		return new Territory().add(new Polygon().add(82187, 56046).setZmin(-1520).setZmax(-1420).add(82183, 56166).setZmin(-1520).setZmax(-1420).add(82760, 56174).setZmin(-1520).setZmax(-1420).add(82767, 56052).setZmin(-1520).setZmax(-1420));
	}
	
	public Territory getGuardsMobsTerritory17()
	{
		return new Territory().add(new Polygon().add(82708, 56157).setZmin(-1520).setZmax(-1420).add(82860, 56134).setZmin(-1520).setZmax(-1420).add(82883, 55595).setZmin(-1520).setZmax(-1420).add(82714, 55603).setZmin(-1520).setZmax(-1420));
	}
	
	public Territory getGuardsMobsTerritory18()
	{
		return new Territory().add(new Polygon().add(82792, 55698).setZmin(-1520).setZmax(-1420).add(82809, 55582).setZmin(-1520).setZmax(-1420).add(82215, 55557).setZmin(-1520).setZmax(-1420).add(82205, 55695).setZmin(-1520).setZmax(-1420));
	}
	
	public Territory getGuardsMobsTerritory19()
	{
		return new Territory().add(new Polygon().add(81959, 55397).setZmin(-1520).setZmax(-1420).add(81940, 56050).setZmin(-1520).setZmax(-1420).add(82125, 56100).setZmin(-1520).setZmax(-1420).add(82178, 55399).setZmin(-1520).setZmax(-1420));
	}
	
	public Territory getGuardsMobsTerritory20()
	{
		return new Territory().add(new Polygon().add(82203, 55451).setZmin(-1520).setZmax(-1420).add(82212, 55214).setZmin(-1520).setZmax(-1420).add(82578, 55176).setZmin(-1520).setZmax(-1420).add(82624, 55471).setZmin(-1520).setZmax(-1420));
	}
	
	public Territory getGuardsMobsTerritory21()
	{
		return new Territory().add(new Polygon().add(82844, 55494).setZmin(-1520).setZmax(-1420).add(82885, 56084).setZmin(-1520).setZmax(-1420).add(83219, 56085).setZmin(-1520).setZmax(-1420).add(83196, 55479).setZmin(-1520).setZmax(-1420));
	}
	
	public Territory getGuardsMobsTerritory22()
	{
		return new Territory().add(new Polygon().add(82680, 56161).setZmin(-1520).setZmax(-1420).add(82683, 56319).setZmin(-1520).setZmax(-1420).add(82018, 56343).setZmin(-1520).setZmax(-1420).add(82002, 56131).setZmin(-1520).setZmax(-1420));
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
		return null;
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
		return null;
	}
	
	public String[] getGuardsMobs8()
	{
		return null;
	}
	
	public String[] getGuardsMobs9()
	{
		return null;
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
		return new Location(82417, 55876, -1520);
	}
	
	@Override
	public void onShowChat(NpcInstance actor)
	{
		if (life)
		{
			if (actor.isInZone(_zone1))
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
				sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeFinishOren", null);
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
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeStatuya1Oren", null);
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeStatuya3Oren", null);
			
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
			sayToAll("scripts.events.Cataclizm.AnnounceCataclysmSiegeStatuya2Oren", null);
			
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