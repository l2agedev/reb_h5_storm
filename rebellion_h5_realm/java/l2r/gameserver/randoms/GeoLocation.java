package l2r.gameserver.randoms;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maxmind.geoip.LookupService;

/**
 * GeoLocation grep.
 * @author Infern0
 */
public class GeoLocation
{
	private static final Logger _log = LoggerFactory.getLogger(GeoLocation.class);
	private static String[] russianSpeakingCountries = { "Russia", "Belarus", "Kazakhstan", "Moldova", "Kyrgyzstan", "Tajikistan", "Ukraine" };
	public static LookupService lookup = null;
	static
	{
		try
		{
			lookup = new LookupService(Config.getFile("config/GeoLiteCity.dat"), LookupService.GEOIP_MEMORY_CACHE);
			lookup.close(); // Everything is cached, close the file.
		}
		catch (NullPointerException | IOException e)
		{
			_log.warn("GeoLocation unable to load, skipping: ", e);
		}
	}
	
	public static boolean isFromRussia(Player player)
	{
		if (player == null || player.getClient() == null)
			return false;
		
		String ip = player.getIP();
		if (ip == null || ip.equalsIgnoreCase(Player.NOT_CONNECTED))
			return false;
		
		for (String count : russianSpeakingCountries)
		{
			if (count.equalsIgnoreCase(getCountry(ip)))
				return true;
		}
		
		return false;
		
	}
	public static String getCountryCode(Player player)
	{
		if (player == null || player.getClient() == null)
			return "NULL";
		
		String ip = player.getIP();
		boolean notConnected = false;
		if (notConnected = ip.equalsIgnoreCase(Player.NOT_CONNECTED))
			ip = player.getVar("LastIP") == null ? "NULL" : ip;
		
		return getCountryCode(ip) + (notConnected ? "*" : "");
	}
	
	public static String getCountryCode(String ip)
	{
		if (lookup == null)
			return "NULL";
		
		try
		{			
			
			return lookup.getLocation(ip) == null ? "Null" : lookup.getLocation(ip).countryCode;
		}
		catch (Exception e)
		{
			_log.warn("Error while getting GeoLocation data for IP: " + ip + "; err:", e);
			return "NULL";
		}
	}
	
	public static String getCountry(Player player)
	{
		if (player == null || player.getClient() == null)
			return "NULL";
		
		String ip = player.getIP();
		boolean notConnected = false;
		if (notConnected = ip.equalsIgnoreCase(Player.NOT_CONNECTED))
			ip = player.getVar("LastIP") == null ? "NULL" : ip;
		
		return getCountry(ip) + (notConnected ? "*" : "");
	}
	
	public static String getCountry(String ip)
	{
		if (lookup == null)
			return "NULL";
		
		try
		{			
			return lookup.getLocation(ip) == null ? "Null" : lookup.getLocation(ip).countryName;
		}
		catch (Exception e)
		{
			_log.warn("Error while getting GeoLocation data for IP: " + ip + "; err:", e);
			return "NULL";
		}
	}
	
	public static String getCity(Player player)
	{
		if (player == null || player.getClient() == null)
			return "NULL";
		
		String ip = player.getIP();
		boolean notConnected = false;
		if (notConnected = ip.equalsIgnoreCase(Player.NOT_CONNECTED))
			ip = player.getVar("LastIP") == null ? "NULL" : ip;
		
		return getCity(ip) + (notConnected ? "*" : "");
	}
	
	public static String getCity(String ip)
	{
		if (lookup == null)
			return "NULL";
		
		try
		{
			return lookup.getLocation(ip) == null ? "Null" : lookup.getLocation(ip).city;
		}
		catch (Exception e)
		{
			_log.warn("Error while getting GeoLocation data for IP: " + ip + "; err:", e);
			return "NULL";
		}
	}
	
	public static String getCityRegion(Player player)
	{
		if (player == null || player.getClient() == null)
			return "NULL";
		
		String ip = player.getIP();
		boolean notConnected = false;
		if (notConnected = ip.equalsIgnoreCase(Player.NOT_CONNECTED))
			ip = player.getVar("LastIP") == null ? "NULL" : ip;
		
		return getCityRegion(ip) + (notConnected ? "*" : "");
	}
	
	public static String getCityRegion(String ip)
	{
		if (lookup == null)
			return "NULL";
		
		try
		{			
			return lookup.getLocation(ip) == null ? "Null" : lookup.getLocation(ip).region;
		}
		catch (Exception e)
		{
			_log.warn("Error while getting GeoLocation data for IP: " + ip + "; err:", e);
			return "NULL";
		}
	}
}