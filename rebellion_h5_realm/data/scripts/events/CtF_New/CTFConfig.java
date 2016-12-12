package events.CtF_New;

import l2r.gameserver.utils.GArray;
import l2r.gameserver.utils.Location;

import java.io.File;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CTFConfig
{
	private static final Logger _log = Logger.getLogger(CTFConfig.class.getName());
	public static GArray<Configs> _configs = new GArray<Configs>();

	public static void load()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);

			File file = new File("./config/events/CTF_New.xml");

			Calendar _date = Calendar.getInstance();

			Document doc = factory.newDocumentBuilder().parse(file);

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
				if (!"list".equalsIgnoreCase(n.getNodeName()))
					continue;
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (!"ctf".equalsIgnoreCase(d.getNodeName()))
						continue;
					Configs _config = new Configs();

					NamedNodeMap attrs = d.getAttributes();

					_date.set(11, Integer.parseInt(attrs.getNamedItem("hour").getNodeValue()));
					_date.set(12, Integer.parseInt(attrs.getNamedItem("min").getNodeValue()));

					_config.START_TIME = (_date.getTimeInMillis() > System.currentTimeMillis() ? _date.getTimeInMillis() / 1000L : _date.getTimeInMillis() / 1000L + 86400L);

					_config.TIME_TO_END_BATTLE = Integer.parseInt(attrs.getNamedItem("TimeToEvent").getNodeValue());

					for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
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
						else if ("KillReward".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.KILL_BONUS_ID = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							_config.KILL_BONUS_COUNT = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
						}
						else if ("Reward".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.ST_REWARD_ITEM_ID = attrs.getNamedItem("id").getNodeValue();
							_config.ST_REWARD_COUNT = attrs.getNamedItem("count").getNodeValue();
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
						else if ("StopAllEffects".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.STOP_ALL_EFFECTS = Boolean.parseBoolean(attrs.getNamedItem("val").getNodeValue());
						}
						else if ("TeamCoords".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.TEAM_NAME.add(attrs.getNamedItem("name").getNodeValue());
							_config.TEAM_COUNTS = 2;
						}
						else if ("FlagCords".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.FLAG_COORDS.add(new Location(Integer.parseInt(attrs.getNamedItem("x").getNodeValue()), Integer.parseInt(attrs.getNamedItem("y").getNodeValue()), Integer.parseInt(attrs.getNamedItem("z").getNodeValue()), Integer.parseInt(attrs.getNamedItem("h").getNodeValue())));
						}
						else if ("NeedFlags".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.NEED_SCORE = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
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
						else if ("PauseTime".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.PAUSE_TIME = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
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
						else if("RestrictItems".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.RESTRICT_ITEMS = attrs.getNamedItem("val").getNodeValue();
						}
						else if ("RestrictSkills".equalsIgnoreCase(cd.getNodeName()))
						{
							attrs = cd.getAttributes();
							_config.RESTRICT_SKILLS = attrs.getNamedItem("val").getNodeValue();
						}
					}
					_configs.add(_config);
				}
			}

			_configs.sort();
			_log.info("Loaded " + _configs.size() + " configs for " + CtF_New.getInstance().getName() + " event.");
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error parsing CTF.xml, by error: ", e);
		}
	}
}