package l2r.gameserver.nexus_engine.events.engine.main.globalevent.raidevent.bosses;

import l2r.gameserver.nexus_engine.events.engine.base.EventSpawn;
import l2r.gameserver.nexus_engine.events.engine.base.Loc;
import l2r.gameserver.nexus_engine.events.engine.main.globalevent.raidevent.RaidbossEvent;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.NpcData;
import l2r.gameserver.nexus_interface.delegate.NpcTemplateData;


/**
 * @author hNoke
 *
 */
public class GenericBoss extends BossTemplate
{
	private final int _bossId;
	private final int _chance;

	private final int _x;
	private final int _y;
	private final int _z;

	private final int _xPlayer;
	private final int _yPlayer;
	private final int _zPlayer;

	private final String _name;

	public GenericBoss(String name, int bossId, int chance, int x, int y, int z, int xPlayer, int yPlayer, int zPlayer)
	{
		super();

		_name = name;
		_bossId = bossId;
		_chance = chance;

		_x = x;
		_y = y;
		_z = z;

		_xPlayer = xPlayer;
		_yPlayer = yPlayer;
		_zPlayer = zPlayer;
	}

	public void addReward(int itemId, int ammount, int chance)
	{
		BossRewardItem item = new BossRewardItem(itemId, ammount, chance);
		_rewards.add(item);
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
		return _bossId;
	}

	@Override
	public int getChance()
	{
		return _chance;
	}

	@Override
	public String getName()
	{
		return _name;
	}

	@Override
	public EventSpawn getBossSpawn()
	{
		return new EventSpawn(1, 1, new Loc(_x, _y, _z), 1, "Boss");
	}

	@Override
	public EventSpawn getPlayersSpawn()
	{
		return new EventSpawn(1, 1, new Loc(_xPlayer, _yPlayer, _zPlayer), 1, "Regular");
	}
	
	@Override
	public void monsterDied(RaidbossEvent event, NpcData npc)
	{
		super.monsterDied(event, npc);

		if(getBossId() == npc.getNpcId())
		{
			event.bossDied();
		}
	}

	@Override
	public void rewardPlayer(PlayerEventInfo player)
	{
		for(BossRewardItem item : _rewards)
		{
			if(CallBack.getInstance().getOut().random(100) < item.chance)
				player.addItem(item.itemId, item.ammount, true);
		}
	}

	@Override
	public void clockTick()
	{
		super.clockTick();
	}
}
