package l2r.gameserver.handler.admincommands.impl;

import l2r.commons.collections.MultiValueSet;
import l2r.gameserver.Config;
import l2r.gameserver.ai.CharacterAI;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.instancemanager.RaidBossSpawnManager;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.model.Spawner;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Scripts;
import l2r.gameserver.tables.SpawnTable;
import l2r.gameserver.templates.npc.NpcTemplate;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AdminSpawn implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_show_spawns,
		admin_spawn,
		admin_spawn_monster,
		admin_spawn_index,
		admin_spawn1,
		admin_setheading,
		admin_setai,
		admin_setaiparam,
		admin_dumpparams,
		admin_generate_loc,
		admin_dumpspawn
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		StringTokenizer st;
		NpcInstance target;
		Spawner spawn;
		NpcInstance npc;

		switch(command)
		{
			case admin_show_spawns:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/spawns.htm"));
				break;
			case admin_spawn_index:
				try
				{
					String val = fullString.substring(18);
					activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/spawns/" + val + ".htm"));
				}
				catch(StringIndexOutOfBoundsException e)
				{
				}
				break;
			case admin_spawn1:
				st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					String id = st.nextToken();
					int mobCount = 1;
					if(st.hasMoreTokens())
						mobCount = Integer.parseInt(st.nextToken());
					spawnMonster(activeChar, id, 0, mobCount);
				}
				catch(Exception e)
				{
					// Case of wrong monster data
				}
				break;
			case admin_spawn:
			case admin_spawn_monster:
				st = new StringTokenizer(fullString, " ");
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
				{
					// Case of wrong monster data
				}
				break;
			case admin_setai:
				if(activeChar.getTarget() == null || !activeChar.getTarget().isNpc())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message1", activeChar));
					return false;
				}

				st = new StringTokenizer(fullString, " ");
				st.nextToken();
				if(!st.hasMoreTokens())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message2", activeChar));
					return false;
				}
				String aiName = st.nextToken();
				target = (NpcInstance) activeChar.getTarget();

				Constructor<?> aiConstructor = null;
				try
				{
					if(!aiName.equalsIgnoreCase("npc"))
						aiConstructor = Class.forName("l2r.gameserver.ai." + aiName).getConstructors()[0];
				}
				catch(Exception e)
				{
					try
					{
						aiConstructor = Scripts.getInstance().getClasses().get("ai." + aiName).getConstructors()[0];
					}
					catch(Exception e1)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message3", activeChar));
						return false;
					}
				}

				if(aiConstructor != null)
				{
					try
					{
						target.setAI((CharacterAI) aiConstructor.newInstance(new Object[]{target}));
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					target.getAI().startAITask();
				}
				break;
			case admin_setaiparam:
				if(activeChar.getTarget() == null || !activeChar.getTarget().isNpc())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message4", activeChar));
					return false;
				}

				st = new StringTokenizer(fullString, " ");
				st.nextToken();

				if(!st.hasMoreTokens())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message5", activeChar));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message6", activeChar));
					return false;
				}

				String paramName = st.nextToken();
				if(!st.hasMoreTokens())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message7", activeChar));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message8", activeChar));
					return false;
				}
				String paramValue = st.nextToken();
				target = (NpcInstance) activeChar.getTarget();
				target.setParameter(paramName, paramValue);
				target.decayMe();
				target.spawnMe();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message9", activeChar, paramName, paramValue));
				break;
			case admin_dumpparams:
				if(activeChar.getTarget() == null || !activeChar.getTarget().isNpc())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message10", activeChar));
					return false;
				}
				target = (NpcInstance) activeChar.getTarget();
				MultiValueSet<String> set = target.getParameters();
				if(!set.isEmpty())
					System.out.println("Dump of Parameters:\r\n" + set.toString());
				else
					System.out.println("Parameters is empty.");
				break;
			case admin_setheading:
				GameObject obj = activeChar.getTarget();
				if(!obj.isNpc())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message11", activeChar));
					return false;
				}

				npc = (NpcInstance) obj;
				npc.setHeading(activeChar.getHeading());
				npc.decayMe();
				npc.spawnMe();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message12", activeChar, activeChar.getHeading()));

				spawn = npc.getSpawn();
				if(spawn == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message13", activeChar));
					return false;
				}
				break;
			case admin_generate_loc:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message14", activeChar));
					return false;
				}

				int id = Integer.parseInt(wordList[1]);
				int id2 = 0;
				if(wordList.length > 2)
					id2 = Integer.parseInt(wordList[2]);

				int min_x = Integer.MIN_VALUE;
				int min_y = Integer.MIN_VALUE;
				int min_z = Integer.MIN_VALUE;
				int max_x = Integer.MAX_VALUE;
				int max_y = Integer.MAX_VALUE;
				int max_z = Integer.MAX_VALUE;

				String name = "";

				for(NpcInstance _npc : World.getAroundNpc(activeChar))
					if(_npc.getNpcId() == id || _npc.getNpcId() == id2)
					{
						name = _npc.getName();
						min_x = Math.min(min_x, _npc.getX());
						min_y = Math.min(min_y, _npc.getY());
						min_z = Math.min(min_z, _npc.getZ());
						max_x = Math.max(max_x, _npc.getX());
						max_y = Math.max(max_y, _npc.getY());
						max_z = Math.max(max_z, _npc.getZ());
					}

				min_x -= 500;
				min_y -= 500;
				max_x += 500;
				max_y += 500;

				System.out.println("(0,'" + name + "'," + min_x + "," + min_y + "," + min_z + "," + max_z + ",0),");
				System.out.println("(0,'" + name + "'," + min_x + "," + max_y + "," + min_z + "," + max_z + ",0),");
				System.out.println("(0,'" + name + "'," + max_x + "," + max_y + "," + min_z + "," + max_z + ",0),");
				System.out.println("(0,'" + name + "'," + max_x + "," + min_y + "," + min_z + "," + max_z + ",0),");

				System.out.println("delete from spawnlist where npc_templateid in (" + id + ", " + id2 + ")" + //
						" and locx <= " + min_x + //
						" and locy <= " + min_y + //
						" and locz <= " + min_z + //
						" and locx >= " + max_x + //
						" and locy >= " + max_y + //
						" and locz >= " + max_z + //
						";");
				break;
			case admin_dumpspawn:
				st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					String id3 = st.nextToken();
					int respawnTime = 30;
					int mobCount = 1;
					spawnMonster(activeChar, id3, respawnTime, mobCount);
					try
					{
						new File("dumps").mkdir();
						File f = new File("dumps/spawndump.txt");
						if(!f.exists())
							f.createNewFile();
						FileWriter writer = new FileWriter(f, true);
						writer.write("<spawn count=\"1\" respawn=\"60\" respawn_random=\"0\" period_of_day=\"none\">\n\t" + "<point x=\"" + activeChar.getLoc().x + "\" y=\""+ activeChar.getLoc().y  + "\" z=\""+ activeChar.getLoc().z  + "\" h=\""+ activeChar.getLoc().h  + "\" />\n\t" + "<npc id=\"" + Integer.parseInt(id3) + "\" /><!--" + NpcHolder.getInstance().getTemplate(Integer.parseInt(id3)).getName() + "-->\n" + "</spawn>\n");
						writer.close();
					}
					catch(Exception e)
					{

					}
				}
				catch(Exception e)
				{
					// Case of wrong monster data
				}
				break;
		}
		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
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
			template = NpcHolder.getInstance().getTemplate(monsterTemplate, false);
		}
		else
		{
			// First parameter wasn't just numbers so go by name not ID
			monsterId = monsterId.replace('_', ' ');
			template = NpcHolder.getInstance().getTemplateByName(monsterId);
		}

		if(template == null)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message15", activeChar));
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

			if(RaidBossSpawnManager.getInstance().isDefined(template.getNpcId()))
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message16", activeChar, template.name));
			else
			{
				if (Config.SAVE_GM_SPAWN)
					SpawnTable.getInstance().addNewSpawn(spawn);
				spawn.init();
				if(respawnTime == 0)
					spawn.stopRespawn();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message17", activeChar, template.name, target.getObjectId()));
			}
		}
		catch(Exception e)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminspawn.message18", activeChar));
		}
	}
}