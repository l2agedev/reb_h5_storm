package l2r.gameserver.model.actor.listener;

import l2r.commons.listener.Listener;
import l2r.gameserver.listener.actor.player.OnLevelChangeListener;
import l2r.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2r.gameserver.listener.actor.player.OnPlayerExitListener;
import l2r.gameserver.listener.actor.player.OnPlayerPartyInviteListener;
import l2r.gameserver.listener.actor.player.OnPlayerPartyLeaveListener;
import l2r.gameserver.listener.actor.player.OnPlayerSayListener;
import l2r.gameserver.listener.actor.player.OnServerPacketListener;
import l2r.gameserver.listener.actor.player.OnTeleportListener;
import l2r.gameserver.listener.skills.OnSkillEnchantListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.IStaticPacket;

public class PlayerListenerList extends CharListenerList
{
	public PlayerListenerList(Player actor)
	{
		super(actor);
	}

	@Override
	public Player getActor()
	{
		return (Player) actor;
	}

	public void onEnter()
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerEnterListener.class.isInstance(listener))
					((OnPlayerEnterListener) listener).onPlayerEnter(getActor());

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerEnterListener.class.isInstance(listener))
					((OnPlayerEnterListener) listener).onPlayerEnter(getActor());
	}

	public void onExit()
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerExitListener.class.isInstance(listener))
					((OnPlayerExitListener) listener).onPlayerExit(getActor());

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerExitListener.class.isInstance(listener))
					((OnPlayerExitListener) listener).onPlayerExit(getActor());
	}

	public void onTeleport(int x, int y, int z, Reflection reflection)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnTeleportListener.class.isInstance(listener))
					((OnTeleportListener) listener).onTeleport(getActor(), x, y, z, reflection);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnTeleportListener.class.isInstance(listener))
					((OnTeleportListener) listener).onTeleport(getActor(), x, y, z, reflection);
	}

	public void onPartyInvite()
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerPartyInviteListener.class.isInstance(listener))
					((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor());

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerPartyInviteListener.class.isInstance(listener))
					((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor());
	}

	public void onPartyLeave()
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerPartyLeaveListener.class.isInstance(listener))
					((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor());

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerPartyLeaveListener.class.isInstance(listener))
					((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor());
	}
	
	public boolean onPacketReceived(IStaticPacket packet)
	{
		boolean executePacket = true;
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnServerPacketListener.class.isInstance(listener))
					executePacket = (((OnServerPacketListener) listener).onPacketReceived(getActor(), packet) == false ? false : executePacket);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnServerPacketListener.class.isInstance(listener))
					executePacket = (((OnServerPacketListener) listener).onPacketReceived(getActor(), packet) == false ? false : executePacket);
		
		return executePacket;
	}
	
	public void onLevelChange(int oldLvl, int newLvl)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnLevelChangeListener.class.isInstance(listener))
					((OnLevelChangeListener) listener).onLevelChange(getActor(), oldLvl, newLvl);
		
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnLevelChangeListener.class.isInstance(listener))
					((OnLevelChangeListener) listener).onLevelChange(getActor(), oldLvl, newLvl);
	}
	
	public void onSay(ChatType type, String target, String text)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerSayListener.class.isInstance(listener))
					((OnPlayerSayListener) listener).onSay(getActor(), type, target, text);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerSayListener.class.isInstance(listener))
					((OnPlayerSayListener) listener).onSay(getActor(), type, target, text);
	}
	
	public void onSkillEnchant(Skill skill, boolean success, boolean safeEnchant)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnSkillEnchantListener.class.isInstance(listener))
					((OnSkillEnchantListener) listener).onSkillEnchant(getActor(), skill, success, safeEnchant);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnSkillEnchantListener.class.isInstance(listener))
					((OnSkillEnchantListener) listener).onSkillEnchant(getActor(), skill, success, safeEnchant);
	}
}
