package l2r.gameserver.model.instances;

import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.MyTargetSelected;
import l2r.gameserver.network.serverpackets.ValidateLocation;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.templates.npc.NpcTemplate;

public class EventMapGuardInstance extends GuardInstance
{
	public EventMapGuardInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return attacker.isMonster() && ((MonsterInstance)attacker).isAggressive() || attacker.isPlayable() && attacker.getKarma() > 0;
	}
	
	@Override
	public boolean isInvul()
	{
		return true;
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
	public void onAction(Player player, boolean shift)
	{
		if(this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));

			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));

			if(isAutoAttackable(player))
			{
				player.getAI().Attack(this, false, shift);
				return;
			}

			if(!isInRange(player, INTERACTION_DISTANCE))
			{
				if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
				return;
			}
			else
			{
				player.sendMessage(new CustomMessage("l2r.gameserver.model.instances.eventmapguardinstance.message1", player));
				player.sendPacket(ActionFail.STATIC);
			}
		}

		player.sendPacket(ActionFail.STATIC);
	}
}