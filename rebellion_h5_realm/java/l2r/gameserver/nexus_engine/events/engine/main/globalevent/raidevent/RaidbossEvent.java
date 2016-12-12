package l2r.gameserver.nexus_engine.events.engine.main.globalevent.raidevent;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2r.gameserver.Config;
import l2r.gameserver.nexus_engine.events.NexusLoader;
import l2r.gameserver.nexus_engine.events.engine.EventManager;
import l2r.gameserver.nexus_engine.events.engine.main.globalevent.GlobalEvent;
import l2r.gameserver.nexus_engine.events.engine.main.globalevent.raidevent.bosses.BossTemplate;
import l2r.gameserver.nexus_engine.events.engine.main.globalevent.raidevent.bosses.GenericBoss;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.NpcData;

/**
 * @author hNoke
 * this boss is loaded from xmls
 */
public class RaidbossEvent extends GlobalEvent
{
	// TODO xml..
	private static final String CONFIG_FILE = "config/NexusEngine/raidboss.xml";

	private final Map<Integer, BossTemplate> _data = new FastMap<Integer, BossTemplate>();
	
	public RaidbossEvent()
	{
		loadBossesFromXml();

		//_data.add(new GenericBoss());
		//_data.add(new MalogBoss());
	}

	@Override
	public void reload()
	{
		_data.clear();
		loadBossesFromXml();
	}

	private void loadBossesFromXml()
	{
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
						if("boss".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();

							String name = attrs.getNamedItem("name").getNodeValue();
							int npcid = Integer.parseInt(attrs.getNamedItem("npcid").getNodeValue());
							int chanceToAppear = Integer.parseInt(attrs.getNamedItem("chanceToAppear").getNodeValue());

							int x = Integer.parseInt(attrs.getNamedItem("x").getNodeValue());
							int y = Integer.parseInt(attrs.getNamedItem("y").getNodeValue());
							int z = Integer.parseInt(attrs.getNamedItem("z").getNodeValue());

							int playerX = Integer.parseInt(attrs.getNamedItem("playerX").getNodeValue());
							int playerY = Integer.parseInt(attrs.getNamedItem("playerY").getNodeValue());
							int playerZ = Integer.parseInt(attrs.getNamedItem("playerZ").getNodeValue());

							GenericBoss boss = new GenericBoss(name, npcid, chanceToAppear, x, y, z, playerX, playerY, playerZ);

							for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if("reward".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();

									int itemId = Integer.parseInt(attrs.getNamedItem("itemId").getNodeValue());
									int ammount = Integer.parseInt(attrs.getNamedItem("ammount").getNodeValue());
									int chance = Integer.parseInt(attrs.getNamedItem("chance").getNodeValue());

									boss.addReward(itemId, ammount, chance);
								}
							}

							_data.put(order, boss);
							order ++;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private BossTemplate _currentBossData;
	private NpcData _raidboss;
	
	@Override
	public String getName()
	{
		return "Raidboss";
	}

	@Override
	public boolean canStart(String param)
	{
		return true;
	}

	private BossTemplate getRandomBoss()
	{
		loadBossesFromXml();

		return _data.get(CallBack.getInstance().getOut().random(_data.size()));
	}

	@Override
	public void start(String param)
	{
		BossTemplate boss = null;

		if(param != null)
		{
			for(BossTemplate template : _data.values())
			{
				if(template.getName().equalsIgnoreCase(param))
				{
					boss = template;
					break;
				}
			}
		}

		if(boss == null)
			boss = getRandomBoss();

		_currentBossData = boss;
		
		_raidboss = boss.doSpawn();
		
		if(_raidboss == null)
		{
			NexusLoader.debug("Could not spawn RAIDBOSS - template propably doesn't exist.", Level.SEVERE);
			announce("Raidboss event has been canceled due to an error.");
			EventManager.getInstance().getMainEventManager().getGlobalEventManager().stop();
		}
		
		// TODO Infern0 confirm this.
		//_raidboss.setGlobalEvent(this);
		
		announce("Global raidboss " + _raidboss.getName() + " has been spawned.");
	}

	@Override
	public void end()
	{
		if(_raidboss == null)
			return;

		if(!_raidboss.isDead())
		{
			_raidboss.deleteMe();
		}
		else
		{
			for(PlayerEventInfo player : _raidboss.getAroundPlayers(30000))
			{
				_currentBossData.rewardPlayer(player);
			}
		}
		
		announce("Global raidboss event has ended.");
		announce(_raidboss.getName() + " has been killed.");
	}
	
	public void bossDied()
	{
		announce("The boss has been killed.");
		announce("Congratulations.");
		EventManager.getInstance().getMainEventManager().getGlobalEventManager().stop();
	}

	@Override
	public boolean canRegister(PlayerEventInfo player)
	{
		return true;
	}
	
	@Override
	public void monsterDies(NpcData npc)
	{
		if(_currentBossData != null)
		{
			_currentBossData.monsterDied(this, npc);
		}
	}

	@Override
	public void addPlayer(PlayerEventInfo player)
	{
		if(_raidboss != null && _currentBossData != null)
		{
			if(player.isDead())
			{
				player.doRevive();
			}
			
			player.screenMessage("You will be teleported closer to the boss soon.", getName(), false);

			player.teleport(_currentBossData.getPlayersSpawn().getLoc(), 10000, 0);
		}
		else
		{
			player.sendMessage("The boss is gone.");
		}
	}

	@Override
	public String getStateNameForHtml()
	{
		return "Dangerous";
	}
}
