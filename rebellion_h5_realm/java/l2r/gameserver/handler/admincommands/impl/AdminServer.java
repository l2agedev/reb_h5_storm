package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.DeadlockDetector;
import l2r.gameserver.ai.CharacterAI;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.model.WorldRegion;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.instances.RaidBossInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.templates.npc.NpcTemplate;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class handles following admin commands: - help path = shows
 * admin/path file to char, should not be used by GM's directly
 */
public class AdminServer implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_server,
		admin_check_actor,
		admin_setservervar,
		admin_set_ai_interval,
		admin_spawn2,
		admin_deadlock_check,
		admin_deadlock_fix
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		
		switch(command)
		{
			case admin_server:
				try
				{
					String val = fullString.substring(13);
					showHelpPage(activeChar, val);
				}
				catch(StringIndexOutOfBoundsException e)
				{
					// case of empty filename
				}
				break;
			case admin_check_actor:
				GameObject obj = activeChar.getTarget();
				if(obj == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminserver.message1", activeChar));
					return false;
				}

				if(!obj.isCreature())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminserver.message2", activeChar));
					return false;
				}

				Creature target = (Creature)obj;
				CharacterAI ai = target.getAI();
				if(ai == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminserver.message3", activeChar));
					return false;
				}

				Creature actor = ai.getActor();
				if(actor == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminserver.message4", activeChar));
					return false;
				}

				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminserver.message5", activeChar, actor));
				break;
			case admin_setservervar:
				if(wordList.length != 3)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminserver.message6", activeChar));
					return false;
				}
				ServerVariables.set(wordList[1], wordList[2]);
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminserver.message7", activeChar));
				break;
			case admin_set_ai_interval:
				if(wordList.length != 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminserver.message8", activeChar));
					return false;
				}
				int interval = Integer.parseInt(wordList[1]);
				int count = 0;
				int count2 = 0;
				for(final NpcInstance npc : GameObjectsStorage.getAllNpcsForIterate())
				{
					if(npc == null || npc instanceof RaidBossInstance)
						continue;
					final CharacterAI char_ai = npc.getAI();
					if(char_ai.isDefaultAI())
						try
					{
							final java.lang.reflect.Field field = l2r.gameserver.ai.DefaultAI.class.getDeclaredField("AI_TASK_DELAY");
							field.setAccessible(true);
							field.set(char_ai, interval);

							if(char_ai.isActive())
							{
								char_ai.stopAITask();
								count++;
								WorldRegion region = npc.getCurrentRegion();
								if(region != null && region.isActive())
								{
									char_ai.startAITask();
									count2++;
								}
							}
					}
					catch(Exception e)
					{

					}
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminserver.message9", activeChar, count, count2));
				break;
			case admin_spawn2: // Игнорирует запрет на спавн рейдбоссов
				StringTokenizer st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					String id = st.nextToken();
					int respawnTime = 30;
					int mobCount = 1;
					if(st.hasMoreTokens())
						mobCount = Integer.parseInt(st.nextToken());
					if(st.hasMoreTokens())
						respawnTime = Integer.parseInt(st.nextToken());
					spawnMonster(activeChar, id, respawnTime, mobCount);
				}
				catch(Exception e)
				{}
				break;
			case admin_deadlock_check:
				activeChar.sendMessage("Checking for deadlocks...");
				
				if (DeadlockDetector.checkForDeadlocks())
					activeChar.sendMessage("DEADLOCKS FOUND!!!! Logged!");
				else
					activeChar.sendMessage("No deadlocks found...");
				break;
			case admin_deadlock_fix:
				activeChar.sendMessage("Trying to fix deadlocks...");
				
				if (DeadlockDetector.fixDeadlocks())
					activeChar.sendMessage("Deadlocks fixed, but no guarantee that they are fixed. Better check if everything is running ok.");
				else
					activeChar.sendMessage("No deadlocks fixed, you have to schedule a deadlock check with positive results before calling this command.");
				break;
		}

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	// PUBLIC & STATIC so other classes from package can include it directly
	public static void showHelpPage(Player targetChar, String filename)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("admin/" + filename);
		targetChar.sendPacket(adminReply);
	}

	private void spawnMonster(Player activeChar, String monsterId, int respawnTime, int mobCount)
	{
		GameObject target = activeChar.getTarget();
		if(target == null)
			target = activeChar;

		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher regexp = pattern.matcher(monsterId);
		NpcTemplate template;
		if(regexp.matches())
		{
			// First parameter was an ID number
			int monsterTemplate = Integer.parseInt(monsterId);
			template = NpcHolder.getInstance().getTemplate(monsterTemplate);
		}
		else
		{
			// First parameter wasn't just numbers so go by name not ID
			monsterId = monsterId.replace('_', ' ');
			template = NpcHolder.getInstance().getTemplateByName(monsterId);
		}

		if(template == null)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminserver.message10", activeChar));
			return;
		}

		try
		{
			SimpleSpawner spawn = new SimpleSpawner(template);
			spawn.setLoc(target.getLoc());
			spawn.setAmount(mobCount);
			spawn.setHeading(activeChar.getHeading());
			spawn.setRespawnDelay(respawnTime);
			spawn.setReflection(activeChar.getReflection());
			spawn.init();
			if(respawnTime == 0)
				spawn.stopRespawn();
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminserver.message11", activeChar, template.name, target.getObjectId()));
		}
		catch(Exception e)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminserver.message12", activeChar));
		}
	}
}