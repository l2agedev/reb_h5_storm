package l2r.gameserver.network.serverpackets;

import l2r.gameserver.Config;
import l2r.gameserver.cache.ImagesChache;
import l2r.gameserver.data.StringHolder;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.Scripts;
import l2r.gameserver.scripts.Scripts.ScriptClassAndMethod;
import l2r.gameserver.utils.HtmlUtils;
import l2r.gameserver.utils.Language;
import l2r.gameserver.utils.Strings;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the HTML parser in the client knowns these standard and non-standard tags and attributes
 * VOLUMN
 * UNKNOWN
 * UL
 * U
 * TT
 * TR
 * TITLE
 * TEXTCODE
 * TEXTAREA
 * TD
 * TABLE
 * SUP
 * SUB
 * STRIKE
 * SPIN
 * SELECT
 * RIGHT
 * PRE
 * P
 * OPTION
 * OL
 * MULTIEDIT
 * LI
 * LEFT
 * INPUT
 * IMG
 * I
 * HTML
 * H7
 * H6
 * H5
 * H4
 * H3
 * H2
 * H1
 * FONT
 * EXTEND
 * EDIT
 * COMMENT
 * COMBOBOX
 * CENTER
 * BUTTON
 * BR
 * BODY
 * BAR
 * ADDRESS
 * A
 * SEL
 * LIST
 * VAR
 * FORE
 * READONL
 * ROWS
 * VALIGN
 * FIXWIDTH
 * BORDERCOLORLI
 * BORDERCOLORDA
 * BORDERCOLOR
 * BORDER
 * BGCOLOR
 * BACKGROUND
 * ALIGN
 * VALU
 * READONLY
 * MULTIPLE
 * SELECTED
 * TYP
 * TYPE
 * MAXLENGTH
 * CHECKED
 * SRC
 * Y
 * X
 * QUERYDELAY
 * NOSCROLLBAR
 * IMGSRC
 * B
 * FG
 * SIZE
 * FACE
 * COLOR
 * DEFFON
 * DEFFIXEDFONT
 * WIDTH
 * VALUE
 * TOOLTIP
 * NAME
 * MIN
 * MAX
 * HEIGHT
 * DISABLED
 * ALIGN
 * MSG
 * LINK
 * HREF
 * ACTION
 * ClassId
 * fstring
 */
public class NpcHtmlMessage extends L2GameServerPacket
{
	protected static final Logger _log = LoggerFactory.getLogger(NpcHtmlMessage.class);
	protected static final Pattern objectId = Pattern.compile("%objectId%");
	protected static final Pattern playername = Pattern.compile("%playername%");

	protected int _npcObjId;
	protected String _html;
	protected String _file = null;
	protected List<String> _replaces = new ArrayList<String>();
	protected boolean have_appends = false;

	public NpcHtmlMessage(Player player, int npcId, String filename, int val)
	{
		List<ScriptClassAndMethod> appends = Scripts.dialogAppends.get(npcId);
		if(appends != null && appends.size() > 0)
		{
			have_appends = true;
			if(filename != null && filename.equalsIgnoreCase("npcdefault.htm"))
				setHtml(""); // контент задается скриптами через DialogAppend_
			else
				setFile(filename);

			String replaces = "";

			// Добавить в конец странички текст, определенный в скриптах.
			Object[] script_args = new Object[] { new Integer(val) };
			for(ScriptClassAndMethod append : appends)
			{
				Object obj = Scripts.getInstance().callScripts(player, append.className, append.methodName, script_args);
				if(obj != null)
					replaces += obj;
			}

			if(!replaces.equals(""))
				replace("</body>", "\n" + Strings.bbParse(replaces) + "</body>");
		}
		else
			setFile(filename);
	}

	public NpcHtmlMessage(Player player, NpcInstance npc, String filename, int val)
	{
		this(player, npc.getNpcId(), filename, val);

		_npcObjId = npc.getObjectId();

		player.setLastNpc(npc);

		replace("%npcId%", String.valueOf(npc.getNpcId()));
		replace("%npcname%", npc.getName());
		replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStr());
	}

	public NpcHtmlMessage(Player player, NpcInstance npc)
	{
		if(npc == null)
		{
			_npcObjId = 5;
			player.setLastNpc(null);
		}
		else
		{
			_npcObjId = npc.getObjectId();
			player.setLastNpc(npc);
		}
	}

	public NpcHtmlMessage(int npcObjId)
	{
		_npcObjId = npcObjId;
	}

	public final NpcHtmlMessage setHtml(String text)
	{
		if(!text.contains("<html>"))
			text = "<html><body>" + text + "</body></html>"; //<title>Message:</title> <br><br><br>
		_html = text;
		return this;
	}

	public final NpcHtmlMessage setFile(String file)
	{
		_file = file;
		if(_file.startsWith("data/html/"))
		{
			_log.info("NpcHtmlMessage: need fix : " + file, new Exception());
			_file = _file.replace("data/html/", "");
		}
		return this;
	}

	public NpcHtmlMessage replace(String pattern, String value)
	{
		if(pattern == null || value == null)
			return this;
		_replaces.add(pattern);
		_replaces.add(value);
		return this;
	}
	
	public NpcHtmlMessage replace(String pattern, Object value)
	{
		return replace(pattern, value.toString());
	}

	public NpcHtmlMessage replaceNpcString(String pattern, NpcString npcString, Object... arg)
	{
		if(pattern == null)
			return this;
		if(npcString.getSize() != arg.length)
			throw new IllegalArgumentException("Not valid size of parameters: " + npcString);

		_replaces.add(pattern);
		_replaces.add(HtmlUtils.htmlNpcString(npcString, arg));
		return this;
	}

	@Override
	protected void writeImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(_file != null)
		{
			Functions.sendDebugMessage(player, "HTML: " + _file);
			String content = HtmCache.getInstance().getNotNull(_file, player);
			String content2 = HtmCache.getInstance().getNullable(_file, player);
			if(content2 == null)
				setHtml(have_appends && _file.endsWith(".htm") ? "" : content);
			else
				setHtml(content);
		}

		for(int i = 0; i < _replaces.size(); i += 2)
			_html = _html.replace(_replaces.get(i), _replaces.get(i + 1));

		if(_html == null)
			return;

		Matcher m = objectId.matcher(_html);
		if(m != null)
			_html = m.replaceAll(String.valueOf(_npcObjId));

		_html = playername.matcher(_html).replaceAll(player.getName());

		player.cleanBypasses(false);
		_html = player.encodeBypasses(_html, false);

		m = ImagesChache.HTML_PATTERN.matcher(_html);
		
		while (m.find())
		{
			String imageName = m.group(1);
			int imageId = ImagesChache.getInstance().getImageId(imageName);
			_html = _html.replaceAll("%image:" + imageName + "%", "Crest.crest_" + player.getServerId() + "_" + imageId);
			byte[] image = ImagesChache.getInstance().getImage(imageId);
			if (image != null)
				player.sendPacket(new PledgeCrest(imageId, image));
		}
		
		// include шаблонов
		String path_file_community = "scripts/services/CommunityBoard/pages/";
		Pattern pi = Pattern.compile("\\%include\\(([^\\)]+)\\)\\%");
		m = pi.matcher(_html);
		Language lang = player.getLanguage();
		if (lang == null)
			lang = Language.ENGLISH;
		
		while (m.find())
		{
			if (new File(Config.DATAPACK_ROOT, "data/html/html-" + lang.getShortName() + "/" + path_file_community + m.group(1)).exists() == false)
				_html = _html.replace(m.group(0), "<font color=\"FF0000\">Page " + m.group(1) + " was not found!</font>");
			else
				_html = _html.replace(m.group(0), HtmCache.getInstance().getNotNull(path_file_community + m.group(1), player));
		}
		
		// string в шаблонов
		Pattern ps = Pattern.compile("\\%string\\(([^\\)]+)\\)\\%");
		Matcher ms = ps.matcher(_html);
		while (ms.find())
		{
			_html = _html.replace(ms.group(0), StringHolder.getInstance().getNotNull(player, ms.group(1)));
		}
		
		// include class object и преобразование типа данных
		// %object(Config,BBS_FOLDER)%
		Pattern po = Pattern.compile("\\%object\\(([^\\)]+),([^\\)]+)\\)\\%");
		Matcher mo = po.matcher(_html);
		Object c = null;
		while (mo.find())
		{
			// Если свойство Config - сразу вытягиваем значение
			if (mo.group(1).equals("Config"))
			{
				_html = _html.replace(mo.group(0), Config.getField(mo.group(2)));
				// если метод не предопределен, создаем экземляр класса и обращаемся к свойству
			}
			else
			{
				try
				{
					c = Class.forName(mo.group(1)).newInstance();
					Field field = c.getClass().getField(mo.group(2));
					_html = _html.replace(mo.group(0), field.get(c).toString());
				}
				catch (InstantiationException e)
				{
					e.printStackTrace();
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
				catch (NoSuchFieldException e)
				{
					e.printStackTrace();
				}
				catch (SecurityException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		writeC(0x19);
		writeD(_npcObjId);
		writeS(_html);
		writeD(0x00);
	}
	
	public String getText()
	{
		return _html;
	}
	
	public static String title(String title, String subTitle)
	{
		return "<html><title>" + title + "</title><body><center><br>" + 
			"<b><font color=ffcc00>" + subTitle + "</font></b>" + 
			"<br><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br></center>";
	}
	
	public static String title2(String title, String subTitle)
	{
		return "<html><title>" + title + "</title><body><center><br>";
	}
	
	public static String footer(String name, String version)
	{
		return "<br><center><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br>" + 
		"<br></center></body></html>";
	}
	
	public static String footer(String footer)
	{
		return "<br><center><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br>" + 
		"<br><font color=\"303030\">" + footer + "</font></center></body></html>";
	}
	
	public static String footer()
	{
		return "<br><center><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br>" + 
		"<br></center></body></html>";
	}
	
	/**
	 * @param type : 0 - Normal, 1 - Yellow, 2 - Blue, 3 - Green, 4 - Red, 5 - Purple, 6 - Grey
	 * @param revert : revert ? fore : back
	 * @return 	return "[button value=value action="bypass -h Quest questName questEvent" width=width height=height back=type fore=type]";
	 */
	public static String questButton(String questName, String value, String questEvent, int width, int height, int type, boolean revert)
	{
		String back = getButtonType(type, !revert);
		String fore = getButtonType(type, revert);
		return "<button value=\"" + value + "\" action=\"bypass -h Quest " + questName + " " + questEvent + "\" " + 
			"width=\"" + Integer.toString(width) + "\" height=\"" + Integer.toString(height) + "\" " + 
			"back=\"" + back + "\" fore=\"" + fore + "\">";
	}
	
	/**
	 * @param type : 0 - Normal, 1 - Yellow, 2 - Blue, 3 - Green, 4 - Red, 5 - Purple, 6 - Grey
	 * @param revert : revert ? fore : back
	 * @return 	return "[button value=value action="bypass -h Quest questName questEvent" width=width height=height back=type fore=type]";
	 */
	public static String bypassButton(String bypass, String displayName, int width, int height, int type, boolean revert)
	{
		String back = getButtonType(type, !revert);
		String fore = getButtonType(type, revert);
		return "<button value=\"" + displayName + "\" action=\"bypass -h " + bypass + "\" " + 
			"width=\"" + Integer.toString(width) + "\" height=\"" + Integer.toString(height) + "\" " + 
			"back=\"" + back + "\" fore=\"" + fore + "\">";
	}
	
	private static String getButtonType(int type, boolean back)
	{
		switch (type)
		{
			case 0: // Normal
				if (back) return "L2UI_ct1.button_df";
				else return "L2UI_ct1.button_df";
			case 1: // Yellow
				if (back) return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_earth";
				else return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_earth_bg";			
			case 2: // Blue
				if (back) return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_Water";
				else return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_Water_bg";
			case 3: // Green
				if (back) return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_wind";
				else return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_wind_bg";
			case 4: // Red
				if (back) return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_fire";
				else return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_fire_bg";
			case 5: // Purple
				if (back) return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_dark";
				else return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_dark_bg";
			case 6: // Grey
				if (back) return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_divine";
				else return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_divine_bg";
			default:
				if (back) return "L2UI_ct1.button_df";
				else return "L2UI_ct1.button_df";
		}
	}
	/**
	 * @return [a action="bypass -h Quest questName event"][font color="color">value[/font][/a]
	 */
	public static String questLink(String questName, String value, String event, String color)
	{
		return "<a action=\"bypass -h Quest " + questName + " " + event + "\">" + "<font color=\"" + color + "\">" + value + "</font></a>";
	}
	
	/**
	 * @return [a action="bypass -h bypassValue"][font color="color">displayName[/font][/a]
	 */
	public static String bypass(String bypassValue, String displayName, String color)
	{
		return "<a action=\"bypass -h " + bypassValue + "\">" + "<font color=\"" + color + "\">" + displayName + "</font></a>";
	}
	
	/**
	 * @return [a action="bypass bypassValue"][font color="color">displayName[/font][/a]
	 */
	public static String bypassNoH(String bypass, String displayName, String color)
	{
		return "<a action=\"bypass " + bypass + "\">" + "<font color=\"" + color + "\">" + displayName + "</font></a>";
	}
	
	/**
	 * @return [table width="260" align="center"][tr][td width="260" align="center"] title [/td][/tr][/table][br]
	 */
	public static String topic(String title)
	{
		return "<table width=\"260\" align=\"center\"><tr><td width=\"260\" align=\"center\"> " + title + " </td></tr></table><br>";
	}
}