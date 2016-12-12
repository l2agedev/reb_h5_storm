package l2r.gameserver.nexus_engine.events.engine.main.events;

import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.base.RewardPosition;
import l2r.gameserver.nexus_engine.events.engine.main.MainEventManager;

/**
 * @author hNoke
 *
 */
public class TreasureHuntPvp extends TreasureHunt
{
	public TreasureHuntPvp(EventType type, MainEventManager manager)
	{
		super(type, manager);
		
		setRewardTypes(new RewardPosition[]{ RewardPosition.Looser, RewardPosition.Tie, RewardPosition.Numbered, RewardPosition.Range, RewardPosition.FirstBlood, RewardPosition.FirstRegistered, RewardPosition.OnKill });
		
		_allowPvp = true;
	}
}
