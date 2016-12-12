package ai;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.scripts.Functions;

public class SearchRaidBossAI extends Fighter
{
	private long _lastSay;
	
	private static final String[] _stay =
	{
		"Ha ... Ha ... You came to save the cow?",
		"So I just do not give it to you!",
		"To save your cow, you have to kill me!",
		"Ha ... Ha ... You think it's that simple?"
	};
	
	private static final String[] _attacked =
	{
		"You must all die!",
		"The cow is mine and you will not have a delicious milk!",
		"I'll kill you all!",
		"And it's called heroes?",
		"Do not you see ladybugs!",
		"Only the ancient weapon capable of beating me!"
	};
	
	public SearchRaidBossAI(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if (actor.isDead())
			return true;
		
		// Ругаемся не чаще, чем раз в 10 секунд
		if (!actor.isInCombat() && System.currentTimeMillis() - _lastSay > 10000)
		{
			Functions.npcSay(actor, _stay[Rnd.get(_stay.length)]);
			_lastSay = System.currentTimeMillis();
		}
		return super.thinkActive();
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if (attacker == null || attacker.getPlayer() == null)
			return;
		
		// Ругаемся не чаще, чем раз в 5 секунд
		if (System.currentTimeMillis() - _lastSay > 5000)
		{
			Functions.npcSay(actor, _attacked[Rnd.get(_attacked.length)]);
			_lastSay = System.currentTimeMillis();
		}
		super.onEvtAttacked(attacker, damage);
	}
}
