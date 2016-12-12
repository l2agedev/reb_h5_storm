package ai.primeval_isle;

import l2r.gameserver.ai.DefaultAI;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SkillTable;

public class SprigantStun extends DefaultAI
{
	private final Skill SKILL = SkillTable.getInstance().getInfo(5085, 1);
	private long _waitTime;

	private static final int TICK_IN_MILISECONDS = 15000;

	public SprigantStun(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		if(System.currentTimeMillis() > _waitTime)
		{
			NpcInstance actor = getActor();

			actor.doCast(SKILL, actor, false);

			_waitTime = System.currentTimeMillis() + TICK_IN_MILISECONDS;
			return true;
		}

		return false;
	}
}
