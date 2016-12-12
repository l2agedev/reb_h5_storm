package l2r.gameserver.nexus_engine.events.engine.main.globalevent.raidevent.bosses;

import l2r.gameserver.nexus_engine.events.engine.base.EventSpawn;
import l2r.gameserver.nexus_engine.events.engine.base.Loc;
import l2r.gameserver.nexus_engine.events.engine.main.globalevent.raidevent.RaidbossEvent;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.NpcData;
import l2r.gameserver.nexus_interface.delegate.NpcTemplateData;


/**
 * @author hNoke
 *
 */
public class MalogBoss extends BossTemplate
{
	public MalogBoss()
	{
		super();
	}
	
	@Override
	public NpcData doSpawn()
	{
		NpcData bossNpc;
		
		NpcTemplateData template = new NpcTemplateData(getBossId());
		bossNpc = template.doSpawn(getBossSpawn().getLoc().getX(), getBossSpawn().getLoc().getY(), getBossSpawn().getLoc().getZ(), 1, 0);
		return bossNpc;
	}

	@Override
	public int getBossId()
	{
		return 95770;
	}

	@Override
	public int getChance()
	{
		return 100;
	}

	@Override
	public String getName()
	{
		return "Malog";
	}

	@Override
	public EventSpawn getBossSpawn()
	{
		return new EventSpawn(1, 1, new Loc(-83027, 150871, -3132), 1, "Boss");
	}

	@Override
	public EventSpawn getPlayersSpawn()
	{
		//can have more than one spawn
		return new EventSpawn(1, 1, new Loc(-80651, 149981, -3044), 1, "Regular");
	}
	
	@Override
	public void monsterDied(RaidbossEvent event, NpcData npc)
	{
		super.monsterDied(event, npc);

		if(getBossId() == npc.getNpcId())
		{
			event.bossDied();
		}
		
		//TODO special actions after boss dies?
	}

	@Override
	public void rewardPlayer(PlayerEventInfo player)
	{
		// TODO REWARDS
		player.addItem(57, 1, true);
	}

	@Override
	public void clockTick()
	{
		//TODO special actions for the boss
		
		super.clockTick();
	}
}
