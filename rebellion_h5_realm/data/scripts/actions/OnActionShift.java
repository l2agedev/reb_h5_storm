package actions;

import l2r.gameserver.Config;
import l2r.gameserver.achievements.PlayerTops;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.handler.admincommands.impl.AdminEditChar;
import l2r.gameserver.model.AggroList.HateComparator;
import l2r.gameserver.model.AggroList.HateInfo;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.entity.events.GlobalEvent;
import l2r.gameserver.model.instances.BossInstance;
import l2r.gameserver.model.instances.DoorInstance;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.instances.PetInstance;
import l2r.gameserver.model.instances.RaidBossInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestEventType;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.HtmlUtils;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

public class OnActionShift extends Functions
{
	public boolean OnActionShift_NpcInstance(Player player, GameObject object)
	{
		if(player == null || object == null)
			return false;
		
		if(!Config.ALLOW_NPC_SHIFTCLICK && !player.canOverrideCond(PcCondOverride.HTML_ACTION_CONDITIONS))
		{
			if(Config.ALT_GAME_SHOW_DROPLIST && object.isNpc())
			{
				NpcInstance npc = (NpcInstance) object;
				if(npc.isDead())
					return false;
				droplist(player, npc, 1);
			}
			return false;
		}
		if(object.isNpc())
		{
			NpcInstance npc = (NpcInstance) object;

			// Для мертвых мобов не показываем табличку, иначе спойлеры плачут
			if(npc.isDead())
				return false;

			String dialog;

			if (Config.ALT_BYPASS_SHIFT_CLICK_NPC_TO_CB)
			{
				generatenpcInfo(player, npc);
				return true;
			}
			
			if(Config.ALT_FULL_NPC_STATS_PAGE)
			{
				dialog = HtmCache.getInstance().getNotNull("scripts/actions/player.L2NpcInstance.onActionShift.full.htm", player);
				dialog = dialog.replaceFirst("%class%", String.valueOf(npc.getClass().getSimpleName().replaceFirst("L2", "").replaceFirst("Instance", "")));
				dialog = dialog.replaceFirst("%id%", String.valueOf(npc.getNpcId()));
				dialog = dialog.replaceFirst("%respawn%", String.valueOf(npc.getSpawn() != null ? Util.formatTime(npc.getSpawn().getRespawnDelay()) : "0"));
				dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(npc.getWalkSpeed()));
				dialog = dialog.replaceFirst("%evs%", String.valueOf(npc.getEvasionRate(null)));
				dialog = dialog.replaceFirst("%acc%", String.valueOf(npc.getAccuracy()));
				dialog = dialog.replaceFirst("%crt%", String.valueOf(npc.getCriticalHit(null, null)));
				dialog = dialog.replaceFirst("%aspd%", String.valueOf(npc.getPAtkSpd()));
				dialog = dialog.replaceFirst("%cspd%", String.valueOf(npc.getMAtkSpd()));
				dialog = dialog.replaceFirst("%currentMP%", String.valueOf(npc.getCurrentMp()));
				dialog = dialog.replaceFirst("%currentHP%", String.valueOf(npc.getCurrentHp()));
				dialog = dialog.replaceFirst("%loc%", "");
				dialog = dialog.replaceFirst("%dist%", String.valueOf((int) npc.getDistance3D(player)));
				dialog = dialog.replaceFirst("%killed%", String.valueOf(0));//TODO [G1ta0] убрать
				dialog = dialog.replaceFirst("%spReward%", String.valueOf(npc.getSpReward()));
				dialog = dialog.replaceFirst("%xyz%", npc.getLoc().x + " " + npc.getLoc().y + " " + npc.getLoc().z);
				dialog = dialog.replaceFirst("%ai_type%", npc.getAI().getClass().getSimpleName());
				dialog = dialog.replaceFirst("%direction%", Location.getDirectionTo(npc, player).toString().toLowerCase());

				StringBuilder b = new StringBuilder("");
				for(GlobalEvent e : npc.getEvents())
					b.append(e.toString()).append(";");
				dialog = dialog.replaceFirst("%event%", b.toString());
			}
			else
				dialog = HtmCache.getInstance().getNotNull("scripts/actions/player.L2NpcInstance.onActionShift.htm", player);

			dialog = dialog.replaceFirst("%name%", nameNpc(npc));
			dialog = dialog.replaceFirst("%id%", String.valueOf(npc.getNpcId()));
			dialog = dialog.replaceFirst("%level%", String.valueOf(npc.getLevel()));
			dialog = dialog.replaceFirst("%respawn%", String.valueOf(npc.getSpawn() != null ? Util.formatTime(npc.getSpawn().getRespawnDelay()) : "0"));
			dialog = dialog.replaceFirst("%factionId%", String.valueOf(npc.getFaction()));
			dialog = dialog.replaceFirst("%aggro%", String.valueOf(npc.getAggroRange()));
			dialog = dialog.replaceFirst("%maxHp%", String.valueOf(npc.getMaxHp()));
			dialog = dialog.replaceFirst("%maxMp%", String.valueOf(npc.getMaxMp()));
			dialog = dialog.replaceFirst("%pDef%", String.valueOf(npc.getPDef(null)));
			dialog = dialog.replaceFirst("%mDef%", String.valueOf(npc.getMDef(null, null)));
			dialog = dialog.replaceFirst("%pAtk%", String.valueOf(npc.getPAtk(null)));
			dialog = dialog.replaceFirst("%mAtk%", String.valueOf(npc.getMAtk(null, null)));
			dialog = dialog.replaceFirst("%expReward%", String.valueOf(npc.getExpReward()));
			dialog = dialog.replaceFirst("%spReward%", String.valueOf(npc.getSpReward()));
			dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(npc.getRunSpeed()));

			// Дополнительная инфа для ГМов
			if(player.canOverrideCond(PcCondOverride.HTML_ACTION_CONDITIONS))
				dialog = dialog.replaceFirst("%AI%", String.valueOf(npc.getAI()) + ",<br1>active: " + npc.getAI().isActive() + ",<br1>intention: " + npc.getAI().getIntention());
			else
				dialog = dialog.replaceFirst("%AI%", "");

			show(dialog, player, npc);
		}
		return true;
	}

	public String getNpcRaceById(int raceId)
	{
		switch(raceId)
		{
			case 1:
				return "Undead";
			case 2:
				return "Magic Creatures";
			case 3:
				return "Beasts";
			case 4:
				return "Animals";
			case 5:
				return "Plants";
			case 6:
				return "Humanoids";
			case 7:
				return "Spirits";
			case 8:
				return "Angels";
			case 9:
				return "Demons";
			case 10:
				return "Dragons";
			case 11:
				return "Giants";
			case 12:
				return "Bugs";
			case 13:
				return "Fairies";
			case 14:
				return "Humans";
			case 15:
				return "Elves";
			case 16:
				return "Dark Elves";
			case 17:
				return "Orcs";
			case 18:
				return "Dwarves";
			case 19:
				return "Others";
			case 20:
				return "Non-living Beings";
			case 21:
				return "Siege Weapons";
			case 22:
				return "Defending Army";
			case 23:
				return "Mercenaries";
			case 24:
				return "Unknown Creature";
			case 25:
				return "Kamael";
			default:
				return "Not defined";
		}
	}

	public void droplist(String[] var)
	{
		if (var.length != 1)
			return;
		
		int page = Integer.valueOf(var[0]);
		
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		droplist(player, npc, page);
	}

	public void droplist(Player player, NpcInstance npc, int page)
	{
		if(player == null || npc == null)
			return;

		if(Config.ALT_GAME_SHOW_DROPLIST)
			RewardListInfo.showInfo(player, npc, page);
	}

	public void quests()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(nameNpc(npc)).append("<br></font></center><br>");

		Map<QuestEventType, Quest[]> list = npc.getTemplate().getQuestEvents();
		for(Map.Entry<QuestEventType, Quest[]> entry : list.entrySet())
		{
			for(Quest q : entry.getValue())
				dialog.append(entry.getKey()).append(" ").append(q.getClass().getSimpleName()).append("<br1>");
		}

		dialog.append("</body></html>");
		show(dialog.toString(), player, npc);
	}

	public void skills()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder();
		dialog.append("<html><body><center><font color=\"LEVEL\">NPC - ").append(nameNpc(npc)).append("<br></font></center>");

		Collection<Skill> list = npc.getAllSkills();
		if(list != null && !list.isEmpty())
		{
			dialog.append("<br><center><font color=0099FF name=hs12>Active Skills</font></center><br>");
			for(Skill s : list)
				if (s.isActive())
				{
					dialog.append("<table border=0 cellspacing=0 cellpadding=0>");
					dialog.append("<tr>");
					dialog.append("<td width=345>");
					dialog.append("<img src=l2ui.squaregray width=290 height=1>");
					dialog.append("</td>");
					dialog.append("</tr>");
					dialog.append("</table>");
					dialog.append("<table border=0 cellspacing=6 cellpadding=3>");
					dialog.append("<tr>");
					dialog.append("<td align=right valign=top>");
					dialog.append("<img src=").append(s.getIcon()).append(" width=32 height=32>");
					dialog.append("</td>");
					dialog.append("<td FIXWIDTH=200 align=left valign=top>");
					dialog.append("<font color=0099FF>").append(s.getName()).append("</font>&nbsp;<br1>›&nbsp;").append("Id: ").append(s.getId()).append(" Level: ").append(s.getLevel());
					dialog.append("</td>");
					dialog.append("</tr>");
					dialog.append("</table>");
				}
			dialog.append("<br><center><font color=FF9933 name=hs12>Passive Skills</font></center><br>");
			for (Skill s : list)
				if (!s.isActive())
				{
					dialog.append("<table border=0 cellspacing=0 cellpadding=0>");
					dialog.append("<tr>");
					dialog.append("<td width=345>");
					dialog.append("<img src=l2ui.squaregray width=290 height=1>");
					dialog.append("</td>");
					dialog.append("</tr>");
					dialog.append("</table>");
					dialog.append("<table border=0 cellspacing=6 cellpadding=3>");
					dialog.append("<tr>");
					dialog.append("<td align=right valign=top>");
					dialog.append("<img src=").append(s.getIcon()).append(" width=32 height=32>");
					dialog.append("</td>");
					dialog.append("<td FIXWIDTH=200 align=left valign=top>");
					dialog.append("<font color=FF9933>").append(s.getName()).append("</font>&nbsp;<br1>›&nbsp;").append("Id: ").append(s.getId()).append(" Level: ").append(s.getLevel());
					dialog.append("</td>");
					dialog.append("</tr>");
					dialog.append("</table>");
				}
		}

		dialog.append("</body></html>");
		show(dialog.toString(), player, npc);
	}

	public void effects()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(nameNpc(npc)).append("<br></font></center><br>");

		List<Effect> list = npc.getEffectList().getAllEffects();
		if(list != null && !list.isEmpty())
			for(Effect e : list)
				dialog.append(e.getSkill().getName()).append("<br1>");

		dialog.append("<br><center><button value=\"");
		dialog.append(player.isLangRus() ? "Обновить" : "Refresh");
		dialog.append("\" action=\"bypass -h scripts_actions.OnActionShift:effects\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center></body></html>");

		show(dialog.toString(), player, npc);
	}

	public void stats()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		String dialog = HtmCache.getInstance().getNotNull("scripts/actions/player.L2NpcInstance.stats.htm", player);
		dialog = dialog.replaceFirst("%name%", nameNpc(npc));
		dialog = dialog.replaceFirst("%level%", String.valueOf(npc.getLevel()));
		dialog = dialog.replaceFirst("%factionId%", String.valueOf(npc.getFaction()));
		dialog = dialog.replaceFirst("%aggro%", String.valueOf(npc.getAggroRange()));
		dialog = dialog.replaceFirst("%race%", getNpcRaceById(npc.getTemplate().getRace()));
		dialog = dialog.replaceFirst("%maxHp%", String.valueOf(npc.getMaxHp()));
		dialog = dialog.replaceFirst("%maxMp%", String.valueOf(npc.getMaxMp()));
		dialog = dialog.replaceFirst("%pDef%", String.valueOf(npc.getPDef(null)));
		dialog = dialog.replaceFirst("%mDef%", String.valueOf(npc.getMDef(null, null)));
		dialog = dialog.replaceFirst("%pAtk%", String.valueOf(npc.getPAtk(null)));
		dialog = dialog.replaceFirst("%mAtk%", String.valueOf(npc.getMAtk(null, null)));
		dialog = dialog.replaceFirst("%accuracy%", String.valueOf(npc.getAccuracy()));
		dialog = dialog.replaceFirst("%evasionRate%", String.valueOf(npc.getEvasionRate(null)));
		dialog = dialog.replaceFirst("%criticalHit%", String.valueOf(npc.getCriticalHit(null, null)));
		dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(npc.getRunSpeed()));
		dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(npc.getWalkSpeed()));
		dialog = dialog.replaceFirst("%pAtkSpd%", String.valueOf(npc.getPAtkSpd()));
		dialog = dialog.replaceFirst("%mAtkSpd%", String.valueOf(npc.getMAtkSpd()));
		show(dialog, player, npc);
	}

	public void resists()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(nameNpc(npc)).append("<br></font></center><table width=\"80%\">");

		boolean hasResist;

		hasResist = addResist(dialog, "Fire", npc.calcStat(Stats.DEFENCE_FIRE, 0, null, null));
		hasResist |= addResist(dialog, "Wind", npc.calcStat(Stats.DEFENCE_WIND, 0, null, null));
		hasResist |= addResist(dialog, "Water", npc.calcStat(Stats.DEFENCE_WATER, 0, null, null));
		hasResist |= addResist(dialog, "Earth", npc.calcStat(Stats.DEFENCE_EARTH, 0, null, null));
		hasResist |= addResist(dialog, "Light", npc.calcStat(Stats.DEFENCE_HOLY, 0, null, null));
		hasResist |= addResist(dialog, "Darkness", npc.calcStat(Stats.DEFENCE_UNHOLY, 0, null, null));
		hasResist |= addResist(dialog, "Bleed", npc.calcStat(Stats.BLEED_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Poison", npc.calcStat(Stats.POISON_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Stun", npc.calcStat(Stats.STUN_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Root", npc.calcStat(Stats.ROOT_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Sleep", npc.calcStat(Stats.SLEEP_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Paralyze", npc.calcStat(Stats.PARALYZE_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Mental", npc.calcStat(Stats.MENTAL_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Debuff", npc.calcStat(Stats.DEBUFF_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Cancel", npc.calcStat(Stats.CANCEL_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Sword", 100 - npc.calcStat(Stats.SWORD_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Dual Sword", 100 - npc.calcStat(Stats.DUAL_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Blunt", 100 - npc.calcStat(Stats.BLUNT_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Dagger", 100 - npc.calcStat(Stats.DAGGER_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Bow", 100 - npc.calcStat(Stats.BOW_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Crossbow", 100 - npc.calcStat(Stats.CROSSBOW_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Polearm", 100 - npc.calcStat(Stats.POLE_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Fist", 100 - npc.calcStat(Stats.FIST_WPN_VULNERABILITY, null, null));

		if(!hasResist)
			dialog.append("</table>No resists</body></html>");
		else
			dialog.append("</table></body></html>");
		show(dialog.toString(), player, npc);
	}

	private boolean addResist(StringBuilder dialog, String name, double val)
	{
		if (val == 0)
			return false;

		dialog.append("<tr><td>").append(name).append("</td><td>");
		if (val == Double.POSITIVE_INFINITY)
			dialog.append("MAX");
		else if (val == Double.NEGATIVE_INFINITY)
			dialog.append("MIN");
		else
		{
			dialog.append(String.valueOf((int)val));
			dialog.append("</td></tr>");
			return true;
		}

		dialog.append("</td></tr>");
		return true;
	}

	public void aggro()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><table width=\"80%\"><tr><td>Attacker</td><td>Damage</td><td>Hate</td></tr>");

		Set<HateInfo> set = new TreeSet<HateInfo>(HateComparator.getInstance());
		set.addAll(npc.getAggroList().getCharMap().values());
		for(HateInfo aggroInfo : set)
		{
			if (!player.canOverrideCond(PcCondOverride.HTML_ACTION_CONDITIONS) && aggroInfo.attacker.isPlayer() && aggroInfo.attacker.getPlayer().isGM())
				continue;
			
			dialog.append("<tr><td>" + aggroInfo.attacker.getName() + "</td><td>" + aggroInfo.damage + "</td><td>" + aggroInfo.hate + "</td></tr>");
		}
		dialog.append("</table><br><center><button value=\"");
		dialog.append(player.isLangRus() ? "Обновить" : "Refresh");
		dialog.append("\" action=\"bypass -h scripts_actions.OnActionShift:aggro\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center></body></html>");
		show(dialog.toString(), player, npc);
	}

	public boolean OnActionShift_DoorInstance(Player player, GameObject object)
	{
		if(player == null || object == null || !player.canOverrideCond(PcCondOverride.HTML_ACTION_CONDITIONS) || !object.isDoor())
			return false;

		String dialog;
		DoorInstance door = (DoorInstance) object;
		dialog = HtmCache.getInstance().getNotNull("scripts/actions/admin.L2DoorInstance.onActionShift.htm", player);
		dialog = dialog.replaceFirst("%CurrentHp%", String.valueOf((int) door.getCurrentHp()));
		dialog = dialog.replaceFirst("%MaxHp%", String.valueOf(door.getMaxHp()));
		dialog = dialog.replaceAll("%ObjectId%", String.valueOf(door.getObjectId()));
		dialog = dialog.replaceFirst("%doorId%", String.valueOf(door.getDoorId()));
		dialog = dialog.replaceFirst("%pdef%", String.valueOf(door.getPDef(null)));
		dialog = dialog.replaceFirst("%mdef%", String.valueOf(door.getMDef(null, null)));
		dialog = dialog.replaceFirst("%type%", door.getDoorType().name());
		dialog = dialog.replaceFirst("%upgradeHP%", String.valueOf(door.getUpgradeHp()));
		StringBuilder b = new StringBuilder("");
		for(GlobalEvent e : door.getEvents())
			b.append(e.toString()).append(";");
		dialog = dialog.replaceFirst("%event%", b.toString());

		show(dialog, player);
		player.sendActionFailed();
		return true;
	}

	public boolean OnActionShift_Player(Player player, GameObject object)
	{
		if(player == null || object == null)
			return false;
		
		Player target = (Player) object;
		String tempstats = HtmCache.getInstance().getNotNull("scripts/actions/stats.htm", player);
		String temp = HtmCache.getInstance().getNotNull("scripts/actions/player_shift.htm", player);
		
		player.setTarget(target);
		
		if(target.isPlayer() && player.canOverrideCond(PcCondOverride.HTML_ACTION_CONDITIONS))
		{
			AdminEditChar.showCharacterList(player, (Player) object);
		}
		else if (target != null && target.isPlayer() && Config.ALT_PLAYER_SHIFTCLICK && PremiumAccountsTable.getPlayerShiftClick(player))
		{
			tempstats = tempstats.replaceAll("%target%", target.getName());
			tempstats = tempstats.replaceFirst("%classname%", HtmlUtils.htmlClassName(target.getClassId().getId()));
			tempstats = tempstats.replaceFirst("%level%", "" + target.getLevel());
			tempstats = tempstats.replaceFirst("%icon%", "icon.skill" + target.getRace().name());
			tempstats = tempstats.replaceFirst("%recommends%", "" + target.getRecomHave());

			if(target.getPlayer() != null && PlayerTops.getInstance().getPlayerInTop(target.getName(), "_PvP") != null)
			{
				tempstats = tempstats.replaceFirst("%intopPvP%", "" + PlayerTops.getInstance().getPlayerInTop(target.getName(), "_PvP").getPlace());
				tempstats = tempstats.replaceFirst("%pvpkills%", "" + PlayerTops.getInstance().getPlayerInTop(target.getName(), "_PvP").getTop());
			}
			else
			{
				tempstats = tempstats.replaceFirst("%pvpkills%", "0");
				tempstats = tempstats.replaceFirst("%intopPvP%", "0");
			}

			if(target.getPlayer() != null && PlayerTops.getInstance().getPlayerInTop(target.getName(), "_PK") != null)
			{
				tempstats = tempstats.replaceFirst("%intopPK%", "" + PlayerTops.getInstance().getPlayerInTop(target.getName(), "_PK").getPlace());
				tempstats = tempstats.replaceFirst("%pkkills%", "" + PlayerTops.getInstance().getPlayerInTop(target.getName(), "_PK").getTop());
			}
			else
			{
				tempstats = tempstats.replaceFirst("%pkkills%", "0");
				tempstats = tempstats.replaceFirst("%intopPK%", "0");
			}

			for(Entry<String, Integer> stat : target.getStatsRecorder().get().entrySet())
			{
				int h = stat.getValue();
				String n = stat.getKey();
				tempstats = tempstats.replaceAll("%" + n + "1%", "" + h);
				tempstats = tempstats.replaceAll("%" + n + "2%", "" + (60 - h));
				tempstats = tempstats.replaceAll("%cap" + n + "1%", h == 0 ? "Gauge_DF_Exp_bg_Left" : "Gauge_DF_CP_Left");
				tempstats = tempstats.replaceAll("%cap" + n + "2%", h == 60 ? "Gauge_DF_CP_Right" : "Gauge_DF_Exp_bg_Right");
			}

			Functions.show(tempstats, player, null);
		}
		else if (target != null && target.isPlayer() && Config.ALT_PLAYER_SHIFTCLICK)
		{
			temp = temp.replaceAll("%target%", target.getName());
			temp = temp.replaceFirst("%classname%", HtmlUtils.htmlClassName(target.getClassId().getId()));
			temp = temp.replaceFirst("%level%", "" + target.getLevel());
			temp = temp.replaceFirst("%icon%", "icon.skill" + target.getRace().name());
			temp = temp.replaceFirst("%recommends%", "" + target.getRecomHave());

			if(target.getPlayer() != null && PlayerTops.getInstance().getPlayerInTop(target.getName(), "_PvP") != null)
			{
				temp = temp.replaceAll("%intopPvP%", "" + PlayerTops.getInstance().getPlayerInTop(target.getName(), "_PvP").getPlace());
				temp = temp.replaceAll("%pvpkills%", "" + PlayerTops.getInstance().getPlayerInTop(target.getName(), "_PvP").getTop());
			}
			else
			{
				temp = temp.replaceAll("%intopPvP%", "0");
				temp = temp.replaceAll("%pvpkills%", "0");
				
			}

			if(target.getPlayer() != null && PlayerTops.getInstance().getPlayerInTop(target.getName(), "_PK") != null)
			{
				temp = temp.replaceAll("%intopPK%", "" + PlayerTops.getInstance().getPlayerInTop(target.getName(), "_PK").getPlace());
				temp = temp.replaceAll("%pkkills%", "" + PlayerTops.getInstance().getPlayerInTop(target.getName(), "_PK").getTop());
			}
			else
			{
				temp = temp.replaceAll("%intopPK%", "0");
				temp = temp.replaceAll("%pkkills%", "0");
				
			}

			for(Entry<String, Integer> stat : target.getStatsRecorder().get().entrySet())
			{
				int h = stat.getValue();
				String n = stat.getKey();
				temp = temp.replaceAll("%" + n + "1%", "" + h);
				temp = temp.replaceAll("%" + n + "2%", "" + (60 - h));
				temp = temp.replaceAll("%cap" + n + "1%", h == 0 ? "Gauge_DF_Exp_bg_Left" : "Gauge_DF_CP_Left");
				temp = temp.replaceAll("%cap" + n + "2%", h == 60 ? "Gauge_DF_CP_Right" : "Gauge_DF_Exp_bg_Right");
			}

			Functions.show(temp, player, null);
		}
		
		return true;
	}

	public boolean OnActionShift_PetInstance(Player player, GameObject object)
	{
		if(player == null || object == null || !player.canOverrideCond(PcCondOverride.HTML_ACTION_CONDITIONS))
			return false;
		if(object.isPet())
		{
			PetInstance pet = (PetInstance) object;

			player.setTarget(pet);
			
			String dialog;

			dialog = HtmCache.getInstance().getNotNull("scripts/actions/admin.L2PetInstance.onActionShift.htm", player);
			dialog = dialog.replaceFirst("%name%", HtmlUtils.htmlNpcName(pet.getNpcId()));
			dialog = dialog.replaceFirst("%title%", String.valueOf(StringUtils.isEmpty(pet.getTitle()) ? "Empty" : pet.getTitle()));
			dialog = dialog.replaceFirst("%level%", String.valueOf(pet.getLevel()));
			dialog = dialog.replaceFirst("%class%", String.valueOf(pet.getClass().getSimpleName().replaceFirst("L2", "").replaceFirst("Instance", "")));
			dialog = dialog.replaceFirst("%xyz%", pet.getLoc().x + " " + pet.getLoc().y + " " + pet.getLoc().z);
			dialog = dialog.replaceFirst("%heading%", String.valueOf(pet.getLoc().h));

			dialog = dialog.replaceFirst("%owner%", String.valueOf(pet.getPlayer().getName()));
			dialog = dialog.replaceFirst("%ownerId%", String.valueOf(pet.getPlayer().getObjectId()));
			dialog = dialog.replaceFirst("%npcId%", String.valueOf(pet.getNpcId()));
			dialog = dialog.replaceFirst("%controlItemId%", String.valueOf(pet.getControlItem().getItemId()));

			dialog = dialog.replaceFirst("%exp%", String.valueOf(pet.getExp()));
			dialog = dialog.replaceFirst("%sp%", String.valueOf(pet.getSp()));

			dialog = dialog.replaceFirst("%maxHp%", String.valueOf(pet.getMaxHp()));
			dialog = dialog.replaceFirst("%maxMp%", String.valueOf(pet.getMaxMp()));
			dialog = dialog.replaceFirst("%currHp%", String.valueOf((int) pet.getCurrentHp()));
			dialog = dialog.replaceFirst("%currMp%", String.valueOf((int) pet.getCurrentMp()));

			dialog = dialog.replaceFirst("%pDef%", String.valueOf(pet.getPDef(null)));
			dialog = dialog.replaceFirst("%mDef%", String.valueOf(pet.getMDef(null, null)));
			dialog = dialog.replaceFirst("%pAtk%", String.valueOf(pet.getPAtk(null)));
			dialog = dialog.replaceFirst("%mAtk%", String.valueOf(pet.getMAtk(null, null)));
			dialog = dialog.replaceFirst("%accuracy%", String.valueOf(pet.getAccuracy()));
			dialog = dialog.replaceFirst("%evasionRate%", String.valueOf(pet.getEvasionRate(null)));
			dialog = dialog.replaceFirst("%crt%", String.valueOf(pet.getCriticalHit(null, null)));
			dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(pet.getRunSpeed()));
			dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(pet.getWalkSpeed()));
			dialog = dialog.replaceFirst("%pAtkSpd%", String.valueOf(pet.getPAtkSpd()));
			dialog = dialog.replaceFirst("%mAtkSpd%", String.valueOf(pet.getMAtkSpd()));
			dialog = dialog.replaceFirst("%dist%", String.valueOf((int) pet.getRealDistance(player)));

			dialog = dialog.replaceFirst("%STR%", String.valueOf(pet.getSTR()));
			dialog = dialog.replaceFirst("%DEX%", String.valueOf(pet.getDEX()));
			dialog = dialog.replaceFirst("%CON%", String.valueOf(pet.getCON()));
			dialog = dialog.replaceFirst("%INT%", String.valueOf(pet.getINT()));
			dialog = dialog.replaceFirst("%WIT%", String.valueOf(pet.getWIT()));
			dialog = dialog.replaceFirst("%MEN%", String.valueOf(pet.getMEN()));

			show(dialog, player);
		}
		return true;
	}
	
	public boolean OnActionShift_ItemInstance(Player player, GameObject object)
	{
		if(player == null || object == null || !player.canOverrideCond(PcCondOverride.HTML_ACTION_CONDITIONS))
			return false;
		if(object.isItem())
		{
			String dialog;
			ItemInstance item = (ItemInstance) object;
			dialog = HtmCache.getInstance().getNotNull("scripts/actions/admin.L2ItemInstance.onActionShift.htm", player);
			dialog = dialog.replaceFirst("%name%", String.valueOf(item.getTemplate().getName()));
			dialog = dialog.replaceFirst("%objId%", String.valueOf(item.getObjectId()));
			dialog = dialog.replaceFirst("%itemId%", String.valueOf(item.getItemId()));
			dialog = dialog.replaceFirst("%grade%", String.valueOf(item.getCrystalType()));
			dialog = dialog.replaceFirst("%count%", String.valueOf(item.getCount()));

			Player owner = GameObjectsStorage.getPlayer(item.getOwnerId()); //FIXME [VISTALL] несовсем верно, может быть CCE при условии если овнер не игрок
			dialog = dialog.replaceFirst("%owner%", String.valueOf(owner == null ? "none" : owner.getName()));
			dialog = dialog.replaceFirst("%ownerId%", String.valueOf(item.getOwnerId()));

			for(Element e : Element.VALUES)
				dialog = dialog.replaceFirst("%" + e.name().toLowerCase() + "Val%", String.valueOf(item.getAttributeElementValue(e, true)));

			dialog = dialog.replaceFirst("%attrElement%", String.valueOf(item.getAttributeElement()));
			dialog = dialog.replaceFirst("%attrValue%", String.valueOf(item.getAttributeElementValue()));

			dialog = dialog.replaceFirst("%enchLevel%", String.valueOf(item.getEnchantLevel()));
			dialog = dialog.replaceFirst("%type%", String.valueOf(item.getItemType()));

			dialog = dialog.replaceFirst("%dropTime%", String.valueOf(item.getDropTimeOwner()));
			//dialog = dialog.replaceFirst("%dropOwner%", String.valueOf(item.getDropOwnerId()));
			//dialog = dialog.replaceFirst("%dropOwnerId%", String.valueOf(item.getDropOwnerId()));

			show(dialog, player);
			player.sendActionFailed();
		}
		return true;
	}
	
	private String nameNpc(NpcInstance npc)
	{
		if(npc.getNameNpcString() == NpcString.NONE)
			return HtmlUtils.htmlNpcName(npc.getNpcId());
		else
			return HtmlUtils.htmlNpcString(npc.getNameNpcString().getId(), npc.getName());
	}
	
	private void generatenpcInfo(Player player, NpcInstance npc)
	{
		String htmltosend = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/droplist/npcinfo.htm", player);
		
		if (npc != null)
		{
			htmltosend = htmltosend.replaceAll("%icon%", getIconByRace(npc.getTemplate().getRace()));
			htmltosend = htmltosend.replaceAll("%name%", npc.getName());
			htmltosend = htmltosend.replaceAll("%id%", String.valueOf(npc.getNpcId()));
			htmltosend = htmltosend.replaceAll("%race%", getNpcRaceById(npc.getTemplate().getRace()));
			htmltosend = htmltosend.replaceAll("%level%", String.valueOf(npc.getLevel()));
			htmltosend = htmltosend.replaceAll("%respawn%", String.valueOf(npc.getSpawn() != null ? Util.formatTime(npc.getSpawn().getRespawnDelay()) : "0"));
			htmltosend = htmltosend.replaceAll("%factionId%", String.valueOf(npc.getFaction()));
			htmltosend = htmltosend.replaceAll("%aggro%", String.valueOf(npc.getAggroRange()));
			htmltosend = htmltosend.replaceAll("%maxHp%", String.valueOf(npc.getMaxHp()));
			htmltosend = htmltosend.replaceAll("%maxMp%", String.valueOf(npc.getMaxMp()));
			htmltosend = htmltosend.replaceAll("%pDef%", String.valueOf(npc.getPDef(null)));
			htmltosend = htmltosend.replaceAll("%mDef%", String.valueOf(npc.getMDef(null, null)));
			htmltosend = htmltosend.replaceAll("%pAtk%", String.valueOf(npc.getPAtk(null)));
			htmltosend = htmltosend.replaceAll("%mAtk%", String.valueOf(npc.getMAtk(null, null)));
			htmltosend = htmltosend.replaceAll("%expReward%", String.valueOf(npc.getExpReward()));
			htmltosend = htmltosend.replaceAll("%spReward%", String.valueOf(npc.getSpReward()));
			htmltosend = htmltosend.replaceAll("%runSpeed%", String.valueOf(npc.getRunSpeed()));
			htmltosend = htmltosend.replaceAll("%class%", getTypeofNpc(npc.getTemplate()));
			htmltosend = htmltosend.replaceAll("%walkSpeed%", String.valueOf(npc.getWalkSpeed()));
			htmltosend = htmltosend.replaceAll("%evs%", String.valueOf(npc.getEvasionRate(null)));
			htmltosend = htmltosend.replaceAll("%acc%", String.valueOf(npc.getAccuracy()));
			htmltosend = htmltosend.replaceAll("%aspd%", String.valueOf(npc.getPAtkSpd()));
			htmltosend = htmltosend.replaceAll("%cspd%", String.valueOf(npc.getMAtkSpd()));
			
			htmltosend = htmltosend.replaceAll("%defFire%", getResist(npc.calcStat(Stats.DEFENCE_FIRE, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%defWind%", getResist(npc.calcStat(Stats.DEFENCE_WIND, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%defWater%", getResist(npc.calcStat(Stats.DEFENCE_WATER, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%defEarth%", getResist(npc.calcStat(Stats.DEFENCE_EARTH, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%defHoly%", getResist(npc.calcStat(Stats.DEFENCE_HOLY, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%defDark%", getResist(npc.calcStat(Stats.DEFENCE_UNHOLY, 0, null, null)));
			
			htmltosend = htmltosend.replaceAll("%vulSword%", getResist(100 - npc.calcStat(Stats.SWORD_WPN_VULNERABILITY, null, null)));
			htmltosend = htmltosend.replaceAll("%vulDual%", getResist(100 - npc.calcStat(Stats.DUAL_WPN_VULNERABILITY, null, null)));
			htmltosend = htmltosend.replaceAll("%vulBlunt%", getResist(100 - npc.calcStat(Stats.BLUNT_WPN_VULNERABILITY, null, null)));
			htmltosend = htmltosend.replaceAll("%vulDagger%", getResist(100 - npc.calcStat(Stats.DAGGER_WPN_VULNERABILITY, null, null)));
			htmltosend = htmltosend.replaceAll("%vulBow%", getResist(100 - npc.calcStat(Stats.BOW_WPN_VULNERABILITY, null, null)));
			htmltosend = htmltosend.replaceAll("%vulCrossbow%", getResist(100 - npc.calcStat(Stats.CROSSBOW_WPN_VULNERABILITY, null, null)));
			htmltosend = htmltosend.replaceAll("%vulPole%", getResist(100 - npc.calcStat(Stats.POLE_WPN_VULNERABILITY, null, null)));
			htmltosend = htmltosend.replaceAll("%vulFist%", getResist(100 - npc.calcStat(Stats.FIST_WPN_VULNERABILITY, null, null)));
			
			htmltosend = htmltosend.replaceAll("%resStun%", getResist(npc.calcStat(Stats.STUN_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resRoot%", getResist(npc.calcStat(Stats.ROOT_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resSleep%", getResist(npc.calcStat(Stats.SLEEP_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resPara%", getResist(npc.calcStat(Stats.PARALYZE_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resBleed%", getResist(npc.calcStat(Stats.BLEED_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resPoison%", getResist(npc.calcStat(Stats.POISON_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resCancel%", getResist(npc.calcStat(Stats.CANCEL_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resMental%", getResist(npc.calcStat(Stats.MENTAL_RESIST, 0, null, null)));
			htmltosend = htmltosend.replaceAll("%resDebuff%", getResist(npc.calcStat(Stats.DEBUFF_RESIST, 0, null, null)));
			
			htmltosend = htmltosend.replaceAll("%npcImage%", "%image:" + npc.getNpcId() + ".jpg%");
			
			ShowBoard.separateAndSend(htmltosend, player);
		}
	}
	
	private static String getResist(double val)
	{
		if (val == 0)
			return "-";

		if (val == Double.POSITIVE_INFINITY)
			return "<font color=\"F80000\">Max</font>"; // max value color red
		else if (val == Double.NEGATIVE_INFINITY)
			return "<font color=\"FFFF33\">Min</font>"; // min value color yellow
		else if (val < 0)
			return "<font color=\"787878\">" + (int)val + "</font>"; // negative color grey
		
		return "<font color=\"66CC33\">" + (int)val + "</font>"; // positive color green
	}
	
	private static String getTypeofNpc(NpcTemplate npc)
	{
		if (npc.isInstanceOf(BossInstance.class))
			return "GrandBoss";
		else if (npc.isInstanceOf(RaidBossInstance.class))
			return "RaidBoss";
		else if (npc.isInstanceOf(MonsterInstance.class))
			return "Monster";
		else if (npc.isInstanceOf(NpcInstance.class))
			return "Npc";
		
		return "Unknown";
	}
	
	private static String getIconByRace(int race)
	{
		switch (race)
		{
			case 1:
				return "icon.skill4290";
			case 2:
				return "icon.skill4291";
			case 3:
				return "icon.skill4292";
			case 4:
				return "icon.skill4293";
			case 5:
				return "icon.skill4294";
			case 6:
				return "icon.skill4295";
			case 7:
				return "icon.skill4296";
			case 8:
				return "icon.skill4297";
			case 9:
				return "icon.skill4298";
			case 10:
				return "icon.skill4299";
			case 11:
				return "icon.skill4300";
			case 12:
				return "icon.skill4301";
			case 13:
				return "icon.skill4302";
			case 14:
				return "icon.skill4416_human";
			case 15:
				return "icon.skill4416_elf";
			case 16:
				return "icon.skill4416_darkelf";
			case 17:
				return "icon.skill4416_orc";
			case 18:
				return "icon.skill4416_dwarf";
			case 19:
				return "icon.skill4416_etc";
			case 20:
				return "icon.skill4416_none";
			case 21:
				return "icon.skill4416_siegeweapon";
			case 22:
				return "icon.skill4416_castleguard";
			case 23:
				return "icon.skill4416_mercenary";
			case 24:
				return "icon.skill4286";
			case 25:
				return "icon.skill4416_kamael";
		}
		
		return "L2UI.TutorialHelp";
	}
}