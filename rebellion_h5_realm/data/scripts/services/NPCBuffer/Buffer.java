package services.NPCBuffer;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Files;

import java.util.StringTokenizer;

import javolution.text.TextBuilder;

public class Buffer extends Functions implements ScriptFile
{
	public static int priceBuff = 0;// Цена за один бафф
	public static int creatSchema = 0;// Цена за создание схемы
	public static int priceBuffNabor = 0;
	
	public void onLoad()
	{
		
	}
	
	public void onReload()
	{
	}
	
	public void onShutdown()
	{
	}
	
	/*
	public String DialogAppend_406(Integer val)
	{
		if (val != 0)
			return "";
		return OutDia();
	}
	*/
	
	public void doBuff(String[] args)
	{
		Player player = (Player) getSelf();
		NpcInstance npc = getNpc();
		String page = args[1];
		int petorplayer = Integer.valueOf(args[2]);
		if (!checkCondition(player, npc))
		{
			player.sendMessage(new CustomMessage("scripts.services.npcbuffer.condition1", player));
			return;
		}
		
		if (player.getAdena() < priceBuff)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		
		try
		{
			int skill_id = Integer.valueOf(args[0]);
			
			Skill skill = SkillTable.getInstance().getInfo(skill_id, SkillTable.getInstance().getBaseLevel(skill_id));
			if (petorplayer == 0)
			{
				skill.getEffects(player, player, false, false);
				getNpc().broadcastPacket(new MagicSkillUse(getNpc(), player, skill.getDisplayId(), skill.getLevel(), skill.getHitTime(), 0));
			}
			else
			{
				if (player.getPet() == null)
				{
					player.sendMessage(new CustomMessage("scripts.services.npcbuffer.no_summon", player));
					return;
				}
				Creature pets = player.getPet();
				skill.getEffects(player, pets, false, false);
				getNpc().broadcastPacket(new MagicSkillUse(getNpc(), pets, skill.getDisplayId(), skill.getLevel(), skill.getHitTime(), 0));
			}
			player.reduceAdena(priceBuff, true);
		}
		catch (Exception e)
		{
			player.sendMessage(new CustomMessage("scripts.services.npcbuffer.invalid_skill", player));
		}
		
		String html = null;
		
		html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/" + page + ".htm", player);
		show(html, player, npc);
	}
	
	public boolean checkCondition(Player player, NpcInstance npc)
	{
		if (player == null || npc == null || !player.isConnected() || player.isInOfflineMode())
			return false;
		
		if (player.isCursedWeaponEquipped() || !player.isInPeaceZone()  || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.isFlying() || player.isTerritoryFlagEquipped())
			return false;
		
		if (!NpcInstance.canBypassCheck(player, npc))
			return false;
		
		return true;
	}

	public void showHtml(String[] args)
	{
		
		Player player = (Player) getSelf();
		NpcInstance npc = getNpc();
		String page = args[0];
		String html = null;
		
		html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/" + page + ".htm", player);
		show(html, player, npc);
	}
	
	public void SelectBuffs()
	{
		Player player = (Player) getSelf();
		NpcInstance lastNpc = player.getLastNpc();
		
		if (!checkCondition(player, lastNpc))
		{
			player.sendMessage(new CustomMessage("scripts.services.npcbuffer.condition1", player));
			return;
		}
		String html = null;
		
		html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/buffs.htm", player);
		show(html, player, lastNpc);
	}
	
	public String OutDia()
	{
		Player activeChar = (Player) getSelf();
		
		String html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/buffs.htm", activeChar);
		String schema = "";
		String one = "First";
		String two = "Second";
		String three = "Third";
		
		for (int i = 1; i < 4; ++i)
		{
			if (i < 4)
			{
				String setname = "buffset" + i;
				if (activeChar.getVar(setname) == null)
				{
					schema += ("<td width=189><button value=\"Create\" action=\"bypass -h scripts_services.NPCBuffer.Buffer:newSchema " + setname + " " + i + "\" width=86 height=24 back=\"L2UI_CT1.ListCTRL_DF_Title\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"></td>");
				}
				else if (activeChar.getVar(setname) != null)
				{
					String namebutton = "";
					if (i == 1)
						namebutton = one;
					else if (i == 2)
						namebutton = two;
					else
						namebutton = three;
					schema += ("<td width=189><button value=\"" + namebutton + "\" action=\"bypass -h scripts_services.NPCBuffer.Buffer:showSchema " + setname + "\" width=86 height=24 back=\"L2UI_CT1.ListCTRL_DF_Title\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"></td>");
				}
			}
		}
		html = html.replaceFirst("%buffset%", schema);
		return html;
	}
	
	public void newSchema(String[] args)
	{
		Player activeChar = (Player) getSelf();
		if (activeChar.getAdena() < creatSchema)
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.npcbuffer.notenoughadena1", activeChar));
			return;
		}
		String buffset = args[0];
		String SchemaName = args[1];
		activeChar.setVar(buffset, SchemaName + ";", -1);
		String adrg[] =
		{
			buffset
		};
		showSchema(adrg);
		activeChar.reduceAdena(creatSchema, true);
	}
	
	public void delSchema(String[] args)
	{
		Player activeChar = (Player) getSelf();
		activeChar.unsetVar(args[0]);
	}
	
	public void editSchema(String[] args)
	{
		Player player = (Player) getSelf();
		NpcInstance npc = getNpc();
		String setname = args[0];
		String html = "";
		html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/schemaedit.htm", player);
		html = html.replaceAll("%buffsetname%", setname);
		
		StringTokenizer st = new StringTokenizer(player.getVar(setname), ";");
		st.nextToken();
		int page = Integer.valueOf(args[1]);
		int lengtharray = st.countTokens();
		int ipage = 0;
		ipage = (page) * 28 - 28;
		int len = (page) * 28;
		
		int i = 0;
		boolean closed = true;
		String SkillId = "";
		String icon = "";
		int count = 0;
		while (st.hasMoreTokens())
		{
			count++;
			if (ipage >= count || len < count)
			{
				st.nextToken();
				continue;
			}
			try
			{
				int skillid = Integer.parseInt(st.nextToken());
				
				if ((++i == 1) || (i == 8) || (i == 15) || (i == 22) || (i == 29))
				{
					closed = false;
					icon += ("<td><table width=\"60\" border=\"0\">");
				}
				Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
				if (skill.getId() < 1000)
				{
					SkillId = "0" + skill.getId();
				}
				else
				{
					SkillId = "" + skill.getId();
				}
				if (skill.getId() == 4700 || skill.getId() == 4699)
				{
					SkillId = "1331 ";
				}
				else if (skill.getId() == 4703 || skill.getId() == 4702)
				{
					SkillId = "1332 ";
				}
				icon += ("<tr><td><img src=icon.skill" + SkillId + " width=32 height=32></td><td width=25><button value=- action=\"bypass -h scripts_services.NPCBuffer.Buffer:removeOneBuff " + skillid + " " + setname + " " + page + "\" width=25 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
				if ((i == 7) || (i == 14) || (i == 21) || (i == 28) || (i == 35))
				{
					closed = true;
					icon += ("</table></td>");
					
				}
				
			}
			catch (Exception e)
			{
				continue;
			}
		}
		if (!(closed))
			icon += ("</table></td>");
		if (icon.length() == 0)
			icon += ("<td>No buffs, use button Add Buffs.</td>");
		
		TextBuilder sb = new TextBuilder();
		int pages = Math.max(1, lengtharray / 28 + 1);
		sb.append("<table><tr>");
		if (pages != 1)
			for (int ii = 1; ii <= pages; ii++)
			{
				if (ii != page)
					sb.append("<td width=20 height=20><a action=\"bypass -h scripts_services.NPCBuffer.Buffer:editSchema " + setname + " " + ii + "\"><font color=F2C202>[" + ii + "]</font></a></td>");
				else
					sb.append("<td width=10 height=20><font color=FFFFFF>" + ii + "</font></a></td>");
			}
		sb.append("</tr></table>");
		html += sb.toString();
		// icon+=("<td><img src=icon.skill" + SkillId + " width=32 height=32></td><td width=25><button value=- action=\"bypass -h scripts_services.NPCBuffer.Buffer:removeOneBuff " + skillid + " " + setname + "\" width=25 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		
		html = html.replaceAll("%buff%", icon);
		
		show(html, player, npc);
		
	}
	
	public void addBuffSchema(String[] args)
	{
		Player player = (Player) getSelf();
		NpcInstance npc = getNpc();
		String setname = args[0];
		int page = Integer.valueOf(args[1]);
		int lengtharray = BUFF_ADD.length;
		int ipage = 0;
		ipage = (page) * 24 - 24;
		int len = (page) * 24;
		String html = "";
		html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/schemaadd.htm", player);
		html = html.replaceAll("%buffsetname%", setname);
		
		int i = 0;
		boolean closed = true;
		String SkillId = "";
		String icon = "";
		
		for (; ipage < len; ipage++)
		{
			if (lengtharray == ipage)
				break;
			/*
			 * if (!checkSchema(player,setname,BUFF_ADD[ipage])) { len++; continue; }
			 */
			
			if ((++i == 1) || (i == 7) || (i == 13) || (i == 19) || (i == 25))
			{
				closed = false;
				icon += ("<td><table width=\"60\" border=\"0\">");
			}
			Skill skill = SkillTable.getInstance().getInfo(BUFF_ADD[ipage], SkillTable.getInstance().getBaseLevel(BUFF_ADD[ipage]));
			if (skill.getId() < 1000)
			{
				SkillId = "0" + skill.getId();
			}
			else
			{
				SkillId = "" + skill.getId();
			}
			if (skill.getId() == 4700 || skill.getId() == 4699)
			{
				SkillId = "1331 ";
			}
			else if (skill.getId() == 4703 || skill.getId() == 4702)
			{
				SkillId = "1332 ";
			}
			icon += ("<tr><td><img src=icon.skill" + SkillId + " width=32 height=32></td><td width=25><button value=+ action=\"bypass -h scripts_services.NPCBuffer.Buffer:addOneBuff " + BUFF_ADD[ipage] + " " + setname + " " + page + "\" width=25 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			if ((i == 6) || (i == 12) || (i == 18) || (i == 24) || (i == 30))
			{
				closed = true;
				icon += ("</table></td>");
			}
		}
		if (!(closed))
			icon += ("</table></td>");
		if (icon.length() == 0)
			icon += ("<td>No buffs, use button Add Buffs.</td>");
		
		TextBuilder sb = new TextBuilder();
		int pages = Math.max(1, lengtharray / 24 + 1);
		sb.append("<table><tr>");
		if (pages != 1)
			for (int ii = 1; ii <= pages; ii++)
			{
				if (ii != page)
					sb.append("<td width=20 height=20><a action=\"bypass -h scripts_services.NPCBuffer.Buffer:addBuffSchema " + setname + " " + ii + "\"><font color=F2C202>[" + ii + "]</font></a></td>");
				else
					sb.append("<td width=10 height=20><font color=FFFFFF>" + ii + "</font></a></td>");
			}
		sb.append("</tr></table>");
		html += sb.toString();
		
		// icon+=("<td><img src=icon.skill" + SkillId + " width=32 height=32></td><td width=25><button value=- action=\"bypass -h scripts_services.NPCBuffer.Buffer:removeOneBuff " + skillid + " " + setname + "\" width=25 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		
		html = html.replaceAll("%buff%", icon);
		
		show(html, player, npc);
		
	}
	
	public boolean checkSchema(Player player, String buffset, int buffId)
	{
		StringTokenizer st = new StringTokenizer(player.getVar(buffset), ";");
		st.nextToken();
		
		while (st.hasMoreTokens())
		{
			int skillid = Integer.parseInt(st.nextToken());
			if (buffId == skillid)
				return false;
		}
		
		return true;
	}
	
	public void addOneBuff(String[] args)
	{
		Player player = (Player) getSelf();
		int buffid = Integer.valueOf(args[0]);;
		String buffset = args[1];
		String page = args[2];
		String adrg[] =
		{
			buffset,
			page
		};
		if ((player.getVar(buffset) == null))
			return;
		if (!checkSchema(player, buffset, buffid))
		{
			player.sendMessage(new CustomMessage("scripts.services.npcbuffer.already_added", player));
			addBuffSchema(adrg);
			return;
		}
		
		if (new StringTokenizer(player.getVar(buffset), ";").countTokens() < 61)
		{
			player.setVar(buffset, player.getVar(buffset) + buffid + ";", -1);
		}
		
		addBuffSchema(adrg);
	}
	
	public void removeOneBuff(String[] args)
	{
		Player player = (Player) getSelf();
		String page = args[2];
		String buffid = args[0];
		String buffset = args[1];
		if ((player.getVar(buffset) == null))
			return;
		
		player.setVar(buffset, player.getVar(buffset).replaceFirst(buffid + ";", ""), -1);
		
		String adrg[] =
		{
			buffset,
			page
		};
		editSchema(adrg);
		
	}
	
	public void showSchema(String[] args)
	{
		Player player = (Player) getSelf();
		NpcInstance npc = getNpc();
		String setname = args[0];
		String html = "";
		html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/schema.htm", player);
		html = html.replaceAll("%buffsetname%", setname);
		StringTokenizer st = new StringTokenizer(player.getVar(setname), ";");
		st.nextToken();
		
		int i = 0;
		boolean closed = true;
		String SkillId = "";
		String icon = "";
		
		while (st.hasMoreTokens())
		{
			try
			{
				int skillid = Integer.parseInt(st.nextToken());
				
				if ((++i == 1) || (i == 7) || (i == 13) || (i == 19) || (i == 25) || (i == 31) || (i == 37) || (i == 43) || (i == 49) || (i == 55))
				{
					closed = false;
					icon += ("<tr>");
				}
				Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
				if (skill.getId() < 1000)
				{
					SkillId = "0" + skill.getId();
				}
				else
				{
					SkillId = "" + skill.getId();
				}
				if (skill.getId() == 4700 || skill.getId() == 4699)
				{
					SkillId = "1331 ";
				}
				else if (skill.getId() == 4703 || skill.getId() == 4702)
				{
					SkillId = "1332 ";
				}
				icon += ("<td><img src=icon.skill" + SkillId + " width=32 height=32></td>");
				if ((i == 6) || (i == 12) || (i == 18) || (i == 24) || (i == 30) || (i == 36) || (i == 42) || (i == 48) || (i == 54) || (i == 60))
				{
					closed = true;
					icon += ("</tr>");
				}
			}
			catch (Exception e)
			{
				continue;
			}
		}
		if (!(closed))
			icon += ("</tr>");
		
		html = html.replaceAll("%icon%", icon);
		show(html, player, npc);
		
	}
	
	public void buffSchema(String[] args)
	{
		Player player = (Player) getSelf();
		String setname = args[0];
		
		NpcInstance lastNpc = player.getLastNpc();
		
		if (!checkCondition(player, lastNpc))
		{
			player.sendMessage(new CustomMessage("scripts.services.npcbuffer.condition1", player));
			return;
		}
		
		StringTokenizer st = new StringTokenizer(player.getVar(setname), ";");
		if (player.getAdena() < st.countTokens() * priceBuff)
		{
			player.sendMessage(new CustomMessage("scripts.services.npcbuffer.notenoughadena2", player));
			return;
		}
		st.nextToken();
		
		while (st.hasMoreTokens())
		{
			int skill_id = Integer.valueOf(st.nextToken()).intValue();
			Skill skill = SkillTable.getInstance().getInfo(skill_id, SkillTable.getInstance().getBaseLevel(skill_id));
			skill.getEffects(player, player, false, false);
		}
		player.reduceAdena(st.countTokens() * priceBuff, true);
		String adrg[] =
		{
			setname
		};
		showSchema(adrg);
	}
	
	public void buffSchemaPet(String[] args)
	{
		Player player = (Player) getSelf();
		String setname = args[0];
		
		NpcInstance lastNpc = player.getLastNpc();
		
		if (!checkCondition(player, lastNpc))
		{
			player.sendMessage(new CustomMessage("scripts.services.npcbuffer.condition1", player));
			return;
		}
		
		StringTokenizer st = new StringTokenizer(player.getVar(setname), ";");
		if (player.getAdena() < st.countTokens() * priceBuff)
		{
			player.sendMessage(new CustomMessage("scripts.services.npcbuffer.notenoughadena2", player));
			return;
		}
		st.nextToken();
		if (player.getPet() == null)
		{
			player.sendMessage(new CustomMessage("scripts.services.npcbuffer.no_summon1", player));
			String adrg[] =
			{
				setname
			};
			showSchema(adrg);
			return;
		}
		Creature pet = player.getPet();
		
		while (st.hasMoreTokens())
		{
			int skill_id = Integer.valueOf(st.nextToken()).intValue();
			Skill skill = SkillTable.getInstance().getInfo(skill_id, SkillTable.getInstance().getBaseLevel(skill_id));
			skill.getEffects(player, pet, false, false);
		}
		player.reduceAdena(st.countTokens() * priceBuff, true);
		String adrg[] =
		{
			setname
		};
		showSchema(adrg);
		
	}
	
	
	public class BeginBuff implements Runnable
	{
		Creature _buffer;
		Skill _skill;
		Player _target;
		
		public BeginBuff(Creature buffer, Skill skill, Player target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}
		
		public void run()
		{
			if (_target.isInOlympiadMode())
				return;
			_buffer.broadcastPacket(new MagicSkillUse(_buffer, _target, _skill.getDisplayId(), _skill.getLevel(), _skill.getHitTime(), 0));
			ThreadPoolManager.getInstance().schedule(new EndBuff(_buffer, _skill, _target), _skill.getHitTime());
		}
	}
	
	public class EndBuff implements Runnable
	{
		Creature _buffer;
		Skill _skill;
		Player _target;
		
		public EndBuff(Creature buffer, Skill skill, Player target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}
		
		public void run()
		{
			_skill.getEffects(_buffer, _target, false, false);
			_buffer.broadcastPacket(new MagicSkillLaunched(_buffer.getObjectId(), _skill.getId(), _skill.getLevel(), _target));
		}
	}
	
	public class BeginPetBuff implements Runnable
	{
		Creature _buffer;
		Skill _skill;
		Summon _target;
		
		public BeginPetBuff(Creature buffer, Skill skill, Summon target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}
		
		public void run()
		{
			_buffer.broadcastPacket(new MagicSkillUse(_buffer, _target, _skill.getDisplayId(), _skill.getLevel(), _skill.getHitTime(), 0));
			ThreadPoolManager.getInstance().schedule(new EndPetBuff(_buffer, _skill, _target), _skill.getHitTime());
		}
	}
	
	public class EndPetBuff implements Runnable
	{
		Creature _buffer;
		Skill _skill;
		Summon _target;
		
		public EndPetBuff(Creature buffer, Skill skill, Summon target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}
		
		public void run()
		{
			_skill.getEffects(_buffer, _target, false, false);
			_buffer.broadcastPacket(new MagicSkillLaunched(_buffer.getObjectId(), _skill.getId(), _skill.getLevel(), _target));
		}
	}
	
	public static final int[] BUFF_ADD =
	{
		1499,
		1500,
		1501,
		1502,
		1503,
		1504,
		1519,
		1204,
		1068,
		1040,
		1388,
		1389,
		1045,
		1311,
		1048,
		1036,
		1268,
		1086,
		1062,
		1085,
		1059,
		1303,
		1240,
		1077,
		1242,
		1087,
		1078,
		1044,
		1047,
		1397,
		1243,
		1304,
		1035,
		1259,
		1032,
		1191,
		1182,
		1189,
		1033,
		1392,
		1393,
		1352,
		1353,
		1354,
		1043,
		271,
		272,
		273,
		274,
		275,
		276,
		277,
		307,
		309,
		530,
		311,
		310,
		915,
		366,
		365,
		264,
		265,
		266,
		267,
		268,
		269,
		304,
		306,
		308,
		529,
		270,
		914,
		363,
		364,
		349,
		1007,
		1009,
		1390,
		1391,
		1006,
		1002,
		1251,
		1252,
		1253,
		1310,
		1309,
		1308,
		1362,
		1461,
		1363,
		1413,
		1364,
		1003,
		1004,
		1005,
		1008,
		1249,
		1250,
		1260,
		1261,
		1305,
		1365,
		1416,
		1415,
		1414,
		1356,
		1355,
		1357,
		4699,
		4700,
		4702,
		4703,
		825,
		826,
		827,
		828,
		829,
		830,
		1307,
		1323
	};
	
	public static final int[] ALL_NORM =
	{
	};
	
	public static final int[] ALL_RESIST =
	{
	};
	
	public static final int[] ALL_HEAL =
	{
	};
	
	public static final int[] ALL_WARC =
	{
	};
	
	public static final int[] ALL_OVER =
	{
		
	};
	
	public static final int[] ALL_IMP =
	{
	};
	
	public static final int[] ALL_WARS =
	{
		
	};
	
	public static final int[] ALL_DANCE =
	{
	};
	
	public static final int[] ALL_SONG =
	{
	};
	
	public static final int[] BUFF_FIGHT =
	{
		1204,
		1040,
		1068,
		1035,
		1045,
		1048,
		1036,
		1259,
		1086,
		1388,
		1397,
		1087,
		1352,
		1364,
		1250,
		4699,
		1357,
		1353,
		1304,
		1392,
		1393,
		1077,
		1242,
		825,
		828,
		1415,
		1461,
		1416,
		1310,
		264,
		265,
		267,
		268,
		269,
		349,
		270,
		304,
		306,
		529,
		308,
		271,
		272,
		310,
		275,
		274,
		307,
		309,
		311,
		530,
		266,
		364
	};
	
	public static final int[] BUFF_MAGE =
	{
		1204,
		1040,
		1035,
		1045,
		1048,
		1259,
		1078,
		1085,
		1059,
		1389,
		1303,
		1087,
		1352,
		1364,
		1250,
		4703,
		1036,
		1393,
		1392,
		1353,
		1304,
		1397,
		830,
		1415,
		1461,
		1363,
		1416,
		264,
		265,
		267,
		268,
		349,
		270,
		304,
		363,
		306,
		529,
		308,
		273,
		276,
		265,
		307,
		309,
		311,
		530,
		266,
		364
	};
	
	public void removeBuff()
	{
		Player activeChar = (Player) getSelf();
		NpcInstance npc = getNpc();
		activeChar.getEffectList().stopAllEffects();
		
		show(OutDia(), activeChar, npc);
	}
	
	public void recoveryHPMP()
	{
		Player activeChar = (Player) getSelf();
		NpcInstance npc = getNpc();
		activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
		
		show(OutDia(), activeChar, npc);
	}
	
	public void allWars()
	{
		Player activeChar = (Player) getSelf();
		NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
			pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor)
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.npcbuffer.notenoughadena2", activeChar));
			return;
		}
		
		Summon summon = activeChar.getPet();
		for (int skillid : ALL_WARS)
		{
			Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget() && pet == true))
			{
				skill.getEffects(activeChar, summon, false, false);
			}
			else
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		show(Files.read("data/scripts/services/NPCBuffer/buffswars.htm", activeChar), activeChar, npc);
	}
	
	public void allImp()
	{
		Player activeChar = (Player) getSelf();
		NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
			pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor)
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.npcbuffer.notenoughadena2", activeChar));
			return;
		}
		
		Summon summon = activeChar.getPet();
		for (int skillid : ALL_IMP)
		{
			Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget() && pet == true))
			{
				skill.getEffects(activeChar, summon, false, false);
			}
			else
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		String html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/buffsimp.htm", activeChar);
		show(html, activeChar, npc);
	}
	
	public void allOver()
	{
		Player activeChar = (Player) getSelf();
		NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
			pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor)
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.npcbuffer.notenoughadena2", activeChar));
			return;
		}
		
		Summon summon = activeChar.getPet();
		for (int skillid : ALL_OVER)
		{
			Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget() && pet == true))
			{
				skill.getEffects(activeChar, summon, false, false);
			}
			else
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		String html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/buffsover.htm", activeChar);
		show(html, activeChar, npc);
	}
	
	public void allWarc()
	{
		Player activeChar = (Player) getSelf();
		NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
			pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor)
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.npcbuffer.notenoughadena2", activeChar));
			return;
		}
		
		Summon summon = activeChar.getPet();
		for (int skillid : ALL_WARC)
		{
			Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget() && pet == true))
			{
				skill.getEffects(activeChar, summon, false, false);
			}
			else
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		String html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/buffswarc.htm", activeChar);
		show(html, activeChar, npc);
	}
	
	public void allHeal()
	{
		Player activeChar = (Player) getSelf();
		NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
			pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor)
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.npcbuffer.notenoughadena2", activeChar));
			return;
		}
		
		Summon summon = activeChar.getPet();
		for (int skillid : ALL_HEAL)
		{
			Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget() && pet == true))
			{
				skill.getEffects(activeChar, summon, false, false);
			}
			else
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		String html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/buffsheal.htm", activeChar);
		show(html, activeChar, npc);
	}
	
	public void allResist()
	{
		Player activeChar = (Player) getSelf();
		NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
			pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor)
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.npcbuffer.notenoughadena2", activeChar));
			return;
		}
		
		Summon summon = activeChar.getPet();
		for (int skillid : ALL_RESIST)
		{
			Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget() && pet == true))
			{
				skill.getEffects(activeChar, summon, false, false);
			}
			else
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		String html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/buffsresist.htm", activeChar);
		show(html, activeChar, npc);
	}
	
	public void allNorm()
	{
		Player activeChar = (Player) getSelf();
		NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
			pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor)
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.npcbuffer.notenoughadena2", activeChar));
			return;
		}
		
		Summon summon = activeChar.getPet();
		for (int skillid : ALL_NORM)
		{
			Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget() && pet == true))
			{
				skill.getEffects(activeChar, summon, false, false);
			}
			else
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		String html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/buffsnorm.htm", activeChar);
		show(html, activeChar, npc);
	}
	
	public void allDance()
	{
		Player activeChar = (Player) getSelf();
		NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
			pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor)
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.npcbuffer.notenoughadena2", activeChar));
			return;
		}
		
		Summon summon = activeChar.getPet();
		for (int skillid : ALL_DANCE)
		{
			Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget() && pet == true))
			{
				skill.getEffects(activeChar, summon, false, false);
			}
			else
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		String html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/buffsdance.htm", activeChar);
		show(html, activeChar, npc);
	}
	
	public void allSong()
	{
		Player activeChar = (Player) getSelf();
		NpcInstance npc = getNpc();
		boolean pet = false;
		if (activeChar.getPet() != null)
		{
			pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor)
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.npcbuffer.notenoughadena2", activeChar));
			return;
		}
		
		Summon summon = activeChar.getPet();
		for (int skillid : ALL_SONG)
		{
			Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget() && pet == true))
			{
				skill.getEffects(activeChar, summon, false, false);
			}
			else
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		String html = HtmCache.getInstance().getNotNull("scripts/services/NPCBuffer/buffsdance.htm", activeChar);
		show(html, activeChar, npc);
	}
	
	public void buffFight()
	{
		Player activeChar = (Player) getSelf();
		NpcInstance npc = getNpc();
		boolean pet = false;
		
		if (!checkCondition(activeChar, npc))
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.npcbuffer.condition1", activeChar));
			return;
		}	
		
		if (activeChar.getPet() != null)
		{
			pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor)
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.npcbuffer.notenoughadena2", activeChar));
			return;
		}
		
		Summon summon = activeChar.getPet();
		for (int skillid : BUFF_FIGHT)
		{
			Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget() && pet == true))
			{
				skill.getEffects(activeChar, summon, false, false);
			}
			else
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		
		show(OutDia(), activeChar, npc);
	}
	
	public void buffMage()
	{
		Player activeChar = (Player) getSelf();
		NpcInstance npc = getNpc();
		boolean pet = false;
		
		if (!checkCondition(activeChar, npc))
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.npcbuffer.condition1", activeChar));
			return;
		}
		
		if (activeChar.getPet() != null)
		{
			pet = true;
		}
		if (activeChar.getAdena() < priceBuffNabor)
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.npcbuffer.notenoughadena2", activeChar));
			return;
		}
		Summon summon = activeChar.getPet();
		for (int skillid : BUFF_MAGE)
		{
			Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getBaseLevel(skillid));
			if ((summon != null) && (summon == activeChar.getTarget() && pet == true))
			{
				skill.getEffects(activeChar, summon, false, false);
			}
			else
			{
				skill.getEffects(activeChar, activeChar, false, false);
			}
		}
		activeChar.reduceAdena(priceBuffNabor, true);
		
		show(OutDia(), activeChar, npc);
	}
	
}