package l2r.gameserver.nexus_engine.events.engine.main.globalevent.raidevent.bosses;

/**
 * Created by hNoke
 */
public class BossRewardItem
{
	public int itemId;
	public int ammount;
	public int chance;

	public BossRewardItem(int itemId, int ammount, int chance)
	{
		this.itemId = itemId;
		this.ammount = ammount;
		this.chance = chance;
	}

}
