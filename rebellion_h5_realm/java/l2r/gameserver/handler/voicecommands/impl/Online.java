package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;

public class Online extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList =
	{
		"online"
	};
	
	public String[] getVoicedCommandList()
	{
		return this._commandList;
	}
	
	private static int ONLINE = 0;
	static
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				ONLINE = GameObjectsStorage.getAllPlayersCount();
			}
		}, 1000, 30000);
	}
	
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (!Config.ALLOW_ONLINE_COMMAND)
			return false;
		
		if (command.equals("online"))
		{
			int onlinePlayers = ONLINE;
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.online.message1", activeChar, onlinePlayers));
			
			if (activeChar.isGM() && activeChar.getAccessLevel().canViewAccountInfo())
			{
				int offlineShops = 0;
				for (Player player : GameObjectsStorage.getAllPlayersForIterate())
				{
					if (player.isInOfflineMode())
						offlineShops++;
				}
				
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.online.message2", activeChar, offlineShops));
				onlinePlayers = GameObjectsStorage.getAllPlayers().size() - offlineShops;
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.online.message3", activeChar, onlinePlayers));
			}
			
			return true;
		}
		return false;
	}
}