package ai;

import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.SpawnTable;
import l2r.gameserver.utils.Location;

/**
 * Босс 26й камалоки
 *
 * @author pchayka
 */
public class OiAriosh extends Fighter
{
	private static final int _followerId = 18556;  // Follower of Ariosh
	private NpcInstance actor = getActor();
	private long _spawnTimer = 0L;
	private final static long _spawnInterval = 60000L;
	NpcInstance follower = null;

	public OiAriosh(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void thinkAttack()
	{
		if((follower == null || follower.isDead()) && _spawnTimer + _spawnInterval < System.currentTimeMillis())
		{
			follower = SpawnTable.spawnSingle(_followerId, Location.findPointToStay(actor, 100, 120).setR(actor.getReflection()));
			follower.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), 1000);
			_spawnTimer = System.currentTimeMillis();
			Functions.npcSay(actor, NpcString.WHAT_ARE_YOU_DOING_HURRY_UP_AND_HELP_ME);
		}
		super.thinkAttack();
	}
}