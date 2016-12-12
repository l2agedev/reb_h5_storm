package ai;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Location;

/**
 * AI боса Core:
 * <br> - Бубнит при атаке и смерти.
 * <br> - При смерти играет музыку и спаунит обратные порталы, которые удаляются через 15 минут
 *
 * @author SYS
 */
public class Core extends Fighter
{
	private boolean _firstTimeAttacked = true;
	private static final int TELEPORTATION_CUBIC_ID = 31842;
	private static final Location CUBIC_1_POSITION = new Location(16502, 110165, -6394, 0);
	private static final Location CUBIC_2_POSITION = new Location(18948, 110165, -6394, 0);
	private static final int CUBIC_DESPAWN_TIME = 15 * 60 * 1000; // 15 min
	private static final int CORE = 29006;
	
	public Core(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(_firstTimeAttacked)
		{
			Functions.npcSay(actor, NpcString.A_NONPERMITTED_TARGET_HAS_BEEN_DISCOVERED);
			Functions.npcSay(actor, NpcString.INTRUDER_REMOVAL_SYSTEM_INITIATED);
			_firstTimeAttacked = false;
		}
		else if(Rnd.chance(1))
			Functions.npcSay(actor, NpcString.REMOVING_INTRUDERS);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		ServerVariables.set("CoreDeath", System.currentTimeMillis());
		
		NpcInstance actor = getActor();

		actor.broadcastPacket(new PlaySound(PlaySound.Type.MUSIC, "BS02_D", 1, 0, actor.getLoc()));
		Functions.npcSay(actor, NpcString.A_FATAL_ERROR_HAS_OCCURRED);
		Functions.npcSay(actor, NpcString.SYSTEM_IS_BEING_SHUT_DOWN);
		Functions.npcSay(actor, NpcString.CORE_);

		try
		{
			NpcInstance cubic1 = NpcHolder.getInstance().getTemplate(TELEPORTATION_CUBIC_ID).getNewInstance();
			cubic1.setReflection(actor.getReflection());
			cubic1.setCurrentHpMp(cubic1.getMaxHp(), cubic1.getMaxMp(), true);
			cubic1.spawnMe(CUBIC_1_POSITION);

			NpcInstance cubic2 = NpcHolder.getInstance().getTemplate(TELEPORTATION_CUBIC_ID).getNewInstance();
			cubic2.setReflection(actor.getReflection());
			cubic2.setCurrentHpMp(cubic1.getMaxHp(), cubic1.getMaxMp(), true);
			cubic2.spawnMe(CUBIC_2_POSITION);

			ThreadPoolManager.getInstance().schedule(new DeSpawnScheduleTimerTask(cubic1, cubic2), CUBIC_DESPAWN_TIME);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		if (Config.ENABLE_PLAYER_COUNTERS && getActor().getNpcId() == CORE)
		{
			if (killer != null && killer.getPlayer() != null)
			{
				for (Player member : killer.getPlayer().getPlayerGroup())
				{
					if (member != null && Location.checkIfInRange(3000, member, killer, false))
						member.getCounters().addPoint("_Core_Killed");
				}
			}
		}
		
		_firstTimeAttacked = true;
		super.onEvtDead(killer);
	}

	class DeSpawnScheduleTimerTask extends RunnableImpl
	{
		final NpcInstance cubic1;
		final NpcInstance cubic2;

		public DeSpawnScheduleTimerTask(NpcInstance cubic1, NpcInstance cubic2)
		{
			this.cubic1 = cubic1;
			this.cubic2 = cubic2;
		}

		@Override
		public void runImpl()
		{
			cubic1.deleteMe();
			cubic2.deleteMe();
		}
	}
}