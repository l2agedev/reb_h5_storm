package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.achievements.Achievements;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.randoms.CharacterEmails;
import l2r.gameserver.randoms.CharacterIntro;
import l2r.gameserver.randoms.PvPCharacterIntro;

public class RequestTutorialPassCmdToServer extends L2GameClientPacket
{
	// format: cS

	String _bypass = null;

	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Quest tutorial = QuestManager.getQuest(255);

		if(tutorial != null)
			player.processQuestEvent(tutorial.getName(), _bypass, null);
		
		if (_bypass.startsWith("emailvalidation") && Config.ENABLE_EMAIL_VALIDATION)
		{
			String[] cm = _bypass.split(" ");
			
			if (cm.length < 3)
			{
				player.sendMessage("Please fill all required fields.");
				return;
			}
			
			String command = cm[0];
			String email1 = cm[1];
			String email2 = cm[2];
			String friend = "";
			if (cm.length > 3)
				friend = cm[3];
			
			CharacterEmails.change(command, player, email1, email2, friend);
		}
		else if (_bypass.startsWith("_bbs_achievements") && Config.ENABLE_ACHIEVEMENTS)
		{
			String[] cm = _bypass.split(" ");
			
			Achievements.getInstance().usebypass(player, _bypass, cm);
		}
		else if(Config.ENABLE_ACHIEVEMENTS && _bypass.startsWith("_bbs_achievements_cat"))
		{
			String[] cm = _bypass.split(" ");

			int page = 0;
			if (cm.length < 1)
				page = 1;
			else
				page = Integer.parseInt(cm[2]);
				
			Achievements.getInstance().generatePage(player, Integer.parseInt(cm[1]), page);
			return;
		}
		else if(Config.ENABLE_ACHIEVEMENTS && _bypass.startsWith("_bbs_achievements_close"))
		{
			String[] cm = _bypass.split(" ");

			Achievements.getInstance().usebypass(player, _bypass, cm);
			return;
		}
		else if (_bypass.startsWith("_pvpcharintro"))
		{
			PvPCharacterIntro.getInstance().bypassIntro(player, _bypass);
		}
		else if (_bypass.startsWith("characterintro_"))
		{
			if (_bypass.startsWith("characterintro_emailvalidation"))
			{
				String[] cm = _bypass.split(" ");
				
				if (cm.length < 3)
				{
					player.sendMessage("Please fill all required fields.");
					return;
				}
				
				String email1 = cm[1];
				String email2 = cm[2];
				
				CharacterIntro.sendEmailValidation(player, email1, email2);
			}
			if (_bypass.startsWith("characterintro_referral"))
			{
				
				if (!Config.ENABLE_REFERRAL_SYSTEM)
				{
					player.sendMessageS("Sorry, but referral system is Disabled!", 5);
					return;
				}
				
				String[] cm = _bypass.split(" ");
				
				if (cm.length < 2)
				{
					player.sendMessage("Please fill all required fields.");
					return;
				}
				
				String referral = cm[1];
				
				CharacterIntro.referralSystem(player, referral);
			}
			else
			{
				String command = _bypass.substring(15);
				CharacterIntro.bypassIntro(player, command);
			}
		}
	}
}