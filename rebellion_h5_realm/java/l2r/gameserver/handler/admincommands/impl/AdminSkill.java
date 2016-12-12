package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.data.xml.holder.SkillAcquireHolder;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.SkillLearn;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.AcquireType;
import l2r.gameserver.model.base.EnchantSkillLearn;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SkillCoolTime;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.stats.Calculator;
import l2r.gameserver.stats.Env;
import l2r.gameserver.stats.funcs.Func;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.tables.SkillTreeTable;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.StringUtil;
import l2r.gameserver.utils.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class AdminSkill implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_show_skills,
		admin_remove_skills,
		admin_skill_list,
		admin_skill_index,
		admin_add_skill,
		admin_remove_skill,
		admin_get_skills,
		admin_reset_skills,
		admin_give_all_skills,
		admin_show_effects,
		admin_debug_stats,
		admin_remove_cooldown,
		admin_remove_reuse,
		admin_remove_skill_delay_all,
		admin_get_buffskills,
		admin_getbuffs,
		admin_buff,
		admin_areabuff,
		admin_serverbuff,
		admin_stopbuff,
		admin_cancel,
		admin_areacancel
	}

	private static Skill[] adminSkills;

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_show_skills:
				showSkillsPage(activeChar);
				break;
			case admin_show_effects:
				if (wordList.length > 1)
					showEffects(activeChar, World.getPlayer(wordList[1]), 1);
				else
					showEffects(activeChar, activeChar.getTarget(), 1);
				break;
			case admin_getbuffs:
				if (activeChar.getTarget() != null && activeChar.getTarget().isCreature())
				{
					try
					{
						if (wordList.length > 2)
							showEffects(activeChar, GameObjectsStorage.findObject(Integer.parseInt(wordList[1])), Integer.parseInt(wordList[2]));
						else if (wordList.length > 1)
							showEffects(activeChar, GameObjectsStorage.findObject(Integer.parseInt(wordList[1])), 1);
						else
							showEffects(activeChar, activeChar.getTarget(), 1);
					}
					catch (Exception e)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message1", activeChar));
					}
				}
				break;
			case admin_remove_skills:
				removeSkillsPage(activeChar, Integer.parseInt(wordList[1]));
				break;
			case admin_skill_list:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/skills.htm"));
				break;
			case admin_skill_index:
				if(wordList.length > 1)
					activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/skills/" + wordList[1] + ".htm"));
				break;
			case admin_add_skill:
				adminAddSkill(activeChar, wordList);
				break;
			case admin_remove_skill:
				adminRemoveSkill(activeChar, wordList);
				break;
			case admin_get_skills:
				adminGetSkills(activeChar);
				break;
			case admin_reset_skills:
				adminResetSkills(activeChar);
				break;
			case admin_give_all_skills:
				adminGiveAllSkills(activeChar);
				break;
			case admin_debug_stats:
				debug_stats(activeChar);
				break;
			case admin_remove_cooldown:
			case admin_remove_skill_delay_all:
			case admin_remove_reuse:
				try
				{
					if (wordList.length > 1)
					{
						int radius = Integer.parseInt(wordList[1]);
						if (radius > 0)
						{
							for (Player player : World.getAroundPlayers(activeChar, radius, 500))
							{
								player.resetReuse();
								player.sendPacket(new SkillCoolTime(activeChar));
								player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message2", player));
							}
							
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message3", activeChar, radius));
						}
						else
						{
							Player player = World.getPlayer(wordList[1]);
							if (player != null)
							{
								player.resetReuse();
								player.sendPacket(new SkillCoolTime(activeChar));
								player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message4", player));
								activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message5", activeChar, player.getName()));
							}
						}
					}
					
					Player player = activeChar.getTarget() != null ? activeChar.getTarget().getPlayer() : null;
					if (player != null)
					{
						player.resetReuse();
						player.sendPacket(new SkillCoolTime(activeChar));
						player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message6", player));
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message7", activeChar, player.getName()));
					}
					else
					{
						activeChar.resetReuse();
						activeChar.sendPacket(new SkillCoolTime(activeChar));
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message8", activeChar));
					}
				}
				catch (Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message9", activeChar));
				}
				break;
			case admin_get_buffskills:
				for(int i = 7041; i <= 7064; i++)
					activeChar.addSkill(SkillTable.getInstance().getInfo(i, 1));
				activeChar.sendPacket(new SkillList(activeChar));
				break;
			case admin_buff:
				if (activeChar.getTarget() != null && activeChar.getTarget().isCreature())
				{
					try
					{
						int skillId = Integer.parseInt(wordList[1]);
						int level = Integer.parseInt(wordList[2]);
						Skill skill = SkillTable.getInstance().getInfo(skillId, level);
						skill.getEffects(activeChar, (Creature) activeChar.getTarget(), false, true);
						
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message10", activeChar, activeChar.getTarget().getName(), skill.getName(), level));
					}
					catch (Exception e)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message11", activeChar));
					}
				}
				break;
			case admin_areabuff:
				try
				{
					int skillId = Integer.parseInt(wordList[1]);
					int level = Integer.parseInt(wordList[2]);
					int radius = 0;
					if (wordList.length > 3)
						radius = Integer.parseInt(wordList[3]);
					
					Skill skill = SkillTable.getInstance().getInfo(skillId, level);
					
					for (Player player2 : World.getAroundPlayers(activeChar, radius, 500))
						skill.getEffects(activeChar, player2, false, true);
					
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message12", activeChar, radius, skill.getName(), level));
				}
				catch (Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message13", activeChar));
				}
				break;
			case admin_serverbuff:
				try
				{
					int skillId = Integer.parseInt(wordList[1]);
					int level = Integer.parseInt(wordList[2]);
					Skill skill = SkillTable.getInstance().getInfo(skillId, level);
					
					for (Player player2 : GameObjectsStorage.getAllPlayersForIterate())
						skill.getEffects(activeChar, player2, false, true);
					
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message14", activeChar, skill.getName(), level));
				}
				catch (Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message15", activeChar));
				}
				break;
			case admin_cancel:
				if (activeChar.getTarget() != null && activeChar.getTarget().isCreature())
				{
					((Creature)activeChar.getTarget()).getEffectList().stopAllEffects();
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message16", activeChar, activeChar.getTarget().getName()));
				}
				else
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message17", activeChar));
				break;
			case admin_areacancel:
				try
				{
					int radius = Integer.parseInt(wordList[1]);
					for (Player player2 : World.getAroundPlayers(activeChar, radius, 500))
						player2.getEffectList().stopAllEffects();
					
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message18", activeChar, radius));
				}
				catch (Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message19", activeChar));
				}
				break;
			case admin_stopbuff:
				try
				{
					int objId = Integer.parseInt(wordList[1]);
					int skillId = Integer.parseInt(wordList[2]);
					int returnToPage = -1;
					if (wordList.length > 3)
						returnToPage = Integer.parseInt(wordList[3]);
					if (objId > 0)
					{
						Creature target = (Creature) GameObjectsStorage.findObject(objId);
						if (target != null)
						{
							if (skillId > 0)
								target.getEffectList().stopEffect(skillId);
							else
							{
								target.getEffectList().stopAllEffects();
								activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message20", activeChar, target.getName()));
							}
							
							if (returnToPage > 0)
								showEffects(activeChar, target, returnToPage);
						}
					}
						
				}
				catch (Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message21", activeChar));
				}
				break;
		}

		return true;
	}
	
	private void debug_stats(Player activeChar)
	{
		GameObject target_obj = activeChar.getTarget();
		if(!target_obj.isCreature())
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		Creature target = (Creature) target_obj;

		Calculator[] calculators = target.getCalculators();

		String log_str = "--- Debug for " + target.getName() + " ---\r\n";

		for(Calculator calculator : calculators)
		{
			if(calculator == null)
				continue;
			Env env = new Env(target, activeChar, null);
			env.value = calculator.getBase();
			log_str += "Stat: " + calculator._stat.getValue() + ", prevValue: " + calculator.getLast() + "\r\n";
			Func[] funcs = calculator.getFunctions();
			for(int i = 0; i < funcs.length; i++)
			{
				String order = Integer.toHexString(funcs[i].order).toUpperCase();
				if(order.length() == 1)
					order = "0" + order;
				log_str += "\tFunc #" + i + "@ [0x" + order + "]" + funcs[i].getClass().getSimpleName() + "\t" + env.value;
				if(funcs[i].getCondition() == null || funcs[i].getCondition().test(env))
					funcs[i].calc(env);
				log_str += " -> " + env.value + (funcs[i].owner != null ? "; owner: " + funcs[i].owner.toString() : "; no owner") + "\r\n";
			}
		}

		Log.addGame(log_str, "debug_stats");
	}

	/**
	 * This function will give all the skills that the gm target can have at its
	 * level to the traget
	 *
	 * @param activeChar: the gm char
	 */
	private void adminGiveAllSkills(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		Player player = null;
		if(target != null && target.isPlayer())
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}
		int unLearnable = 0;
		int skillCounter = 0;
		Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL);
		while(skills.size() > unLearnable)
		{
			unLearnable = 0;
			for(SkillLearn s : skills)
			{
				Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if(sk == null || !sk.getCanLearn(player.getClassId()))
				{
					unLearnable++;
					continue;
				}
				if(player.getSkillLevel(sk.getId()) == -1)
					skillCounter++;
				player.addSkill(sk, true);
			}
			skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL);
		}

		player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message22", player, skillCounter));
		player.sendPacket(new SkillList(player));
		activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message23", activeChar, skillCounter, player.getName()));
	}

	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void removeSkillsPage(Player activeChar, int page)
	{
		GameObject target = activeChar.getTarget();
		Player player;
		if(target != null && target.isPlayer())
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		int blockforvisual = 0;
		int all = 0;
		boolean pagereached = false;
		
		List<Skill> list = new ArrayList<Skill>();
		for(Skill skill : player.getAllSkills())
			list.add(skill);
		
		Collections.sort(list, (o1, o2) -> o1.getId() - o2.getId());
		
		int totalpages = (int) Math.round(list.size() / 16.0 + 1);
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Editing: " + player.getName() + " Lvl: " + player.getLevel() + " Class: " + player.getTemplate().className + "</center>");
		replyMSG.append("<br><table width=270>");
		replyMSG.append("<tr><td width=40>Icon:</td><td width=80>Name:</td><td width=50>Level:</td><td width=40>Id:</td></tr>");
		
		if (page <= totalpages)
		{
			for (Skill element : player.getAllSkills())
			{
				all++;
				if (page == 1 && blockforvisual > 16)
					continue;
				if (!pagereached && all > page * 16)
					continue;
				if (!pagereached && all <= (page - 1) * 16)
					continue;
				blockforvisual++;
				
				replyMSG.append("<tr><td width=40><img src=\"" + element.getIcon() + "\" width=\"32\" height=\"32\" /></td><td width=80><a action=\"bypass -h admin_remove_skill " + element.getId() + " " + page + "\">" + element.getName() + "</a></td><td width=50>" + element.getLevel() + "</td><td width=40>" + element.getId() + "</td></tr>");
			}
		}

		replyMSG.append("</table>");
		replyMSG.append("<br><table width=260>");
		if(page == 1)
		{
			
			replyMSG.append("<tr><td width=210>&nbsp;</td>");
			replyMSG.append("<td width=50><button value=\"Next\" action=\"bypass -h admin_remove_skills " + (page + 1) + "\" width=60 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		}
		else if(page > 1)
			if(totalpages == page)
			{
				replyMSG.append("<tr><td width=210>&nbsp;</td>");
				replyMSG.append("<td width=50><button value=\"Prev\" action=\"bypass -h admin_remove_skills " + (page - 1) + "\" width=60 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			}
			else
			{
				replyMSG.append("<tr><td width=210><button value=\"Prev\" action=\"bypass -h admin_remove_skills " + (page - 1) + "\" width=60 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				replyMSG.append("<td width=50><button value=\"Next\" action=\"bypass -h admin_remove_skills " + (page + 1) + "\" width=60 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
			}

		
		replyMSG.append("</table>");
		
		replyMSG.append("<br><center><table>");
		replyMSG.append("Remove custom skill:");
		replyMSG.append("<tr><td width=33>Id: </td>");
		replyMSG.append("<td><edit var=\"id_to_remove\" width=100></td></tr>");
		replyMSG.append("</table></center>");
		replyMSG.append("<br>");
		replyMSG.append("<center><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill $id_to_remove " + page + "\" width=110 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
		replyMSG.append("<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=60 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showSkillsPage(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		Player player;
		if(target != null && target.isPlayer())
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Editing character: " + player.getName() + "</center>");
		replyMSG.append("<br><table width=270><tr><td>Lv: " + player.getLevel() + " " + player.getTemplate().className + "</td></tr></table>");
		replyMSG.append("<br><center><table>");
		replyMSG.append("<tr><td><button value=\"Add skills\" action=\"bypass -h admin_skill_list\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Get skills\" action=\"bypass -h admin_get_skills\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"Delete skills\" action=\"bypass -h admin_remove_skills 1\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Reset skills\" action=\"bypass -h admin_reset_skills\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"Give All Skills\" action=\"bypass -h admin_give_all_skills\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		replyMSG.append("</table></center>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showEffects(Player activeChar, GameObject targetObject, int page)
	{
		if (targetObject == null || !targetObject.isCreature())
			return;
		final int PAGE_LIMIT = 20;
		Creature target = (Creature) targetObject;
		final List<Effect> effects = target.getEffectList().getAllEffects();
		
		int max = effects.size() / PAGE_LIMIT;
		if (page > max  + 1 || page < 1)
			return;
		
		
		if (effects.size() > 20 * max)
			max++;
		
		final StringBuilder html = StringUtil.startAppend(500 + effects.size() * 200,
		"<html><table width=\"100%\"><tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=180><center><font color=\"LEVEL\">Effects of ", target.getName(),
		"</font></td><td width=45><button value=\"Back\" action=\"bypass -h admin_current_player\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br><table width=\"100%\"><tr><td width=190>Skill</td><td width=40>Rem. Time</td><td width=60>Action</td></tr>");
		
		for (Effect e : effects)
		{
			if (e != null)
			{
				StringUtil.append(html,"<tr><td>",e.getSkill().getName(),"</td><td>",
				e.getSkill().isToggle() ? "toggle" : Util.formatTime(e.getTimeLeft(), 2),
				"</td><td><button value=\"Remove\" action=\"bypass -h admin_stopbuff ",
				Integer.toString(target.getObjectId()), " ", String.valueOf(e.getSkill().getId()), " ", String.valueOf(page),
				"\" width=55 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			}
		}
		
		html.append("</table><table width=300 bgcolor=444444><tr>");
		for (int x = 0; x < max; x++)
		{
			int pagenr = x + 1;
			if (page == pagenr)
			{
				html.append("<td>Page ");
				html.append(pagenr);
				html.append("</td>");
			}
			else
			{
				html.append("<td><a action=\"bypass -h admin_getbuffs ");
				html.append(target.getObjectId());
				html.append(" ");
				html.append(x + 1);
				html.append("\"> Page ");
				html.append(pagenr);
				html.append(" </a></td>");
			}
		}
		
		html.append("</tr></table>");
		
		StringUtil.append(html, "<br><center><button value=\"Remove All\" action=\"bypass -h admin_stopbuff ", Integer.toString(target.getObjectId()), " -1 1\" width=80 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></html>");
		
		NpcHtmlMessage ms = new NpcHtmlMessage(1);
		ms.setHtml(html.toString());
		activeChar.sendPacket(ms);
	}

	private void adminGetSkills(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		Player player;
		if(target != null && target.isPlayer())
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		if(player.getName().equals(activeChar.getName()))
			player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message24", player));
		else
		{
			Collection<Skill> skills = player.getAllSkills();
			adminSkills = activeChar.getAllSkillsArray();
			for(Skill element : adminSkills)
				activeChar.removeSkill(element, true);
			for(Skill element : skills)
				activeChar.addSkill(element, true);
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message25", activeChar, player.getName()));
		}

		showSkillsPage(activeChar);
	}

	private void adminResetSkills(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		Player player = null;
		if(target != null && target.isPlayer())
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		Skill[] skills = player.getAllSkillsArray();
		int counter = 0;
		for(Skill skill : skills)
		{
			player.removeSkill(skill, true);
			counter++;
		}
		player.sendPacket(new SkillList(player));
		player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message26", player, activeChar.getName()));
		activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message27", activeChar, counter));

		showSkillsPage(activeChar);
	}

	private void adminAddSkill(Player activeChar, String[] wordList)
	{
		GameObject target = activeChar.getTarget();
		Player player;
		if(target != null && target.isPlayer())
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		if(wordList.length == 3)
		{
			int id = Integer.parseInt(wordList[1]);
			int level = Integer.parseInt(wordList[2]);
			
			if (level > 100)
			{
				EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(id, level);
				if(sl == null)
					return;

				level = SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), level, sl.getMaxLevel());
			}
			
			Skill skill = SkillTable.getInstance().getInfo(id, level);
			if(skill != null)
			{
				player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message28", player, skill.getName()));
				player.addSkill(skill, true);
				player.sendPacket(new SkillList(player));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message29", activeChar, skill.getName(), player.getName()));
			}
			else
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message30", activeChar));
		}

		showSkillsPage(activeChar);
	}

	private void adminRemoveSkill(Player activeChar, String[] wordList)
	{
		GameObject target = activeChar.getTarget();
		Player player = null;
		if(target != null && target.isPlayer())
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}

		if(wordList.length >= 2)
		{
			int id = Integer.parseInt(wordList[1]);
			int level = player.getSkillLevel(id);
			Skill skill = SkillTable.getInstance().getInfo(id, level);
			if(skill != null)
			{
				player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message31", player, skill.getName()));
				player.removeSkill(skill, true);
				player.sendPacket(new SkillList(player));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message32", activeChar, skill.getName(), player.getName()));
			}
			else
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminskill.message33", activeChar));
		}

		removeSkillsPage(activeChar, Integer.parseInt(wordList[2]));
	}
}