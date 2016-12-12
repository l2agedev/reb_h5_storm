package l2r.gameserver.ai;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.PlayableAI.nextAction;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.Die;
import l2r.gameserver.utils.Location;

import java.util.List;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class CharacterAI extends AbstractAI
{
	private TIntSet _blockedTimers = new TIntHashSet();
	
	public CharacterAI(Creature actor)
	{
		super(actor);
	}

	@Override
	protected void onIntentionIdle()
	{
		clientStopMoving();
		changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
	}

	@Override
	protected void onIntentionActive()
	{
		clientStopMoving();
		changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		onEvtThink();
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
		setAttackTarget(target);
		clientStopMoving();
		changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
		onEvtThink();
	}

	@Override
	protected void onIntentionCast(Skill skill, Creature target)
	{
		setCastTarget(target);
		changeIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		onEvtThink();
	}

	@Override
	protected void onIntentionFollow(Creature target, Integer offset)
	{
		changeIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, offset);
		onEvtThink();
	}

	@Override
	protected void onIntentionInteract(GameObject object)
	{}

	@Override
	protected void onIntentionPickUp(GameObject item)
	{}

	@Override
	protected void onIntentionRest()
	{}

	@Override
	protected void onIntentionCoupleAction(Player player, Integer socialId)
	{}

	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_pos)
	{
		Creature actor = getActor();
		if(actor.isPlayer())
		{
			// Приводит к застреванию в стенах:
			//if(actor.isInRange(blocked_at_pos, 1000))
			//	actor.setLoc(blocked_at_pos, true);
			// Этот способ надежнее:
			Location loc = ((Player) actor).getLastServerPosition();
			if(loc != null)
				actor.setLoc(loc, true);
			actor.stopMove();
		}
		onEvtThink();
	}

	@Override
	protected void onEvtForgetObject(GameObject object)
	{
		if(object == null)
			return;

		Creature actor = getActor();

		if(actor.isAttackingNow() && getAttackTarget() == object)
			actor.abortAttack(true, true);

		if(actor.isCastingNow() && getCastTarget() == object)
			actor.abortCast(true, true);

		if(getAttackTarget() == object)
			setAttackTarget(null);

		if (getCastTarget() == object)
			setCastTarget(null);
		
		if(actor.getTargetId() == object.getObjectId())
			actor.setTarget(null);

		if(actor.getFollowTarget() == object)
			actor.setFollowTarget(null);

		if(actor.getPet() != null)
			actor.getPet().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		Creature actor = getActor();

		actor.abortAttack(true, true);
		actor.abortCast(true, true);
		actor.stopMove();
		actor.broadcastPacket(new Die(actor));

		setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}

	@Override
	protected void onEvtFakeDeath()
	{
		clientStopMoving();
		setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{

	}

	@Override
	protected void onEvtClanAttacked(Creature attacked_member, Creature attacker, int damage)
	{}

	public void Attack(GameObject target, boolean forceUse, boolean dontMove)
	{
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
	}

	public void Cast(Skill skill, Creature target)
	{
		Cast(skill, target, false, false);
	}

	public void Cast(Skill skill, Creature target, boolean forceUse, boolean dontMove)
	{
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
	}

	@Override
	protected void onEvtThink()
	{}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{}

	@Override
	protected void onEvtFinishCasting()
	{}

	@Override
	protected void onEvtReadyToAct()
	{}

	@Override
	protected void onEvtArrived()
	{}

	@Override
	protected void onEvtArrivedTarget()
	{}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{}

	@Override
	protected void onEvtSpawn()
	{}

	@Override
	public void onEvtDeSpawn()
	{}
	
	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{}
	
	public void stopAITask()
	{}

	public void startAITask()
	{}

	public void setNextAction(nextAction action, Object arg0, Object arg1, boolean arg2, boolean arg3)
	{}

	public void clearNextAction()
	{}

	public boolean isActive()
	{
		return true;
	}
	
	public void addTimer(int timerId, long delay)
	{
		addTimer(timerId, null, null, delay);
	}

	public void addTimer(int timerId, Object arg1, long delay)
	{
		addTimer(timerId, arg1, null, delay);
	}

	public void addTimer(int timerId, Object arg1, Object arg2, long delay)
	{
		ThreadPoolManager.getInstance().schedule(new Timer(timerId, arg1, arg2), delay);
	}
	
	public void blockTimer(int timerId)
	{
		_blockedTimers.add(timerId);
	}
	
	public void unblockTimer(int timerId)
	{
		_blockedTimers.remove(timerId);
	}
	
	protected class Timer extends RunnableImpl
	{
		private int _timerId;
		private Object _arg1;
		private Object _arg2;

		public Timer(int timerId, Object arg1, Object arg2)
		{
			_timerId = timerId;
			_arg1 = arg1;
			_arg2 = arg2;
		}

		@Override
		public void runImpl()
		{
			if (_blockedTimers.contains(_timerId))
				return;
			
			notifyEvent(CtrlEvent.EVT_TIMER, _timerId, _arg1, _arg2);
		}
	}
	
	protected void onEvtScriptEvent(String event, Object arg1, Object arg2)
	{
		Creature actor = getActor();
		if (actor == null)
			return;
		
		actor.onEvtScriptEvent(event, arg1, arg2);
	}
	
	public void broadCastScriptEvent(String event, int radius)
	{
		broadCastScriptEvent(event, null, null, radius);
	}
	
	public void broadCastScriptEvent(String event, Object arg1, int radius)
	{
		broadCastScriptEvent(event, arg1, null, radius);
	}
	
	public void broadCastScriptEvent(String event, Object arg1, Object arg2, int radius)
	{
		List<NpcInstance> npcs = World.getAroundNpc(getActor(), radius, radius);
		for (NpcInstance npc : npcs)
			npc.getAI().notifyEvent(CtrlEvent.EVT_SCRIPT_EVENT, event, arg1, arg2);
	}
	
	@Override
	public boolean isCharacterAI()
	{
		return true;
	}
}