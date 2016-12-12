package services.community;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.scripts.ScriptFile;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CommunityNews implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityNews.class);
	
	private long cache;
	
	private List<NewsData> news_data = new ArrayList<>();
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_bbs_news"
		};
	}
	
	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		st.nextToken();
		st.nextToken();
		if (!st.hasMoreTokens())
		{
			player.unsetVar("CommPageArhive");
			ShowBoard.separateAndSend(getMain(), player);
		}
		else
		{
			switch (st.nextToken())
			{
				case "arhive":
					int page = Integer.parseInt(st.nextToken());
					player.setVar("CommPageArhive", page, -1);
					ShowBoard.separateAndSend(getArhive(page), player);
					break;
				case "show":
					ShowBoard.separateAndSend(showNews(st.nextToken()), player);
					break;
				case "return":
					if (player.getVarInt("CommPageArhive") == 0)
						ShowBoard.separateAndSend(getMain(), player);
					else
						ShowBoard.separateAndSend(getArhive(player.getVarInt("CommPageArhive")), player);
					break;
			}
		}
	}
	
	public String getTitle(String action)
	{
		String title = "";
		switch (action)
		{
			case "arhive":
				title = "Старые новости";
				break;
			case "show":
				title = "Просмотр новости";
				break;
			case "main":
				title = "Просмотр новости";
				break;
		}
		return "Community News - " + title;
	}
	
	public String getMain()
	{
		cache();
		
		String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/news/main.htm", null);
		NewsData nd = news_data.get(0);
		
		if (nd == null)
		{
			html = html.replace("{title}", "Новостей пока нет");
			html = html.replace("{description}", "На текущий момент у нашего проекта нет новостей");
			html = html.replace("{button}", "");
		}
		else
		{
			html = html.replace("{title}", nd.title);
			html = html.replace("{description}", nd.description.length() > (250 - 3) ? nd.description.substring(250) : (nd.description + " ..."));
			html = html.replace("{button}", "<button value=\"Читать далее\" action=\"bypass _bbs_news_show_" + 0 + "\" width=90 height=25 back=\"L2UI_CT1.button_df\" fore=\"L2UI_CT1.Button_DF\"/>");
		}
		
		return html;
	}
	
	public String getArhive(int page)
	{
		cache();
		
		StringBuilder sb = new StringBuilder();
		int limit = 5; //TODO limit Config.COMMUNITY_NEWS_LIMIT
		
		String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/news/item.htm", null);
		int i = 1;
		int begin = page * limit - limit;
		int end = page * limit;
		
		for (NewsData nd : news_data)
		{
			if (i <= begin || i >= end)
			{
				i++;
				continue;
			}
			
			String description = (nd.description.length() > (230 - 3) ? nd.description.substring(0, 230) + "..." : nd.description);
			String button = "<button value=\"Continue Reading...\" action=\"bypass _bbs_news_show_" + (i - 1) + "\" width=120 height=25 back=\"L2UI_CT1.button_df\" fore=\"L2UI_CT1.Button_DF\"/>";
			
			sb.append(html.replace("{title}", nd.title).replace("{description}", description).replace("{button}", button));
			i++;
		}
		
		int num = (int) ((Math.ceil(news_data.size() / limit) < (news_data.size() / limit)) ? (Math.ceil(news_data.size() / limit) + 1) : Math.ceil(news_data.size() / limit));
		
		String pages_item = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/news/pages_item.htm", null);
		StringBuilder pages_sb = new StringBuilder();
		for (int j = 1; j <= num; j++)
		{
			if (page == j)
			{
				pages_sb.append(pages_item.replace("{url}", "<button value=\"" + j + "\" action=\"bypass _bbs_news_arhive_" + j + "\" width=27 height=25 back=\"L2UI_CT1.button_df\" fore=\"L2UI_CT1.button_df\"/>"));
			}
			else
			{
				pages_sb.append(pages_item.replace("{url}", "<button value=\"" + j + "\" action=\"bypass _bbs_news_arhive_" + j + "\" width=27 height=25 back=\"L2UI_CT1.button_df\" fore=\"L2UI_CT1.button_df\"/>"));
			}
		}
		return HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/news/arhive.htm", null).replace("{items}", sb.toString()).replace("{pages}", HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/news/pages.htm", null).replace("{items}", pages_sb.toString()));
	}
	
	public String showNews(String id)
	{
		cache();
		
		int news_id = Integer.parseInt(id);
		
		String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/news/show.htm", null);
		NewsData nd = news_data.get(news_id);
		
		if (nd == null)
		{
			html = html.replace("{title}", "Новостей пока нет");
			html = html.replace("{description}", "На текущий момент у нашего проекта нет новостей");
		}
		else
		{
			html = html.replace("{title}", nd.title);
			html = html.replace("{description}", nd.description);
		}
		
		return html;
	}
	
	private void cache()
	{
		if (cache <= System.currentTimeMillis())
		{
			load();
			cache = System.currentTimeMillis() + (60 * 1000); //Config.COMMUNITY_NEWS_CACHE in sec
		}
	}
	
	private void load()
	{
		news_data.clear();
		try
		{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			URL rssURL = new URL("http://lineage-realm.net/forum/index.php?forums/-/index.rss"); //Config.COMMUNITY_NEWS_URL) url
			Document doc = builder.parse(rssURL.openStream());
			
			NodeList items = doc.getElementsByTagName("item");
			
			for (int i = 0; i < items.getLength(); i++)
			{
				Element item = (Element) items.item(i);
				String title = getValue(item, "title");
				String description = getValue(item, "content:encoded"); // TODO Config.COMMUNITY_NEWS_ENGINE.equalsIgnoreCase("XenForo") ? getValue(item, "content:encoded") : getValue(item, "description");
				news_data.add(new NewsData(title, stripTags(description)));
			}
		}
		catch (Exception ex)
		{
			_log.error("CommunityCustom->News->Error: ", ex);
		}
	}
	
	public String stripTags(String str)
	{
		str = str.replace("<p>&nbsp;</p>", "");
		str = str.replace("<p>", "");
		str = str.replace("<br>", "%br1%");
		str = str.replace("</p>", "%br%");
		
		str = str.replaceAll("<(.)+?>", "");
		str = str.replaceAll("<(\n)+?>", "");
		
		str = str.replace("%br1%", "<br1>");
		str = str.replace("%br%", "<br>");
		
		str = str.replaceAll("&#033;", " ");
		str = str.replaceAll("&#033;", "!");
		str = str.replaceAll("&#034;", "!");
		str = str.replaceAll("&#035;", "#");
		str = str.replaceAll("&#036;", "$");
		str = str.replaceAll("&#037;", "%");
		str = str.replaceAll("&#038;", "&");
		str = str.replaceAll("&#039;", "'");
		str = str.replaceAll("&#040;", "(");
		str = str.replaceAll("&#041;", ")");
		str = str.replaceAll("&#042;", "*");
		str = str.replaceAll("&#043;", "+");
		str = str.replaceAll("&#044;", ",");
		str = str.replaceAll("&#045;", "-");
		str = str.replaceAll("&#046;", ".");
		str = str.replaceAll("&#046;", "/");
		str = str.replaceAll("&#058;", ":");
		str = str.replaceAll("&#059;", ";");
		str = str.replaceAll("&#060;", "<");
		str = str.replaceAll("&#061;", "=");
		str = str.replaceAll("&#062;", ">");
		str = str.replaceAll("&#063;", "?");
		str = str.replaceAll("&#064;", "@");
		str = str.replaceAll("&#091;", "[");
		str = str.replaceAll("&#092;", "/");
		str = str.replaceAll("&#093;", "]");
		str = str.replaceAll("&#094;", "^");
		str = str.replaceAll("&#095;", "_");
		str = str.replaceAll("&#096;", "`");
		str = str.replaceAll("&#123;", "{");
		str = str.replaceAll("&#124;", "|");
		str = str.replaceAll("&#125;", "}");
		str = str.replaceAll("&#126;", "~");
		
		return str;
	}
	
	public String getValue(Element parent, String nodeName)
	{
		try
		{
			return parent.getElementsByTagName(nodeName).item(0).getFirstChild().getNodeValue();
		}
		catch (Exception e)
		{
			return "";
		}
	}
	
	class NewsData
	{
		
		public String title;
		public String description;
		
		public NewsData(String title, String description)
		{
			this.title = title;
			this.description = description;
		}
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
	
	@Override
	public void onLoad()
	{
		CommunityBoardManager.getInstance().registerHandler(this);
	}
	
	@Override
	public void onReload()
	{
		
	}
	
	@Override
	public void onShutdown()
	{
		
	}
}
