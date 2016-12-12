package ai;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.ExSendUIEvent;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;

import instances.ZakenDay83;

/**
 * Daytime Zaken.
 * - иногда телепортируется в случайную комнату
 */
public class ZakenDaytime83 extends Fighter
{
	private static final Location[] _locations = new Location[] { new Location(55272, 219112, -3496),
			new Location(56296, 218072, -3496), new Location(54232, 218072, -3496), new Location(54248, 220136, -3496),
			new Location(56296, 220136, -3496), new Location(55272, 219112, -3224), new Location(56296, 218072, -3224),
			new Location(54232, 218072, -3224), new Location(54248, 220136, -3224), new Location(56296, 220136, -3224),
			new Location(55272, 219112, -2952), new Location(56296, 218072, -2952), new Location(54232, 218072, -2952),
			new Location(54248, 220136, -2952), new Location(56296, 220136, -2952) };

	private long _teleportSelfTimer = 0L;
	private long _teleportSelfReuse = 120000L; // 120 secs
	private NpcInstance actor = getActor();

	public ZakenDaytime83(NpcInstance actor)
	{
		super(actor);
		MAX_PURSUE_RANGE = Integer.MAX_VALUE / 2;
	}

	@Override
	protected void thinkAttack()
	{
		if(_teleportSelfTimer + _teleportSelfReuse < System.currentTimeMillis())
		{
			_teleportSelfTimer = System.currentTimeMillis();
			if(Rnd.chance(20))
			{
				actor.doCast(SkillTable.getInstance().getInfo(4222, 1), actor, false);
				ThreadPoolManager.getInstance().schedule(new RunnableImpl(){
					@Override
					public void runImpl()
					{
						actor.teleToLocation(_locations[Rnd.get(_locations.length)].setR(actor).findPointToStay(50, 100));
						actor.getAggroList().clear(true);
					}
				}, 500);
			}
		}
		super.thinkAttack();
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		long _timePassed = System.currentTimeMillis() - ZakenDay83._savedTime;
		Reflection r = actor.getReflection();
		r.setReenterTime(System.currentTimeMillis());
		for(Player p : r.getPlayers())
			p.sendPacket(new ExSendUIEvent(p, true, true, 0, 0));
		for(Player p : r.getPlayers())
			if(_timePassed < 5 * 60 * 1000)
			{
				if(Rnd.chance(50))
					ItemFunctions.addItem(p, 15763, 1, true);
			}
			else if(_timePassed < 10 * 60 * 1000)
			{
				if(Rnd.chance(30))
					ItemFunctions.addItem(p, 15764, 1, true);
			}
			else if(_timePassed < 15 * 60 * 1000)
			{
				if(Rnd.chance(25))
					ItemFunctions.addItem(p, 15763, 1, true);
			}
		actor.broadcastPacket(new PlaySound(PlaySound.Type.MUSIC, "BS02_D", 1, actor.getObjectId(), actor.getLoc()));
		super.onEvtDead(killer);
	}

	@Override
	protected void teleportHome()
	{
	}
}