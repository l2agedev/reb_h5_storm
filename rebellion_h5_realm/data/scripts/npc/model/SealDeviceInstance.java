package npc.model;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.ExStartScenePlayer;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;

/**
 * @author pchayka
 */
public final class SealDeviceInstance extends MonsterInstance
{
	private boolean _gaveItem = false;

	public SealDeviceInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void reduceCurrentHp(double i, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(this.getCurrentHp() < i)
		{
			if(!_gaveItem && ItemFunctions.getItemCount(attacker.getPlayer(), 13846) < 4)
			{
				this.setRHandId(15281);
				this.broadcastCharInfo();
				ItemFunctions.addItem(attacker.getPlayer(), 13846, 1, true);
				_gaveItem = true;

				if(ItemFunctions.getItemCount(attacker.getPlayer(), 13846) >= 4)
				{
					attacker.getPlayer().showQuestMovie(ExStartScenePlayer.SCENE_SSQ_SEALING_EMPEROR_2ND);
					ThreadPoolManager.getInstance().schedule(new TeleportPlayer(attacker.getPlayer()), 26500L);
				}
			}
			i = this.getCurrentHp() - 1;
		}
		attacker.reduceCurrentHp(450, this, null, true, false, true, false, false, false, true);
		super.reduceCurrentHp(i, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}

	private class TeleportPlayer extends RunnableImpl
	{
		Player _p;

		public TeleportPlayer(Player p)
		{
			_p = p;
		}

		@Override
		public void runImpl() throws Exception
		{
			for(NpcInstance n : _p.getReflection().getNpcs())
				if(n.getNpcId() != 32586 && n.getNpcId() != 32587)
					n.deleteMe();
			_p.getPlayer().teleToLocation(new Location(-89560, 215784, -7488));
		}
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public boolean isMovementDisabled()
	{
		return true;
	}
}