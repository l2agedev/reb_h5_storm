package events.TvT_New;

import l2r.gameserver.utils.GArray;

import java.io.File;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class TvTConfig
{
	private static final Logger _log = Logger.getLogger(TvTConfig.class.getName());
	public static GArray<Configs> _configs = new GArray<Configs>();

	public static void load()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);

			File file = new File("./config/events/TvT_New.xml");

			Calendar _date = Calendar.getInstance();

			Document doc = factory.newDocumentBuilder().parse(file);

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if (!"list".equalsIgnoreCase(n.getNodeName()))
					continue;
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (!"tvt".equalsIgnoreCase(d.getNodeName()))
						continue;
					Configs _config = new Configs();
					NamedNodeMap attrs = d.getAttributes();

					_date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(attrs.getNamedItem("hour").getNodeValue()));
					_date.set(Calendar.MINUTE, Integer.parseInt(attrs.getNamedItem("min").getNodeValue()));
					_config.START_TIME = (_date.getTimeInMillis() > System.currentTimeMillis() ? _date.getTimeInMillis() / 1000 : _date.getTimeInMillis() / 1000 + 86400);
					_config.TIME_TO_END_BATTLE = Integer.parseInt(attrs.getNamedItem("TimeToEvent").getNodeValue());

					for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
					{
						if ("Participants".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.MIN_PARTICIPANTS = Integer.parseInt(attrs.getNamedItem("min").getNodeValue());
							_config.MAX_PARTICIPANTS = Integer.parseInt(attrs.getNamedItem("max").getNodeValue());
						}
						else if ("TimeToRegistration".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.TIME_TO_START_BATTLE = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
						}
						else if ("AllowKillBonus".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.ALLOW_KILL_BONUS = Boolean.parseBoolean(attrs.getNamedItem("val").getNodeValue());
						}
						else if ("AllowHeroSkillAndWeapons".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.ALLOW_HERO_WEAPONS = Boolean.parseBoolean(attrs.getNamedItem("val").getNodeValue());
						}
						else if ("KillReward".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.KILL_BONUS_ID = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							_config.KILL_BONUS_COUNT = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
						}
						else if ("StopAllEffects".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.STOP_ALL_EFFECTS = Boolean.parseBoolean(attrs.getNamedItem("val").getNodeValue());
						}
						else if ("AllowTakeItems".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.ALLOW_TAKE_ITEM = Boolean.parseBoolean(attrs.getNamedItem("val").getNodeValue());
						}
						else if ("TakeItems".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.TAKE_ITEM_ID = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							_config.TAKE_COUNT = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
						}
						else if ("TeamCoords".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.TEAM_NAME.add(attrs.getNamedItem("name").getNodeValue());
							_config.TEAM_COUNTS += 1;
						}
						else if ("Reward".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.ST_REWARD_ITEM_ID = attrs.getNamedItem("id").getNodeValue();
							_config.ST_REWARD_COUNT = attrs.getNamedItem("count").getNodeValue();
						}
						else if ("TeamCoords".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.TEAM_NAME.add(attrs.getNamedItem("name").getNodeValue());
							_config.TEAM_COUNTS += 1;
						}
						else if ("NumberOfRounds".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.NUMBER_OF_ROUNDS = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
						}
						else if ("ResurrectionTime".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.RESURRECTION_TIME = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
						}
						else if ("Level".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.MIN_LEVEL = Integer.parseInt(attrs.getNamedItem("min").getNodeValue());
							_config.MAX_LEVEL = Integer.parseInt(attrs.getNamedItem("max").getNodeValue());
						}
						else if ("OpenDoor".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.DOORS.put(Integer.parseInt(attrs.getNamedItem("id").getNodeValue()), Boolean.valueOf(true));
						}
						else if ("CloseDoor".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.DOORS.put(Integer.parseInt(attrs.getNamedItem("id").getNodeValue()), Boolean.valueOf(false));
						}
						else if ("PauseTime".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.PAUSE_TIME = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
						}
						else if ("RestrictItems".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.RESTRICT_ITEMS = attrs.getNamedItem("val").getNodeValue();
						}
						else if ("RestrictSkills".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.RESTRICT_SKILLS = attrs.getNamedItem("val").getNodeValue();
						}
						else if ("ListMageFaiterSupport".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							String[] skills = attrs.getNamedItem("val").getNodeValue().split(",");
							for (String id : skills)
								_config.LIST_MAGE_FAITER_SUPPORT.add(Integer.parseInt(id));
						}
						else if ("ListMageMagSupport".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							String[] skills = attrs.getNamedItem("val").getNodeValue().split(",");
							for (String id : skills)
								_config.LIST_MAGE_MAG_SUPPORT.add(Integer.parseInt(id));
						}
						else
						{
							if (!"RewardForKill".equalsIgnoreCase(cd.getNodeName()))
								continue;
							attrs = cd.getAttributes();
							_config.REWARD_FOR_KILL = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
						}
					}

					_configs.add(_config);
				}
			}

			_configs.sort();
			_log.info("Loaded " + _configs.size() + " configs for " + TvT_New.getInstance().getName() + " event.");
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error parsing tvt_new.xml, by error: ", e);
		}
	}
}