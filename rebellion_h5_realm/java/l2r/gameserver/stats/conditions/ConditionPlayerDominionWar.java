package l2r.gameserver.stats.conditions;

import l2r.gameserver.stats.Env;
import l2r.gameserver.model.entity.events.impl.DominionSiegeEvent;

/**
 * @author VISTALL
 * @date 7:55/03.10.2011
 */
public class ConditionPlayerDominionWar extends Condition
{
	private int _id;

	public ConditionPlayerDominionWar(int id)
	{
		_id = id;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		DominionSiegeEvent dominionSiegeEvent = env.character.getEvent(DominionSiegeEvent.class);

		return dominionSiegeEvent != null && dominionSiegeEvent.getId() == _id;
	}
}
