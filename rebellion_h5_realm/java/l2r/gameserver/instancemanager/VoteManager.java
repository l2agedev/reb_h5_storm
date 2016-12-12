package l2r.gameserver.instancemanager;

import l2r.commons.dbutils.DbUtils;
import l2r.commons.lang.ArrayUtils;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.database.mysql;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.utils.Util;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class VoteManager implements IVoicedCommandHandler
{
	private static class Vote
	{
		public boolean active;
		public String name;
		public int id;
		public int maxPerHWID;
		public TreeMap<Integer, String> variants = new TreeMap<Integer, String>();
		public Map<String, Integer[]> results = new HashMap<String, Integer[]>();
	}

	private static Map<Integer, Vote> Poll = new HashMap<Integer, Vote>();
	
	private static VoteManager _instance;
	
	public static VoteManager getInstance()
	{
		if(_instance == null)
			_instance = new VoteManager();
		return _instance;
	}

	public VoteManager()
	{
		if (Config.ENABLE_POLL_SYSTEM)
		{
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
			load();
		}
		else
		{
			VoicedCommandHandler.getInstance().removeVoicedCommandHandler(this);
			_log.info("PollManager: Disabled.");
		}
			
	}
	
	@SuppressWarnings("unchecked")
	private static boolean vote(String command, Player activeChar, String args)
	{
		String htmContent = HtmCache.getInstance().getNotNull("mods/poll/poll.htm", activeChar);
		
		if (!activeChar.hasHWID())
		{
			activeChar.sendMessage("You cannot vote...");
			return false;
		}
		
		if(args != null && !args.isEmpty()) // применение голоса
		{
			String[] param = args.split(" ");
			if(param.length >= 2 && Util.isNumber(param[0]) && Util.isNumber(param[1]))
			{
				String comment = "";
				
				if (param.length == 3)
					comment = param[2];
				
				if(comment.length() > 1000)
					comment = comment.substring(0, 1000); // max 1000 symbols in comment.
				
				String hwid = activeChar.getHWID();
				Vote v = Poll.get(Integer.parseInt(param[0]));
				if(v == null || !v.active)
					return false;
				int var = Integer.parseInt(param[1]);
				Integer[] alreadyResults = v.results.get(hwid);
				if(alreadyResults == null)
				{
					v.results.put(hwid, new Integer[] { var });
					mysql.set("INSERT IGNORE INTO vote (`id`, `HWID`, `vote`, `comment`) VALUES (?,?,?,?)", param[0], hwid, param[1], comment);
				}
				else if(alreadyResults.length < v.maxPerHWID)
				{
					for(int id : alreadyResults)
						if(id == var)
						{
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.instancemanager.votemanager.message1", activeChar));
							return false;
						}
					v.results.put(hwid, ArrayUtils.add(alreadyResults, var));
					mysql.set("INSERT IGNORE INTO vote (`id`, `HWID`, `vote`) VALUES (?,?,?,?)", param[0], hwid, param[1], comment);
				}
				else
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.instancemanager.votemanager.message2", activeChar));
					return false;
				}
			}
		}
		
		int count = 0;
		StringBuilder html = new StringBuilder();
		String hwid = activeChar.getHWID();
		for(Entry<Integer, Vote> e : Poll.entrySet())
			if(e.getValue().active)
			{
				count++;
				html.append("<br>");
				html.append("<center>");
				html.append("<font color=\"LEVEL\">Question</font><br>");
				html.append(e.getValue().name).append(":<br>");
				Integer[] already = e.getValue().results.get(hwid);
				Entry<Integer, String>[] variants = new Entry[e.getValue().variants.size()];
				
				if(already != null && already.length >= e.getValue().maxPerHWID)
				{
					html.append("You have already voted in this poll.<br>");
					
					for(Entry<Integer, String> variant : e.getValue().variants.entrySet())
					{
						html.append(variant.getValue() + " - " + getvoteresut(e.getValue().id, variant.getKey()) + " <br1>");
					}
				}	
				else
				{
					int i = 0;
					for(Entry<Integer, String> variant : e.getValue().variants.entrySet())
					{
						variants[i] = variant;
						i++;
					}
					shuffle(variants); // do we need to shuffle the answers ?

					variants: for(Entry<Integer, String> variant : variants)
					{
						if(already != null)
							for(Integer et : already)
								if(et.equals(variant.getKey()))
									continue variants;
						if (variant.getKey() == 1)
						{
							html.append("<button value=\"" + variant.getValue() + "\" action=\"bypass -h user_poll " + e.getValue().id + " " + variant.getKey() + " $comment\" width=\"120\" height=\"16\" back=\"L2UI_CT1.Button_DF_Calculator\" fore=\"L2UI_CT1.Button_DF_Calculator\"><br>");
						}
							
						else
						{
							html.append("<button value=\"" + variant.getValue() + "\" action=\"bypass -h user_poll " + e.getValue().id + " " + variant.getKey() + " $comment\" width=\"120\" height=\"16\" back=\"L2UI_CT1.Button_DF_Calculator\" fore=\"L2UI_CT1.Button_DF_Calculator\"><br>");
						}
					}
					html.append("Write a comment<br>");
					html.append("<MultiEdit var =\"comment\" width=250 height=40>");
					html.append("<br1>");
					
					for(Entry<Integer, String> variant : variants)
					{
						html.append(variant.getValue() + " - " + getvoteresut(e.getValue().id, variant.getKey()) + " <br1>");
					}
					
					html.append("<br1>");
				}
			}
		if(count == 0)
			html.append("No active poll.");
		
		html.append("</center>");
		
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
		npcHtmlMessage.setHtml(htmContent);
		npcHtmlMessage.replace("%body%", html.toString());
		activeChar.sendPacket(npcHtmlMessage);

		return true;
	}

	private static void shuffle(Entry<Integer, String>[] array)
	{
		int j;
		Entry<Integer, String> tmp;
		// i is the number of items remaining to be shuffled.
		for(int i = array.length; i > 1; i--)
		{
			// Pick a random element to swap with the i-th element.
			j = Rnd.get(i); // 0 <= j <= i-1 (0-based array)
			// Swap array elements.
			tmp = array[j];
			array[j] = array[i - 1];
			array[i - 1] = tmp;
		}
	}

	public void load()
	{
		Poll.clear();

		loadXML();
		loadVotes();
		
		_log.info("PollManager: Loaded.");
	}
	
	private static int getvoteresut(int pollId, int voteVar)
	{
		int votesCount = 0;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT COUNT(*) FROM vote WHERE vote=? AND id=?");
			statement.setInt(1, voteVar);
			statement.setInt(2, pollId);
			rset = statement.executeQuery();
			if(rset.next())
				votesCount = rset.getInt(1);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return votesCount;
	}
	
	public boolean canVote(String hwid)
	{
		for(Entry<Integer, Vote> results : Poll.entrySet())
		{
			if(results.getValue().active)
			{
				Integer[] playerHWID = results.getValue().results.get(hwid);
				int maxAnswersPerAcc = results.getValue().maxPerHWID;
				if (playerHWID == null)
					return true;
				else if (playerHWID != null && playerHWID.length < maxAnswersPerAcc)
					return true;
				else if (playerHWID != null && playerHWID.length >= maxAnswersPerAcc)
					return false;
			}
		}
		
		return false;
	}
	
	public String pollTopic()
	{
		for(Entry<Integer, Vote> results : Poll.entrySet())
		{
			if(!results.getValue().active)
				return results.getValue().name;
		}
		
		return "";
	}
	
	public boolean pollisActive()
	{
		for(Entry<Integer, Vote> results : Poll.entrySet())
			if(results.getValue().active)
				return true;
		
		return false;
	}
	
	public static void loadXML()
	{
		// грузим голосования
		try
		{
			File file = new File(Config.DATAPACK_ROOT, "config/poll.xml");
			DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
			factory2.setValidating(false);
			factory2.setIgnoringComments(true);
			Document doc2 = factory2.newDocumentBuilder().parse(file);
			
			for (Node n2 = doc2.getFirstChild(); n2 != null; n2 = n2.getNextSibling())
				if ("list".equalsIgnoreCase(n2.getNodeName()))
					for (Node d2 = n2.getFirstChild(); d2 != null; d2 = d2.getNextSibling())
						if ("vote".equalsIgnoreCase(d2.getNodeName()))
						{
							Vote v = new Vote();
							v.id = Integer.parseInt(d2.getAttributes().getNamedItem("id").getNodeValue());
							v.maxPerHWID = Integer.parseInt(d2.getAttributes().getNamedItem("maxPerHWID").getNodeValue());
							v.name = d2.getAttributes().getNamedItem("name").getNodeValue();
							v.active = Boolean.parseBoolean(d2.getAttributes().getNamedItem("active").getNodeValue());
							
							for (Node i = d2.getFirstChild(); i != null; i = i.getNextSibling())
								if ("variant".equalsIgnoreCase(i.getNodeName()))
									v.variants.put(Integer.parseInt(i.getAttributes().getNamedItem("id").getNodeValue()), i.getAttributes().getNamedItem("desc").getNodeValue());
							
							Poll.put(v.id, v);
						}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void loadVotes()
	{
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("SELECT * FROM vote");
			rs = st.executeQuery();
			while (rs.next())
			{
				Vote v = Poll.get(rs.getInt("id"));
				if (v != null)
				{
					String HWID = rs.getString("HWID");
					Integer[] rez = v.results.get(HWID);
					v.results.put(HWID, ArrayUtils.add(rez, rs.getInt("vote")));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, st, rs);
		}
	}

	private String[] _commandList = new String[] { "poll" };

	public void sendPoll(Player player)
	{
		 vote(null, player, null);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(command.equalsIgnoreCase("poll"))
			return vote(command, activeChar, args);
		return false;
	}
}