package l2r.gameserver.ai;

import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Summon;

public class SummonAI extends PlayableAI
{
	private CtrlIntention _storedIntention = null;
	private Object _storedIntentionArg0 = null;
	private Object _storedIntentionArg1 = null;
	private boolean _storedForceUse = false;
	
	public SummonAI(Summon actor)
	{
		super(actor);
	}

	public void storeIntention()
	{
		if (_storedIntention == null)
		{
			_storedIntention = getIntention();
			_storedIntentionArg0 = _intention_arg0;
			_storedIntentionArg1 = _intention_arg1;
			_storedForceUse = _forceUse;
		}
	}

	public boolean restoreIntention()
	{
		final CtrlIntention intention = _storedIntention;
		final Object arg0 = _storedIntentionArg0;
		final Object arg1 = _storedIntentionArg1;
		if (intention != null)
		{
			_forceUse = _storedForceUse;
			setIntention(intention, arg0, arg1);
			clearStoredIntention();

			onEvtThink();
			return true;
		}
		return false;
	}

	public void clearStoredIntention()
	{
		_storedIntention = null;
		_storedIntentionArg0 = null;
		_storedIntentionArg1 = null;
	}

	@Override
	protected void onIntentionIdle()
	{
		clearStoredIntention();
		super.onIntentionIdle();
	}

	@Override
	protected void onEvtFinishCasting()
	{
		if (!restoreIntention())
			super.onEvtFinishCasting();
	}
	
	@Override
	protected boolean thinkActive()
	{
		Summon actor = getActor();

		Player owner = actor.getPlayer();
		
		clearNextAction();
		if(actor.isDepressed())
		{
			setAttackTarget(actor.getPlayer());
			changeIntention(CtrlIntention.AI_INTENTION_ATTACK, actor.getPlayer(), null);
			thinkAttack(true);
		}
		else if (owner != null && owner.isConnected() && actor.getDistance(owner) > 4000 && !owner.isAlikeDead())
		{
			actor.teleportToOwner();
			return super.thinkActive();
		}
		else if(owner == null || owner.isAlikeDead() || actor.getDistance(owner) > 4000 || !owner.isConnected())
		{
			super.onIntentionActive();
			return super.thinkActive();
		}
		else if(actor.isFollowMode() && !actor.isAfraid())
		{
			changeIntention(CtrlIntention.AI_INTENTION_FOLLOW, actor.getPlayer(), Config.FOLLOW_RANGE);
			thinkFollow();
		}

		return super.thinkActive();
	}

	@Override
	protected void thinkAttack(boolean checkRange)
	{
		Summon actor = getActor();

		if(actor.isDepressed())
			setAttackTarget(actor.getPlayer());

		super.thinkAttack(checkRange);
	}
	
	@Override
	protected void thinkCast(boolean checkRange)
	{
		Summon actor = getActor();
		
		if(actor.isDepressed()) //TODO: Infern0 todo find some way to get check's for thinkcast...
			setAttackTarget(actor.getPlayer());
			
		super.thinkCast(checkRange);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		Summon actor = getActor();
		if(attacker != null && actor.getPlayer().isDead() && !actor.isDepressed())
			Attack(attacker, false, false);
		
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	public void Cast(Skill skill, Creature target, boolean forceUse, boolean dontMove)
	{
		storeIntention();
		super.Cast(skill, target, forceUse, dontMove);
	}
	
	@Override
	public Summon getActor()
	{
		return (Summon) super.getActor();
	}
	
	@Override
	public boolean isSummonAI()
	{
		return true;
	}
}