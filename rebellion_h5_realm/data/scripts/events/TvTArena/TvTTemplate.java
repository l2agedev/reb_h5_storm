package events.TvTArena;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.Announcements;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.Revive;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;


public abstract class TvTTemplate extends Functions
{
	private static int ITEM_ID = 4357;
	private static String ITEM_NAME = "Silver Shilen";
	private static int LENGTH_TEAM = 12;
	private static boolean ALLOW_BUFFS = false;
	private static boolean ALLOW_CLAN_SKILL = true;
	private static boolean ALLOW_HERO_SKILL = false;

	/*
	Config.EVENT_TVT_ARENA_ITEM_ID;
	Config.EVENT_TVT_ARENA_ITEM_NAME;
	Config.EVENT_TVT_ARENA_MAX_LENGTH_TEAM;
	Config.EVENT_TVT_ARENA_ALLOW_BUFFS;
	Config.EVENT_TVT_ARENA_ALLOW_CLAN_SKILL;
	Config.EVENT_TVT_ARENA_ALLOW_HERO_SKILL;
	 */

	protected int _managerId;
	protected String _className;

	protected Long _creatorId;
	protected NpcInstance _manager;
	protected int _status = 0;
	protected int _CharacterFound = 0;
	protected int _price = 10000;
	protected int _team1count = 1;
	protected int _team2count = 1;
	protected int _team1min = 1;
	protected int _team1max = 85;
	protected int _team2min = 1;
	protected int _team2max = 85;
	protected int _timeToStart = 10;
	protected boolean _timeOutTask;

	protected List<Location> _team1points;
	protected List<Location> _team2points;

	protected List<Long> _team1list;
	protected List<Long> _team2list;
	protected List<Long> _team1live;
	protected List<Long> _team2live;

	protected Zone _zone;
	protected ZoneListener _zoneListener;

	protected abstract void onLoad();

	protected abstract void onReload();

	public void template_stop()
	{
		if(_status <= 0)
			return;

		sayToAll("scripts.events.TvTArena.TvTTemplate.first", null);

		unParalyzeTeams();
		ressurectPlayers();
		returnItemToTeams();
		healPlayers();
		removeBuff();
		teleportPlayersToSavedCoords();
		clearTeams();
		_status = 0;
		_timeOutTask = false;
	}

	public void template_create1(Player player)
	{
		if(_status > 0)
		{
			show(player.isLangRus() ? "Дождитесь окончания боя." : "Wait until the fight ends.", player);
			return;
		}

		if(player.getTeam() != TeamType.NONE)
		{
			show(player.isLangRus() ? "Вы уже зарегистрированы." : "You are already a member.", player);
			return;
		}
		show("scripts/events/TvTArena/" + _managerId + "-1.htm", player);
	}

	public void template_register(Player player)
	{
		if(_status == 0)
		{
			show(player.isLangRus() ? "Бой на данный момент не создан." : "Fight is not currently established.", player);
			return;
		}

		if(_status > 1)
		{
			show(player.isLangRus() ? "Дождитесь окончания боя." : "Wait until the fight ends.", player);
			return;
		}

		if(player.getTeam() != TeamType.NONE)
		{
			show(player.isLangRus() ? "Вы уже зарегистрированы." : "You are already a member.", player);
			return;
		}
		show("scripts/events/TvTArena/" + _managerId + "-3.htm", player);
	}

	public void template_check1(Player player, NpcInstance manager, String[] var)
	{
		if(var.length != 8)
		{
			show(player.isLangRus() ? "Некорректные данные." : "Incorrect data.", player);
			return;
		}

		if(_status > 0)
		{
			show(player.isLangRus() ? "Дождитесь окончания боя." : "Wait until the fight ends.", player);
			return;
		}
		if(manager == null || !manager.isNpc())
		{
			show(player.isLangRus() ? "Hacker? :) " : "Хакер? :)" + manager, player);
			return;
		}
		_manager = manager;
		try
		{
			_price = Integer.valueOf(var[0]);
			_team1count = Integer.valueOf(var[1]);
			_team2count = Integer.valueOf(var[2]);
			_team1min = Integer.valueOf(var[3]);
			_team1max = Integer.valueOf(var[4]);
			_team2min = Integer.valueOf(var[5]);
			_team2max = Integer.valueOf(var[6]);
			_timeToStart = Integer.valueOf(var[7]);
		}
		catch(Exception e)
		{
			show(player.isLangRus() ? "Некорректные данные." : "Incorrect data.", player);
			return;
		}
		if(_price < 1 || _price > 500)
		{
			show(player.isLangRus() ? "Неправильная ставка." : "Incorrect rate.", player);
			return;
		}
		if(_team1count < 1 || _team1count > LENGTH_TEAM || _team2count < 1 || _team2count > LENGTH_TEAM)
		{
			show(player.isLangRus() ? "Неправильный размер команды." : "Incorrect team size.", player);
			return;
		}
		if(_team1min < 1 || _team1min > 86 || _team2min < 1 || _team2min > 86 || _team1max < 1 || _team1max > 86 || _team2max < 1 || _team2max > 86 || _team1min > _team1max || _team2min > _team2max)
		{
			show(player.isLangRus() ? "Неправильный уровень." : "Wrong level.", player);
			return;
		}
		if(player.getLevel() < _team1min || player.getLevel() > _team1max)
		{
			show(player.isLangRus() ? "Неправильный уровень." : "Wrong level.", player);
			return;
		}
		if(_timeToStart < 1 || _timeToStart > 10)
		{
			show(player.isLangRus() ? "Неправильное время." : "Wrong time.", player);
			return;
		}
		if(getItemCount(player, ITEM_ID) < _price)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		_creatorId = player.getStoredId();
		removeItem(player, ITEM_ID, _price);
		player.setTeam(TeamType.BLUE);
		_status = 1;
		_team1list.clear();
		_team2list.clear();
		_team1live.clear();
		_team2live.clear();
		_team1list.add(player.getStoredId());
		
		String[] param = 
		{
			String.valueOf(player.getName()),	// {0}
			String.valueOf(_team1count),		// {1}
			String.valueOf(_team2count),		// {2}
			String.valueOf(_team1min),			// {3}
			String.valueOf(_team1max),			// {4}
			String.valueOf(_team2min),			// {5}
			String.valueOf(_team2max),			// {6}
			String.valueOf(_price),				// {7}
			String.valueOf(ITEM_NAME),			// {8}
			String.valueOf(_timeToStart),		// {9}
			String.valueOf(_manager.getName())	// {10}
		};
		sayToAll("scripts.events.TvTArena.TvTTemplate.announce1", param);
		executeTask("events.TvTArena." + _className, "announce", new Object[0], 60000);
	}

	public void template_register_check(Player player)
	{
		if(_status == 0)
		{
			show(player.isLangRus() ? "Бой на данный момент не создан." : "Fight is not currently established.", player);
			return;
		}

		if(_status > 1)
		{
			show(player.isLangRus() ? "Дождитесь окончания боя." : "Wait until the fight ends.", player);
			return;
		}

		if(_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId()))
		{
			show(player.isLangRus() ? "Вы уже зарегистрированы." : "You are already a member.", player);
			return;
		}

		if(player.getTeam() != TeamType.NONE)
		{
			show(player.isLangRus() ? "Вы уже зарегистрированы." : "You are already a member.", player);
			return;
		}

		if(getItemCount(player, ITEM_ID) < _price)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}

		int size1 = _team1list.size(), size2 = _team2list.size();

		if(size1 > size2)
		{
			String t = null;
			if(tryRegister(2, player) != null)
				if((t = tryRegister(1, player)) != null)
					show(t, player);
		}
		else if(size1 < size2)
		{
			String t = null;
			if(tryRegister(1, player) != null)
				if((t = tryRegister(2, player)) != null)
					show(t, player);
		}
		else
		{
			int team = Rnd.get(1, 2);
			String t = null;
			if(tryRegister(team, player) != null)
				if((t = tryRegister(team == 1 ? 2 : 1, player)) != null)
					show(t, player);
		}
	}

	private String tryRegister(int team, Player player)
	{
		if(team == 1)
		{
			if(player.getLevel() < _team1min || player.getLevel() > _team1max)
				return(player.isLangRus() ? "Вы не подходите по уровню." : "You do not match the level.");
			
			if(_team1list.size() >= _team1count)
				return(player.isLangRus() ? "Команда 1 переполнена." : "Team 1 is full.");
			doRegister(1, player);
			return null;
		}
		
		if(player.getLevel() < _team2min || player.getLevel() > _team2max)
			return(player.isLangRus() ? "Вы не подходите по уровню." : "You do not match the level.");
		
		if(_team2list.size() >= _team2count)
			return(player.isLangRus() ? "Команда 2 переполнена." : "Team 2 is full.");
		doRegister(2, player);
		return null;
	}

	private void doRegister(int team, Player player)
	{
		removeItem(player, ITEM_ID, _price);

		if(team == 1)
		{
			_team1list.add(player.getStoredId());
			player.setTeam(TeamType.BLUE);
			
			String[] param = 
			{
				String.valueOf(player.getName()) // {0}
			};
			sayToAll("scripts.events.TvTArena.TvTTemplate.RegisteredForTeam1", param);
		}
		else
		{
			_team2list.add(player.getStoredId());
			player.setTeam(TeamType.RED);

			String[] param = 
			{
				String.valueOf(player.getName()) // {0}
			};
			sayToAll("scripts.events.TvTArena.TvTTemplate.RegisteredForTeam2", param);
		}

		if(_team1list.size() >= _team1count && _team2list.size() >= _team2count)
		{
			sayToAll("scripts.events.TvTArena.TvTTemplate.TeamsReady1min", null);
			_timeToStart = 1;
		}
	}

	public void template_announce()
	{
		Player creator = GameObjectsStorage.getAsPlayer(_creatorId);

		if(_status != 1 || creator == null)
			return;

		if(_timeToStart > 1)
		{
			_timeToStart--;
			
			String[] param = 
			{
				String.valueOf(creator.getName()),	// {0}
				String.valueOf(_team1count),		// {1}
				String.valueOf(_team2count),		// {2}
				String.valueOf(_team1min),			// {3}
				String.valueOf(_team1max),			// {4}
				String.valueOf(_team2min),			// {5}
				String.valueOf(_team2max),			// {6}
				String.valueOf(_price),				// {7}
				String.valueOf(ITEM_NAME),			// {8}
				String.valueOf(_timeToStart),		// {9}
			};
			sayToAll("scripts.events.TvTArena.TvTTemplate.announce2", param);
			executeTask("events.TvTArena." + _className, "announce", new Object[0], 60000);
		}
		else if(_team2list.size() > 0)
		{
			sayToAll("scripts.events.TvTArena.TvTTemplate.PrepareToFight", null);
			executeTask("events.TvTArena." + _className, "prepare", new Object[0], 5000);
		}
		else
		{
			sayToAll("scripts.events.TvTArena.TvTTemplate.NoBattleOponents", null);
			_status = 0;
			returnItemToTeams();
			clearTeams();
		}
	}

	public void template_prepare()
	{
		if(_status != 1)
			return;

		_status = 2;
		for(Player player : getPlayers(_team1list))
			if(!player.isDead())
				_team1live.add(player.getStoredId());
		for(Player player : getPlayers(_team2list))
			if(!player.isDead())
				_team2live.add(player.getStoredId());
		if(!checkTeams())
			return;
		saveBackCoords();
		clearArena();
		ressurectPlayers();
		removeBuff();
		healPlayers();
		paralyzeTeams();
		teleportTeamsToArena();
		sayToAll("scripts.events.TvTArena.TvTTemplate.FightStartsIn30sec", null);
		executeTask("events.TvTArena." + _className, "start", new Object[0], 30000);
	}

	public void template_start()
	{
		if(_status != 2)
			return;

		if(!checkTeams())
			return;

		sayToAll("scripts.events.TvTArena.TvTTemplate.Go", null);
		unParalyzeTeams();
		_status = 3;
		executeTask("events.TvTArena." + _className, "timeOut", new Object[0], 180000);
		_timeOutTask = true;
	}

	public void clearArena()
	{
		for(GameObject obj : _zone.getObjects())
			if(obj != null && obj.isPlayable())
				((Playable) obj).teleToLocation(_zone.getSpawn());
	}

	public boolean checkTeams()
	{
		if(_team1live.isEmpty())
		{
			teamHasLost(1);
			return false;
		}
		else if(_team2live.isEmpty())
		{
			teamHasLost(2);
			return false;
		}
		return true;
	}

	public void saveBackCoords()
	{
		for(Player player : getPlayers(_team1list))
			player.setVar("TvTArena_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflectionId(), -1);
		for(Player player : getPlayers(_team2list))
			player.setVar("TvTArena_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflectionId(), -1);
	}

	public void teleportPlayersToSavedCoords()
	{
		for(Player player : getPlayers(_team1list))
			try
		{
				String var = player.getVar("TvTArena_backCoords");
				if(var == null || var.equals(""))
					continue;
				String[] coords = var.split(" ");
				if(coords.length != 4)
					continue;
				player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
				player.unsetVar("TvTArena_backCoords");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		for(Player player : getPlayers(_team2list))
			try
		{
				String var = player.getVar("TvTArena_backCoords");
				if(var == null || var.equals(""))
					continue;
				String[] coords = var.split(" ");
				if(coords.length != 4)
					continue;
				player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
				player.unsetVar("TvTArena_backCoords");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void healPlayers()
	{
		for(Player player : getPlayers(_team1list))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
		for(Player player : getPlayers(_team2list))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}

	public void ressurectPlayers()
	{
		for(Player player : getPlayers(_team1list))
			if(player.isDead())
			{
				player.restoreExp();
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
				player.broadcastPacket(new Revive(player));
			}
		for(Player player : getPlayers(_team2list))
			if(player.isDead())
			{
				player.restoreExp();
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
				player.broadcastPacket(new Revive(player));
			}
	}

	public void removeBuff()
	{
		for(Player player : getPlayers(_team1list))
			if(player != null)
				try
		{
					if(player.isCastingNow())
						player.abortCast(true, true);

					if(!ALLOW_CLAN_SKILL)
						if(player.getClan() != null)
							for(Skill skill : player.getClan().getAllSkills())
								player.removeSkill(skill, false);

					if(!ALLOW_HERO_SKILL)
						if(player.isHero())
							Hero.removeSkills(player);

					if(!ALLOW_BUFFS)
					{
						player.getEffectList().stopAllEffects();

						if(player.getPet() != null)
						{
							Summon summon = player.getPet();
							summon.getEffectList().stopAllEffects();
							if(summon.isPet())
								summon.unSummon();
						}

						if(player.getAgathionId() > 0)
							player.setAgathion(0);
					}

					player.sendPacket(new SkillList(player));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		for(Player player : getPlayers(_team2list))
			if(player != null)
				try
		{
					if(player.isCastingNow())
						player.abortCast(true, true);

					if(!ALLOW_CLAN_SKILL)
						if(player.getClan() != null)
							for(Skill skill : player.getClan().getAllSkills())
								player.removeSkill(skill, false);

					if(!ALLOW_HERO_SKILL)
						if(player.isHero())
							Hero.removeSkills(player);

					if(!ALLOW_BUFFS)
					{
						player.getEffectList().stopAllEffects();

						if(player.getPet() != null)
						{
							Summon summon = player.getPet();
							summon.getEffectList().stopAllEffects();
							if(summon.isPet())
								summon.unSummon();
						}

						if(player.getAgathionId() > 0)
							player.setAgathion(0);
					}

					player.sendPacket(new SkillList(player));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void backBuff()
	{
		for(Player player : getPlayers(_team1list))
		{
			if(player == null)
				continue;
			try
			{
				player.getEffectList().stopAllEffects();

				if(!ALLOW_CLAN_SKILL)
					if(player.getClan() != null)
						for(Skill skill : player.getClan().getAllSkills())
							if(skill.getMinPledgeClass() <= player.getPledgeClass())
								player.addSkill(skill, false);

				if(!ALLOW_HERO_SKILL)
					if(player.isHero())
						Hero.addSkills(player);

				player.sendPacket(new SkillList(player));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		for(Player player : getPlayers(_team2list))
		{
			if(player == null)
				continue;
			try
			{
				player.getEffectList().stopAllEffects();

				if(!ALLOW_CLAN_SKILL)
					if(player.getClan() != null)
						for(Skill skill : player.getClan().getAllSkills())
							if(skill.getMinPledgeClass() <= player.getPledgeClass())
								player.addSkill(skill, false);

				if(!ALLOW_HERO_SKILL)
					if(player.isHero())
						Hero.addSkills(player);

				player.sendPacket(new SkillList(player));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void paralyzeTeams()
	{
		Skill revengeSkill = SkillTable.getInstance().getInfo(Skill.SKILL_RAID_CURSE, 1);
		for(Player player : getPlayers(_team1list))
		{
			player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);
			revengeSkill.getEffects(player, player, false, false);
			if(player.getPet() != null)
				revengeSkill.getEffects(player, player.getPet(), false, false);
		}
		for(Player player : getPlayers(_team2list))
		{
			player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);
			revengeSkill.getEffects(player, player, false, false);
			if(player.getPet() != null)
				revengeSkill.getEffects(player, player.getPet(), false, false);
		}
	}

	public void unParalyzeTeams()
	{
		for(Player player : getPlayers(_team1list))
		{
			player.getEffectList().stopEffect(Skill.SKILL_RAID_CURSE);
			if(player.getPet() != null)
				player.getPet().getEffectList().stopEffect(Skill.SKILL_RAID_CURSE);

			player.leaveParty();
		}
		for(Player player : getPlayers(_team2list))
		{
			player.getEffectList().stopEffect(Skill.SKILL_RAID_CURSE);
			if(player.getPet() != null)
				player.getPet().getEffectList().stopEffect(Skill.SKILL_RAID_CURSE);

			player.leaveParty();
		}
	}

	public void teleportTeamsToArena()
	{
		Integer n = 0;
		for(Player player : getPlayers(_team1live))
		{
			unRide(player);
			unSummonPet(player, true);
			player.teleToLocation(_team1points.get(n), ReflectionManager.DEFAULT);
			n++;
		}
		n = 0;
		for(Player player : getPlayers(_team2live))
		{
			unRide(player);
			unSummonPet(player, true);
			player.teleToLocation(_team2points.get(n), ReflectionManager.DEFAULT);
			n++;
		}
	}

	public boolean playerHasLost(Player player)
	{
		if(player.getTeam() == TeamType.BLUE)
			_team1live.remove(player.getStoredId());
		else
			_team2live.remove(player.getStoredId());
		Skill revengeSkill = SkillTable.getInstance().getInfo(Skill.SKILL_RAID_CURSE, 1);
		revengeSkill.getEffects(player, player, false, false);
		return !checkTeams();
	}

	public void teamHasLost(Integer team_id)
	{
		if(team_id == 1)
		{
			sayToAll("scripts.events.TvTArena.TvTTemplate.Team2Won", null);
			payItemToTeam(2);
		}
		else
		{
			sayToAll("scripts.events.TvTArena.TvTTemplate.Team1Won", null);
			payItemToTeam(1);
		}
		unParalyzeTeams();
		backBuff();
		teleportPlayersToSavedCoords();
		ressurectPlayers();
		healPlayers();
		clearTeams();
		_status = 0;
		_timeOutTask = false;
	}

	public void template_timeOut()
	{
		if(_timeOutTask && _status == 3)
		{
			sayToAll("scripts.events.TvTArena.TvTTemplate.TimeEnded", null);
			returnItemToTeams();
			unParalyzeTeams();
			backBuff();
			teleportPlayersToSavedCoords();
			ressurectPlayers();
			healPlayers();
			clearTeams();
			_status = 0;
			_timeOutTask = false;
		}
	}

	public void payItemToTeam(Integer team_id)
	{
		if(team_id == 1)
			for(Player player : getPlayers(_team1list))
				addItem(player, ITEM_ID, _price + _team2list.size() * _price / _team1list.size());
		else
			for(Player player : getPlayers(_team2list))
				addItem(player, ITEM_ID, _price + _team2list.size() * _price / _team1list.size());
	}

	public void returnItemToTeams()
	{
		for(Player player : getPlayers(_team1list))
			addItem(player, ITEM_ID, _price);
		for(Player player : getPlayers(_team2list))
			addItem(player, ITEM_ID, _price);
	}

	public void clearTeams()
	{
		for(Player player : getPlayers(_team1list))
			player.setTeam(TeamType.NONE);
		for(Player player : getPlayers(_team2list))
			player.setTeam(TeamType.NONE);
		_team1list.clear();
		_team2list.clear();
		_team1live.clear();
		_team2live.clear();
	}

	public void onDeath(Creature self, Creature killer)
	{
		if(_status >= 2 && self.isPlayer() && self.getTeam() != TeamType.NONE && (_team1list.contains(self.getStoredId()) || _team2list.contains(self.getStoredId())))
		{
			Player player = self.getPlayer();
			Player kplayer = killer.getPlayer();
			if(kplayer != null)
			{
				String[] param = 
				{
					String.valueOf(kplayer.getName()),	// {0}
					String.valueOf(player.getName())	// {1}
				};
				sayToAll("scripts.events.TvTArena.TvTTemplate.killed", param);
				
				if(player.getTeam() == kplayer.getTeam() || !_team1list.contains(kplayer.getStoredId()) && !_team2list.contains(kplayer.getStoredId()))
				{
					String[] param1 = 
					{
						String.valueOf(kplayer.getName()),	// {0}
						String.valueOf(_price),				// {2}
						String.valueOf(ITEM_NAME)			// {3}
					};
					sayToAll("scripts.events.TvTArena.TvTTemplate.RulesViolation", param1);
					removeItem(kplayer, ITEM_ID, _price);
				}
				playerHasLost(player);
			}
			else
			{
				String[] param = 
				{
					String.valueOf(player.getName())	// {0}
				};
				sayToAll("scripts.events.TvTArena.TvTTemplate.isDead", param);
				playerHasLost(player);
			}
		}
	}

	public void onPlayerExit(Player player)
	{
		if(player != null && _status > 0 && player.getTeam() != TeamType.NONE && (_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId())))
			switch(_status)
			{
				case 1:
					removePlayer(player);
					String[] param = 
					{
						String.valueOf(player.getName())	// {0}
					};
					sayToAll("scripts.events.TvTArena.TvTTemplate.disqualified", param);
					
					if(player.getStoredId() == _creatorId)
					{
						sayToAll("scripts.events.TvTArena.TvTTemplate.interrupted", null);
						
						returnItemToTeams();
						backBuff();
						teleportPlayersToSavedCoords();
						unParalyzeTeams();
						ressurectPlayers();
						healPlayers();
						clearTeams();
						
						unParalyzeTeams();
						clearTeams();
						_status = 0;
						_timeOutTask = false;
					}
					break;
				case 2:
					removePlayer(player);
					String[] param1 = 
					{
						String.valueOf(player.getName())	// {0}
					};
					sayToAll("scripts.events.TvTArena.TvTTemplate.disqualified", param1);
					checkTeams();
					break;
				case 3:
					removePlayer(player);
					String[] param2 = 
					{
						String.valueOf(player.getName())	// {0}
					};
					sayToAll("scripts.events.TvTArena.TvTTemplate.disqualified", param2);
					checkTeams();
					break;
			}
	}

	public void onTeleport(Player player)
	{
		if(player != null && _status > 1 && player.getTeam() != TeamType.NONE && player.isInZone(_zone))
			onPlayerExit(player);
	}

	public class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			Player player = cha.getPlayer();
			if(_status >= 2 && player != null && !(_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId())))
				ThreadPoolManager.getInstance().schedule(new TeleportTask(cha, _zone.getSpawn()), 3000);
		}

		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			Player player = cha.getPlayer();
			if(_status >= 2 && player != null && player.getTeam() != TeamType.NONE && (_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId())))
			{
				double angle = Location.convertHeadingToDegree(cha.getHeading()); // угол в градусах
				double radian = Math.toRadians(angle - 90); // угол в радианах
				int x = (int) (cha.getX() + 50 * Math.sin(radian));
				int y = (int) (cha.getY() - 50 * Math.cos(radian));
				int z = cha.getZ();
				ThreadPoolManager.getInstance().schedule(new TeleportTask(cha, new Location(x, y, z)), 3000);
			}
		}
	}

	public class TeleportTask extends RunnableImpl
	{
		Location loc;
		Creature target;

		public TeleportTask(Creature target, Location loc)
		{
			this.target = target;
			this.loc = loc;
			target.block();
		}

		@Override
		public void runImpl() throws Exception
		{
			target.unblock();
			target.teleToLocation(loc);
		}
	}

	private void removePlayer(Player player)
	{
		if(player != null)
		{
			_team1list.remove(player.getStoredId());
			_team2list.remove(player.getStoredId());
			_team1live.remove(player.getStoredId());
			_team2live.remove(player.getStoredId());
			player.setTeam(TeamType.NONE);
		}
	}

	private List<Player> getPlayers(List<Long> list)
	{
		List<Player> result = new ArrayList<Player>();
		for(Long storeId : list)
		{
			Player player = GameObjectsStorage.getAsPlayer(storeId);
			if(player != null)
				result.add(player);
		}
		return result;
	}

	public void sayToAll(String address, String[] replacements)
	{
		Announcements.getInstance().announceByCustomMessage(address, replacements, ChatType.CRITICAL_ANNOUNCE);
	}
	
	public void sayToAll(String text)
	{
		Announcements.getInstance().announceToAll(_manager.getName() + ": " + text, ChatType.CRITICAL_ANNOUNCE);
	}
}