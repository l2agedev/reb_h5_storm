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
import l2r.gameserver.data.htm.HtmCache;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

/**
 * @author Nik, Infern0<br>
 */
public class WebForum
{
	private static Logger _log = Logger.getLogger(WebForum.class.getName());
	
	private static Map<Integer, ForumTopic> _allTopics = new FastMap<>();
	public static String CHANGELOG_HTML = "<html><body>No data.</body></html>";
	public static String ANNOUNCEMENTS_HTML = "<html><body>No data.</body></html>";
	
	private static String CHANGELOG_URL = Config.FORUM_URL_TO_LEECH_CHANGELOG;
	private static String ANNOUNCEMENT_URL = Config.FORUM_URL_TO_LEECH_ANNOUNCE;
	
	static
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				updateChangelogData();
				updateAnnouncementsData();
			}
		}, 1000, 30 * 60000);
	}
	
	public static void updateChangelogData()
	{
		List<ForumTopic> changelogTopics = getTopics(CHANGELOG_URL);
		StringBuilder sb = new StringBuilder();
		
		for (ForumTopic topic : changelogTopics)
		{
			sb.append("<table  width=610 height=46>");
			sb.append("<tr>");
			sb.append("<td width=36 align=\"center\"><img src=\"l2ui.bbs_board\" width=32 height=32></td>");
			sb.append("<td width=36 align=\"center\"><a action=\"bypass -h scripts_services.WebLeech:change viewforumtopic " + topic.topicName.hashCode() + "\">View</a></td>");
			sb.append("<td width=350 align=\"center\"><font color=\"FF9900\"> " + topic.topicName + " </font></td>");
			sb.append("<td width=100 align=\"center\">" + topic.topicDate + "</td>");
			sb.append("</tr>");
			sb.append("</table>");
		}
		
		CHANGELOG_HTML = HtmCache.getInstance().getNullable(Config.BBS_HOME_DIR + "pages/WebLeech/forumChangelog.htm", null);
		CHANGELOG_HTML = CHANGELOG_HTML.replaceAll("%topics%", sb.toString());
	}
	
	public static void updateAnnouncementsData()
	{
		List<ForumTopic> announcementsTopics = getTopics(ANNOUNCEMENT_URL);
		StringBuilder sb = new StringBuilder();
		
		for (ForumTopic topic : announcementsTopics)
		{
			sb.append("<table  width=610 height=46>");
			sb.append("<tr>");
			sb.append("<td width=36 align=\"center\"><img src=\"l2ui.bbs_board\" width=32 height=32></td>");
			sb.append("<td width=36 align=\"center\"><a action=\"bypass -h scripts_services.WebLeech:change viewforumtopic " + topic.topicName.hashCode() + "\">View</a></td>");
			sb.append("<td width=350 align=\"center\"><font color=\"FF9900\"> " + topic.topicName + " </font></td>");
			sb.append("<td width=100 align=\"center\">" + topic.topicDate + "</td>");
			sb.append("</tr>");
			sb.append("</table>");
		}
		
		ANNOUNCEMENTS_HTML = HtmCache.getInstance().getNullable(Config.BBS_HOME_DIR + "pages/WebLeech/forumAnnouncements.htm", null);
		ANNOUNCEMENTS_HTML = ANNOUNCEMENTS_HTML.replaceAll("%topics%", sb.toString());
	}
	
	/**
	 * 
	 * @param topicNameHashCode : the hash code of the topic's name.
	 * @return a ready to send html-string of the topic's content.
	 */
	public static String getTopicContent(int topicNameHashCode)
	{
		String toReturn = HtmCache.getInstance().getNullable(Config.BBS_HOME_DIR + "pages/WebLeech/forumTopicContent.htm", null);
		
		ForumTopic topic = _allTopics.get(topicNameHashCode);
		String topicContent = topic.topicContent;
		topicContent = topicContent.replace("<br />", "");
		
		toReturn = toReturn.replaceAll("%content%", topicContent);
		toReturn = toReturn.replaceAll("%topicName%", topic.topicName);
		toReturn = toReturn.replaceAll("%topicDate%", topic.topicDate);
		return toReturn;
	}
	
	/**
	 * 
	 * @param url
	 * @return list of ForumTopics found on the given URL
	 */
	private static ArrayList<ForumTopic> getTopics(String url)
	{
		ArrayList<ForumTopic> topics = new ArrayList<>();
		try
		{
			URL pageToDisplay = new URL(url);
			URLConnection conn = pageToDisplay.openConnection();
			//Disguise the web connection type from java to mozilla conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			conn.connect();
			
			InputStream in = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			long timeout = System.currentTimeMillis() + 15000000; // 15sec timeout.
			while(timeout > System.currentTimeMillis())
			{
				String newLine = br.readLine();
				if (newLine == null)
					break;
				
				if (newLine.contains("class='topic_title'")) // this is surely a new topic
				{
					int topicLinkIndex = newLine.indexOf("href=\"");
					int topicDateIndex = newLine.indexOf("title='View topic, started");
					int topicNameIndex = newLine.indexOf("class='topic_title'>");
					
					ForumTopic topic = new ForumTopic();
					// Highly order-dependant. Be careful!
					topic.topicLink = newLine.substring(topicLinkIndex + 6, topicDateIndex - 2).trim();
					topic.topicDate = newLine.substring(topicDateIndex + 26, topicNameIndex -2).trim();
					topic.topicName = newLine.substring(topicNameIndex + 20, newLine.indexOf("</a></h4>")).trim();
					topic.topicContent = getTopicContent(topic.topicLink);
					
					topic.topicName = topic.topicName.replaceAll("&#33;", " ");
					topic.topicName = topic.topicName.replaceAll("&#33;", "!");
					topic.topicName = topic.topicName.replaceAll("&#34;", "!");
					topic.topicName = topic.topicName.replaceAll("&#35;", "#");
					topic.topicName = topic.topicName.replaceAll("&#36;", "$");
					topic.topicName = topic.topicName.replaceAll("&#37;", "%");
					topic.topicName = topic.topicName.replaceAll("&#38;", "&");
					topic.topicName = topic.topicName.replaceAll("&#39;", "'");
					topic.topicName = topic.topicName.replaceAll("&#40;", "(");
					topic.topicName = topic.topicName.replaceAll("&#41;", ")");
					topic.topicName = topic.topicName.replaceAll("&#42;", "*");
					topic.topicName = topic.topicName.replaceAll("&#43;", "+");
					topic.topicName = topic.topicName.replaceAll("&#44;", ",");
					topic.topicName = topic.topicName.replaceAll("&#45;", "-");
					topic.topicName = topic.topicName.replaceAll("&#46;", ".");
					topic.topicName = topic.topicName.replaceAll("&#46;", "/");
					topic.topicName = topic.topicName.replaceAll("&#58;", ":");
					topic.topicName = topic.topicName.replaceAll("&#59;", ";");
					topic.topicName = topic.topicName.replaceAll("&#60;", "<");
					topic.topicName = topic.topicName.replaceAll("&#61;", "=");
					topic.topicName = topic.topicName.replaceAll("&#62;", ">");
					topic.topicName = topic.topicName.replaceAll("&#63;", "?");
					topic.topicName = topic.topicName.replaceAll("&#64;", "@");
					topic.topicName = topic.topicName.replaceAll("&#91;", "[");
					topic.topicName = topic.topicName.replaceAll("&#92;", "/");
					topic.topicName = topic.topicName.replaceAll("&#93;", "]");
					topic.topicName = topic.topicName.replaceAll("&#94;", "^");
					topic.topicName = topic.topicName.replaceAll("&#95;", "_");
					topic.topicName = topic.topicName.replaceAll("&#96;", "`");
					topic.topicName = topic.topicName.replaceAll("&#123;", "{");
					topic.topicName = topic.topicName.replaceAll("&#124;", "|");
					topic.topicName = topic.topicName.replaceAll("&#125;", "}");
					topic.topicName = topic.topicName.replaceAll("&#126;", "~");
					
					topics.add(topic);
					_allTopics.put(topic.topicName.hashCode(), topic);
				}
			}
			br.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error while reading webpage: ", e);
		}
		
		return topics;
	}
	
	/**
	 * Shows the given forum topic's 1st post.
	 * @param url - the website URL which leads to this topic.
	 * @return 
	 */
	@SuppressWarnings("unused")
	private static String getTopicContent(String url)
	{
		try
		{
			String str = "";
			
			URL pageToDisplay = new URL(url);
			URLConnection conn = pageToDisplay.openConnection();
			conn.connect();
			
			InputStream in = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			long timeout = System.currentTimeMillis() + 15000; // 15sec timeout.
			while(true)//timeout > System.currentTimeMillis())
			{
				String newLine = br.readLine();
				if (newLine == null)
					break;
				
				if (newLine.contains("<div itemprop=\"commentText\" class='post entry-content '>")) // this is surely a post
				{
					while (!newLine.contains("</div>"))// && timeout > System.currentTimeMillis())
					{
						newLine = br.readLine();
						str += newLine;
					}
					
					// Remove HTML comments, replace the BRs' <> with %%
					str = str.substring(str.indexOf("-->") + 3);
					str = str.replaceAll("<br />", "\n");
					// Remove the unwanted, unsupported HTML tags
					while (str.contains("<") && str.contains(">"))// && timeout > System.currentTimeMillis())
					{
						int startIndex = str.indexOf("<");
						int endIndex = str.indexOf(">") + 1;
						//str = str.replaceAll(str.substring(startIndex, endIndex), "");
						String temp = "";
						temp = str.substring(0, startIndex);
						temp += str.substring(endIndex);
						str = temp;
					}
					
					break; // Break, we need only the 1st post, which contains the changelog material.
				}
			}
			br.close();
			
			// Parse the forum topic content into L2 HTML content
			str = str.replaceAll("&#33;", " ");
			str = str.replaceAll("&#33;", "!");
			str = str.replaceAll("&#34;", "!");
			str = str.replaceAll("&#35;", "#");
			str = str.replaceAll("&#36;", "$");
			str = str.replaceAll("&#37;", "%");
			str = str.replaceAll("&#38;", "&");
			str = str.replaceAll("&#39;", "'");
			str = str.replaceAll("&#40;", "(");
			str = str.replaceAll("&#41;", ")");
			str = str.replaceAll("&#42;", "*");
			str = str.replaceAll("&#43;", "+");
			str = str.replaceAll("&#44;", ",");
			str = str.replaceAll("&#45;", "-");
			str = str.replaceAll("&#46;", ".");
			str = str.replaceAll("&#46;", "/");
			str = str.replaceAll("&#58;", ":");
			str = str.replaceAll("&#59;", ";");
			str = str.replaceAll("&#60;", "<");
			str = str.replaceAll("&#61;", "=");
			str = str.replaceAll("&#62;", ">");
			str = str.replaceAll("&#63;", "?");
			str = str.replaceAll("&#64;", "@");
			str = str.replaceAll("&#91;", "[");
			str = str.replaceAll("&#92;", "/");
			str = str.replaceAll("&#93;", "]");
			str = str.replaceAll("&#94;", "^");
			str = str.replaceAll("&#95;", "_");
			str = str.replaceAll("&#96;", "`");
			str = str.replaceAll("&#123;", "{");
			str = str.replaceAll("&#124;", "|");
			str = str.replaceAll("&#125;", "}");
			str = str.replaceAll("&#126;", "~");
			
			str = str.replaceAll("\n", "<br>");
			
			return str;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error while reading webpage: ", e);
		}
		
		return "Unable to load content.";
	}
	
	public static class ForumTopic
	{
		String topicName = "";
		String topicDate = "";
		String topicLink = "";
		String topicContent = "Content is missing.";
		public ForumTopic() {}
		
		@Override
		public String toString()
		{
			return "Name: " + topicName + " Date: " + topicDate + " Link: " + topicLink + " Content: " + topicContent;
		}
	}
}
