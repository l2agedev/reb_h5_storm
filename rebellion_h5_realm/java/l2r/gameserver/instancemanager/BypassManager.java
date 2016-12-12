package l2r.gameserver.instancemanager;

import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.Strings;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BypassManager
{
	// http://regexr.com/ - IT WILL SOLVE ALL YOUR FUCKING PROBLEMS!!!!!!!!
	private static final Pattern p = Pattern.compile("\"(bypass +-h +|bypass +)(.+?)\"");

	public static enum BypassType
	{
		ENCODED,
		ENCODED_BBS,
		SIMPLE,
		SIMPLE_BBS,
		SIMPLE_DIRECT
	}

	public static BypassType getBypassType(String bypass)
	{
		switch(bypass.charAt(0))
		{
			case '0':
				return BypassType.ENCODED;
			case '1':
				return BypassType.ENCODED_BBS;
			default:
				if(Strings.matches(bypass, "^(_mrsl|_diary|_match|manor_menu_select|_match|_olympiad).*", Pattern.DOTALL))
					return BypassType.SIMPLE;
				if(CommunityBoardManager.getInstance().getCommunityHandler(bypass) != null)
					return BypassType.SIMPLE_BBS;
				return BypassType.SIMPLE_DIRECT;
		}
	}

	public static String encode(String html, List<String> bypassStorage, boolean bbs)
	{
		Matcher m = p.matcher(html);
		StringBuffer sb = new StringBuffer();

		while(m.find())
		{
			boolean hasH = m.group(1).startsWith("bypass -h ");
			String bypass = m.group(2);
			String code = bypass;
			String params = "";
			int i = bypass.indexOf(" $");
			boolean use_params = i >= 0;
			if(use_params)
			{
				code = bypass.substring(0, i);
				params = bypass.substring(i).replace("$", "\\$");
			}

			if(bbs)
				m.appendReplacement(sb, "\"bypass" + (hasH ? " -h" : "") + " 1" + Integer.toHexString(bypassStorage.size()) + params + "\"");
			else
				m.appendReplacement(sb, "\"bypass" + (hasH ? " -h" : "") + " 0" + Integer.toHexString(bypassStorage.size()) + params + "\"");

			bypassStorage.add(code);
		}

		m.appendTail(sb);
		
		return sb.toString();
	}

	public static DecodedBypass decode(String bypass, List<String> bypassStorage, boolean bbs, Player player)
	{
		synchronized (bypassStorage)
		{
			String[] bypass_parsed = bypass.split(" ");
			String bp;

			try
			{
				int idx = Integer.parseInt(bypass_parsed[0].substring(1), 16);
				bp = bypassStorage.get(idx);
			}
			catch(Exception e)
			{
				bp = null;
			}

			if(bp == null)
			{
				Log.addGame("Can't decode bypass (bypass not exists): " + (bbs ? "[bbs] " : "") + bypass + " / Player: " + player.getName() + " / Npc: " + (player.getLastNpc() == null ? "null" : player.getLastNpc().getName()), "debug_bypass");
				return null;
			}

			DecodedBypass result = null;
			result = new DecodedBypass(bp);
			for(int i = 1; i < bypass_parsed.length; i++)
				result.bypass += " " + bypass_parsed[i];
			result.trim();

			return result;
		}
	}

	public static class DecodedBypass
	{
		public String bypass;
		public ICommunityBoardHandler handler;

		public DecodedBypass(String _bypass)
		{
			bypass = _bypass;
			handler = CommunityBoardManager.getInstance().getCommunityHandler(bypass);
		}

		public DecodedBypass(String _bypass, ICommunityBoardHandler _handler)
		{
			bypass = _bypass;
			handler = _handler;
		}

		public DecodedBypass trim()
		{
			bypass = bypass.trim();
			return this;
		}
	}
}