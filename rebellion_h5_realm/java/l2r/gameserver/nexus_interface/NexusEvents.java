package l2r.gameserver.nexus_interface;

import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.nexus_engine.events.EventGame;
import l2r.gameserver.nexus_engine.events.NexusLoader;
import l2r.gameserver.nexus_engine.events.NexusLoader.NexusBranch;
import l2r.gameserver.nexus_engine.events.engine.EventBuffer;
import l2r.gameserver.nexus_engine.events.engine.EventConfig;
import l2r.gameserver.nexus_engine.events.engine.EventManagement;
import l2r.gameserver.nexus_engine.events.engine.EventManager;
import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.main.MainEventManager.State;
import l2r.gameserver.nexus_engine.events.engine.mini.EventMode.FeatureType;
import l2r.gameserver.nexus_engine.events.engine.mini.MiniEventManager;
import l2r.gameserver.nexus_engine.events.engine.mini.features.AbstractFeature;
import l2r.gameserver.nexus_engine.events.engine.mini.features.EnchantFeature;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.callback.HtmlManager;
import l2r.gameserver.nexus_interface.callback.api.DescriptionLoader;
import l2r.gameserver.nexus_interface.delegate.CharacterData;
import l2r.gameserver.nexus_interface.delegate.SkillData;
import l2r.gameserver.templates.item.ItemTemplate;

/**
 * @author hNoke
 *
 */
public class NexusEvents
{
	public static final String _desc = "l2r HighFive";
	public static final NexusBranch _branch = NexusBranch.Hi5;
	public static final double _interfaceVersion = 3.2;
	public static final boolean _allowInstances = true;
	public static final String _libsFolder = "../libs/";
	public static final String _serialPath = "config/nexus_serial.txt";
	public static final String _charIdColumnName = "charId";
	
	public static final boolean _changeButtonStyle = false;
	public static final String _buttonBackDesign = "xxx";
	public static final String _buttonForeDesign = "xxx";
	
	public static final String _colorac9887 = "ac9887";
	public static final String _color9f9f9f = "9f9f9f";
	
	// true if the client has html size limit (interlude has it, maybe some other client has it too) which when is exceeded, client crashes
	// gracia final and above do not seem to have it
	public static final boolean _limitedHtml = false;
	
	public static void start()
	{
		NexusOut.getInstance().load();
		PlayerBase.getInstance().load();
		Values.getInstance().load();
		
		NexusLoader.init(_branch, _interfaceVersion, _desc, _allowInstances, _libsFolder, _serialPath, _limitedHtml);
	}
	
	public static void loadHtmlManager()
	{
		if (Config.DONTLOADNEXUS)
			return;
		
		HtmlManager.load();
		DescriptionLoader.load();
	}
	
	public static void serverShutDown()
	{
		//TODO save data
	}
	
	public static void onLogin(Player player)
	{
		if (Config.DONTLOADNEXUS)
			return;
		
		EventBuffer.getInstance().loadPlayer(player.getEventInfo());
		EventManager.getInstance().onPlayerLogin(player.getEventInfo());
	}
	
	public static PlayerEventInfo getPlayer(Player player)
	{
		if (Config.DONTLOADNEXUS)
			return null;
		
		return NexusLoader.loaded() ? PlayerBase.getInstance().getPlayer(player) : null;
	}
	
	public static boolean isRegistered(Player player)
	{
		PlayerEventInfo pi = getPlayer(player);
		return pi != null && pi.isRegistered();
	}
	
	public static boolean isInEvent(Player player)
	{
		if (Config.DONTLOADNEXUS)
			return false;
		
		PlayerEventInfo pi = getPlayer(player);
		return pi != null && pi.isInEvent();
	}
	
	public static boolean isInEvent(Creature ch)
	{
		if (Config.DONTLOADNEXUS)
			return false;
		
		if(ch.isPlayable())
			return isInEvent(ch.getPlayer());
		else
			return EventManager.getInstance().isInEvent(new CharacterData(ch));
	}
	
	public static boolean allowDie(Creature ch, Creature attacker)
	{
		if (Config.DONTLOADNEXUS)
			return true;
		
		if(isInEvent(ch) && isInEvent(attacker))
			return EventManager.getInstance().allowDie(new CharacterData(ch), new CharacterData(attacker));
		return true;
	}
	
	public static boolean isInMiniEvent(Player player)
	{
		PlayerEventInfo pi = getPlayer(player);
		return pi != null && pi.getActiveGame() != null;
	}
	
	public static boolean isInMainEvent(Player player)
	{
		PlayerEventInfo pi = getPlayer(player);
		return pi != null && pi.getActiveEvent() != null;
	}
	
	public static boolean canShowToVillageWindow(Player player)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			return pi.canShowToVillageWindow();
		return true;
	}
	
	public static boolean canAttack(Player player, Creature target)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			return pi.canAttack(target);
		return true;
	}
	
	public static boolean onAttack(Creature cha, Creature target)
	{
		if (Config.DONTLOADNEXUS)
			return true;
		
		return EventManager.getInstance().onAttack(new CharacterData(cha), new CharacterData(target));
	}
	
	public static void trySuicide(Player player)
	{
		final PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
		{
			if(pi.getActiveEvent() != null && pi.getActiveEvent().getEventType() != EventType.Mutant)
			{
				pi.sendMessage("You will commit a suicide in 30 seconds.");
				
				CallBack.getInstance().getOut().scheduleGeneral(new Runnable() 
				{
					@Override
					public void run() 
					{
						if(pi.getActiveEvent() != null && EventManager.getInstance().getMainEventManager().getState() == State.RUNNING)
						{
							pi.doDie();
						}
					}
				}, 30000);
				
			}
			else
			{
				pi.sendMessage("You cannot do that in this event.");
			}
		}
	}
	
	public static boolean canSupport(Player player, Creature target)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			return pi.canSupport(target);
		return true;
	}
	
	public static boolean canTarget(Player player, GameObject target)
	{
		//TODO: unknown todo.
		return true;
	}
	
	// ***
	
	public static void onHit(Player player, Creature target, int damage, boolean isDOT)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			pi.onDamageGive(target, damage, isDOT);
	}
	
	public static void onDamageGive(Creature cha, Creature target, int damage, boolean isDOT)
	{
		if (Config.DONTLOADNEXUS)
			return;
		
		EventManager.getInstance().onDamageGive(new CharacterData(cha), new CharacterData(target), damage, isDOT);
	}
	
	public static void onKill(Player player, Creature target)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			pi.notifyKill(target);
	}
	
	public static void onDie(Player player, Creature killer)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			pi.notifyDie(killer);
	}
	
	/** returning true will make the default NPC actions skipped (showing html, etc. */
	public static boolean onNpcAction(Player player, NpcInstance target)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			return pi.notifyNpcAction(target);
		return false;
	}
	
	public static boolean canUseItem(Player player, ItemInstance item)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			return pi.canUseItem(item);
		return true;
	}
	
	public static void onUseItem(Player player, ItemInstance item)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			pi.notifyItemUse(item);
	}
	
	public static boolean onSay(Player player, String text, int channel)
	{
		if (Config.DONTLOADNEXUS)
			return true;
		
		if (text.startsWith("."))
		{
			if (EventManager.getInstance().tryVoicedCommand(player.getEventInfo(), text))
				return false;
		}
		
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			return pi.notifySay(text, channel);
		return true;
	}
	
	public static boolean canUseSkill(Player player, Skill skill)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			return pi.canUseSkill(skill);
		return true;
	}
	
	public static void onUseSkill(Player player, Skill skill)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			pi.onUseSkill(skill);
	}
	
	public static boolean canDestroyItem(Player player, ItemInstance item)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			return pi.canDestroyItem(item);
		return true;
	}
	
	public static void pvpZoneSwitched()
	{
		EventManager.getInstance().pvpZoneSwitched();
	}
	
	public static boolean canInviteToParty(Player player, Player target)
	{
		PlayerEventInfo pi = getPlayer(player);
		PlayerEventInfo targetPi = getPlayer(target);
		if(pi != null)
		{
			if(targetPi == null)
				return false;
			else
				return pi.canInviteToParty(pi, targetPi);
		}
		return true;
	}
	
	public static boolean canTransform(Player player)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			return pi.canTransform(pi);
		return true;
	}
	
	/**
	 * @return 0 if the engine doesn't care, -1 if the engine doesn't allow this skill, 1 if the engine allows this skill
	 */
	public static int allowTransformationSkill(Player player, Skill s)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			return pi.allowTransformationSkill(s);
		return 0;
	}
	
	public static boolean canBeDisarmed(Player player)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			return pi.canBeDisarmed(pi);
		return true;
	}
	
	/** returning true will 'mark' the bypass as already assigned and perfomed */
	public static boolean onBypass(Player player, String command)
	{
		if (Config.DONTLOADNEXUS)
			return false;
		
		if(command.startsWith("nxs_"))
			return EventManager.getInstance().onBypass(player.getEventInfo(), command.substring(4));
		return false;
	}
	
	public static void onAdminBypass(PlayerEventInfo player, String command)
	{
		if (Config.DONTLOADNEXUS)
			return;
		
		EventManagement.getInstance().onBypass(player, command);
	}
	
	// ***
	
	public static boolean canLogout(Player player)
	{
		PlayerEventInfo pi = getPlayer(player);
		return !(pi != null && pi.isInEvent());
	}
	
	public static void onLogout(Player player)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null)
			pi.notifyDisconnect();
	}
	
	public static boolean isObserving(Player player)
	{
		return player.getEventInfo().isSpectator();
	}
	
	public static void endObserving(Player player)
	{
		if (Config.DONTLOADNEXUS)
			return;
		
		EventManager.getInstance().removePlayerFromObserverMode(player.getEventInfo());
	}
	
	public static boolean canSaveShortcuts(Player activeChar)
	{
		PlayerEventInfo pi = getPlayer(activeChar);
		if(pi != null)
			pi.canSaveShortcuts();
		return true;
	}
	
	public static int getItemAutoEnchantValue(Player player, ItemInstance item)
	{
		if(isInEvent(player))
		{
			PlayerEventInfo pi = PlayerBase.getInstance().getPlayer(player);
			
			MiniEventManager event = pi.getRegisteredMiniEvent();
			if(event == null)
				return 0;
			
			for(AbstractFeature f : event.getMode().getFeatures())
			{
				if(f.getType() == FeatureType.Enchant)
				{
					switch(item.getTemplate().getType2())
					{
						case ItemTemplate.TYPE2_WEAPON:
							return ((EnchantFeature) f).getAutoEnchantWeapon();
						case ItemTemplate.TYPE2_SHIELD_ARMOR:
							return ((EnchantFeature) f).getAutoEnchantArmor();
						case ItemTemplate.TYPE2_ACCESSORY:
							return ((EnchantFeature) f).getAutoEnchantJewel();
					}
				}
			}
			
			return 0;
		}
		else
			return 0;
	}
	
	public static boolean removeCubics()
	{
		if (Config.DONTLOADNEXUS)
			return false;
		
		return EventConfig.getInstance().getGlobalConfigBoolean("removeCubicsOnDie");
	}
	
	public static boolean gainPvpPointsOnEvents()
	{
		if (Config.DONTLOADNEXUS)
			return false;
		
		return EventConfig.getInstance().getGlobalConfigBoolean("pvpPointsOnKill");
	}
	
	public static boolean cbBypass(Player player, String command)
	{
		PlayerEventInfo pi = getPlayer(player);
		if(pi != null && command != null)
			return EventManager.getInstance().getHtmlManager().onCbBypass(pi, command);
		return false;
	}
	
	public static String consoleCommand(String cmd)
	{
		if(cmd.startsWith("reload_globalconfig"))
		{
			EventConfig.getInstance().loadGlobalConfigs();
			//TODO: check how we can translate this.
			return "Global configs reloaded.";
		}
		//TODO: check how we can translate this.
		else return "This command doesn't exist.";
	}

	public static boolean adminCommandRequiresConfirm(String cmd)
	{
		if(cmd.split(" ").length > 1)
		{
			String command = cmd.split(" ")[1];
			return EventManagement.getInstance().commandRequiresConfirm(command);
		}
		
		return false;
	}
	
	public static boolean isSkillOffensive(Player activeChar, Skill skill)
	{
		PlayerEventInfo pi = getPlayer(activeChar);
		if(pi != null)
		{
			if(pi.isInEvent())
			{
				EventGame game = pi.getEvent();
				int val = game.isSkillOffensive(new SkillData(skill));
				if(val == 1)
					return true;
				else if(val == 0)
					return false;
			}
		}
		
		return skill.isOffensive();
	}
	
	public static boolean isSkillNeutral(Player activeChar, Skill skill)
	{
		PlayerEventInfo pi = getPlayer(activeChar);
		if(pi != null)
		{
			if(pi.isInEvent())
			{
				EventGame game = pi.getEvent();
				return game.isSkillNeutral(new SkillData(skill));
			}
		}
		return false;
	}
	
	public static boolean isPvpZoneActive()
	{
		return EventManager.getInstance().getMainEventManager().getPvpZoneManager().isActive();
	}
	
	public static String getPvpZoneName()
	{
		return EventManager.getInstance().getMainEventManager().getPvpZoneManager().getMapName();
	}
	
	public static String getPvpZoneTimeActive()
	{
		return EventManager.getInstance().getMainEventManager().getPvpZoneManager().timeActive();
	}
	
	public static int getPvpZonePlayersCount()
	{
		return EventManager.getInstance().getMainEventManager().getPvpZoneManager().getPlayersCount();
	}
	
	public static void registerToPvpZone(PlayerEventInfo player)
	{
		EventManager.getInstance().getMainEventManager().getPvpZoneManager().registerPlayer(player);
	}
	
	public static void unregisterFromPvpZone(PlayerEventInfo player, boolean forced)
	{
		EventManager.getInstance().getMainEventManager().getPvpZoneManager().unregisterPlayer(player, forced);
	}
	
	public static String getCharIdColumnName()
	{
		return _charIdColumnName;
	}
	
	public static boolean changeButtonStyle()
	{
		return _changeButtonStyle;
	}
	
	public static String buttonBackDesign()
	{
		return _buttonBackDesign;
	}
	
	public static String buttonForeDesign()
	{
		return _buttonForeDesign;
	}
	
	public static String colorac9887()
	{
		return _colorac9887;
	}
	
	public static String color9f9f9f()
	{
		return _color9f9f9f;
	}
}
