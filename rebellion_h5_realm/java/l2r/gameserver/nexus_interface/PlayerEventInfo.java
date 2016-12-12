/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.nexus_interface;

import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.actor.instances.player.ShortCut;
import l2r.gameserver.model.base.ClassType;
import l2r.gameserver.model.base.PlayerClass;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.events.impl.DuelEvent;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.SetupGauge;
import l2r.gameserver.network.serverpackets.ShortCutInit;
import l2r.gameserver.network.serverpackets.ShortCutRegister;
import l2r.gameserver.network.serverpackets.SkillCoolTime;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.nexus_engine.events.EventGame;
import l2r.gameserver.nexus_engine.events.engine.EventConfig;
import l2r.gameserver.nexus_engine.events.engine.EventManager;
import l2r.gameserver.nexus_engine.events.engine.base.EventPlayerData;
import l2r.gameserver.nexus_engine.events.engine.base.EventSpawn;
import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.base.Loc;
import l2r.gameserver.nexus_engine.events.engine.base.PvPEventPlayerData;
import l2r.gameserver.nexus_engine.events.engine.html.PartyMatcher;
import l2r.gameserver.nexus_engine.events.engine.main.events.AbstractMainEvent;
import l2r.gameserver.nexus_engine.events.engine.mini.MiniEventGame;
import l2r.gameserver.nexus_engine.events.engine.mini.MiniEventManager;
import l2r.gameserver.nexus_engine.events.engine.stats.EventStatsManager;
import l2r.gameserver.nexus_engine.events.engine.team.EventTeam;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_engine.l2r.IPlayerEventInfo;
import l2r.gameserver.nexus_engine.l2r.IValues;
import l2r.gameserver.nexus_interface.delegate.CharacterData;
import l2r.gameserver.nexus_interface.delegate.ItemData;
import l2r.gameserver.nexus_interface.delegate.NpcData;
import l2r.gameserver.nexus_interface.delegate.PartyData;
import l2r.gameserver.nexus_interface.delegate.ShortCutData;
import l2r.gameserver.nexus_interface.delegate.SkillData;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.skills.EffectType;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Util;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastTable;

/**
 * @author hNoke
 *
 */
public class PlayerEventInfo implements IPlayerEventInfo
{
	public static final boolean AFK_CHECK_ENABLED = EventConfig.getInstance().getGlobalConfigBoolean("afkChecksEnabled");
	public static final int AFK_WARNING_DELAY = EventConfig.getInstance().getGlobalConfigInt("afkWarningDelay");
	public static final int AFK_KICK_DELAY = EventConfig.getInstance().getGlobalConfigInt("afkKickDelay");

	// Main variables
	private Player _owner;
	private int _playersId;
	private boolean _isInEvent;
	private boolean _isRegistered; 
	private boolean _isRegisteredPvpZone; 
	private boolean _isInFFAEvent;
	private boolean _isSpectator;
	private boolean _canBuff;
	private boolean _canParty = true;
	
	private boolean _antifeedProtection;
	//
	private boolean _titleUpdate;
	private boolean _disableAfkCheck;
	
	// Original data, which will be restored when the event ends
	private int _origNameColor;
	private Location _origLoc;
	private String _origTitle;	
	private int _origTitleColor;
	
	// Event data
	private EventPlayerData _eventData;
	
	private int _status;
	
	// Active event and teams
	private EventGame _activeEvent;
	private EventTeam _eventTeam;

	private EventSpawn _lastSpawnTeleported;
	
	private MiniEventManager _registeredMiniEvent;
	private EventType _registeredMainEvent;
	
	private AfkChecker _afkChecker;
	private Radar _radar;
	
	private String _tempName;
	private int _tempLevel;
	
	private int _healAmount;
	
	public PlayerEventInfo(Player owner)
	{
		_owner = owner;
		_playersId = owner == null ? -1 : owner.getObjectId();
		
		_isRegistered = false;
		_isInEvent = false;
		_isInFFAEvent = false;
		
		_status = 0;
		
		_disableAfkCheck = false;
		_titleUpdate = true;
		
		_afkChecker = null;
		
		_healAmount = 0;
	}
	
	public PlayerEventInfo(String name, int level)
	{
		_tempName = name;
		_tempLevel = level;
	}
	
	public PlayerEventInfo(String name, int level, ClassType t)
	{
		_tempName = name;
		_tempLevel = level;
	}
	
	// =========================================================
	// ======== GENERAL EVENT METHODS ==========================
	// =========================================================
	
	@Override
	public void initOrigInfo()
	{
		 _owner.setEventStatus();
		 _origNameColor = _owner.getNameColor();
		 _origTitle = _owner.getTitle();
		 _origTitleColor = _owner.getTitleColor();
		 _origLoc = new Location(_owner.getX(), _owner.getY(), _owner.getZ(), _owner.getHeading());
		    
		/* Infern0 : another method to save cordinate... 
		_owner.setEventStatus();
		_owner.setVar("NexusNameColor", _owner.getNameColor(), -1);
		_owner.setVar("NexusTitleColor", _owner.getTitleColor(), -1);
		_owner.setVar("NexusTitle", _owner.getTitle(), -1);
		_owner.setVar("NexusPlayerLoc",  _owner.getX() + " " + _owner.getY() + " " + _owner.getZ(), -1);
		*/
	}
	
	@Override
	public void restoreData()
	{
		/* Infern0 : another method to save cordinate... 
		// Location
		try
		{
			String var = _owner.getVar("NexusPlayerLoc");
			if (var == null || var.equals(""))
				return;
			
			String[] coords = var.split(" ");
			if (coords.length != 3)
				return;
			
			_owner.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), 0);
			_owner.unsetVar("NexusPlayerLoc");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// Name color
		try
		{
			String var = _owner.getVar("NexusNameColor");
			if (var == null || var.equals(""))
				return;
			
			_owner.setNameColor(var);
			
			_owner.unsetVar("NexusNameColor");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// Title color
		try
		{
			String var = _owner.getVar("NexusTitleColor");
			if (var == null || var.equals(""))
				return;
			
			_owner.setTitleColor(Integer.parseInt(var));
			
			_owner.unsetVar("NexusTitleColor");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// Title name
		try
		{
			String var = _owner.getVar("NexusTitle");
			if (var == null || var.equals(""))
				return;
			
			_owner.setTitle(var);
			
			_owner.unsetVar("NexusTitle");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		*/
		
		_owner.setNameColor(_origNameColor);
	    _owner.setTitle(_origTitle);
	    _owner.setTitleColor(_origTitleColor);
	    
		_owner.broadcastCharInfo();
		_owner.broadcastUserInfo(true);
		//_owner.clearGearScoreData();
		
		clean();
	}
	
	@Override
	public void onEventStart(EventGame event)
	{
		initOrigInfo();
		
		_isInEvent = true;
		_activeEvent = event;
		
		_eventData = event.createPlayerData(this);
		
		if(AFK_CHECK_ENABLED)
			_afkChecker = new AfkChecker(this);
	}
	
	@Override
	public void clean()
	{
		if(_afkChecker != null)
			_afkChecker.stop();
		
		if(_radar != null)
			_radar.disable();
		
		_isRegistered = false;
		_isRegisteredPvpZone = false;
		_isInEvent = false;
		_isInFFAEvent = false;
		
		_registeredMiniEvent = null;
		_registeredMainEvent = null;
		
		_activeEvent = null;
		_eventTeam = null;
		
		_canParty = true;
		
		_eventData = null;
		
		_status = 0;
		
		_healAmount = 0;
	}
	
	private class Teleport implements Runnable
	{
		final Player owner;
		Loc loc;
		//TODO: check this.
		//int delay;
		//boolean randomOffset;
		int instanceId;
		
		Teleport(Player owner, Loc loc, int delay, int instanceId)
		{
			this.owner = owner;
			this.loc = loc;
			//TODO: check this.
			//this.delay = delay;
			//this.randomOffset = randomOffset;
			this.instanceId = instanceId;
			
			if(delay == 0)
				CallBack.getInstance().getOut().executeTask(this);
			else
				CallBack.getInstance().getOut().scheduleGeneral(this, delay);
		}

		@Override
		public void run()
		{
			Player player = owner;
			
			if (player == null)
				return;
			
			Summon summon = player.getPet();
			
			player.abortCast(true, false);
			
			if (summon != null)
				summon.unSummon();

			DuelEvent duel = player.getEvent(DuelEvent.class);
			if(duel != null)
				duel.abortDuel(player);
			
			if (player != null && player.isDead())
			{
				player.doRevive();
				player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
			}
			
			for(Effect e : player.getEffectList().getAllEffects())
			{
				if(e != null && e.getSkill() != null && !e.getSkill().isBuff())
					e.exit();
			}
			
			if(player.isSitting())
				player.standUp();
			
			player.teleToLocation(loc.getX(), loc.getY(), loc.getZ());
			
			player.setTarget(null);
			
			if(instanceId != -1)
				player.setReflection(instanceId);

			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp(), false);
			player.setCurrentMp(player.getMaxMp());
			
			player.broadcastStatusUpdate();
			player.broadcastUserInfo(true);
		}
	}
	
	@Override
	public void teleport(Loc loc, int delay, int instanceId)
	{
		new Teleport(_owner, loc, delay, instanceId);
	}
	
	@Override
	public void teleToLocation(Loc loc)
	{
		_owner.teleToLocation(loc.getX(), loc.getY(), loc.getZ());
	}
	
	@Override
	public void teleToLocation(int x, int y, int z)
	{
		_owner.teleToLocation(x, y, z);
	}
	
	@Override
	public void teleToLocation(int x, int y, int z, int heading)
	{
		_owner.teleToLocation(x, y, z, heading);
	}
	
	@Override
	public void setXYZInvisible(int x, int y, int z)
	{
		_owner.setXYZInvisible(x, y, z);
	}
	
	@Override
	public void setFame(int count)
	{
		_owner.setFame(count);
	}
	
	@Override
	public int getFame()
	{
		return _owner.getFame();
	}
	
	// =========================================================
	// ======== Player ACTIONS ===========================
	// =========================================================
	
	protected void notifyKill(Creature target)
	{
		if(_activeEvent != null && !_isSpectator)
			_activeEvent.onKill(this, new CharacterData(target));
	}
	
	protected void notifyDie(Creature killer)
	{
		if(_activeEvent != null && !_isSpectator)
			_activeEvent.onDie(this, new CharacterData(killer));
	}
	
	protected void notifyDisconnect()
	{
		PartyMatcher.onDisconnect(this);
		
		if(_activeEvent != null && !_isSpectator)
			_activeEvent.onDisconnect(this);
		
		if(_registeredMainEvent != null)
		{
			EventManager.getInstance().getMainEventManager().unregisterPlayer(this, true);
		}
		else if(_registeredMiniEvent != null)
		{
			// already handled
		}
		
		EventStatsManager.getInstance().onDisconnect(this);
		PlayerBase.getInstance().eventEnd(this);
	}
	
	protected boolean canAttack(Creature target)
	{
		if(_activeEvent != null && !_isSpectator)
			return _activeEvent.canAttack(this, new CharacterData(target));
		
		return true;
	}
	
	protected boolean canSupport(Creature target)
	{
		if(_activeEvent != null && !_isSpectator)
			return _activeEvent.canSupport(this, new CharacterData(target));
		
		return true;
	}
	
	public void onAction()
	{
		if(_afkChecker != null)
			_afkChecker.onAction();
	}
	
	protected void onDamageGive(Creature target, int ammount, boolean isDOT)
	{
		if(_activeEvent != null && !_isSpectator)
			_activeEvent.onDamageGive(getCharacterData(), new CharacterData(target), ammount, isDOT);
	}
	
	/** returning false will make the text not shown */
	protected boolean notifySay(String text, int channel)
	{
		if(_activeEvent != null)
			return _activeEvent.onSay(this, text, channel);
		return true;
	}
	
	/** false means that a html page has been already sent by Nexus engine */
	protected boolean notifyNpcAction(NpcInstance npc)
	{
		if(_isSpectator)
			return true;
		
		if(EventManager.getInstance().showNpcHtml(this, new NpcData(npc)))
			return true;
		
		if(_activeEvent != null)
			return _activeEvent.onNpcAction(this, new NpcData(npc));
		
		return false;
	}
	
	protected boolean canUseItem(ItemInstance item)
	{
		if(_isSpectator)
			return false;
		
		if(_activeEvent != null)
			return _activeEvent.canUseItem(this, new ItemData(item));
		
		return true;
	}
	
	protected void notifyItemUse(ItemInstance item)
	{
		if(_activeEvent != null)
			_activeEvent.onItemUse(this, new ItemData(item));
	}
	
	protected boolean canUseSkill(Skill skill)
	{
		if(_isSpectator)
			return false;
		
		if(_activeEvent != null)
			return _activeEvent.canUseSkill(this, new SkillData(skill));
		return true;
	}
	
	protected void onUseSkill(Skill skill)
	{
		if(_activeEvent != null)
			_activeEvent.onSkillUse(this, new SkillData(skill));
	}
	
	protected boolean canShowToVillageWindow()
	{
		//if(_isInEvent) // this is already checked
			return false;
	}
	
	protected boolean canDestroyItem(ItemInstance item)
	{
		if(_activeEvent != null)
			return _activeEvent.canDestroyItem(this, new ItemData(item));
		return true;
	}
	
	protected boolean canInviteToParty(PlayerEventInfo player, PlayerEventInfo target)
	{
		if(_activeEvent != null)
			return _activeEvent.canInviteToParty(player, target);
		return true;
	}
	
	protected boolean canTransform(PlayerEventInfo player)
	{
		if(_activeEvent != null)
			return _activeEvent.canTransform(player);
		return true;
	}
	
	protected boolean canBeDisarmed(PlayerEventInfo player)
	{
		if(_activeEvent != null)
			return _activeEvent.canBeDisarmed(player);
		return true;
	}
	
	protected int allowTransformationSkill(Skill s)
	{
		if(_activeEvent != null)
			return _activeEvent.allowTransformationSkill(this, new SkillData(s));
		return 0;
	}
	
	protected boolean canSaveShortcuts()
	{
		if(_activeEvent != null)
			return _activeEvent.canSaveShortcuts(this);
		return true;
	}
	
	// =========================================================
	// ======== Player GENERAL METHODS ===================
	// =========================================================
	
	@Override
	public void setInstanceId(int id)
	{
		_owner.setReflection(id);
	}
	
	@Override
	public void sendPacket(String html)
	{
		sendHtmlText(html);
	}
	
	@Override
	public void screenMessage(String message, String name, boolean special)
	{
		L2GameServerPacket packet;
		
		if(special)
			packet = new ExShowScreenMessage(message, 5000, ScreenMessageAlign.TOP_CENTER, special);
		else
			packet = new CreatureSay(0, ChatType.COMMANDCHANNEL_ALL.ordinal(), name, message);
		
		if(_owner != null)
			_owner.sendPacket(packet);
	}
	
	@Override
	public void creatureSay(String message, String announcer, int channel)
	{
		if(_owner != null)
			_owner.sendPacket(new CreatureSay(0, channel, announcer, message));
	}
	
	@Override
	public void sendMessage(String message)
	{
		if(_owner != null)
			_owner.sendMessage(message);
	}
	
	@Override
	public void sendEventScoreBar(String text)
	{
		if(_owner != null)
			_owner.sendPacket(new ExShowScreenMessage(1, -1, ScreenMessageAlign.TOP_RIGHT, 0, 0, 0, true, false, 5000, false, text));
	}
	
	@Override
	public void broadcastUserInfo()
	{
		if(_owner != null)
			_owner.broadcastUserInfo(true);
	}
	
	@Override
	public void broadcastTitleInfo()
	{
		if(_owner != null)
			_owner.broadcastCharInfo();
	}
	
	@Override
	public void sendSkillList()
	{
		_owner.sendPacket(new SkillList(_owner));
	}
	
	@Override
	public void transform(int transformId)
	{
		if(_owner != null)
			_owner.setTransformation(transformId);
	}
	
	@Override
	public boolean isTransformed()
	{
		if(_owner != null && _owner.getTransformation() > 0)
			return true;
		return false;
	}
	
	@Override
	public void untransform(boolean removeEffects)
	{
		if(_owner != null && _owner.getTransformation() > 0)
			_owner.setTransformation(0);
	}
	
	@Override
	public ItemData addItem(int id, int ammount, boolean msg)
	{
		return new ItemData(_owner.getInventory().addItem(id, ammount));
	}
	
	@Override
	public void addExpAndSp(long exp, int sp)
	{
		_owner.addExpAndSp(exp, sp);
	}
	
	@Override
	public void doDie()
	{
		_owner.doDie(_owner);
	}
	
	@Override
	public void doDie(CharacterData killer)
	{
		_owner.doDie(killer.getOwner());
	}
	
	@Override
	public ItemData[] getItems()
	{
		List<ItemData> items = new FastTable<ItemData>();
		
		for(ItemInstance item : _owner.getInventory().getItems())
		{
			items.add(new ItemData(item));
		}
		
		return items.toArray(new ItemData[items.size()]);
	}
	
	@Override
	public void getPetSkillEffects(int skillId, int level)
	{
		if(_owner.getPet() != null)
		{
			Skill skill = SkillTable.getInstance().getInfo(skillId, level);
			if (skill != null)
			{
				skill.getEffects(_owner.getPet(), _owner.getPet(), false, false);
			}
		}
	}
	
	@Override
	public void getSkillEffects(int skillId, int level)
	{
		Skill skill = SkillTable.getInstance().getInfo(skillId, level);
		if (skill != null)
			skill.getEffects(_owner, _owner, false, false);
	}
	
	@Override
	public void addSkill(SkillData skill, boolean store)
	{
		getOwner().addSkill(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()), store);
	}
	
	@Override
	public void removeSkillById(Integer id)
	{
		getOwner().removeSkillById(id);
	}
	
	@Override
	public void removeCubics()
	{
		if (!_owner.getCubics().isEmpty())
			_owner.getEffectList().stopAllSkillEffects(EffectType.Cubic);
	}
	
	@Override
	public void removeSummon()
	{
		if(_owner.getPet() != null)
			_owner.getPet().unSummon();
	}
	
	@Override 
	public boolean hasPet()
	{
		return _owner.getPet() != null;
	}
	
	@Override
	public void removeBuffsFromPet()
	{
		if(_owner != null && _owner.getPet() != null)
			_owner.getPet().getEffectList().stopAllEffects();
	}
	
	@Override
	public void removeBuffs()
	{
		if(_owner != null)
			_owner.getEffectList().stopAllEffects();
	}
	
	@Override
	public int getBuffsCount()
	{
		List<Effect> effectsList = _owner.getEffectList().getAllEffects();
		int buffCount = effectsList.size();
		
		return buffCount;
	}
	
	@Override
	public int getDancesCount()
	{
		int danceCount = 0;
		for (Effect e : _owner.getEffectList().getAllEffects())
		{
			if (e == null)
				continue;
			
			if (e.getSkill().isMusic())
				danceCount++;
		}
		
		return danceCount;
	}
	
	@Override
	public int getPetBuffCount()
	{
		if(_owner.getPet() != null)
		{
			List<Effect> effectsList = _owner.getPet().getEffectList().getAllEffects();
			int petbuffCount = effectsList.size();
			return petbuffCount;
		}
		else
			return 0;
	}
	
	@Override
	public int getPetDanceCount()
	{
		int danceCount = 0;
		
		if (_owner.getPet() != null)
		{
			for (Effect e : _owner.getPet().getEffectList().getAllEffects())
			{
				if (e == null)
					continue;
				
				if (e.getSkill().isMusic())
					danceCount++;
			}
			
			return danceCount;
		}
		else
			return 0;
	}
	
	@Override
	public int getMaxBuffCount()
	{
		return _owner.getBuffLimit();
	}
	
	@Override
	public int getMaxDanceCount()
	{
		return Config.ALT_MUSIC_LIMIT;
	}
	
	@Override
	public void removeBuff(int id)
	{
		if(_owner != null)
			_owner.getEffectList().stopEffect(id);
	}
	
	@Override
	public void abortCasting()
	{
		if(_owner.isCastingNow())
			_owner.abortCast(true, true);
		
		if(_owner.isAttackingNow())
			_owner.abortAttack(true, true);
	}
	
	@Override
	public void playSound(String file)
	{
		_owner.sendPacket(new PlaySound(file));
	}
	
	//TODO: check this.
	/* Infern0 not used..
	@Override
	public void setVisible()
	{
		_owner.setVisible();
	}
	*/
	
	@Override
	public void rebuffPlayer()
	{
		/* Infern0 not used..
		/*if(getActiveGame() != null)
		{
			for(AbstractFeature feature : getActiveGame().getEvent().getMode().getFeatures())
			{
				if(feature.getType() == FeatureType.Buffer)
				{
					if(Manager.getInstance().getBoolean("eventBufferEnabled") && ((BufferFeature) feature).canRebuff())
						Buffer.getInstance().buffPlayer(_owner);
					
					((BufferFeature) feature).buffPlayer(_owner);
				}
			}
		}*/
	}
	
	@Override
	public void enableAllSkills()
	{
		for(Skill skill : _owner.getAllSkills())
		{
			if (skill.getReuseDelay() <= 900000)
				_owner.enableSkill(skill);
		}
		_owner.sendPacket(new SkillCoolTime(_owner));
	}
	
	@Override
	public void sendSetupGauge(int time)
	{
		SetupGauge sg = new SetupGauge(_owner, 0, time);
		_owner.sendPacket(sg);
	}
	
	@Override
	public void block()
	{
		_owner.block();
		_owner.startAbnormalEffect(AbnormalEffect.STEALTH);
	}
	
	@Override
	public void unblock()
	{
		if(_owner.isBlocked())
			_owner.unblock();;
		_owner.stopAbnormalEffect(AbnormalEffect.STEALTH);
	}
	
	@Override
	public void paralizeEffect(boolean b)
	{
		if(b) 
			getOwner().startAbnormalEffect(AbnormalEffect.HOLD_1);
		else 
			getOwner().stopAbnormalEffect(AbnormalEffect.HOLD_1);
	}
	
	@Override
	public void setIsParalyzed(boolean b)
	{
		if (b)
		{
			if (!_owner.isParalyzed())
				_owner.startParalyzed();
		}
		else
		{
			if (_owner.isParalyzed())
				_owner.stopParalyzed();
		}
	}
	
	@Override
	public void setIsInvul(boolean b)
	{
		_owner.setIsInvul(b);
	}
	
	@Override
	public void setCanInviteToParty(boolean b)
	{
		_canParty = b;
	}
	
	@Override
	public boolean canInviteToParty()
	{
		return _canParty;
	}
	
	@Override
	public void showEventEscapeEffect()
	{
		_owner.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		_owner.setTarget(_owner);
		_owner.abortAttack(true, true);
		_owner.abortCast(true, true);
		_owner.stopMove();
		
		_owner.broadcastPacket(new MagicSkillUse(_owner, _owner, 1050, 1, 10000, 0));
		_owner.sendPacket(new SetupGauge(_owner, SetupGauge.BLUE, 10000));
	}
	
	@Override
	public void startAntifeedProtection(boolean broadcast)
	{
		_owner.startAntifeedProtection(true, broadcast);
		_antifeedProtection = true;
		
		if(broadcast)
			broadcastUserInfo();
	}
	
	@Override
	public void stopAntifeedProtection(boolean broadcast)
	{
		_owner.startAntifeedProtection(false, broadcast);
		_antifeedProtection = false;
		
		if(broadcast)
			broadcastUserInfo();
	}
	
	@Override
	public boolean hasAntifeedProtection()
	{
		return _antifeedProtection;
	}
	
	/** 
	 * @param owner - null to put there this PlayerEventInfo
	 * @param target - null to put there this PlayerEventInfo
	 * @param skillId
	 * @param level
	 */
	@Override
	public void broadcastSkillUse(CharacterData owner, CharacterData target, int skillId, int level)
	{
		Skill skill = SkillTable.getInstance().getInfo(skillId, level);
		
		if (skill != null)
			getOwner().broadcastPacket(new MagicSkillUse(owner == null ? getOwner() : owner.getOwner(), target == null ? getOwner() : target.getOwner(), skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
	}
	
	@Override
	public void broadcastSkillLaunched(CharacterData owner, CharacterData target, int skillId, int level)
	{
		Skill skill = SkillTable.getInstance().getInfo(skillId, level);
		
		if (skill != null)
			getOwner().broadcastPacket(new MagicSkillLaunched(owner == null ? getOwner().getObjectId() : owner.getOwner().getObjectId(), skill.getId(), skill.getLevel(), target.getOwner()));
	}
	
	@Override
	public void enterObserverMode(int x, int y, int z)
	{
		_owner.enterObserverMode(new Location(x, y, z));
	}
	
	@Override
	public void removeObserveMode()
	{
		setIsSpectator(false);
		setActiveGame(null);
		
		_owner.leaveOlympiadObserverMode(true);
		_owner.setReflection(0);
		_owner.teleToLocation(getOrigLoc().getX(), getOrigLoc().getY(), getOrigLoc().getZ());
	}
	
	@Override
	public void sendStaticPacket()
	{
		_owner.sendPacket(ActionFail.STATIC);
	}
	
	@Override
	public void sendHtmlText(String text)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(text);
		
		_owner.sendPacket(msg);
	}
	
	@Override
	public void sendHtmlPage(String path)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(path);

		_owner.sendPacket(html);
		sendStaticPacket();
	}
	
	@Override
	public void startAbnormalEffect(AbnormalEffect ae)
	{
		IValues val = CallBack.getInstance().getValues();
		if(ae.getMask() == val.ABNORMAL_S_INVINCIBLE())
		{
			_owner.startAbnormalEffect(ae);
		}
		else
			_owner.startAbnormalEffect(ae);
	}
	
	@Override
	public void stopAbnormalEffect(AbnormalEffect ae)
	{
		IValues val = CallBack.getInstance().getValues();
		if(ae.getMask() == val.ABNORMAL_S_INVINCIBLE())
		{
			_owner.stopAbnormalEffect(ae);
		}
		else
			_owner.stopAbnormalEffect(ae);
	}
	
	// Nexus ShortCut engine
	
	private List<ShortCutData> _customShortcuts = new FastTable<ShortCutData>();
	
	/**
	 * restore back removed shortcuts by calling restoreOriginalShortcuts()
	 */
	@Override
	public void removeOriginalShortcuts()
	{
		if(_owner == null) return;
		
		_owner.removeAllShortcuts();
		_owner.sendPacket(new ShortCutInit(_owner)); 
	}
	
	@Override
	public void restoreOriginalShortcuts()
	{
		if(_owner == null) return;
		
		_owner.restoreShortCuts();
		_owner.sendPacket(new ShortCutInit(_owner)); 
	}
	
	@Override
	public void removeCustomShortcuts()
	{
		if(_owner == null) return;
		
		for(ShortCutData sh : _customShortcuts)
		{
			_owner.deleteShortCut(sh.getSlot(), sh.getPage(), false);
		}
		
		_customShortcuts.clear();
	}
	
	/**
	 * @param shortcut - the shortcut you wanna register
	 * @param eventShortcut - if the shortcut exists only during the event; can be deleted by calling removeCustomShortcuts(); won't be saved to db
	 */
	@Override
	public void registerShortcut(ShortCutData shortcut, boolean eventShortcut)
	{
		if(eventShortcut)
			_customShortcuts.add(shortcut);
		
		if(_owner != null)
		{
			ShortCut sh = new ShortCut(shortcut.getSlot(), shortcut.getPage(), shortcut.getType(), shortcut.getId(), shortcut.getLevel(), shortcut.getCharacterType());
			
			_owner.sendPacket(new ShortCutRegister(_owner, sh));
			_owner.registerShortCut(sh, !eventShortcut);
		}
	}
	
	/**
	 * @param shortcut - the shortcut you wanna unregister
	 */
	@Override
	public void removeShortCut(ShortCutData shortcut, boolean eventShortcut)
	{
		if(eventShortcut && _customShortcuts.contains(shortcut))
			_customShortcuts.remove(shortcut);
		
		if(_owner != null)
			_owner.deleteShortCut(shortcut.getSlot(), shortcut.getPage(), !eventShortcut);
	}
	
	@Override
	public ShortCutData createItemShortcut(int slotId, int pageId, ItemData item)
	{
		ShortCutData shortcut = new ShortCutData(slotId, pageId, Values.getInstance().TYPE_ITEM(), item.getObjectId(), 0, 1);
		return shortcut;
	}
	
	@Override
	public ShortCutData createSkillShortcut(int slotId, int pageId, SkillData skill)
	{
		ShortCutData shortcut = new ShortCutData(slotId, pageId, Values.getInstance().TYPE_SKILL(), skill.getId(), skill.getLevel(), 1);
		return shortcut;
	}
	
	@Override
	public ShortCutData createActionShortcut(int slotId, int pageId, int actionId)
	{
		ShortCutData shortcut = new ShortCutData(slotId, pageId, Values.getInstance().TYPE_ACTION(), actionId, 0, 1);
		return shortcut;
	}
	
	// =========================================================
	// ======== Player GET METHODS =======================
	// =========================================================
	
	public Player getOwner()
	{
		return _owner;
	}
	
	@Override
	public boolean isOnline()
	{
		return isOnline(false);
	}
	
	@Override
	public boolean isOnline(boolean strict)
	{
		if(strict)
			return _owner != null && _owner.isOnline();
		else
			return _owner != null;
	}
	
	@Override
	public boolean isDead()
	{
		return _owner.isDead();
	}
	
	@Override
	public boolean isVisible()
	{
		return _owner.isVisible();
	}
	
	@Override
	public void doRevive()
	{
		_owner.doRevive();
	}
	
	@Override
	public CharacterData getTarget()
	{
		if(_owner.getTarget() == null || !(_owner.getTarget() instanceof Creature))
			return null;
		else
			return new CharacterData((Creature) _owner.getTarget());
	}
	
	@Override
	public String getPlayersName()
	{
		if(_owner != null)
			return _owner.getName();
		else
			return _tempName;
	}
	
	@Override
	public int getLevel()
	{
		if(_owner != null)
			return _owner.getLevel();
		return _tempLevel;
	}
	
	@Override
	public int getPvpKills()
	{
		return _owner.getPvpKills();
	}
	
	@Override
	public int getPkKills()
	{
		return _owner.getPkKills();
	}
	
	@Override
	public int getMaxHp() { return _owner.getMaxHp(); }
	@Override
	public int getMaxCp() { return _owner.getMaxCp(); }
	@Override
	public int getMaxMp() { return _owner.getMaxMp(); }
	
	@Override
	public void setCurrentHp(int hp) { _owner.setCurrentHp(hp, false); }
	@Override
	public void setCurrentCp(int cp) { _owner.setCurrentCp(cp); }
	@Override
	public void setCurrentMp(int mp) { _owner.setCurrentMp(mp); }
	
	@Override
	public double getCurrentHp() { return _owner.getCurrentHp(); }
	@Override
	public double getCurrentCp() { return _owner.getCurrentCp(); }
	@Override
	public double getCurrentMp() { return _owner.getCurrentMp(); }
	
	@Override
	public void healPet()
	{
		if(_owner != null && _owner.getPet() != null)
		{
			_owner.getPet().setCurrentHp(_owner.getPet().getMaxHp(), false);
			_owner.getPet().setCurrentMp(_owner.getPet().getMaxMp());
			//_owner.getPet().setCurrentCp(_owner.getPet().getMaxCp());
		}
	}
	
	@Override
	public void setTitle(String title, boolean updateVisible)
	{
		_owner.setTitle(title);
		if(updateVisible)
			_owner.setTitle(_owner.getTitle());
	}
	
	/** returns true if player is not mage class */
	@Override
	public boolean isMageClass()
	{
		return _owner.isMageClass();
	}
	
	@Override
	public int getClassIndex()
	{
		if(_owner != null)
			return _owner.getBaseClassId();
		return 0;
	}
	
	@Override
	public int getActiveClass()
	{
		if(_owner != null)
			return _owner.getClassId().getId();
		return 0;
	}
	
	@Override
	public String getClassName()
	{
		return _owner.getClassId().name();
	}
	
	@Override
	public PartyData getParty()
	{
		if(_owner.getParty() == null)
			return null;
		return new PartyData(_owner.getParty());
	}
	
	@Override
	public boolean isFighter()
	{
		return PlayerClass.values()[_owner.getClassId().getId()].isOfType(ClassType.Fighter);
	}
	
	/** returns true if player is of Priest class type (not nuker) */
	@Override
	public boolean isPriest()
	{
		return PlayerClass.values()[_owner.getClassId().getId()].isOfType(ClassType.Priest);
	}
	
	/** returns true if player is of Mystic (nuke) class type, not healer of buffer */
	@Override
	public boolean isMystic()
	{
		return PlayerClass.values()[_owner.getClassId().getId()].isOfType(ClassType.Mystic);
	}
	
	@Override
	public l2r.gameserver.nexus_engine.l2r.ClassType getClassType()
	{
		if(isFighter()) return l2r.gameserver.nexus_engine.l2r.ClassType.Fighter;
		else if(isMystic()) return l2r.gameserver.nexus_engine.l2r.ClassType.Mystic;
		//TODO: check why this is disabled.
		else /*if(isPriest())*/ return l2r.gameserver.nexus_engine.l2r.ClassType.Priest;
	}
	
	@Override
	public int getX()
	{
		return _owner.getX();
	}
	
	@Override
	public int getY()
	{
		return _owner.getY();
	}
	
	@Override
	public int getZ()
	{
		return _owner.getZ();
	}
	
	@Override
	public int getHeading()
	{
		return _owner.getHeading();
	}
	
	@Override
	public int getReflectionId()
	{
		return _owner.getReflectionId();
	}
	
	@Override
	public Reflection getReflection()
	{
		return _owner.getReflection();
	}
	
	@Override
	public int getClanId()
	{
		return _owner.getClanId();
	}
	
	@Override
	public boolean isGM()
	{
		return _owner.isGM();
	}
	
	@Override
	public String getIp()
	{
		if (_owner.getClient() == null)
			return null;
		
		return _owner.getIP();
	}
	
	@Override
	public String getHWID()
	{
		if (_owner.hasHWID())
			return null;
		
		return _owner.getHWID();
	}
	
	@Override
	public boolean getIpDualboxAllowed()
	{
		if (_owner.getClient() == null)
			return false;
		
		return _owner.getClient().getAccountData().accessLevel >= 1;
	}
	
	@Override
	public boolean getHWIDDualboxAllowed()
	{
		if (_owner.getClient() == null)
			return false;
		
		return _owner.getClient().getAccountData().accessLevel >= 2;
	}
	
	@Override
	public boolean isInJail() { return _owner.isInJail(); };
	@Override
	public boolean isInSiege() { return _owner.isInSiege(); };
	@Override
	public boolean isInDuel() { return _owner.isInDuel(); };
	@Override
	public boolean isInOlympiadMode() { return _owner.isInOlympiadMode(); };
	@Override
	public int getKarma() { return _owner.getKarma(); };
	@Override
	public boolean isCursedWeaponEquipped() { return _owner.isCursedWeaponEquipped(); };
	@Override
	public boolean isBlocked() { return _owner.isBlocked(); };
	@Override
	public boolean isParalyzed() { return _owner.isParalyzed(); };
	@Override
	public boolean isAfraid() { return _owner.isAfraid(); };
	
	@Override
	public boolean isOlympiadRegistered()
	{
		return Olympiad.isRegistered(_owner);
	}
	
	@Override
	public void sitDown()
	{
		if(_owner == null)
			return;
		
		_owner.sitDown(null);
		_owner.getEventStatus().eventSitForced = true;
	}
	
	@Override
	public void standUp()
	{
		if(_owner == null)
			return;
		
		_owner.getEventStatus().eventSitForced = false;
		_owner.standUp();
	}
	
	@Override
	public List<SkillData> getSkills()
	{
		List<SkillData> list = new FastTable<SkillData>();
		for(Skill skill : getOwner().getAllSkills())
		{
			list.add(new SkillData(skill));
		}
		return list;
	}
	
	@Override
	public List<Integer> getSkillIds()
	{
		List<Integer> list = new FastTable<Integer>();
		for(Skill skill : getOwner().getAllSkills())
		{
			list.add(skill.getId());
		}
		return list;
	}
	
	@Override
	public double getPlanDistanceSq(int targetX, int targetY)
	{
		return _owner.getSqDistance(targetX, targetY);
	}
	
	@Override
	public double getDistanceSq(int targetX, int targetY, int targetZ)
	{
		return _owner.getDistance(targetX, targetY, targetZ);
	}
	
	// =========================================================
	// ======== EVENT RELATED GET/SET METHODS ==================
	// =========================================================

	@Override
	public boolean isRegistered()
	{
		return _isRegistered;
	}
	
	public boolean isRegisteredToPvpZone()
	{
		return _isRegisteredPvpZone;
	}
	
	@Override
	public boolean isInEvent()
	{
		return _isInEvent;
	}
	
	@Override
	public EventPlayerData getEventData()
	{
		return _eventData;
	}

	@Override
	public void setNameColor(int color)
	{
		_owner.setNameColor(color);
		_owner.broadcastUserInfo(true);
	}
	
	@Override
	public void setCanBuff(boolean canBuff)
	{
		_canBuff = canBuff;
	}

	@Override
	public boolean canBuff()
	{
		return _canBuff;
	}
	
	@Override
	public int getPlayersId()
	{
		return _playersId;
	}
	
	@Override
	public int getKills()
	{
		return _eventData instanceof PvPEventPlayerData ? ((PvPEventPlayerData)_eventData).getKills() : 0;
	}
	
	@Override
	public int getDeaths()
	{
		return _eventData instanceof PvPEventPlayerData ? ((PvPEventPlayerData)_eventData).getDeaths() : 0;
	}
	
	@Override
	public int getScore()
	{
		return _eventData.getScore();
	}
	
	@Override
	public int getStatus()
	{
		return _status;
	}
	
	@Override
	public void raiseKills(int count)
	{
		if(_eventData instanceof PvPEventPlayerData)
			((PvPEventPlayerData)_eventData).raiseKills(count);
	}
	
	@Override
	public void raiseDeaths(int count)
	{
		if(_eventData instanceof PvPEventPlayerData)
			((PvPEventPlayerData)_eventData).raiseDeaths(count);
	}
	
	@Override
	public void raiseScore(int count)
	{
		_eventData.raiseScore(count);
	}
	
	@Override
	public void setScore(int count)
	{
		_eventData.setScore(count);
	}
	
	@Override
	public void setStatus(int count)
	{
		_status = count;
	}
	
	@Override
	public void setKills(int count)
	{
		if(_eventData instanceof PvPEventPlayerData)
			((PvPEventPlayerData)_eventData).setKills(count);
	}
	
	@Override
	public void setDeaths(int count)
	{
		if(_eventData instanceof PvPEventPlayerData)
			((PvPEventPlayerData)_eventData).setDeaths(count);
	}
	
	@Override
	public boolean isInFFAEvent()
	{
		return _isInFFAEvent;
	}
	
	@Override
	public void setIsRegisteredToMiniEvent(boolean b, MiniEventManager minievent, boolean updateGearScore)
	{
		_isRegistered = b;
		_registeredMiniEvent = minievent;

		/*if(updateGearScore)
		{
			if(_isRegistered)
				_owner.getGearScore(true);
			else
				_owner.clearGearScoreData();
		}*/
	}
	
	@Override
	public MiniEventManager getRegisteredMiniEvent()
	{
		return _registeredMiniEvent;
	}
	
	@Override
	public void setIsRegisteredToMainEvent(boolean b, EventType event, boolean updateGearScore)
	{
		_isRegistered = b;
		_registeredMainEvent = event;

		/*if(updateGearScore)
		{
			if(_isRegistered)
				_owner.getGearScore(true);
			else
				_owner.clearGearScoreData();
		}*/
	}

	public boolean checkIfNearbyLastTeleport()
	{
		if(getOwner() == null) return false;

		Location currentLoc = getOwner().getLoc();
		Location supposedLoc = new Location(getLastSpawnTeleported().getLoc().getX(), getLastSpawnTeleported().getLoc().getY(), getLastSpawnTeleported().getLoc().getZ());

		double dist = Location.calculateDistance(currentLoc.getX(), currentLoc.getY(), 0, supposedLoc.getX(), supposedLoc.getY(), 0, false);

		return dist < 550;
	}

	public void setLastTeleportedSpawn(EventSpawn spawn)
	{
		_lastSpawnTeleported = spawn;
	}

	public EventSpawn getLastSpawnTeleported()
	{
		return _lastSpawnTeleported;
	}
	
	public void setIsRegisteredToPvpZone(boolean b)
	{
		_isRegistered = b;
		_isRegisteredPvpZone = b;
	}
	
	@Override
	public EventType getRegisteredMainEvent()
	{
		return _registeredMainEvent;
	}
	
	@Override
	public MiniEventGame getActiveGame()
	{
		if(_activeEvent instanceof MiniEventGame)
			return (MiniEventGame) _activeEvent;
		else
			return null;
	}
	
	@Override
	public AbstractMainEvent getActiveEvent()
	{
		if(_activeEvent instanceof AbstractMainEvent)
			return (AbstractMainEvent) _activeEvent;
		else
			return null;
	}
	
	@Override
	public EventGame getEvent()
	{
		return _activeEvent;
	}
	
	@Override
	public void setActiveGame(MiniEventGame game)
	{
		_activeEvent = game;
	}
	
	@Override
	public void setEventTeam(EventTeam team)
	{
		_eventTeam = team;
	}
	
	@Override
	public EventTeam getEventTeam()
	{
		return _eventTeam;
	}
	
	@Override
	public int getTeamId()
	{
		if(_eventTeam != null)
			return _eventTeam.getTeamId();
		else
			return -1;
	}
	
	@Override
	public Loc getOrigLoc()
	{
		return new Loc(_origLoc.getX(), _origLoc.getY(), _origLoc.getZ());
	}

	@Override
	public void setIsSpectator(boolean isSpectator)
	{
		_isSpectator = isSpectator;
	}

	@Override
	public boolean isSpectator()
	{
		return _isSpectator;
	}
	
	@Override
	public boolean isEventRooted()
	{
		return _disableAfkCheck;
	}
	
	@Override
	public boolean isTitleUpdated()
	{
		return _titleUpdate;
	}
	
	@Override
	public void setTitleUpdated(boolean b)
	{
		_titleUpdate = b;
	}
	
	@Override
	public ItemData getPaperdollItem(int slot)
	{
		return new ItemData(getOwner().getInventory().getPaperdollItem(slot));
	}
	
	@Override
	public void equipItem(ItemData item)
	{
		getOwner().getInventory().equipItem(item.getOwner());
	}
	
	public double getGearScore()
	{
		// TODO Infern0 disable it for now...
		//if(_owner != null)
		//	return _owner.getGearScore(false);
		return 0;
	}

	public void clearGearScore()
	{
		// TODO Infern0
		//_owner.clearGearScoreData();
	}
	
	@Override
	public void unEquipItemInBodySlot(int bodySlot)
	{
		getOwner().getInventory().unEquipItemInBodySlot(bodySlot);
	}
	
	@Override
	public void destroyItemByItemId(int id, int count)
	{
		getOwner().getInventory().destroyItemByItemId(id, count);
	}
	
	@Override
	public void inventoryUpdate(ItemData wpn)
	{
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(wpn.getOwner());
		getOwner().sendPacket(iu);
		getOwner().sendItemList(false); // TODO: false or true ?
		getOwner().broadcastUserInfo(true);
	}
	
	@Override
	public Radar getRadar()
	{
		return _radar;
	}
	
	@Override
	public void createRadar()
	{
		_radar = new Radar(this);
	}
	
	@Override
	public void addRadarMarker(int x, int y, int z)
	{
		if(_owner != null)
			_owner.getRadar().addMarker(x, y, z);
	}

	@Override
	public void removeRadarMarker(int x, int y, int z)
	{
		if(_owner != null)
			_owner.getRadar().removeMarker(x, y, z);
	}
	
	@Override
	public void removeRadarAllMarkers()
	{
		if(_owner != null)
			_owner.getRadar().removeAllMarkers();
	}
	
	// =========================================================
	// ======== AFK Protection =================================
	// =========================================================
	
	@Override
	public void disableAfkCheck(boolean b)
	{
		_disableAfkCheck = b;
		
		if(!b && _afkChecker != null)
			_afkChecker.check();
	}

	@Override
	public int getTotalTimeAfk()
	{
		if(_afkChecker == null)
			return 0;
		
		return Math.max(0, _afkChecker.totalTimeAfk);
	}
	
	@Override
	public boolean isAfk()
	{
		if(_afkChecker != null)
			return _afkChecker.isAfk;
		return false;
	}
	
	@Override
	public AfkChecker getAfkChecker()
	{
		return _afkChecker;
	}
	
	@Override
	public int getHealAmount()
	{
		return _healAmount;
	}
	
	@Override
	public void setHealAmount(int heal)
	{
		_healAmount = heal;
	}
	
	@Override
	public CharacterData getCharacterData()
	{
		return new CharacterData(getOwner());
	}
	
	public class Radar
	{
		private final PlayerEventInfo player;
		private ScheduledFuture<?> refresh;
		
		private boolean enabled;
		private boolean repeat = false;
		private int newX, newY, newZ;
		private int currentX, currentY, currentZ;
		
		private boolean hasRadar;
		
		public Radar(PlayerEventInfo player)
		{
			this.player = player;
			this.refresh = null;
			
			enabled = false;
			hasRadar = false;
		}
		
		public void setLoc(int x, int y, int z)
		{
			newX = x;
			newY = y;
			newZ = z;
		}
		
		public void enable()
		{
			enabled = true;
			applyRadar();
		}
		
		public void disable()
		{
			enabled = false;
			
			if(hasRadar)
			{
				player.removeRadarMarker(currentX, currentY, currentZ);
				hasRadar = false;
			}
		}
		
		public void setRepeat(boolean nextRepeatPolicy)
		{
			if(!enabled || (repeat && !nextRepeatPolicy))
			{
				// cancel repeating
				if(refresh != null)
				{
					refresh.cancel(false);
					refresh = null;
				}
			}
			else if(!repeat && nextRepeatPolicy)
			{
				if(refresh != null)
				{
					refresh.cancel(false);
					refresh = null;
				}
				
				// schedule next repeat
				refresh = CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						applyRadar();
					}
				}, 10000);
			}
			
			repeat = nextRepeatPolicy;
		}
		
		private void applyRadar()
		{
			if(enabled)
			{
				if(hasRadar)
				{
					player.removeRadarMarker(currentX, currentY, currentZ);
					hasRadar = false;
				}
			
				player.addRadarMarker(newX, newY, newZ);
				
				currentX = newX;
				currentY = newY;
				currentZ = newZ;
				
				hasRadar = true;
				
				if(repeat)
					schedule();
			}
		}
		
		private void schedule()
		{
			// schedule next repeat
			refresh = CallBack.getInstance().getOut().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					applyRadar();
				}
			}, 10000);
		}
		
		public boolean isEnabled()
		{
			return enabled;
		}
		
		public boolean isRepeating()
		{
			return repeat;
		}
	}

	public class AfkChecker implements Runnable
	{
		private final PlayerEventInfo player;
		private ScheduledFuture<?> _nextTask;
		
		private boolean isAfk;
		private int totalTimeAfk;
		private int tempTimeAfk;
		
		private boolean isWarned;
		
		public AfkChecker(PlayerEventInfo player)
		{
			this.player = player;
			
			isWarned = false;
			isAfk = false;
			
			totalTimeAfk = 0;
			tempTimeAfk = 0;
			
			check();
		}
		
		public void onAction()
		{
			if(!isInEvent())
				return;
			
			if(_nextTask != null)
				_nextTask.cancel(false);
			
			tempTimeAfk = 0;
			isWarned = false;
			
			if(isAfk)
			{
				_owner.sendMessage("Welcome back. Total time spent AFK so far: " + Util.formatTime(totalTimeAfk));
				isAfk = false;
				
				if(_activeEvent != null)
					_activeEvent.playerReturnedFromAfk(player);
			}
			
			check();
		}
		
		@Override
		public synchronized void run()
		{
			if(!isInEvent())
				return;
			
			if(isWarned) // a warning has already been sent to this player
			{
				if(!_disableAfkCheck && !_owner.isDead())
				{
					if(isAfk)
					{
						totalTimeAfk += 10;
						tempTimeAfk += 10;
					}
					else 
						isAfk = true;
					
					if(_activeEvent != null)
						_activeEvent.playerWentAfk(player, false, tempTimeAfk);
				}
				
				check(10000);
			}
			else // send a warning
			{
				if(!_disableAfkCheck && !_owner.isDead())
				{
					isWarned = true;
					
					if(getActiveGame() != null)
						getActiveGame().playerWentAfk(player, true, 0);
					
					if(getActiveEvent() != null)
						getActiveEvent().playerWentAfk(player, true, 0);
				}
				
				check();
			}
		}
		
		private synchronized void check()
		{
			if(_disableAfkCheck)
				return;
			
			if(_nextTask != null)
				_nextTask.cancel(false);
			
			_nextTask = ThreadPoolManager.getInstance().schedule(this, isWarned ? AFK_KICK_DELAY : AFK_WARNING_DELAY);
		}
		
		private synchronized void check(long delay)
		{
			if(_disableAfkCheck)
				return;
			
			if(_nextTask != null)
				_nextTask.cancel(false);
			
			if(isAfk)
				_nextTask = ThreadPoolManager.getInstance().schedule(this, delay);
		}
		
		// called on the end of event
		public void stop()
		{
			if(_nextTask != null)
				_nextTask.cancel(false);
			
			_nextTask = null;
			
			isAfk = false;
			isWarned = false;
			
			totalTimeAfk = 0;
			tempTimeAfk = 0;
		}
	}
}
