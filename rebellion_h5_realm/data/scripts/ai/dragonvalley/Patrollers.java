package ai.dragonvalley;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Location;

import org.apache.commons.lang3.ArrayUtils;

public class Patrollers extends Fighter
{
	protected Location[] _points;
	private int[] _teleporters =
	{
		22857,
		22833,
		22834
	};
	
	private int _lastPoint = 0;
	private boolean _firstThought = true;
	private volatile boolean _moving = false;
	
	public Patrollers(NpcInstance actor)
	{
		super(actor);
		MAX_PURSUE_RANGE = Integer.MAX_VALUE - 10;
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
	
	@Override
	protected void thinkAttack()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return;

		if(doTask() && !actor.isAttackingNow() && !actor.isCastingNow())
		{
			if(!createNewTask())
			{
				if(System.currentTimeMillis() > getAttackTimeout())
					returnHome();
			}
		}
	}

	@Override
	protected boolean thinkActive()
	{
		if (super.thinkActive())
			return true;
		
		if (!getActor().isMoving())
			startMoveTask();
		
		return true;
	}
	
	private void startMoveTask()
	{
		if (_moving)
			return;
		
		try
		{
			_moving = true;
			NpcInstance npc = getActor();
			if (_firstThought)
			{
				_lastPoint = getIndex(Location.findNearest(npc, _points));
				_firstThought = false;
			}
			else
				_lastPoint++;
			if (_lastPoint >= _points.length)
			{
				_lastPoint = 0;
				if (ArrayUtils.contains(_teleporters, npc.getNpcId()))
					npc.teleToLocation(_points[_lastPoint]);
			}
			npc.setRunning();
			if (Rnd.chance(30))
				npc.altOnMagicUseTimer(npc, SkillTable.getInstance().getInfo(6757, 1));
			addTaskMove(_points[_lastPoint].findPointToStay(450), true);
			if (npc.isMonster())
			{
				MonsterInstance _monster = (MonsterInstance) npc;
				if (_monster.getMinionList() != null && _monster.getMinionList().hasMinions())
					for (NpcInstance _npc : _monster.getMinionList().getAliveMinions())
					{
						_npc.setRunning();
						((Fighter) _npc.getAI()).addTaskMove(_points[_lastPoint].findPointToStay(250), true);
					}
			}
			doTask();
		}
		finally
		{
			_moving = false;
		}
		
	}
	
	private int getIndex(Location loc)
	{
		for (int i = 0; i < _points.length; i++)
			if (_points[i] == loc)
				return i;
		return 0;
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
	
	@Override
	protected boolean maybeMoveToHome()
	{
		return false;
	}
	
	@Override
	protected void teleportHome()
	{
	}
	
	@Override
	protected void returnHome(boolean clearAggro, boolean teleport)
	{
		super.returnHome(clearAggro, teleport);
		clearTasks();
		_firstThought = true;
		startMoveTask();
	}
}
