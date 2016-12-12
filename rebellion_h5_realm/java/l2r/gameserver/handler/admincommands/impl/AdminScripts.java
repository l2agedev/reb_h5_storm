package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Scripts;

public class AdminScripts implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_scripts_reload,
		admin_script_load,
		admin_sreload,
		admin_sqreload,
		admin_quest_reload,
		admin_quest_load,
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.isGM())
			return false;

		switch(command)
		{
			case admin_scripts_reload:
			case admin_sreload:
			case admin_script_load:
				if(wordList.length < 2)
					return false;
				String param = wordList[1];
				if(param.equalsIgnoreCase("all"))
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminscripts.message1", activeChar));
					if(!Scripts.getInstance().reload())
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminscripts.message2", activeChar, Scripts.getInstance().getClasses().size()));
					else
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminscripts.message3", activeChar, Scripts.getInstance().getClasses().size()));
				}
				else if(!Scripts.getInstance().reload(param))
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminscripts.message4", activeChar));
				else
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminscripts.message5", activeChar));
				break;
			case admin_sqreload:
			case admin_quest_reload:
			case admin_quest_load:
				if(wordList.length < 2)
					return false;
				String quest = wordList[1];
				if(!Scripts.getInstance().reload("quests/" + quest))
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminscripts.message6", activeChar, quest));
				else
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminscripts.message7", activeChar, quest));
				reloadQuestStates(activeChar);
				break;
		}
		return true;
	}

	private void reloadQuestStates(Player p)
	{
		for(QuestState qs : p.getAllQuestsStates())
			p.removeQuestState(qs.getQuest().getName());

		Quest.restoreQuestStates(p);
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}