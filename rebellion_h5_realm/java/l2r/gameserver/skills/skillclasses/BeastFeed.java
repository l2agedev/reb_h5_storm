package l2r.gameserver.skills.skillclasses;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.FeedableBeastInstance;
import l2r.gameserver.templates.StatsSet;

import java.util.List;

public class BeastFeed extends Skill
{
	public BeastFeed(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(final Creature activeChar, List<Creature> targets)
	{
		for(final Creature target : targets)
		{
			ThreadPoolManager.getInstance().execute(new RunnableImpl()
			{
				@Override
				public void runImpl() throws Exception
				{
					if(target instanceof FeedableBeastInstance)
						((FeedableBeastInstance) target).onSkillUse((Player) activeChar, _id);
				}
			});
		}
	}
}
