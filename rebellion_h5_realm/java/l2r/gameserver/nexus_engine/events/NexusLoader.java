package l2r.gameserver.nexus_engine.events;

import l2r.gameserver.nexus_engine.debug.DebugConsole;
import l2r.gameserver.nexus_engine.events.engine.EventConfig;
import l2r.gameserver.nexus_engine.events.engine.EventManager;
import l2r.gameserver.nexus_engine.events.engine.EventMapSystem;
import l2r.gameserver.nexus_engine.events.engine.EventRewardSystem;
import l2r.gameserver.nexus_engine.events.engine.EventWarnings;
import l2r.gameserver.nexus_engine.events.engine.lang.LanguageEngine;
import l2r.gameserver.nexus_engine.events.engine.main.OldStats;
import l2r.gameserver.nexus_engine.events.engine.main.base.MainEventInstanceTypeManager;
import l2r.gameserver.nexus_engine.events.engine.stats.EventStatsManager;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_engine.playervalue.PlayerValueEngine;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.text.TextBuilder;
import javolution.util.FastSet;

/**
 * @author hNoke
 * loads the engine.
 */
public class NexusLoader
{
	private static final Logger _log = LoggerFactory.getLogger(NexusLoader.class);
	
	public static final String version = "3.2";
	
	private static FileWriter fileWriter;
	private static final SimpleDateFormat _toFileFormat = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
	
	public enum NexusBranch
	{
		Freya(3.2),
		Hi5(3.2),
		Hi5Priv(3.2),
		Final(3.2);
		//InterludeL2jFrozen(1.5, "L2J-Interlude L2jFrozen", false, "./lib/", "config/nexus_serial.txt", true);
		
		public double _newestVersion;
		NexusBranch(double interfaceVersion)
		{
			_newestVersion = interfaceVersion;
		}
	}
	
	public static boolean debugConsole = false;
	public static boolean detailedDebug = false;
	public static boolean detailedDebugToConsole = false;
	public static boolean logToFile = false;
	
	//private static final SimpleDateFormat _formatter = new SimpleDateFormat("H:mm:ss");
	
	public static DebugConsole debug;
	private static NexusBranch _branch;
	private static String _desc;
	private static String _serialPath;
	private static double _interfaceVersion;
	//private static String _key;
	
	private static boolean loaded = false;
	private static boolean loading = false;
	private static boolean tryReconnect = false;
	private static boolean _instances;
	private static String _libsFolder;
	private static boolean _limitedHtml;
	
	// connection stuff
	private static Socket commandSocket = null;

	private static PrintWriter commandOut = null;
	private static BufferedReader commandIn = null;
	
	public static final void init(NexusBranch l2branch, double interfaceVersion, String desc, boolean allowInstances, String libsFolder, String serialPath, boolean limitedHtml)
	{
		if(_branch == null)
		{
			_branch = l2branch;
			_interfaceVersion = interfaceVersion;
			_serialPath = serialPath;
		}
		
		/*
		if(_key == null)
		{
			String key = null;
			
			InputStream is;
			InputStreamReader reader = null;
			BufferedReader bReader = null;
			
			try
			{
				is = new FileInputStream(new File(_serialPath));
				try
				{
					reader = new InputStreamReader(is, Charset.defaultCharset());
					bReader = new BufferedReader(reader);
					
					String line;
					while((line = bReader.readLine()) != null)
					{
						key = line;
						break;
					}
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				finally
				{
					try
					{
						is.close();
						if (reader != null)
							reader.close();
						if(bReader != null)
							bReader.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			
			_key = key;
			
			debug("License key: " + (_key == null ? "-" : _key));
		}
		*/
		loading = true;
		
		EventConfig.getInstance().loadGlobalConfigs();
		
		debugConsole = EventConfig.getInstance().getGlobalConfigBoolean("debug");
			if(!GraphicsEnvironment.isHeadless())
				debugConsole = false;
		if (debugConsole)
			loadDebugConsole(true);
		
		String fileName = createDebugFile();
		if (fileName != null)
			debug("Nexus Engine: Debug messages are stored in '" + fileName + "'");
		
		debug("Nexus Engine: Thanks for using a legal version of the engine.");
		
		_desc = desc;
		_instances = allowInstances;
		_libsFolder = libsFolder;
		
		_limitedHtml = limitedHtml;
		
		debug("Nexus Engine: Loading engine version " + version + "...");
		debug("Nexus Engine: Using " + _desc + " interface (for engine of v" + interfaceVersion + ").");
		
		if (interfaceVersion != l2branch._newestVersion)
			debug("Nexus Engine: Your interface is outdated for this engine!!! Please update it.", Level.SEVERE);
		
		OldStats.getInstance();
		
		NexusEvents.loadHtmlManager();
		
		logToFile = EventConfig.getInstance().getGlobalConfigBoolean("logToFile");
		detailedDebug = EventConfig.getInstance().getGlobalConfigBoolean("detailedDebug");
		detailedDebugToConsole = EventConfig.getInstance().getGlobalConfigBoolean("detailedDebugToConsole");
		
		LanguageEngine.init();
		
		EventManager.getInstance();
		EventConfig.getInstance().loadEventConfigs();
		
		EventMapSystem.getInstance().loadMaps();
		EventRewardSystem.getInstance();
		
		EventManager.getInstance().getMainEventManager().loadScheduleData();
		
		MainEventInstanceTypeManager.getInstance();
		
		EventStatsManager.getInstance();
		
		EventWarnings.getInstance();
		
		PlayerValueEngine.getInstance();
		
		loaded = true;
		debug("Nexus Engine: Version " + version + " successfully loaded.");

	}
	
	@SuppressWarnings("unused")
	private static final boolean isValid(String key)
	{
		try
		{
			try
			{
				commandSocket = new Socket("31.31.77.50", 4400);

				commandOut = new PrintWriter(commandSocket.getOutputStream(), true);
				commandIn = new BufferedReader(new InputStreamReader(commandSocket.getInputStream()));
			}
			catch (UnknownHostException e)
			{
				debug("Can't connect to the authentication server. Next try in 30 seconds.", Level.SEVERE);
				tryReconnect = true;
				return false;
			}
			catch (IOException e)
			{
				debug("Can't connect to the authentication server. Next try in 30 seconds.", Level.SEVERE);
				tryReconnect = true;
				return false;
			}

			try
			{
				commandSocket.setSoTimeout(30000);
			}
			catch (SocketException se)
			{
				debug("Unable to set commandSocket option SO_TIMEOUT");
			}
			
			commandOut.println("pkey " + key);

			String inputLine;
			boolean b = false;

			while ((inputLine = commandIn.readLine()) != null)
			{
				if (inputLine.startsWith("passed"))
				{
					b = true;
					break;
				}
				else if (inputLine.equals("failed"))
				{
					b = false;
					tryReconnect = false;
					debug("Nexus Engine: Wrong license. Engine not loaded!", Level.SEVERE);
					break;
				}
			}

			commandIn.close();
			commandOut.close();
			closeSockets();
			
			return b;
		}
		catch (Exception e)
		{
			debug("Connection to the authentication server timed out. Next try in 30 seconds.", Level.SEVERE);
			tryReconnect = true;
			closeSockets();
			return false;
		}
	}

	private static final void closeSockets()
	{
		try
		{
			commandSocket.close();
		}
		catch (IOException e)
		{
			NexusLoader.debug("IOException on close - " + e);
		}
	}
	
	@SuppressWarnings("unused")
	private static final void scheduleTryReconnect()
	{
		if(!loaded && tryReconnect)
		{
			CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					tryReconnect();
				}
			}, 30000);
		}
	}
	
	private static final void tryReconnect()
	{
		if(loaded || !tryReconnect)
			return;
		
		debug("Reconnecting to the auth server...");
		init(_branch, _interfaceVersion, _desc, _instances, _libsFolder, _serialPath, _limitedHtml);
	}
	
	public static final void loadDebugConsole(boolean onServerStart)
	{
		if(!GraphicsEnvironment.isHeadless())
		{
            DebugConsole.initGui();
            DebugConsole.info("Nexus Engine: Debug console initialized.");
		}
		else  if(!onServerStart) _log.info("Debug console can't be opened in this environment.");
	}
	
	private static boolean _gmsDebugging = false;
	private static Set<PlayerEventInfo> _gmsDebuggingSet = new FastSet<PlayerEventInfo>();
	
	public static final boolean isDebugging(PlayerEventInfo gm)
	{
		if(!_gmsDebugging)
			return false;
		return _gmsDebuggingSet.contains(gm);
	}
	
	public static final void addGmDebug(PlayerEventInfo gm)
	{
		if(!_gmsDebugging)
			_gmsDebugging = true;
		
		_gmsDebuggingSet.add(gm);
	}
	
	public static final void removeGmDebug(PlayerEventInfo gm)
	{
		if(!_gmsDebugging)
			return;
		
		_gmsDebuggingSet.remove(gm);
		
		if(_gmsDebuggingSet.isEmpty())
			_gmsDebugging = false;
	}
	
	public static final void debug(String msg, Level level)
	{
		if(!msg.startsWith("Nexus ") && !msg.startsWith("nexus"))
			msg = "Nexus Engine: " + msg;
		
		if(debugConsole)
			DebugConsole.log(level, msg);
		else
			_log.info(msg);

		if(_gmsDebugging)
		{
			sendToGms(msg, level, false);
		}
		
		writeToFile(level, msg, false);
	}
	
	public static final void debug(String msg)
	{
		if(!msg.startsWith("Nexus ") && !msg.startsWith("nexus"))
			msg = "Nexus Engine: " + msg;
			
		try
		{
			if(debugConsole)
				DebugConsole.info(msg);
			else
				_log.info(msg);
		}
		catch (Exception e)
		{
		}
		
		try
		{
			if(_gmsDebugging)
			{
				sendToGms(msg, Level.INFO, false);
			}
		}
		catch (Exception e)
		{
		}
		
		writeToFile(Level.INFO, msg, false);
	}
	
	public static int DEBUG_CHAT_CHANNEL_CLASSIC = 7;
	public static int DEBUG_CHAT_CHANNEL = 6; //TODO: add to configs
	
	public static final void sendToGms(String msg, Level level, boolean detailed)
	{
		try
		{
			for(PlayerEventInfo gm : _gmsDebuggingSet)
			{
				gm.creatureSay("*" + (detailed ? msg : msg.substring(14)) + "  (" + level.toString() + ")", (detailed ? "DD" : "DEBUG"), detailed ? DEBUG_CHAT_CHANNEL : DEBUG_CHAT_CHANNEL_CLASSIC);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static final void detailedDebug(String msg)
	{
		if(!msg.startsWith("DD "))
			msg = "DD:  " + msg;
		
		try
		{
			if(_gmsDebugging)
			{
				sendToGms(msg, Level.INFO, true);
			}
		}
		catch (Exception e)
		{
		}
		
		try
		{
			if(detailedDebugToConsole && debugConsole)
				DebugConsole.log(Level.INFO, msg);
		}
		catch (Exception e)
		{
		}
		
		writeToFile(Level.INFO, msg, true);
	}
	
	public static final boolean allowInstances()
	{
		return _instances;
	}
	
	public static final String getLibsFolderName()
	{
		return _libsFolder;
	}
	
	public static final boolean isLimitedHtml()
	{
		return _limitedHtml;
	}
	
	private static File debugFile;
	private static File detailedDebugFile;
	
	private static final String createDebugFile()
	{
		String path = "log/nexus";
		
		File folder = new File(path);
		if(!folder.exists())
		{
			if(!folder.mkdir())
				path = "log";
		}
		
		debugFile = new File(path + "/NexusEvents.log");
		if(!debugFile.exists())
		{
			try
			{
				debugFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		int id = 0;
		for(File f : folder.listFiles())
		{
			if(f.getName().startsWith("NexusEvents_detailed"))
			{
				try
				{
					String name = f.getName().substring(0, f.getName().length() - 4);
					int id2 = Integer.getInteger(name.substring(21));
					if(id2 > id)
						id = id2;
				}
				catch (Exception e)
				{
				}
			}
		}
		
		id += 1;
		
		detailedDebugFile = new File(path + "/NexusEvents_detailed_" + (id) + ".log");

		if(detailedDebugFile.exists())
		{
			try
			{
				detailedDebugFile.delete();	
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
			
		if(!detailedDebugFile.exists())
		{
			try
			{
				detailedDebugFile.createNewFile();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return detailedDebugFile.getAbsolutePath();
		//return path + "/NexusEvents_detailed_" + (id) + ".log";
	}
	
	public static void writeToFile(Level level, String msg, boolean detailed)
	{
		if(detailed)
		{
			//if(!detailedDebug) // already handled
			//	return;
		}
		else
		{
			if(!logToFile)
				return;
		}
		
		try 
		{
			if(!detailed)
				fileWriter = new FileWriter(debugFile, true);
			else
				fileWriter = new FileWriter(detailedDebugFile, true);
			
			fileWriter.write(_toFileFormat.format(new Date()) + ":  " + msg + " (" + level.getLocalizedName() + ")\r\n");
		} 
		catch (Exception e) 
		{
			if(debugConsole)
				DebugConsole.log(Level.WARNING, "Error writing debug msgs to file: " + e.toString());
		} 
		finally 
		{
			try 
			{
				fileWriter.close();
			} 
			catch (Exception e) { }
		}
	}
	
	public static String getTraceString(StackTraceElement[] trace)
	{
		final TextBuilder sbString = new TextBuilder();
		for (final StackTraceElement element : trace)
		{
			sbString.append(element.toString()).append("\n");
		}
		
		String result = sbString.toString();
		//TextBuilder.recycle(sbString);
		return result;
	}
	
	public static void shutdown()
	{
		EventWarnings.getInstance().saveData();
	}
	
	public static boolean loaded()
	{
		return loaded;
	}
	
	public static boolean loadedOrBeingLoaded()
	{
		return loading;
	}
}
