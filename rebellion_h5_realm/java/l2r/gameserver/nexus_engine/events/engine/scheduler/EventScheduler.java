package l2r.gameserver.nexus_engine.events.engine.scheduler;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastTable;
import javolution.util.FastMap;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2r.gameserver.Config;
import l2r.gameserver.nexus_engine.events.NexusLoader;
import l2r.gameserver.nexus_engine.events.engine.EventConfig;
import l2r.gameserver.nexus_engine.events.engine.EventManager;
import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.base.Loc;
import l2r.gameserver.nexus_engine.events.engine.main.MainEventManager;
import l2r.gameserver.nexus_engine.events.engine.mini.MiniEventManager;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.delegate.NpcData;
import l2r.gameserver.nexus_interface.delegate.NpcTemplateData;
import l2r.gameserver.nexus_interface.delegate.SkillData;


/**
 * Created by hNoke
 */
public class EventScheduler
{
	private static final String CONFIG_FILE = "config/NexusEngine/scheduler.xml";

	public static Map<Integer, SchedulerSegment> _segments = new FastMap<>();
	public int _lastSegmentOrder = -1;
	public SchedulerSegment _currentSegment;

	public boolean _isActive = false;

	public ScheduledFuture<?> _future = null;

	//

	private List<MiniEventManager> _openedMiniEvents = new FastTable<MiniEventManager>();

	EventScheduler()
	{
		load();
	}

	public void startNext(boolean checkIsActive)
	{
		if(!checkIsActive)
			_isActive = true;
		else
		{
			if(!_isActive)
				return;
		}

		handleCurrentSegment();
		
		SchedulerSegment next = getNextSegment(_lastSegmentOrder, true);
		
		if(next == null)
		{
			NexusLoader.debug("Auto events cannot be started, something is wrong with the event scheduler config files.");
			return;
		}

		switch(next._type)
		{
			case BREAK:

				scheduleAnnounce("New events will open in " + ((BreakSegment) next)._duration + " minutes!", "Event Manager", 10000);

				schedule(((BreakSegment) next)._duration);
				break;
			case MAIN:

				NexusLoader.debug("Starting a main event.");
				
				if(EventManager.getInstance().getMainEventManager().getState() == MainEventManager.State.IDLE)
					EventManager.getInstance().getMainEventManager().getScheduler().run();

				break;
			case MINI:

				NexusLoader.debug("Starting mini events.");
				
				MiniEventSegment miSegment = (MiniEventSegment) next;
				MiniEventManager event;

				int count = 0;
				
				// randomize
				Collections.shuffle(miSegment._modes);

				// only one event from each class can be started
				for(int i = 0; i < 1000; i++)
				{
					for(MiniEventSegment.Mode segmentMode : miSegment._modes)
					{
						if(segmentMode._eventClass == i)
						{
							EventType type = EventType.getType(segmentMode._name);
							int modeId = segmentMode._id;

							event = EventManager.getInstance().getMiniEvent(type, modeId);
							if(event != null)
								_openedMiniEvents.add(event);
							else
							{
								NexusLoader.debug("Event scheduler: could not find event with name " + segmentMode._name + " and ID " + segmentMode._id + ". Please fix it.");
							}

							count ++;

							break;
						}
					}
				}

				for(MiniEventManager manager : _openedMiniEvents)
				{
					manager.getMode().setAllowed(true);
				}

				if(_registrationNpc != null)
				{
					_registrationNpc.deleteMe();
					_registrationNpc = null;
				}

				int id = EventConfig.getInstance().getGlobalConfigInt("miniEventsManagerId");

				NpcData data;
				final NpcTemplateData template = new NpcTemplateData(id);

				String cords = EventConfig.getInstance().getGlobalConfigValue("spawnRegNpcCords");
				int[] cordsInt = new int[3];

				cordsInt[0] = Integer.parseInt(cords.split(";")[0]);
				cordsInt[1] = Integer.parseInt(cords.split(";")[1]);
				cordsInt[2] = Integer.parseInt(cords.split(";")[2]);

				Loc loc = new Loc(cordsInt[0], cordsInt[1], cordsInt[2]);

				data = template.doSpawn(loc.getX(), loc.getY(), loc.getZ(), 1, 0);

				SkillData skill = new SkillData(5966, 1);
				if(skill.exists())
					data.broadcastSkillUse(data, data, skill.getId(), skill.getLevel());

				_registrationNpc = data;

				if(count > 0)
				{
					scheduleAnnounce(count + " mini events have been opened for registration.", "Event Manager", 5000);
					scheduleAnnounce("They will be opened for registration for " + miSegment._regDuration + " minutes.", "Event Manager", 10000);
				}

				schedule(miSegment._regDuration);

				break;
			case GLOBAL:
			{
				if(CallBack.getInstance().getOut().random(100) < ((GlobalEventSegment) next)._chance)
				{
					NexusLoader.debug("Starting a global event (chance " + ((GlobalEventSegment) next)._chance + " passed.");
					
					// starts a random global event
					EventManager.getInstance().getMainEventManager().getGlobalEventManager().start(null, null);
					
					if(_registrationNpcGlobalEvent != null)
					{
						_registrationNpcGlobalEvent.deleteMe();
						_registrationNpcGlobalEvent = null;
					}

					id = EventConfig.getInstance().getGlobalConfigInt("globalEventManagerId");

					final NpcTemplateData templateGlobal = new NpcTemplateData(id);

					cords = EventConfig.getInstance().getGlobalConfigValue("spawnRegNpcCords");
					cordsInt = new int[3];

					cordsInt[0] = Integer.parseInt(cords.split(";")[0]);
					cordsInt[1] = Integer.parseInt(cords.split(";")[1]);
					cordsInt[2] = Integer.parseInt(cords.split(";")[2]);

					loc = new Loc(cordsInt[0], cordsInt[1], cordsInt[2]);
					data = templateGlobal.doSpawn(loc.getX(), loc.getY(), loc.getZ(), 1, 0);

					skill = new SkillData(5966, 1);
					if(skill.exists())
						data.broadcastSkillUse(data, data, skill.getId(), skill.getLevel());

					_registrationNpcGlobalEvent = data;
				}

				schedule(((GlobalEventSegment) next)._breakDuration);
				
				break;
			}
		}

	}

	public String getDelayUntilNextSegment()
	{
		if(_future == null)
			return "N/A";

		int time = (int) _future.getDelay(TimeUnit.SECONDS);
		if(time > 60)
		{
			int min = time / 60;
			if(min < 1) min = 1;
			return min + " minutes";
		}
		else
		{
			return time + " seconds";
		}
	}

	public NpcData _registrationNpc = null;
	public NpcData _registrationNpcGlobalEvent = null;

	/**
	 * ends events if required
	 */
	private void handleCurrentSegment()
	{
		if(_currentSegment != null)
		{
			switch(_currentSegment._type)
			{
				case GLOBAL:

					if(_registrationNpcGlobalEvent != null)
					{
						_registrationNpcGlobalEvent.deleteMe();
						_registrationNpcGlobalEvent = null;
					}

					break;
				case BREAK:

					// no special actions required to cancel a break

					break;
				case MAIN:

					/*try
					{
						AbstractMainEvent event = EventManager.getInstance().getMainEventManager().getCurrent();
						if(event != null)
						{
							if(EventManager.getInstance().getMainEventManager().getState() != MainEventManager.State.IDLE)
							{
								event.clearEvent();
							}
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}*/

					// the event has already been closed by the Main event engine

					break;
				case MINI:

					if(_registrationNpc != null)
					{
						_registrationNpc.deleteMe();
						_registrationNpc = null;
					}

					int count = 0;
					for(MiniEventManager manager : _openedMiniEvents)
					{
						if(manager != null)
						{
							manager.getMode().setAllowed(false);
							count ++;
						}
					}

					if(count > 0)
						EventManager.getInstance().announce("Mini events have been closed.", "Event Manager");

					_openedMiniEvents.clear();
					break;
			}
		}
	}

	/**
	 *
	 * @param duration - in minutes
	 */
	public void schedule(long duration)
	{
		_future = CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				startNext(false);
			}
		}, duration * 60000);
	}

	public void scheduleAnnounce(final String message, final String announcer, int delay)
	{
		CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if(_isActive)
					EventManager.getInstance().announce(message, announcer);
			}
		}, delay);
	}
	
	public void onClose()
	{
		handleCurrentSegment();
	}

	public void stop()
	{
		NexusLoader.debug("Stopping event scheduler...");

		_isActive = false;

		handleCurrentSegment();

		if(_future != null)
		{
			_future.cancel(false);
			_future = null;
		}

		NexusLoader.debug("Event scheduler stopped.");
	}
	
	public SchedulerSegment getNextSegment()
	{
		return getNextSegment(_lastSegmentOrder, false);
	}

	public SchedulerSegment getNextSegment(int lastSegmentOrder, boolean update)
	{
		SchedulerSegment found = null;
		for(Map.Entry<Integer, SchedulerSegment> e : _segments.entrySet())
		{
			if(e.getKey() == (lastSegmentOrder + 1))
			{
				found = e.getValue();
				
				if(update)
					_lastSegmentOrder = e.getKey();
			}
		}

		if(found == null)
		{
			found = _segments.get(0);
			
			if(update)
				_lastSegmentOrder = 0;
		}

		if(found == null)
		{
			NexusLoader.debug("No valid segment found. Check out " + CONFIG_FILE + " for more informations.");
			return null;
		}

		if(update)
			_currentSegment = found;
		
		return found;
	}

	private void load()
	{
		NexusLoader.debug("Loading the new event scheduler system.");
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);

			int order = 0;

			File file = Config.getFile(CONFIG_FILE);
			if(!file.exists())
			{
				NexusLoader.debug("Could not find " + CONFIG_FILE + ". Please get this file from hNoke and put it there.", Level.SEVERE);
				return;
			}

			Document doc = factory.newDocumentBuilder().parse(file);

			for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if("list".equalsIgnoreCase(n.getNodeName()))
				{
					for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if("break".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();

							int duration = Integer.parseInt(attrs.getNamedItem("duration").getNodeValue());

							BreakSegment breakSegment  = new BreakSegment(duration);

							_segments.put(order, breakSegment);
							order++;
						}

						if("mainevent".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();

							int duration = Integer.parseInt(attrs.getNamedItem("registerDuration").getNodeValue());

							MainEventSegment meSegment = new MainEventSegment(duration);

							_segments.put(order, meSegment);
							order++;
						}

						if("globalevent".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();

							int chance = Integer.parseInt(attrs.getNamedItem("chance").getNodeValue());
							int breakTime = Integer.parseInt(attrs.getNamedItem("breakDuration").getNodeValue());

							GlobalEventSegment gSegment = new GlobalEventSegment(chance, breakTime);

							_segments.put(order, gSegment);
							order++;
						}

						if("minievents".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();

							int registerDuration = Integer.parseInt(attrs.getNamedItem("duration").getNodeValue());

							MiniEventSegment miSegment = new MiniEventSegment(registerDuration);

							for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if("mode".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();

									String eventName = attrs.getNamedItem("eventName").getNodeValue();
									int modeId = Integer.parseInt(attrs.getNamedItem("modeId").getNodeValue());
									int chance = Integer.parseInt(attrs.getNamedItem("chance").getNodeValue());
									int eventClass = Integer.parseInt(attrs.getNamedItem("class").getNodeValue());

									miSegment.addMode(eventName, modeId, chance, eventClass);
								}
							}

							_segments.put(order, miSegment);
							order++;
						}
					}
				}
			}

			NexusLoader.debug("New event scheduler system loaded (" + order + " segments).");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void reload()
	{
		stop();

		load();
	}
	
	public ScheduledFuture<?> getNextFuture()
	{
		return _future;
	}

	public static EventScheduler getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final EventScheduler _instance = new EventScheduler();
	}

}
