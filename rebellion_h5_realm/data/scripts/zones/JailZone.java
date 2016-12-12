package zones;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2r.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.entity.events.impl.DuelEvent;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.utils.AdminFunctions;
import l2r.gameserver.utils.TimeUtils;

public class JailZone implements ScriptFile
{
	private static ZoneListener _zoneListener;
	private static EnterJailedListener _enterListener;
	
	private static String[] zones =
	{
		"[jail_zone]",
		"[jail_zone3]",
		"[jail_zone2]"
	};
	
	@Override
	public void onLoad()
	{
		_zoneListener = new ZoneListener();
		_enterListener = new EnterJailedListener();
		
		for (String s : zones)
		{
			Zone zone = ZoneHolder.getZone(s);
			zone.addListener(_zoneListener);
		}
		
		CharListenerList.addGlobal(_enterListener);
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
		CharListenerList.removeGlobal(_enterListener);
	}
	
	public class EnterJailedListener implements OnPlayerEnterListener
	{
		
		@Override
		public void onPlayerEnter(Player player)
		{
			if (player.isInJail())
			{
				ThreadPoolManager.getInstance().schedule(new BackToJail(player), 2000);
			}
		}
	}
	
	public class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			if (zone == null || cha == null)
				return;
			
			if (cha != null && cha.isPlayer())
			{
				Player player = cha.getPlayer();
				DuelEvent duel = cha.getPlayer().getEvent(DuelEvent.class);
				if (duel != null)
					duel.abortDuel(cha.getPlayer());
				
				long period = player.getVarTimeToExpire("jailed");
				if (period == -1)
				{
					if (player.isLangRus())
						player.sendPacket(new Say2(0, ChatType.ALL, "Администрация", "Вы здесь навсегда"));
					else
						player.sendPacket(new Say2(0, ChatType.ALL, "Server", "Sorry but you are here forever :)"));
				}
				else if (period > 0)
				{
					period /= 1000; // to seconds
					period /= 60; // to minutes
					
					if (player.isLangRus())
						player.sendPacket(new Say2(0, ChatType.ALL, "Администрация", "Сидеть осталось " + TimeUtils.minutesToFullString((int) period)));
					else
						player.sendPacket(new Say2(0, ChatType.ALL, "Server", "You are still jailed for " + TimeUtils.minutesToFullString((int) period)));
				}
			}
		}
		
		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			if (zone == null || cha == null)
				return;
			
			if (cha.isPlayer() && cha.getPlayer().isInJail())
			{
				if (cha.getPlayer().canOverrideCond(PcCondOverride.ZONE_CONDITIONS))
					return;
				
				// when a player wants to exit jail even if he is still jailed, teleport him back to jail
				ThreadPoolManager.getInstance().schedule(new BackToJail(cha.getPlayer()), 2000);
				cha.getPlayer().sendMessage(new CustomMessage("scripts.zones.jailzone.message1", cha.getPlayer()));
			}
		}
	}
	
	static class BackToJail implements Runnable
	{
		private final Player _activeChar;
		
		BackToJail(Creature character)
		{
			_activeChar = (Player) character;
		}
		
		@Override
		public void run()
		{
			if (!_activeChar.isInJail())
				return;
			
			if (_activeChar.isTeleporting())
			{
				ThreadPoolManager.getInstance().schedule(this, 1000);
				return;
			}
			
			boolean inJailZone = false;
			for (String zone : zones)
			{
				if (_activeChar.isInZone(zone))
				{
					inJailZone = true;
					break;
				}
			}
			
			if (!inJailZone)
				_activeChar.teleToLocation(AdminFunctions.JAIL_SPAWN.findPointToStay(200)); // Jail
		}
	}
}
