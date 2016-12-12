package l2r.gameserver.randoms;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ShowBoard;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Infern0
 *
 */
public class LogFileReader
{
	private static final Logger _log = LoggerFactory.getLogger(LogFileReader.class);
	
	private static StringBuilder sb;
	private static int LINES_PER_PAGE = 25;
	
	public static void buidLogInformation(String fileName, Player player, String search, int page)
	{
		if (readLog(fileName))
		{
			player.sendMessage("Log is empty or cannot generate information...");
			return;
		}
		
		sendLog(fileName, player, search, page);
	}
	
	private static boolean readLog(String fileName)
	{
		File file = new File(Config.DATAPACK_ROOT, "/log/" + fileName + ".log");
		if (!file.exists())
		{
			_log.error("LogFileReader: Cannot find " + fileName + "");
			return false;
		}
		
		sb = new StringBuilder();
		int linenumber = 0;
		LineNumberReader reader = null;
		try
		{
			reader = new LineNumberReader(new FileReader(file));
			String line = null;
			while((line = reader.readLine()) != null)
			{
				if (line.isEmpty())
					continue;
				
				linenumber++;
				// Lets not brake the server with too much log.
				if (linenumber > 10000)
					break;
				
				sb.append(line + "\n");
			}
		}
		catch(Exception e)
		{
			_log.error("Error while reading " + fileName + " file...", e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch (Exception e)
			{
			}
		}
		
		return sb.toString().isEmpty();
	}
	
	private static void sendLog(String fileName, Player player, String search, int page)
	{
		StringBuilder sbinfo = new StringBuilder();
		String htmltosend = HtmCache.getInstance().getNotNull("admin/logviewer.htm", player);
		
		String[] lines = sb.toString().split("\n");
		
		int pagen = 0;
		int currentpage = 0;
		for (int i = 0; i < lines.length; i++)
		{
			if (i % LINES_PER_PAGE == 0)
				pagen++;
			
			String line = lines[i];
			
			if (!search.isEmpty())
				if (!line.toLowerCase().contains(search.toLowerCase()))
					continue;
					
			if (pagen == page)
			{
				currentpage++;
				sbinfo.append("<tr><td>");
				sbinfo.append(line);
				sbinfo.append("</td></tr>");
			}
		}
		
		if(page == 1)
		{
			if(currentpage == 1)
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			else
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass -h admin_readlog " + fileName + " " + (page + 1) + " " + search + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			htmltosend = htmltosend.replaceAll("%back%", "&nbsp;");
		}
		else if(page > 1)
		{
			if(currentpage <= page)
			{
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass -h admin_readlog " + fileName + " " + (page - 1) + " " + search + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				htmltosend = htmltosend.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				htmltosend = htmltosend.replaceAll("%more%", "<button value=\"\" action=\"bypass -h admin_readlog " + fileName + " " + (page + 1) + " " + search + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				htmltosend = htmltosend.replaceAll("%back%", "<button value=\"\" action=\"bypass -h admin_readlog " + fileName + " " + (page - 1) + " " + search + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		String searchbutton = "";
		if (search.isEmpty())
			searchbutton = "&nbsp;";
		else
			searchbutton = "Search results for: <font color=LEVEL>" + search + "</font>";
		
		htmltosend = htmltosend.replace("%fileName%", fileName);
		htmltosend = htmltosend.replace("%searchresult%", searchbutton);
		htmltosend = htmltosend.replace("%body%", sbinfo.toString());
		ShowBoard.separateAndSend(htmltosend, player);
	}
}
