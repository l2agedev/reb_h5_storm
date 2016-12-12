package l2r.gameserver.utils;

import l2r.gameserver.model.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Files
{
	private static final Logger _log = LoggerFactory.getLogger(Files.class);
	
	/**
	 * Сохраняет строку в файл в кодировке UTF-8.<br>
	 * Если такой файл существует, то перезаписывает его.
	 * @param path путь к файлу
	 * @param string сохраняемая строка
	 */
	public static void writeFile(String path, String string)
	{
		try
		{
			FileUtils.writeStringToFile(new File(path), string, "UTF-8");
		}
		catch (IOException e)
		{
			_log.error("Error while saving file : " + path, e);
		}
	}
	
	public static boolean copyFile(String srcFile, String destFile)
	{
		try
		{
			FileUtils.copyFile(new File(srcFile), new File(destFile), false);
			return true;
		}
		catch (IOException e)
		{
			_log.error("Error while copying file : " + srcFile + " to " + destFile, e);
		}
		
		return false;
	}
	
	public static String read(String name)
	{
		if (name == null)
			return null;
		
		File file = new File("./" + name);
		
		if (!file.exists())
			return null;
		
		String content = null;
		
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new UnicodeReader(new FileInputStream(file), "UTF-8"));
			StringBuffer sb = new StringBuffer();
			String s = "";
			while ((s = br.readLine()) != null)
				sb.append(s).append("\n");
			content = sb.toString();
			sb = null;
		}
		catch (Exception e)
		{
		}
		finally
		{
			try
			{
				if (br != null)
					br.close();
			}
			catch (Exception e1)
			{
			}
		}
		
		return content;
	}
	
	/**
	 * Сохраняет строку в файл в кодировке UTF-8.<br>
	 * Если такой файл существует, то перезаписывает его.
	 * @param path путь к файлу
	 * @param string сохраняемая строка
	 */
	public static void writeFile1(String path, String string)
	{
		if (string == null || string.length() == 0)
			return;
		
		File target = new File(path);
		
		if (!target.exists())
			try
			{
				target.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace(System.err);
			}
		
		try
		{
			FileOutputStream fos = new FileOutputStream(target);
			fos.write(string.getBytes("UTF-8"));
			fos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
		}
	}
	
	public static String read(String name, Player player)
	{
		if (player == null)
			return "";
		return read(name, player.getLanguage());
	}
	
	public static String read(String name, Language lang)
	{
		String tmp = langFileName(name, lang.getShortName());
		
		long last_modif = lastModified(tmp); // время модификации локализованного файла
		if (last_modif > 0) // если он существует
		{
			if (last_modif >= lastModified(name)) // и новее оригинального файла
				return Strings.bbParse(read(tmp)); // то вернуть локализованный
				
			_log.warn("Last modify of " + name + " more then " + tmp); // если он существует но устарел - выругаться в лог
		}
		
		return Strings.bbParse(read(name)); // если локализованный файл отсутствует вернуть оригинальный
	}
	
	public static String langFileName(String name, String lang)
	{
		if (lang == null || lang.equalsIgnoreCase("en") || lang.equalsIgnoreCase("ENGLISH"))
			lang = "";
		
		String tmp;
		
		tmp = name.replaceAll("(.+)(\\.htm)", "$1-" + lang + "$2");
		if (!tmp.equals(name) && lastModified(tmp) > 0)
			return tmp;
		
		tmp = name.replaceAll("(.+)(/[^/].+\\.htm)", "$1/" + lang + "$2");
		if (!tmp.equals(name) && lastModified(tmp) > 0)
			return tmp;
		
		tmp = name.replaceAll("(.+?/html)/", "$1-" + lang + "/");
		if (!tmp.equals(name) && lastModified(tmp) > 0)
			return tmp;
		
		if (lastModified(name) > 0)
			return name;
		
		return null;
	}
	
	public static long lastModified(String name)
	{
		if (name == null)
			return 0;
		
		return new File(name).lastModified();
	}
}