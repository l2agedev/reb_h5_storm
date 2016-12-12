package npc.model;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.BossInstance;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Location;

public class OrfenInstance extends BossInstance
{
	public static final Location nest = new Location(43728, 17220, -4342);

	private static final int ORFEN = 29014;
		
	public static final Location[] locs = new Location[]
	{
		new Location(55024, 17368, -5412),
		new Location(53504, 21248, -5496),
		new Location(53248, 24576, -5272)
	};
	
	public OrfenInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void setTeleported(boolean flag)
	{
		super.setTeleported(flag);
		Location loc = flag ? nest : locs[Rnd.get(locs.length)];
		setSpawnedLoc(loc);
		getAggroList().clear(true);
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		teleToLocation(loc);
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		setTeleported(false);
		broadcastPacketToOthers(new PlaySound(PlaySound.Type.MUSIC, "BS01_A", 1, 0, getLoc()));
	}

	@Override
	protected void onDeath(Creature killer)
	{
		ServerVariables.set("OrfenDeath", System.currentTimeMillis());
		if (Config.ENABLE_PLAYER_COUNTERS && getNpcId() == ORFEN)
		{
			if (killer != null && killer.getPlayer() != null)
			{
				for (Player member : killer.getPlayer().getPlayerGroup())
				{
					if (member != null && Location.checkIfInRange(3000, member, killer, false))
						member.getCounters().addPoint("_Orfen_Killed");
				}
			}
		}
		
		broadcastPacketToOthers(new PlaySound(PlaySound.Type.MUSIC, "BS02_D", 1, 0, getLoc()));
		super.onDeath(killer);
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
		if(!isTeleported() && getCurrentHpPercents() <= 50)
			setTeleported(true);
	}
}