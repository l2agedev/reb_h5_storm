package l2r.gameserver.utils;

import l2r.gameserver.GameTimeController;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
public class TimeUtils
{
	private static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HH:mm");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	private static final SimpleDateFormat DATE_HOUR_FORMAT = new SimpleDateFormat("HH:mm dd/MM/yyyy");

	public static String toSimpleFormat(Calendar cal)
	{
		return DATE_HOUR_FORMAT.format(cal.getTime());
	}

	public static String convertDateToString(long time)
	{
		Date dt = new Date(time);
		String stringDate = DATE_HOUR_FORMAT.format(dt);
		return stringDate;
	}
	
	public static long getMillisecondsFromString(String datetime)
	{
		return getMillisecondsFromString(datetime, "dd/MM/yyyy HH:mm");
	}
	
	public static long getMillisecondsFromString(String datetime, String format)
	{
		DateFormat df = new SimpleDateFormat(format); 
		try
		{ 
			Date time = df.parse(datetime);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(time);

			return calendar.getTimeInMillis();
		} 
		catch(Exception e) 
		{ 
			e.printStackTrace();
		}

		return 0;
	}
	
	
	public static String convertDateToString(long time, boolean onlyHour, boolean onlyDate)
	{
		Date dt = new Date(time);
		String stringDate = DATE_HOUR_FORMAT.format(dt);
		
		if (onlyHour)
			stringDate = HOUR_FORMAT.format(dt);
		
		if (onlyDate)
			stringDate = DATE_FORMAT.format(dt);
		
		return stringDate;
	}
	
	public static String toSimpleFormat(long cal)
	{
		return DATE_HOUR_FORMAT.format(cal);
	}

	public static String getDateString(Date date)
	{
		return DATE_HOUR_FORMAT.format(date.getTime());
	}
	
	public static String minutesToFullString(int period)
	{
		StringBuilder sb = new StringBuilder();

		// парсим дни
		if(period > 1440) // больше 1 суток
		{
			sb.append((period - (period % 1440)) / 1440).append(" d.");
			period = period % 1440;
		}

		// парсим часы
		if(period > 60) // остаток более 1 часа
		{
			if(sb.length() > 0)
			{
				sb.append(", ");
			}

			sb.append((period - (period % 60)) / 60).append(" h.");

			period = period % 60;
		}

		// парсим остаток
		if(period > 0) // есть остаток
		{
			if(sb.length() > 0)
			{
				sb.append(", ");
			}

			sb.append(period).append(" min.");
		}
		if(sb.length() < 1)
		{
			sb.append("less than 1 min.");
		}

		return sb.toString();
	}
	
	public static String minutesToFullString(int period, boolean FullString, boolean days, boolean hours, boolean minutes)
	{
		StringBuilder sb = new StringBuilder();

		// парсим дни
		if(period > 1440 && days) // больше 1 суток
		{
			if (FullString)
				sb.append((period - (period % 1440)) / 1440).append(" day(s)");
			else
				sb.append((period - (period % 1440)) / 1440).append(" d.");
			period = period % 1440;
		}

		// парсим часы
		if(period > 60 && hours) // остаток более 1 часа
		{
			if(sb.length() > 0)
			{
				sb.append(", ");
			}

			if (FullString)
				sb.append((period - (period % 60)) / 60).append(" hour(s)");
			else
				sb.append((period - (period % 60)) / 60).append(" h.");

			period = period % 60;
		}

		// парсим остаток
		if(period > 0 && minutes) // есть остаток
		{
			if(sb.length() > 0)
			{
				sb.append(", ");
			}

			if (FullString)
				sb.append(period).append(" minute(s)");
			else
				sb.append(period).append(" min.");
		}
		
		if(sb.length() < 1)
		{
			sb.append("less than 1 min.");
		}

		return sb.toString();
	}
	
	public static String getConvertedTime(long seconds)
	{
		int days = (int) (seconds / 86400);
		seconds -= days * 86400;
		int hours = (int) (seconds / 3600);
		seconds -= hours * 3600;
		int minutes = (int) (seconds / 60);
		
		boolean includeNext = true;
		String time = "";
		if (days > 0)
		{
			time = days + " Day(s) ";
			if (days > 5)
				includeNext = false;
		}
		if (hours > 0 && includeNext)
		{
			if (time.length() > 0)
				includeNext = false;
			time += hours + " Hour(s) ";
			if (hours > 10)
				includeNext = false;
		}
		if (minutes > 0 && includeNext)
		{
			time += minutes + " Min(s)";
		}
		return time;
	}
	
	public static String getTimeInServer()
	{
		String hour, minute;
		int h = GameTimeController.getInstance().getGameHour();
		int m = GameTimeController.getInstance().getGameMin();
		
		
		String type;
		if (GameTimeController.getInstance().isNowNight())
			type = "Night";
		else
			type = "Day";
		
		if (h < 10)
			hour = "0" + h;
		else
			hour = "" + h;
		
		if (m < 10)
			minute = "0" + m;
		
		else
			minute = "" + m;
		
		String time = hour + ":" + minute + " (" + type + ")";
		return time;
	}
}
