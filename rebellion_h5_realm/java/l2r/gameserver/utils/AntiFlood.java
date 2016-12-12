package l2r.gameserver.utils;


import l2r.gameserver.Config;

import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.map.hash.TIntLongHashMap;

import org.apache.commons.lang3.StringUtils;

public class AntiFlood
{
	private TIntLongHashMap _recentReceivers = new TIntLongHashMap();
	private TIntLongHashMap _recentInviteAcademy = new TIntLongHashMap();
	private long _lastSent = 0L;
	private String _lastText = StringUtils.EMPTY;

	private long _lastHeroTime;
	private long _lastTradeTime;
	private long _lastShoutTime;

	private long _lastMailTime;
	private long _lastPingTime;
	private long _lastFindPartyTime;
	private long _lastRequestedCaptcha;
	private long _lastClanEmail;
	private long _lastAcademyRegTime;
	private long _lastVisualPost;
	private long _showOfflineShops;

	public boolean canPutVisual()
	{
		long currentMillis = System.currentTimeMillis();

		if (currentMillis - _lastVisualPost < 5 * 1000) // 5 sec
			return false;

		_lastVisualPost = currentMillis;
		return true;
	}
	
	public boolean canDisableOfflineShops()
	{
		long currentMillis = System.currentTimeMillis();

		if (currentMillis - _showOfflineShops < 10 * 1000) // 10 sec
			return false;

		_showOfflineShops = currentMillis;
		return true;
	}
	
	
	public boolean canRegisterForAcademy()
	{
		long currentMillis = System.currentTimeMillis();

		if (currentMillis - _lastAcademyRegTime < 5 * 1000) // 5 sec
			return false;

		_lastAcademyRegTime = currentMillis;
		return true;
	}
	
	public boolean canInviteInAcademy(int charId)
	{
		long currentMillis = System.currentTimeMillis();
		long lastSent;
		int lastChar;

		TIntLongIterator itr = _recentInviteAcademy.iterator();

		while (itr.hasNext())
		{
			itr.advance();
			lastChar = itr.key();
			lastSent = itr.value();
			
			if (lastChar == charId && (currentMillis - lastSent) < Config.ACADEMY_INVITE_DELAY * 60 * 1000) // 5 minutes
				return false;
		}
		
		lastSent = _recentInviteAcademy.put(charId, currentMillis);
		return true;
	}
	
	public boolean canSendClanEmail()
	{
		long currentMillis = System.currentTimeMillis();

		if (currentMillis - _lastClanEmail < 60 * 60000) // every 1 hour
			return false;

		_lastClanEmail = currentMillis;
		return true;
	}
	
	public boolean canRequestCaptcha()
	{
		long currentMillis = System.currentTimeMillis();

		if (currentMillis - _lastRequestedCaptcha < 5 * 1000) // every 5 sec can request new captcha
			return false;

		_lastRequestedCaptcha = currentMillis;
		return true;
	}
	
	public boolean canTrade(String text, boolean Premium)
	{
		long currentMillis = System.currentTimeMillis();

		if (currentMillis - _lastTradeTime < (Config.GLOBAL_TRADE_CHAT && Premium ? ((Config.CHAT_TRADE_TIME_DELAY * 1000) / 2) : (Config.CHAT_TRADE_TIME_DELAY * 1000)))
			return false;

		_lastTradeTime = currentMillis;
		return true;
	}

	public boolean canShout(String text, boolean Premium)
	{
		long currentMillis = System.currentTimeMillis();

		if (currentMillis - _lastShoutTime < (Config.GLOBAL_SHOUT && Premium ? ((Config.CHAT_SHOUT_TIME_DELAY * 1000) / 2) : (Config.CHAT_SHOUT_TIME_DELAY * 1000)))
			return false;

		_lastShoutTime = currentMillis;
		return true;
	}

	public boolean canShout()
	{
		long currentMillis = System.currentTimeMillis();

		if (currentMillis - _lastShoutTime < Config.CHAT_SHOUT_TIME_DELAY * 1000)
			return false;

		_lastShoutTime = currentMillis;
		return true;
	}
	
	public boolean canFindParty()
	{
		long currentMillis = System.currentTimeMillis();

		if (currentMillis - _lastFindPartyTime < 120 * 1000) // 2 minutes..
			return false;

		_lastFindPartyTime = currentMillis;
		return true;
	}
	
	public boolean canHero(String text)
	{
		long currentMillis = System.currentTimeMillis();

		if (currentMillis - _lastHeroTime < Config.CHAT_HERO_TIME_DELAY * 1000)
			return false;

		_lastHeroTime = currentMillis;
		return true;
	}

	public boolean canMail(boolean level)
	{
		long currentMillis = System.currentTimeMillis();

		if (currentMillis - _lastMailTime < (level ? 60000L : 600000L))
			return false;

		_lastMailTime = currentMillis;
		return true;
	}

	public boolean canPing()
	{
		long currentMillis = System.currentTimeMillis();

		if (currentMillis - _lastPingTime < 10000L) // 10 sec
			return false;

		_lastPingTime = currentMillis;
		return true;
	}
	
	public boolean canTell(int charId, String text)
	{
		long currentMillis = System.currentTimeMillis();
		long lastSent;

		TIntLongIterator itr = _recentReceivers.iterator();

		int recent = 0;
		while(itr.hasNext())
		{
			itr.advance();
			lastSent = itr.value();
			if (currentMillis - lastSent < (text.equalsIgnoreCase(_lastText) ? 600000L : 60000L))
				recent++;
			else
				itr.remove();
		}

		lastSent = _recentReceivers.put(charId, currentMillis);

		long delay = 333L;
		if (recent > 3)
		{
			lastSent = _lastSent;
			delay =  (recent - 3) * 3333L;
		}

		_lastText = text;
		_lastSent = currentMillis;

		return currentMillis - lastSent > delay;
	}
}
