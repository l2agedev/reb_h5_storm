package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.NetPingPacket;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.scripts.Functions;

public class Ping extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[]
	{
		"ping"
	};
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.equals("ping") && Config.ENABLE_PING_COMMAND)
		{
			if (activeChar.antiFlood.canPing())
			{
				activeChar.sendChatMessage(0, ChatType.PARTY.ordinal(), "PING", (activeChar.isLangRus() ? "Обработка запроса..." : "Request processing..."));
				activeChar.sendPacket(new NetPingPacket(activeChar));
				ThreadPoolManager.getInstance().schedule(new AnswerTask(activeChar), 3000L);
				return true;
			}
			else
				activeChar.sendChatMessage(0, ChatType.PARTY.ordinal(), "PING", (activeChar.isLangRus() ? "Анти защиты от наводнений. Пожалуйста, повторите попытку позже.." : "Anti flood protection. Please try again later."));
		}
		return false;
	}
	
	static final class AnswerTask implements Runnable
	{
		private final Player _player;
		
		public AnswerTask(Player player)
		{
			_player = player;
		}
		
		public void run()
		{
			int ping = _player.getPing();
			if (ping != -1)
				_player.sendChatMessage(0, ChatType.PARTY.ordinal(), "PING", (_player.isLangRus() ? "Текущий пинг до сервера: " + ping + " мс." : "Current ping to the server is " + ping + " ms."));
			
			else
				_player.sendChatMessage(0, ChatType.PARTY.ordinal(), "PING", (_player.isLangRus() ? "Данные от клиент всё ещё не получены." : "The data from the client is still pending."));
		}
	}
}
