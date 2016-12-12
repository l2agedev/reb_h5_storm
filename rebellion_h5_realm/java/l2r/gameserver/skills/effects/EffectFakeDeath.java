package l2r.gameserver.skills.effects;

import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ChangeWaitType;
import l2r.gameserver.network.serverpackets.Revive;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.stats.Env;

public final class EffectFakeDeath extends Effect
{
	public EffectFakeDeath(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		
		Player player = (Player) getEffected();
		player.setFakeDeath(true);
		player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
		player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_START_FAKEDEATH));
		player.broadcastCharInfo();
		player.abortCast(true, false);
		player.abortAttack(true, false);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		// 5 секунд после FakeDeath на персонажа не агрятся мобы
		Player player = (Player) getEffected();
		player.setNonAggroTime(System.currentTimeMillis() + 5000L);
		player.setFakeDeath(false);
		player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_STOP_FAKEDEATH));
		player.broadcastPacket(new Revive(player));
		player.broadcastCharInfo();
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
			return false;

		double manaDam = calc();

		if(manaDam > getEffected().getCurrentMp() && getSkill().isToggle())
		{
			getEffected().sendPacket(SystemMsg.NOT_ENOUGH_MP);
			getEffected().sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
			return false;
		}

		getEffected().reduceCurrentMp(manaDam, null);
		return true;
	}
}