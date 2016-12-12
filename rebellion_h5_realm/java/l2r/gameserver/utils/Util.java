package l2r.gameserver.utils;

import l2r.commons.util.Rnd;
import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.reward.RewardList;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.templates.item.ItemTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class Util
{
	static final String PATTERN = "0.0000000000E00";
	static final DecimalFormat df;

	/**
	 * Форматтер для адены.<br>
	 * Locale.US заставляет его фортматировать через ",".<br>
	 * Locale.FRANCE форматирует через " "<br>
	 * Для форматирования через "." убрать с аргументов Locale.GERMAN
	 */
	private static NumberFormat adenaFormatter;

	static
	{
		adenaFormatter = NumberFormat.getIntegerInstance(Locale.US);
		df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.applyPattern(PATTERN);
		df.setPositivePrefix("+");
	}

	/**
	 * Used in Enum to Integer conversion to support multiple enum variables.
	 * @param addTo the integer which the enum has to be added to.
	 * @return integer containing the enum.
	 */
	public static int add(int addTo, Enum val)
	{
		return addTo | (1 << val.ordinal());
	}
	
	/**
	 * Used in Enum to Integer conversion to support multiple enum variables.
	 * @param removeFrom the integer which the enum has to be removed from.
	 * @return integer excluding the enum.
	 */
	public static int remove(int removeFrom, Enum val)
	{
		return removeFrom & ~(1 << val.ordinal());
	}
	
	/**
	 * Checks if the given enum is inside the multienum integer.
	 * @param in the multienum integer.
	 * @return true if the enum is inside the multienum integer.
	 */
	public static boolean contains(int in, Enum val)
	{
		return (in & (1 << val.ordinal())) != 0;
	}

	/**
	 * Проверяет строку на соответсвие регулярному выражению
	 * @param text Строка-источник
	 * @param template Шаблон для поиска
	 * @return true в случае соответвия строки шаблону
	 */
	public static boolean isMatchingRegexp(String text, String template)
	{
		Pattern pattern = null;
		try
		{
			pattern = Pattern.compile(template);
		}
		catch(PatternSyntaxException e) // invalid template
		{
			e.printStackTrace();
		}
		if(pattern == null)
			return false;
		Matcher regexp = pattern.matcher(text);
		return regexp.matches();
	}

	public static String formatDouble(double x, String nanString, boolean forceExponents)
	{
		if(Double.isNaN(x))
			return nanString;
		if(forceExponents)
			return df.format(x);
		if((long) x == x)
			return String.valueOf((long) x);
		return String.valueOf(x);
	}

	/**
	 * Return amount of adena formatted with " " delimiter
	 * @param amount
	 * @return String formatted adena amount
	 */
	public static String formatAdena(long amount)
	{
		return adenaFormatter.format(amount);
	}

	/**
	 * 
	 * @param time : time in <b>seconds</b>
	 * @return a string representation of the given time on the bigges possible scale (s, m, h, d)
	 */
	public static String formatTime(int time)
	{
		return formatTime(time, -1);
	}
	
	/**
	 * 
	 * @param time : time in <b>seconds</b>
	 * @param offset : the offset number. If the parsed time is 4d 13h 33m 12s with offset of 2, it will return only 4d and 14h. If set to <= 0, it will be disabled.
	 * @return a string representation of the given time on the bigges possible scale (s, m, h, d)
	 */
	public static String formatTime(int time, int offset)
	{
		if(time == 0)
			return "now";
		
		if(time <= -1)
			return "time ended";
		
		time = Math.abs(time);
		String ret = "";
		
		long numMonths = time / 2592000;
		time -= numMonths * 2592000;
		long numDays = time / 86400;
		time -= numDays * 86400;
		long numHours = time / 3600;
		time -= numHours * 3600;
		long numMins = time / 60;
		time -= numMins * 60;
		long numSeconds = time;
		
		if (offset > 0)
		{
			if (numMonths > 0)
				offset--;
			if (numDays > 0)
			{
				if (offset > 0)
					offset--;
				else
				{
					// Round the months if there is no more offset.
					if (numDays >= 15 && offset == 0)
						numMonths++;
					numDays = 0;
				}
			}
			if (numHours > 0)
			{
				if (offset > 0)
					offset--;
				else
				{
					// Round the days if there is no more offset.
					if (numHours >= 12 && offset == 0)
						numDays++;
					numHours = 0;
				}
			}
			if (numMins > 0)
			{
				if (offset > 0)
					offset--;
				else
				{
					// Round the hours if there is no more offset.
					if (numMins >= 30 && offset == 0)
						numHours++;
					numMins = 0;
				}
			}
			if (numSeconds > 0)
			{
				if (offset > 0)
					offset--;
				else
				{
					// Round the minutes if there is no more offset.
					if (numSeconds >= 30 && offset == 0)
						numMins++;
					numSeconds = 0;
				}
			}
		}
		
		if(numMonths > 0)
			ret += numMonths + "M ";
		if(numDays > 0)
			ret += Math.min(numDays, 30) + "d ";
		if(numHours > 0)
			ret += Math.min(numHours, 23) + "h ";
		if(numMins > 0)
			ret += Math.min(numMins, 59) + "m ";
		if(numSeconds > 0)
			ret += Math.min(numSeconds, 59) + "s";
		
		return ret.trim();
	}

	/**
	 * Инструмент для подсчета выпавших вещей с учетом рейтов.
	 * Возвращает 0 если шанс не прошел, либо количество если прошел.
	 * Корректно обрабатывает шансы превышающие 100%.
	 * Шанс в 1:1000000 (L2Drop.MAX_CHANCE)
	 */
	public static long rollDrop(long min, long max, double calcChance, boolean rate)
	{
		if(calcChance <= 0 || min <= 0 || max <= 0)
			return 0;
		int dropmult = 1;
		if(rate)
			calcChance *= Config.RATE_DROP_ITEMS;
		if(calcChance > RewardList.MAX_CHANCE)
			if(calcChance % RewardList.MAX_CHANCE == 0) // если кратен 100% то тупо умножаем количество
				dropmult = (int) (calcChance / RewardList.MAX_CHANCE);
			else
			{
				dropmult = (int) Math.ceil(calcChance / RewardList.MAX_CHANCE); // множитель равен шанс / 100% округление вверх
				calcChance = calcChance / dropmult; // шанс равен шанс / множитель
			}
		return Rnd.chance(calcChance / 10000.) ? Rnd.get(min * dropmult, max * dropmult) : 0;
	}

	public static int packInt(int[] a, int bits) throws Exception
	{
		int m = 32 / bits;
		if(a.length > m)
			throw new Exception("Overflow");

		int result = 0;
		int next;
		int mval = (int) Math.pow(2, bits);
		for(int i = 0; i < m; i++)
		{
			result <<= bits;
			if(a.length > i)
			{
				next = a[i];
				if(next >= mval || next < 0)
					throw new Exception("Overload, value is out of range");
			}
			else
				next = 0;
			result += next;
		}
		return result;
	}

	public static long packLong(int[] a, int bits) throws Exception
	{
		int m = 64 / bits;
		if(a.length > m)
			throw new Exception("Overflow");

		long result = 0;
		int next;
		int mval = (int) Math.pow(2, bits);
		for(int i = 0; i < m; i++)
		{
			result <<= bits;
			if(a.length > i)
			{
				next = a[i];
				if(next >= mval || next < 0)
					throw new Exception("Overload, value is out of range");
			}
			else
				next = 0;
			result += next;
		}
		return result;
	}

	public static int[] unpackInt(int a, int bits)
	{
		int m = 32 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
		int next;
		for(int i = m; i > 0; i--)
		{
			next = a;
			a = a >> bits;
		result[i - 1] = next - a * mval;
		}
		return result;
	}

	public static int[] unpackLong(long a, int bits)
	{
		int m = 64 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
		long next;
		for(int i = m; i > 0; i--)
		{
			next = a;
			a = a >> bits;
		result[i - 1] = (int) (next - a * mval);
		}
		return result;
	}

	public static float[] parseCommaSeparatedFloatArray(String s)
	{
		if (s.isEmpty())
			return new float[0];
		String[] tmp = s.replaceAll(",", ";").replaceAll("\\n", ";").split(";");
		float[] val = new float[tmp.length];
		for (int i = 0; i < tmp.length; i++)
			val[i] = Float.parseFloat(tmp[i]);
		return val;
	}

	public static int[] parseCommaSeparatedIntegerArray(String s)
	{
		if (s.isEmpty())
			return new int[0];
		String[] tmp = s.replaceAll(",", ";").replaceAll("\\n", ";").split(";");
		int[] val = new int[tmp.length];
		for (int i = 0; i < tmp.length; i++)
			val[i] = Integer.parseInt(tmp[i]);
		return val;
	}

	public static long[] parseCommaSeparatedLongArray(String s)
	{
		if (s.isEmpty())
			return new long[0];
		String[] tmp = s.replaceAll(",", ";").replaceAll("\\n", ";").split(";");
		long[] val = new long[tmp.length];
		for (int i = 0; i < tmp.length; i++)
			val[i] = Long.parseLong(tmp[i]);
		return val;
	}

	public static long[][] parseStringForDoubleArray(String s)
	{
		String[] temp = s.replaceAll("\\n", ";").split(";");
		long[][] val = new long[temp.length][];

		for (int i = 0; i < temp.length; i++)
			val[i] = parseCommaSeparatedLongArray(temp[i]);
		return val;
	}

	/** Just alias */
	public static String joinStrings(String glueStr, String[] strings, int startIdx, int maxCount)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, maxCount);
	}

	/** Just alias */
	public static String joinStrings(String glueStr, String[] strings, int startIdx)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, -1);
	}

	public static boolean isNumber(String s)
	{
		try
		{
			Double.parseDouble(s);
		}
		catch(NumberFormatException e)
		{
			return false;
		}
		return true;
	}

	public static String dumpObject(Object o, boolean simpleTypes, boolean parentFields, boolean ignoreStatics)
	{
		Class<?> cls = o.getClass();
		String val, type, result = "[" + (simpleTypes ? cls.getSimpleName() : cls.getName()) + "\n";
		Object fldObj;
		List<Field> fields = new ArrayList<Field>();
		while(cls != null)
		{
			for(Field fld : cls.getDeclaredFields())
				if(!fields.contains(fld))
				{
					if(ignoreStatics && Modifier.isStatic(fld.getModifiers()))
						continue;
					fields.add(fld);
				}
			cls = cls.getSuperclass();
			if(!parentFields)
				break;
		}

		for(Field fld : fields)
		{
			fld.setAccessible(true);
			try
			{
				fldObj = fld.get(o);
				if(fldObj == null)
					val = "NULL";
				else
					val = fldObj.toString();
			}
			catch(Throwable e)
			{
				e.printStackTrace();
				val = "<ERROR>";
			}
			type = simpleTypes ? fld.getType().getSimpleName() : fld.getType().toString();

			result += String.format("\t%s [%s] = %s;\n", fld.getName(), type, val);
		}

		result += "]\n";
		return result;
	}

	private static Pattern _pattern = Pattern.compile("<!--TEMPLET(\\d+)(.*?)TEMPLET-->", Pattern.DOTALL);

	public static HashMap<Integer, String> parseTemplate(String html)
	{
		Matcher m = _pattern.matcher(html);
		HashMap<Integer, String> tpls = new HashMap<Integer, String>();
		while(m.find())
		{
			tpls.put(Integer.parseInt(m.group(1)), m.group(2));
			html = html.replace(m.group(0), "");
		}

		tpls.put(0, html);
		return tpls;
	}
	
	public static boolean isDigit(String text)
	{
		if (text == null)
			return false;
		return text.matches("[0-9]+");
	}
	
	/**
	 * @param raw
	 * @return
	 */
	public static String printData(byte[] raw)
	{
		return printData(raw, raw.length);
	}
	
	public static String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);
		for(int i = number.length(); i < digits; i++)
		{
			number = "0" + number;
		}
		return number;
	}
	
	public static String printData(byte[] data, int len)
	{
		StringBuffer result = new StringBuffer();
		int counter = 0;
		for(int i = 0; i < len; i++)
		{
			if(counter % 16 == 0)
			{
				result.append(fillHex(i, 4) + ": ");
			}
			result.append(fillHex(data[i] & 0xff, 2) + " ");
			counter++;
			if(counter == 16)
			{
				result.append("   ");
				int charpoint = i - 15;
				for(int a = 0; a < 16; a++)
				{
					int t1 = data[charpoint++];
					if(t1 > 0x1f && t1 < 0x80)
					{
						result.append((char) t1);
					}
					else
					{
						result.append('.');
					}
				}
				result.append("\n");
				counter = 0;
			}
		}
		int rest = data.length % 16;
		if(rest > 0)
		{
			for(int i = 0; i < 17 - rest; i++)
			{
				result.append("   ");
			}
			int charpoint = data.length - rest;
			for(int a = 0; a < rest; a++)
			{
				int t1 = data[charpoint++];
				if(t1 > 0x1f && t1 < 0x80)
				{
					result.append((char) t1);
				}
				else
				{
					result.append('.');
				}
			}
			result.append("\n");
		}
		return result.toString();
	}
	
	public static byte[] generateHex(int size)
	{
		byte[] array = new byte[size];
		Random rnd = new Random();
		for(int i = 0; i < size; i++)
		{
			array[i] = (byte) rnd.nextInt(256);
		}
		return array;
	}
	
	/**
	 * @param <T>
	 * @param array - the array to look into
	 * @param obj - the object to search for
	 * @return {@code true} if the {@code array} contains the {@code obj}, {@code false} otherwise.
	 */
	public static <T> boolean contains(T[] array, T obj)
	{
		for (T element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param array - the array to look into
	 * @param obj - the integer to search for
	 * @return {@code true} if the {@code array} contains the {@code obj}, {@code false} otherwise
	 */
	public static boolean contains(int[] array, int obj)
	{
		for (int element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	public static int getGearPoints(Player player)
	{
		int points = 0;
		ItemInstance weapon = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		ItemInstance chest = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		ItemInstance legs = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		ItemInstance boots = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET);
		ItemInstance gloves = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		ItemInstance helmet = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		ItemInstance ring1 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER);
		ItemInstance ring2 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER);
		ItemInstance earring1 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR);
		ItemInstance earring2 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR);
		ItemInstance necklace = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK);
		
		//===== CALCULATE ITEM POINTS =====
		for (int n=0; n<11; n++)
		{
			double pointsPerEnch = 0;
			ItemInstance item = null;
			double tmpPts = 0;
			boolean isWeapon = false;
			
			switch (n)
			{
				case 0: //weapon
					item = weapon;
					isWeapon = true;
					break;
				case 1: //chest
					item = chest;
					break;
				case 2: //legs
					item = legs;
					break;
				case 3: //boots
					item = boots;
					break;
				case 4: //gloves
					item = gloves;
					break;
				case 5: //helmet
					item = helmet;
					break;
				case 6: //ring1
					item = ring1;
					break;
				case 7: //ring2
					item = ring2;
					break;
				case 8: //earring1
					item = earring1;
					break;
				case 9: //earring2
					item = earring2;
					break;
				case 10: //necklace
					item = necklace;
					break;
			}
			if (item == null)
				continue;
			
			switch(item.getTemplate().getItemGrade())
			{
				case D:
					tmpPts += 25;
					pointsPerEnch = 0.5;
					break;
				case A:
					tmpPts += 75;
					pointsPerEnch = 1.5;
					break;
				case S:
					tmpPts += 125;
					pointsPerEnch = 2.5;
					break;
				case S80:
					tmpPts += 200;
					pointsPerEnch = 4;
					break;
				case S84:
					tmpPts += 300;
					pointsPerEnch = 6;
					if (item.getName().contains("Elegia")) // Im too lazy to do it via IDs
					{
						tmpPts += 150;
						pointsPerEnch = 8;
					}
					else if (item.getName().contains("Vorpal")) // Im too lazy to do it via IDs
					{
						tmpPts += 50;
						pointsPerEnch = 7;
					}
					break;
			}
			
			// get the item enchantment points
			double tempEnchPts = 0;
			for (int i=0; i<item.getEnchantLevel(); i++)
				tempEnchPts += pointsPerEnch*i;
			tmpPts += tempEnchPts;
			
			
			if (isWeapon) // double the points if the item is weapon
				tmpPts *= 2;
			
			//now add the temporary calculated points
			points += tmpPts;
		}
		
		//===== CALCULATE SKILL POINTS =====
		for (Skill skill : player.getAllSkills())
		{
			switch(skill.getId())
			{
				case 3561: //Ring of Baium
					points += 500;
					break;
				case 3562: //Ring of Queen Ant
					points += 300;
					break;
				case 3560: //Earring of Orfen
					points += 100;
					break;
				case 3558: //Earring of Antharas
					points += 700;
					break;
				case 3559: //Zaken's Earring
					points += 400;
					break;
				case 3557: //Necklace of Valakas
					points += 900;
				case 3604: //Frintezza's Necklace
					points += 600;
					break;
				case 3649: //Beleth's Ring
					points += 150;
					break;
				case 3650: //PvP Weapon - CP Drain
				case 3651: //PvP Weapon - Cancel
				case 3652: //PvP Weapon - Ignore Shield Defense
				case 3653: //PvP Weapon - Attack Chance
				case 3654: //PvP Weapon - Casting
				case 3655: //PvP Weapon - Rapid Fire
				case 3656: //PvP Weapon - Decrease Range
				case 3657: //PvP Weapon - Decrease Resist
				case 3658: //PvP Shield - Reflect Damage
					points += 500;
					break;
				case 3659: //PvP Armor - Damage Down
				case 3660: //PvP Armor - Critical Down
				case 3661: //PvP Armor - Heal
				case 3662: //PvP Armor - Speed Down
				case 3663: //PvP Armor - Mirage
					points += 200;
					break;
				case 641: //Knight Ability - Boost HP
				case 642: //Enchanter Ability - Boost Mana
				case 643: //Summoner Ability - Boost HP/MP
				case 644: //Rogue ability - Evasion
				case 645: //Rogue Ability - Long Shot
				case 646: //Wizard Ability - Mana Gain
				case 647: //Enchanter Ability - Mana Recovery
				case 648: //Healer Ability - Prayer
				case 650: //Warrior Ability - Resist Trait
				case 651: //Warrior Ability - Haste
				case 652: //Knight Ability - Defense
				case 653: //Rogue Ability - Critical Chance
				case 654: //Wizard Ability - Mana Stea'
				case 1489: //Summoner Ability - Resist Attribute
				case 1490: //Healer Ability - Heal
				case 1491: //Summoner Ability - Spirit
				case 5572: //Warrior Ability - Haste
				case 5573: //Knight Ability - Defense
				case 5574: //Log Ability - Critical Chance
				case 5575: //Wizard Ability - Mana Steel
				case 5576: //Enchanter Ability - Barrier
				case 5577: //Healer Ability - Heal
				case 5578: //Summoner Ability - Spirit
					points += 100;
					break;
			}
		}
		return points;
	}
	
	/**
	 *  Usable to format size of ... something as memory, file size and etc.
	 * @param bytes
	 * @return B, KB, MB, GB, TB
	 */
	public static String toNumInUnits(long bytes)
	{
		int u = 0;
		for (; bytes > 1024 * 1024; bytes >>= 10)
		{
			u++;
		}
		if (bytes > 1024)
			u++;
		return String.format("%.1f %cB", bytes / 1024f, " KMGTPE".charAt(u));
	}
	
	public static String getCPU_BIOS_HDD_HWID(String hwid)
	{
		byte[] hwidBytes = asByteArray(hwid);
		return asHex(new byte[]
		{
			hwidBytes[0],
			hwidBytes[1],
			hwidBytes[2],
			hwidBytes[3],
			hwidBytes[4],
			hwidBytes[5],
			hwidBytes[6],
			hwidBytes[7],
			hwidBytes[8],
			hwidBytes[9]
		});
	}
	
	public static String getCPU_BIOS_HWID(String hwid)
	{
		byte[] hwidBytes = asByteArray(hwid);
		return asHex(new byte[]
		{
			hwidBytes[0],
			hwidBytes[1],
			hwidBytes[2],
			hwidBytes[3],
			hwidBytes[4],
			hwidBytes[5]
		});
	}
	
	public static byte[] asByteArray(String hex)
	{
		byte[] buf = new byte[hex.length() / 2];
		
		for (int i = 0; i < hex.length(); i += 2)
		{
			int j = Integer.parseInt(hex.substring(i, i + 2), 16);
			buf[(i / 2)] = (byte) (j & 0xFF);
		}
		return buf;
	}
	
	public static final String asHex(byte[] raw, int offset, int size)
	{
		StringBuffer strbuf = new StringBuffer(raw.length * 2);
		
		for (int i = 0; i < size; i++)
		{
			if ((raw[(offset + i)] & 0xFF) < 16)
			{
				strbuf.append("0");
			}
			strbuf.append(Long.toString(raw[(offset + i)] & 0xFF, 16));
		}
		
		return strbuf.toString();
	}
	
	public static final String asHex(byte[] raw)
	{
		return asHex(raw, 0, raw.length);
	}
	
	public static int min(int value1, int value2, int... values)
	{
		int min = Math.min(value1, value2);
		for (int value : values)
		{
			if (min > value)
			{
				min = value;
			}
		}
		return min;
	}
	
	public static int max(int value1, int value2, int... values)
	{
		int max = Math.max(value1, value2);
		for (int value : values)
		{
			if (max < value)
			{
				max = value;
			}
		}
		return max;
	}
	
	public static long min(long value1, long value2, long... values)
	{
		long min = Math.min(value1, value2);
		for (long value : values)
		{
			if (min > value)
			{
				min = value;
			}
		}
		return min;
	}
	
	public static long max(long value1, long value2, long... values)
	{
		long max = Math.max(value1, value2);
		for (long value : values)
		{
			if (max < value)
			{
				max = value;
			}
		}
		return max;
	}
	
	public static float min(float value1, float value2, float... values)
	{
		float min = Math.min(value1, value2);
		for (float value : values)
		{
			if (min > value)
			{
				min = value;
			}
		}
		return min;
	}
	
	public static float max(float value1, float value2, float... values)
	{
		float max = Math.max(value1, value2);
		for (float value : values)
		{
			if (max < value)
			{
				max = value;
			}
		}
		return max;
	}
	
	public static double min(double value1, double value2, double... values)
	{
		double min = Math.min(value1, value2);
		for (double value : values)
		{
			if (min > value)
			{
				min = value;
			}
		}
		return min;
	}
	
	public static double max(double value1, double value2, double... values)
	{
		double max = Math.max(value1, value2);
		for (double value : values)
		{
			if (max < value)
			{
				max = value;
			}
		}
		return max;
	}
	
	public static int getIndexOfMaxValue(int... array)
	{
		int index = 0;
		for (int i = 1; i < array.length; i++)
		{
			if (array[i] > array[index])
			{
				index = i;
			}
		}
		return index;
	}
	
	public static int getIndexOfMinValue(int... array)
	{
		int index = 0;
		for (int i = 1; i < array.length; i++)
		{
			if (array[i] < array[index])
			{
				index = i;
			}
		}
		return index;
	}
	
	/**
	 * Re-Maps a value from one range to another.
	 * @param input
	 * @param inputMin
	 * @param inputMax
	 * @param outputMin
	 * @param outputMax
	 * @return The mapped value
	 */
	public static int map(int input, int inputMin, int inputMax, int outputMin, int outputMax)
	{
		return (((input - inputMin) * (outputMax - outputMin)) / (inputMax - inputMin)) + outputMin;
	}
	
	/**
	 * Re-Maps a value from one range to another.
	 * @param input
	 * @param inputMin
	 * @param inputMax
	 * @param outputMin
	 * @param outputMax
	 * @return The mapped value
	 */
	public static long map(long input, long inputMin, long inputMax, long outputMin, long outputMax)
	{
		return (((input - inputMin) * (outputMax - outputMin)) / (inputMax - inputMin)) + outputMin;
	}
	
	/**
	 * Re-Maps a value from one range to another.
	 * @param input
	 * @param inputMin
	 * @param inputMax
	 * @param outputMin
	 * @param outputMax
	 * @return The mapped value
	 */
	public static double map(double input, double inputMin, double inputMax, double outputMin, double outputMax)
	{
		return (((input - inputMin) * (outputMax - outputMin)) / (inputMax - inputMin)) + outputMin;
	}
	
	/**
	 * Constrains a number to be within a range.
	 * @param input the number to constrain, all data types
	 * @param min the lower end of the range, all data types
	 * @param max the upper end of the range, all data types
	 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
	 */
	public static int constrain(int input, int min, int max)
	{
		return (input < min) ? min : (input > max) ? max : input;
	}
	
	/**
	 * Constrains a number to be within a range.
	 * @param input the number to constrain, all data types
	 * @param min the lower end of the range, all data types
	 * @param max the upper end of the range, all data types
	 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
	 */
	public static long constrain(long input, long min, long max)
	{
		return (input < min) ? min : (input > max) ? max : input;
	}
	
	/**
	 * Constrains a number to be within a range.
	 * @param input the number to constrain, all data types
	 * @param min the lower end of the range, all data types
	 * @param max the upper end of the range, all data types
	 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
	 */
	public static double constrain(double input, double min, double max)
	{
		return (input < min) ? min : (input > max) ? max : input;
	}
	
	/** @return Value at the given index or null if AIOOBE should be thrown. */
	public static <E> E safeGet(List<E> list, int index)
	{
		return (index >= 0 && list.size() > index) ? list.get(index) : null;
	}
	
	/** @return Value at the given index or null if AIOOBE should be thrown. */
	public static int safeGet(int[] arr, int index)
	{
		return (index >= 0 && arr.length > index) ? arr[index] : null;
	}
	
	/** @return Value at the given index or null if AIOOBE should be thrown. */
	public static double safeGet(double[] arr, int index)
	{
		return (index >= 0 && arr.length > index) ? arr[index] : null;
	}
	
	/** @return Value at the given index or null if AIOOBE should be thrown. */
	public static float safeGet(float[] arr, int index)
	{
		return (index >= 0 && arr.length > index) ? arr[index] : null;
	}
	
	/** @return Value at the given index or null if AIOOBE should be thrown. */
	public static long safeGet(long[] arr, int index)
	{
		return (index >= 0 && arr.length > index) ? arr[index] : null;
	}
	
	/** @return Value at the given index or null if AIOOBE should be thrown. */
	public static boolean safeGet(boolean[] arr, int index)
	{
		return (index >= 0 && arr.length > index) ? arr[index] : null;
	}
	
	/** @return Value at the given index or null if AIOOBE should be thrown. */
	public static <T> T safeGet(T[] arr, int index)
	{
		return (index >= 0 && arr.length > index) ? arr[index] : null;
	}
	
	public static boolean isInRange(int value, int min, int max)
	{
		if (value < min)
			return false;
		if (value > max)
			return false;
		
		return true;
	}
	
	public static boolean isInRange(long value, long min, long max)
	{
		if (value < min)
			return false;
		if (value > max)
			return false;
		
		return true;
	}
	
	public static boolean isInRange(double value, double min, double max)
	{
		if (value < min)
			return false;
		if (value > max)
			return false;
		
		return true;
	}
	
	public static boolean isInRange(float value, float min, float max)
	{
		if (value < min)
			return false;
		if (value > max)
			return false;
		
		return true;
	}
	
	/** @return Hashcode representation of the collection and its elements. */
	public static <E> int hashCode(Collection<E> collection)
	{
		int hashCode = 1;
		Iterator<E> i = collection.iterator();
		while (i.hasNext())
		{
			E obj = i.next();
			hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
		}
		return hashCode;
	}
	
	public static String convertToLineagePriceFormat(double price)
	{
		if (price < 10000.0)
			
			return Math.round(price) + "";
			
		if (price < 1000000.0)
			return reduceDecimals(price / 1000.0, 1) + "k";
			
		if (price < 1000000000.0)
			return reduceDecimals(price / 1000.0 / 1000.0, 1) + "kk";
		
		return reduceDecimals(price / 1000.0 / 1000.0 / 1000.0, 1) + "kkk";
	}
	
	public static String reduceDecimals(double original, int nDecim)
	{
		return reduceDecimals(original, nDecim, false);
	}
	
	public static String reduceDecimals(double original, int nDecim, boolean round)
	{
		String decimals = "#";
		if (nDecim > 0)
		{
			decimals = decimals + ".";
			for (int i = 0; i < nDecim; i++)
			{
				decimals = decimals + "#";
			}
		}
		DecimalFormat df = new DecimalFormat(decimals);
		return df.format(round ? Math.round(original) : original).replace(",", ".");
	}
	public static String getFullClassName(int classId)
	{
		switch (classId)
		{
			case 0:
				return "Human Fighter";
			case 1:
				return "Warrior";
			case 2:
				return "Gladiator";
			case 3:
				return "Warlord";
			case 4:
				return "Human Knight";
			case 5:
				return "Paladin";
			case 6:
				return "Dark Avenger";
			case 7:
				return "Rogue";
			case 8:
				return "Treasure Hunter";
			case 9:
				return "Hawkeye";
			case 10:
				return "Human Mystic";
			case 11:
				return "Human Wizard";
			case 12:
				return "Sorcerer";
			case 13:
				return "Necromancer";
			case 14:
				return "Warlock";
			case 15:
				return "Cleric";
			case 16:
				return "Bishop";
			case 17:
				return "Prophet";
			case 18:
				return "Elven Fighter";
			case 19:
				return "Elven Knight";
			case 20:
				return "Temple Knight";
			case 21:
				return "Sword Singer";
			case 22:
				return "Elven Scout";
			case 23:
				return "Plains Walker";
			case 24:
				return "Silver Ranger";
			case 25:
				return "Elven Mystic";
			case 26:
				return "Elven Wizard";
			case 27:
				return "Spellsinger";
			case 28:
				return "Elemental Summoner";
			case 29:
				return "Elven Oracle";
			case 30:
				return "Elven Elder";
			case 31:
				return "Dark Fighter";
			case 32:
				return "Palus Knight";
			case 33:
				return "Shillien Knight";
			case 34:
				return "Bladedancer";
			case 35:
				return "Assassin";
			case 36:
				return "Abyss Walker";
			case 37:
				return "Phantom Ranger";
			case 38:
				return "Dark Mystic";
			case 39:
				return "Dark Wizard";
			case 40:
				return "Spellhowler";
			case 41:
				return "Phantom Summoner";
			case 42:
				return "Shillien Oracle";
			case 43:
				return "Shillien Elder";
			case 44:
				return "Orc Fighter";
			case 45:
				return "Orc Raider";
			case 46:
				return "Destroyer";
			case 47:
				return "Monk";
			case 48:
				return "Tyrant";
			case 49:
				return "Orc Mystic";
			case 50:
				return "Orc Shaman";
			case 51:
				return "Overlord";
			case 52:
				return "Warcryer";
			case 53:
				return "Dwarven Fighter";
			case 54:
				return "Scavenger";
			case 55:
				return "Bounty Hunter";
			case 56:
				return "Artisan";
			case 57:
				return "Warsmith";
			case 88:
				return "Duelist";
			case 89:
				return "Dreadnought";
			case 90:
				return "Phoenix Knight";
			case 91:
				return "Hell Knight";
			case 92:
				return "Sagittarius";
			case 93:
				return "Adventurer";
			case 94:
				return "Archmage";
			case 95:
				return "Soultaker";
			case 96:
				return "Arcana Lord";
			case 97:
				return "Cardinal";
			case 98:
				return "Hierophant";
			case 99:
				return "Eva's Templar";
			case 100:
				return "Sword Muse";
			case 101:
				return "Wind Rider";
			case 102:
				return "Moonlight Sentinel";
			case 103:
				return "Mystic Muse";
			case 104:
				return "Elemental Master";
			case 105:
				return "Eva's Saint";
			case 106:
				return "Shillien Templar";
			case 107:
				return "Spectral Dancer";
			case 108:
				return "Ghost Hunter";
			case 109:
				return "Ghost Sentinel";
			case 110:
				return "Storm Screamer";
			case 111:
				return "Spectral Master";
			case 112:
				return "Shillien Saint";
			case 113:
				return "Titan";
			case 114:
				return "Grand Khavatari";
			case 115:
				return "Dominator";
			case 116:
				return "Doom Cryer";
			case 117:
				return "Fortune Seeker";
			case 118:
				return "Maestro";
			case 123:
				return "Kamael Soldier";
			case 124:
				return "Kamael Soldier";
			case 125:
				return "Trooper";
			case 126:
				return "Warder";
			case 127:
				return "Berserker";
			case 128:
				return "Soul Breaker";
			case 129:
				return "Soul Breaker";
			case 130:
				return "Arbalester";
			case 131:
				return "Doombringer";
			case 132:
				return "Soul Hound";
			case 133:
				return "Soul Hound";
			case 134:
				return "Trickster";
			case 135:
				return "Inspector";
			case 136:
				return "Judicator";
			default:
				return "None";
		}
	}
	
	public static String getNetworkInfo()
	{
		StringBuilder result = new StringBuilder();
		result.append("Auth: ").append(Config.GAME_SERVER_LOGIN_HOST).append('\n');
		result.append("Game: ");
		try
		{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements())
			{
				NetworkInterface iface = interfaces.nextElement();
				if (iface.isLoopback() || !iface.isUp())
					continue;
				
				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				String tmp;
				while (addresses.hasMoreElements())
				{
					InetAddress addr = addresses.nextElement();
					tmp = addr.getHostAddress();
					result.append(" ").append(tmp).append('\n');
				}
			}
		}
		catch (SocketException e)
		{
			return "none";
		}
		
		return result.toString();
	}
	
	public static String getFortName(Player player, int id)
	{
		return new CustomMessage("common.fort." + id, player).toString();
	}
	
	public static String ArrayToString(String[] array, int start)
	{
		String text = "";
		if (array.length > 1)
		{
			int count = 1;
			for (int i = start; i < array.length; i++)
			{
				text = text + (count > 1 ? " " : "") + array[i];
				count++;
			}
		}
		else
		{
			text = array[start];
		}
		return text;
	}
	
	public static boolean getClanPay(Player player, int itemid, long price, boolean b)
	{
		if (player.getClan() == null)
			return false;
		Clan clan = player.getClan();
		
		long wh = clan.getWarehouse().getCountOf(itemid);
		if (clan.getWarehouse().getCountOf(itemid) >= price)
		{
			clan.getWarehouse().destroyItemByItemId(itemid, price);
			return true;
		}
		
		long enought = price - wh;
		enoughtItem(player, itemid, enought);
		return false;
	}
	
	public static boolean getPay(Player player, int itemid, long count, boolean sendMessage)
	{
		if (count == 0)
			return true;
		
		boolean check = false;
		switch (itemid)
		{
			case ItemTemplate.ITEM_ID_FAME:
				if (player.getFame() >= count)
				{
					player.setFame(player.getFame() - (int) count, "Util.GetPay");
					check = true;
				}
				break;
			case ItemTemplate.ITEM_ID_CLAN_REPUTATION_SCORE:
				if (player.getClan() != null && player.getClan().getLevel() >= 5 && player.getClan().getLeader().isClanLeader() && player.getClan().getReputationScore() >= count)
				{
					player.getClan().incReputation((int) -count, false, "Util.GetPay");
					check = true;
				}
				break;
			case ItemTemplate.ITEM_ID_PC_BANG_POINTS:
				if (player.getPcBangPoints() >= count)
					if (player.reducePcBangPoints((int) count))
						check = true;
				break;
			default:
				if (player.getInventory().getCountOf(itemid) >= count)
				{
					if (player.getInventory().destroyItemByItemId(itemid, count))
						check = true;
				}
				break;
		}
		if (!check)
		{
			if (sendMessage)
				enoughtItem(player, itemid, count);
			
			return false;
		}
		if (sendMessage)
			player.sendMessage(new CustomMessage("util.getpay", player).addString(formatPay(player, count, itemid)));
		
		return true;
	}
	
	private static void enoughtItem(Player player, int itemid, long count)
	{
		player.sendPacket(new ExShowScreenMessage(new CustomMessage("util.enoughItemCount", player).addString(formatPay(player, count, itemid)).toString(), 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, -1, false));
		player.sendMessage(new CustomMessage("util.enoughItemCount", player).addString(formatPay(player, count, itemid)));
	}
	
	public static String formatPay(Player player, long count, int item)
	{
		if (count > 0L)
			return formatAdena(count) + " " + getItemName(item);
		
		return new CustomMessage("price.free", player).toString();
	}
	
	public static String getItemName(int itemId)
	{
		if (itemId == -300)
			return "Fame";
		if (itemId == -100)
			return "PC bang Point";
		if (itemId == -200)
			return "Clan Reputation score";
		
		return ItemHolder.getInstance().getTemplate(itemId).getName();
	}

	public static String getItemIcon(int itemId)
	{
		return ItemHolder.getInstance().getTemplate(itemId).getIcon();
	}
	
	public static void sayToAll(String address, String[] replacements)
	{
		Announcements.getInstance().announceByCustomMessage(address, replacements, ChatType.CRITICAL_ANNOUNCE);
	}

	public static void communityNextPage(Player player, String link)
	{
		ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(link);
		if (handler != null)
			handler.onBypassCommand(player, link);
	}
}