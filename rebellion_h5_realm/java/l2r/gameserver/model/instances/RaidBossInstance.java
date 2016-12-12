package l2r.gameserver.model.instances;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.instancemanager.RaidBossSpawnManager;
import l2r.gameserver.model.AggroList.HateInfo;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectTasks;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.PlayerGroup;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.HeroDiary;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Log;

import gov.nasa.worldwind.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.ArrayUtils;

public class RaidBossInstance extends MonsterInstance
{
	private ScheduledFuture<?> minionMaintainTask;

	private static final int MINION_UNSPAWN_INTERVAL = 5000; //time to unspawn minions when boss is dead, msec

	private static String KILLER_NAME = StringUtil.EMPTY;
	
	public RaidBossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isRaid()
	{
		return true;
	}

	protected int getMinionUnspawnInterval()
	{
		return MINION_UNSPAWN_INTERVAL;
	}

	protected int getKilledInterval(MinionInstance minion)
	{
		return 120000; //2 minutes to respawn
	}

	@Override
	public void notifyMinionDied(MinionInstance minion)
	{
		minionMaintainTask = ThreadPoolManager.getInstance().schedule(new MaintainKilledMinion(minion), getKilledInterval(minion));
		super.notifyMinionDied(minion);
	}

	private class MaintainKilledMinion extends RunnableImpl
	{
		private final MinionInstance minion;

		public MaintainKilledMinion(MinionInstance minion)
		{
			this.minion = minion;
		}

		@Override
		public void runImpl() throws Exception
		{
			if(!isDead())
			{
				minion.refreshID();
				spawnMinion(minion);
			}
		}
	}

	@Override
	protected void onDeath(Creature killer)
	{
		if(minionMaintainTask != null)
		{
			minionMaintainTask.cancel(false);
			minionMaintainTask = null;
		}

		final int points = getTemplate().rewardRp;
		if(points > 0)
			calcRaidPointsReward(points);

		if(this instanceof ReflectionBossInstance)
		{
			super.onDeath(killer);
			return;
		}

		Log.addGame(Log.LOG_BOSS_KILLED, new Object[] { getTypeName(), getName(), getNpcId(), killer, getX(), getY(), getZ(), "-" }, "bosses");
		
		if(killer.isPlayable())
		{
			Player player = killer.getPlayer();
			
			boolean skipRecord = ArrayUtils.contains(Config.HERO_DIARY_EXCLUDED_BOSSES, getNpcId());
			
			KILLER_NAME = player.getName();
			
			if(player.isInParty())
			{
				for(Player member : player.getParty())
				{
					if (Config.ENABLE_PLAYER_COUNTERS && member != null)
						member.getCounters().addPoint("_Raids_Killed");
					
					if(member.isNoble() && !skipRecord)
						Hero.getInstance().addHeroDiary(member.getObjectId(), HeroDiary.ACTION_RAID_KILLED, getNpcId());
				}
				
				player.getParty().sendPacket(Msg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
			}
			else
			{
				if (Config.ENABLE_ACHIEVEMENTS)
					player.getPlayer().getCounters().addPoint("_Raids_Killed");
				
				if(player.isNoble() && !skipRecord)
					Hero.getInstance().addHeroDiary(player.getObjectId(), HeroDiary.ACTION_RAID_KILLED, getNpcId());
				player.sendPacket(Msg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
			}

			Quest q = QuestManager.getQuest(508);
			if(q != null)
			{
				final Clan cl = player.getClan();
				if(cl != null && cl.getLeader().isOnline() && cl.getLeader().getPlayer().getQuestState(q.getName()) != null)
				{
					QuestState st = cl.getLeader().getPlayer().getQuestState(q.getName());
					st.getQuest().onKill(this, st);
				}
			}
		}

		if(getMinionList().hasAliveMinions())
			ThreadPoolManager.getInstance().schedule(new RunnableImpl()
			{
				@Override
				public void runImpl() throws Exception
				{
					if(isDead())
						getMinionList().unspawnMinions();
				}
			}, getMinionUnspawnInterval());

		int boxId = 0;
		switch(getNpcId())
		{
			case 25035: // Shilens Messenger Cabrio
				boxId = 31027;
				break;
			case 25054: // Demon Kernon
				boxId = 31028;
				break;
			case 25126: // Golkonda, the Longhorn General
				boxId = 31029;
				break;
			case 25220: // Death Lord Hallate
				boxId = 31030;
				break;
		}

		if(boxId != 0)
		{
			NpcTemplate boxTemplate = NpcHolder.getInstance().getTemplate(boxId);
			if(boxTemplate != null)
			{
				final NpcInstance box = new NpcInstance(IdFactory.getInstance().getNextId(), boxTemplate);
				box.spawnMe(getLoc());
				box.setSpawnedLoc(getLoc());

				ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(box), 60000);
			}
		}

		super.onDeath(killer);
	}
	
	private void calcRaidPointsReward(int totalPoints)
	{
		// Object groupkey (L2Party/L2CommandChannel/L2Player) | Long GroupDdamage
		Map<PlayerGroup, Long> participants = new HashMap<PlayerGroup, Long>();
		double totalHp = getMaxHp();
		
		// Scatter players to groups. Command Channel → Party → StandAlone. Add damage done for each group, including pets.
		for(HateInfo ai : getAggroList().getPlayableMap().values())
		{
			Player player = ai.attacker.getPlayer();
			Long curDamage = participants.get(player.getPlayerGroup());
			if(curDamage == null)
				curDamage = 0L;

			curDamage += ai.damage;
			participants.put(player.getPlayerGroup(), curDamage);
		}

		for(Entry<PlayerGroup, Long> groupInfo : participants.entrySet())
		{
			PlayerGroup group = groupInfo.getKey();
			Long damage = groupInfo.getValue();
			List<Player> activePlayers = new ArrayList<Player>();
			
			for (Player player : group)
			{
				if(player.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE))
					activePlayers.add(player);
			}
			
			// это та часть, которую игрок заслужил дамагом группы, но на нее может быть наложен штраф от уровня игрока
			final int perPlayer = (int) Math.round(totalPoints * damage / (totalHp * activePlayers.size()));
			for(Player player : activePlayers)
			{
				int playerReward = (int) Math.round(perPlayer * Experience.penaltyModifier(calculateLevelDiffForDrop(player.getLevel()), 9));
				if(playerReward == 0)
					continue;
				
				player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_RAID_POINTS).addNumber(playerReward));
				RaidBossSpawnManager.getInstance().addPoints(player.getObjectId(), getNpcId(), playerReward);
			}
		}

		RaidBossSpawnManager.getInstance().updatePointsDb();
		RaidBossSpawnManager.getInstance().calculateRanking();
	}

	@Override
	protected void onDecay()
	{
		super.onDecay();
		RaidBossSpawnManager.getInstance().onBossDespawned(this, true, KILLER_NAME);
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		addSkill(SkillTable.getInstance().getInfo(4045, 1)); // Resist Full Magic Attack
		RaidBossSpawnManager.getInstance().onBossSpawned(this);
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}