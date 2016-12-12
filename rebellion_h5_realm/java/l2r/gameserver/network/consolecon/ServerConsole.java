package l2r.gameserver.network.consolecon;

import l2r.gameserver.network.consolecon.send.AnswereRequestListenServerConsole;
import l2r.gameserver.network.consolecon.send.ServerConsoleMessage;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.inc.incolution.util.list.IncArrayList;
import org.inc.incolution.util.misc.IncFixedArrayQueue;
/**
 * 
 *
 * @author Forsaiken
 */
public final class ServerConsole extends Thread
{
	private static final int MAX_QUEUE_SIZE = 1024;
	private static final int MAX_PACKET_LEN = 1024 * 52;
	
	private static ServerConsole _instance;
	
	public static final void init()
	{
		_instance = new ServerConsole();
	}
	
	public static final ServerConsole getInstance()
	{
		return _instance;
	}
	
	private final ServerConsoleMessageQueue _stdOut;
	private final ServerConsoleMessageQueue _errOut;
	private final ServerConsoleMessageQueue _logOut;
	
	private final IncArrayList<Console> _listeners;
	private final IncArrayList<MessageEntry> _toSendQueue;
	
	@SuppressWarnings("resource")
	private ServerConsole()
	{
		/*if (!CustomConfig.CONSOLE_CLIENT_SERVER_ENABLED || !CustomConfig.CONSOLE_CLIENT_ALLOW_SERVER_CONSOLE)
		{
			_stdOut = null;
			_errOut = null;
			_logOut = null;
			_listeners = null;
			_toSendQueue = null;
		}TODO implement config
		else*/
		{
			_stdOut = new ServerConsoleMessageQueue(ServerConsoleMessage.TYPE_STD);
			_errOut = new ServerConsoleMessageQueue(ServerConsoleMessage.TYPE_ERR);
			_logOut = new ServerConsoleMessageQueue(ServerConsoleMessage.TYPE_LOG);
			
			new ModStdOut(_stdOut);
			new ModErrOut(_errOut);
			
			_listeners = new IncArrayList<>();
			_toSendQueue = new IncArrayList<>();
			
			super.setDaemon(true);
			super.setPriority(Thread.NORM_PRIORITY - 2);
			super.start();
		}
	}
	
	public final void addListener(final Console listener)
	{
		if (_listeners == null)
		{
			listener.sendPacket(AnswereRequestListenServerConsole.STATIC_PACKET_LISTEN_DISABLE);
			return;
		}
		
		synchronized (_listeners)
		{
			if (!_listeners.contains(listener))
				_listeners.add(listener);
		}
		
		listener.sendPacket(AnswereRequestListenServerConsole.STATIC_PACKET_LISTEN_ENABLE);
	}
	
	private final void broadcastMessages(final Console listener, final byte type, final IncFixedArrayQueue<MessageEntry> toSend)
	{
		_toSendQueue.clear();
		MessageEntry message, notSended = null;
		int len = 0;
		while (!toSend.isEmpty() || notSended != null)
		{
			if (notSended != null)
			{
				message = notSended;
				notSended = null;
			}
			else
			{
				message = toSend.poll();
			}
			
			if (len + message.length() > MAX_PACKET_LEN)
			{
				if (message.length() < MAX_PACKET_LEN)
					notSended = message;
				
				listener.sendPacket(new ServerConsoleMessage(type, _toSendQueue.toArray(new MessageEntry[_toSendQueue.size()])));
				_toSendQueue.clear();
				len = 0;
			}
			else
			{
				_toSendQueue.addLast(message);
				len += message.length();
			}
		}
		
		if (!_toSendQueue.isEmpty())
			listener.sendPacket(new ServerConsoleMessage(type, _toSendQueue.toArray(new MessageEntry[_toSendQueue.size()])));
	}
	
	public final void removeListener(final Console listener)
	{
		if (_listeners != null)
		{
			synchronized (_listeners)
			{
				_listeners.remove(listener);
			}
		}
		
		listener.sendPacket(AnswereRequestListenServerConsole.STATIC_PACKET_LISTEN_DISABLE);
	}
	
	public final void publish(final String formated, final LogRecord record)
	{
		if (_logOut == null)
			return;
		
		try
		{
			_logOut.queue(formated, record.getMillis(), record.getLoggerName(), record.getLevel());
		}
		catch (final Throwable t)
		{
			
		}
	}
	
	@Override
	public final void run()
	{
		Console[] listeners;
		
		while (true)
		{
			if (!_listeners.isEmpty())
			{
				synchronized (_listeners)
				{
					listeners = _listeners.toArray(new Console[_listeners.size()]);
				}
				
				_stdOut.prepare();
				_errOut.prepare();
				_logOut.prepare();
				
				for (final Console listener : listeners)
				{
					IncFixedArrayQueue<MessageEntry> messages = _stdOut.get(listener);
					if (messages != null)
						broadcastMessages(listener, ServerConsoleMessage.TYPE_STD, messages);
					
					messages = _errOut.get(listener);
					if (messages != null)
						broadcastMessages(listener, ServerConsoleMessage.TYPE_ERR, messages);
					
					messages = _logOut.get(listener);
					if (messages != null)
						broadcastMessages(listener, ServerConsoleMessage.TYPE_LOG, messages);
				}
			}
			
			try
			{
				Thread.sleep(1000L);
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public final void publish(final SendableConsolePacket packet)
	{
		Console console;
		for (int i = _listeners.size(); i-- > 0;)
		{
			console = _listeners.getUnsafe(i);
			if (console == null)
				continue;
			
			if (console.getConnection().isClosed())
			{
				removeListener(console);
			}
			else
			{
				console.sendPacket(packet);
			}
		}
	}
	
	public final class ServerConsoleMessageQueue
	{
		private final byte _type;
		private final IncFixedArrayQueue<MessageEntry> _queue;
		private int _lastMessageId;
		
		private MessageEntry[] _preparedMessages;
		private int _preparedLastMessageId;
		
		public ServerConsoleMessageQueue(final byte type)
		{
			_type = type;
			_queue = new IncFixedArrayQueue<>(MAX_QUEUE_SIZE);
		}
		
		public final void queue(final String val, final String logger, final Level level)
		{
			queue(val, System.currentTimeMillis(), logger, level);
		}
		
		public final void queue(String val, final long time, final String logger, final Level level)
		{
			if (val == null)
				val = "null";
			
			synchronized (_queue)
			{
				final MessageEntry entry = new MessageEntry(++_lastMessageId, time, level, logger, val);
				while (!_queue.offer(entry))
				{
					_queue.poll();
				}
			}
		}
		
		public final void prepare()
		{
			synchronized (_queue)
			{
				if (_preparedLastMessageId == _lastMessageId)
					return;
				
				_preparedMessages = _queue.toArray(new MessageEntry[_queue.size()]);
				_preparedLastMessageId = _lastMessageId;
			}
		}
		
		public final IncFixedArrayQueue<MessageEntry> get(final Console console)
		{
			final MessageEntry[] preparedMessages;
			final int preparedLastMessageId;
			
			synchronized (_queue)
			{
				preparedMessages = _preparedMessages;
				preparedLastMessageId = _preparedLastMessageId;
			}
			
			if (preparedMessages == null)
				return null;
			
			final IncFixedArrayQueue<MessageEntry> messages;
			
			final int diff = Math.min(preparedLastMessageId - console.getAndSetLastServerConsoleMessageId(_type, preparedLastMessageId), MAX_QUEUE_SIZE);
			
			if (diff > 0)
			{
				final int minMessageId = preparedLastMessageId - diff;
				messages = new IncFixedArrayQueue<>(diff);
				for (final MessageEntry entry : preparedMessages)
				{
					if (entry.getId() > minMessageId)
					{
						messages.offer(entry);
					}
				}
			}
			else
			{
				messages = null;
			}
			
			return messages;
		}
	}
	
	private abstract class ModOut extends PrintStream
	{
		protected final ServerConsoleMessageQueue _queue;
		
		protected ModOut(final OutputStream os, final ServerConsoleMessageQueue queue)
		{
			super(os);
			_queue = queue;
		}
		
		protected abstract void queue(final String val);
		
		@Override
		public final void print(final String val)
		{
			super.print(val);
			queue(String.valueOf(val));
		}
		
		@Override
		public final void print(final boolean val)
		{
			super.print(val);
			queue(String.valueOf(val));
		}
		
		@Override
		public final void print(final char val)
		{
			super.print(val);
			queue(String.valueOf(val));
		}
		
		@Override
		public final void print(final char[] val)
		{
			super.print(val);
			queue(String.valueOf(val));
		}
		
		@Override
		public final void print(final double val)
		{
			super.print(val);
			queue(String.valueOf(val));
		}
		
		@Override
		public final void print(final float val)
		{
			super.print(val);
			queue(String.valueOf(val));
		}
		
		@Override
		public final void print(final int val)
		{
			super.print(val);
			queue(String.valueOf(val));
		}
		
		@Override
		public final void print(final long val)
		{
			super.print(val);
			queue(String.valueOf(val));
		}
		
		@Override
		public final void print(final Object val)
		{
			super.print(val);
			queue(String.valueOf(val));
		}
	}
	
	private final class ModStdOut extends ModOut
	{
		public ModStdOut(final ServerConsoleMessageQueue queue)
		{
			super(System.out, queue);
			System.setOut(this);
		}
		
		@Override
		protected final void queue(final String val)
		{
			_queue.queue(val, "Std", Level.CONFIG);
		}
	}
	
	private final class ModErrOut extends ModOut
	{
		public ModErrOut(final ServerConsoleMessageQueue queue)
		{
			super(System.err, queue);
			System.setErr(this);
		}
		
		@Override
		protected final void queue(final String val)
		{
			_queue.queue(val, "Err", Level.SEVERE);
		}
	}
	
	public final class MessageEntry
	{
		private final int _id;
		private final long _time;
		private final Level _level;
		private final String _logger;
		private final String _message;
		
		public MessageEntry(final int id, final long time, final Level level, final String logger, final String message)
		{
			_id = id;
			_time = time;
			_level = level;
			_logger = logger == null ? "Anonym" : logger;
			_message = message;
		}
		
		public final int getId()
		{
			return _id;
		}
		
		public final long getTime()
		{
			return _time;
		}
		
		public final String getMessage()
		{
			return _message;
		}
		
		public final String getLogger()
		{
			return _logger;
		}
		
		public final int getLevel()
		{
			return _level.intValue();
		}
		
		public final int length()
		{
			return (_message.length() * 2) + (_logger.length() * 2) + 12;
		}
	}
}