package l2r.gameserver.listener.actor.player.impl;

import l2r.commons.lang.reference.HardReference;
import l2r.gameserver.Config;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.randoms.BotCheckerManager;
import l2r.gameserver.utils.AutoBan;
import l2r.gameserver.utils.Log;

public class BotCheckerAnswerListener implements OnAnswerListener
{
	private final HardReference<Player> _playerRef;
	private final int _qId;
	
	public BotCheckerAnswerListener(Player player, int questionId)
	{
		_playerRef = player.getRef();
		_qId = questionId;
	}
	
	public void sayYes()
	{
		Player player = _playerRef.get();
		if (player == null)
			return;
		
		boolean rightAnswer = BotCheckerManager.checkAnswer(_qId, true);
		if (rightAnswer)
			player.sendChatMessage(0, ChatType.BATTLEFIELD.ordinal(), "BotChecker", ""); // TODO
		else
			handlePunishment(player);
	}
	
	public void sayNo()
	{
		Player player = _playerRef.get();
		if (player == null)
			return;
		boolean rightAnswer = BotCheckerManager.checkAnswer(_qId, false);
		if (rightAnswer)
			player.sendChatMessage(0, ChatType.BATTLEFIELD.ordinal(), "BotChecker", ""); // TODO
		else
			handlePunishment(player);
	}
	
	private void handlePunishment(Player player)
	{
		switch(Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TYPE)
		{
			case "KICK":
			{
				if (player != null)
				{
					player.sendChatMessage(0, ChatType.BATTLEFIELD.ordinal(), "BotChecker", ""); // TODO
					player.sendMessage("You have failed on captcha, so the server will kick you. Cya :)");
					player.kick();
					Log.bots("BotCheker: "); // TODO
				}
				else
					Log.bots("Warning: Captcha System: Something went wrong, player is null.....");
				break;
			}
			case "BANCHAR":
			{
				int punishTime = Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TIME;
				if (punishTime != 0 && player != null)
				{
					player.sendChatMessage(0, ChatType.BATTLEFIELD.ordinal(), "BotChecker", ""); // TODO
					player.sendMessage(new CustomMessage("admincommandhandlers.YoureBannedByGM", player));
					AutoBan.Banned(player.getName(), -100, punishTime, "Failed on Captcha", player.getName());
					player.kick();
					Log.bots("BotCheker: "); // TODO
				}
				else
					Log.bots("Warning: Captcha System: Something went wrong, player is null or time is null");
				break;
			}
			case "JAIL":
			{
				int punishTime = Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TIME;
				if (punishTime != 0 && player != null)
				{
					player.sendChatMessage(0, ChatType.BATTLEFIELD.ordinal(), "BotChecker", ""); // TODO
					AutoBan.Jail(player.getName(), punishTime, "Failed on Captcha", null);
					Log.bots("BotCheker: "); // TODO
				}
				else
					Log.bots("Warning: Captcha System: Something went wrong, player is null or time is null");
				break;
			}
		}
	}
}
