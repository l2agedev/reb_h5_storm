package l2r.gameserver.nexus_engine.l2r;

import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.nexus_engine.events.EventGame;
import l2r.gameserver.nexus_engine.events.engine.base.EventPlayerData;
import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.base.Loc;
import l2r.gameserver.nexus_engine.events.engine.main.events.AbstractMainEvent;
import l2r.gameserver.nexus_engine.events.engine.mini.MiniEventGame;
import l2r.gameserver.nexus_engine.events.engine.mini.MiniEventManager;
import l2r.gameserver.nexus_engine.events.engine.team.EventTeam;
import l2r.gameserver.nexus_interface.PlayerEventInfo.AfkChecker;
import l2r.gameserver.nexus_interface.PlayerEventInfo.Radar;
import l2r.gameserver.nexus_interface.delegate.CharacterData;
import l2r.gameserver.nexus_interface.delegate.ItemData;
import l2r.gameserver.nexus_interface.delegate.PartyData;
import l2r.gameserver.nexus_interface.delegate.ShortCutData;
import l2r.gameserver.nexus_interface.delegate.SkillData;
import l2r.gameserver.skills.AbnormalEffect;

import java.util.List;

/**
 * @author hNoke
 *
 */
public interface IPlayerEventInfo
{
	public void initOrigInfo();
	
	public void restoreData();
	
	public void onEventStart(EventGame event);
	
	public void clean();
	
	public void teleport(Loc loc, int delay, int instanceId);
	
	public void teleToLocation(Loc loc);
	
	public void teleToLocation(int x, int y, int z);
	
	public void teleToLocation(int x, int y, int z, int heading);
	
	public void setXYZInvisible(int x, int y, int z);
	
	public void setFame(int count);
	
	public int getFame();
	
	// =========================================================
	// ======== L2PcInstance ACTIONS ===========================
	// =========================================================
	
	/*public void notifyKill(L2Character target);
	
	public void notifyDie(L2Character killer);
	
	public void notifyDisconnect();
	
	public boolean canAttack(L2Character target);
	
	public boolean canSupport(L2Character target);
	
	public void onAction();
	
	public void onDamageGive(L2Character target, int ammount, boolean isDOT);
	
	/** returning false will make the text not shown */
	/*public boolean notifySay(String text, int channel);*/
	
	/** false means that a html page has been already sent by Nexus engine */
	/*public boolean notifyNpcAction(L2Npc npc);
	
	public boolean canUseItem(L2ItemInstance item);
	
	public void notifyItemUse(L2ItemInstance item);
	
	public boolean canUseSkill(L2Skill skill);
	
	public void onUseSkill(L2Skill skill);
	
	public boolean canShowToVillageWindow();
	
	public boolean canDestroyItem(L2ItemInstance item);*/
	
	// =========================================================
	// ======== L2PcInstance GENERAL METHODS ===================
	// =========================================================
	
	public void setInstanceId(int id);
	
	public void sendPacket(String html);
	
	public void screenMessage(String message, String name, boolean special);
	
	public void creatureSay(String message, String announcer, int channel);
	
	public void sendMessage(String message);
	
	public void sendEventScoreBar(String text);
	
	public void broadcastUserInfo();
	
	public void broadcastTitleInfo();
	
	public void sendSkillList();
	
	public void transform(int transformId);
	
	public boolean isTransformed();
	
	public void untransform(boolean removeEffects);
	
	public ItemData addItem(int id, int ammount, boolean msg);
	
	public void addExpAndSp(long exp, int sp);
	
	public void doDie();
	
	public void doDie(CharacterData killer);
	
	public ItemData[] getItems();
	
	public void getSkillEffects(int skillId, int level);
	
	public void getPetSkillEffects(int skillId, int level);
	
	public void addSkill(SkillData skill, boolean store);
	
	public void removeSkillById(Integer id);
	
	public void removeCubics();
	
	public void removeSummon();
	
	public boolean hasPet();
	
	public void removeBuffs();
	
	public void removeBuffsFromPet();
	
	public void removeBuff(int id);
	
	public int getBuffsCount();
	
	public int getDancesCount();
	
	public int getMaxBuffCount();
	
	public int getMaxDanceCount();
	
	public int getPetBuffCount();
	
	public int getPetDanceCount();
	
	public void abortCasting();
	
	public void playSound(String file);
	
	//public void setVisible();
	
	public void rebuffPlayer();
	
	public void enableAllSkills();
	
	public void sendSetupGauge(int time);
	
	public void block();
	
	public void unblock();
	
	public void paralizeEffect(boolean b);
	
	public void setIsParalyzed(boolean b);
	
	public void setIsInvul(boolean b);
	
	public void setCanInviteToParty(boolean b);
	
	public boolean canInviteToParty();
	
	public void showEventEscapeEffect();
	
	/** 
	 * @param owner - null to put there this PlayerEventInfo
	 * @param target - null to put there this PlayerEventInfo
	 * @param skillId
	 * @param level
	 */
	public void broadcastSkillUse(CharacterData owner, CharacterData target, int skillId, int level);
	
	public void broadcastSkillLaunched(CharacterData owner, CharacterData target, int skillId, int level);
	
	public void enterObserverMode(int x, int y, int z);
	
	public void removeObserveMode();
	
	public void sendStaticPacket();
	
	public void sendHtmlText(String text);
	
	public void sendHtmlPage(String path);
	
	public void startAbnormalEffect(AbnormalEffect ae);
	
	public void stopAbnormalEffect(AbnormalEffect ae);
	
	public void startAntifeedProtection(boolean broadcast);
	
	public void stopAntifeedProtection(boolean broadcast);
	
	public boolean hasAntifeedProtection();
	
	public void removeOriginalShortcuts();
	
	public void restoreOriginalShortcuts();
	
	public void removeCustomShortcuts();
	
	public void registerShortcut(ShortCutData shortcut, boolean eventShortcut);
	
	public void removeShortCut(ShortCutData shortcut, boolean eventShortcut);
	
	public ShortCutData createItemShortcut(int slotId, int pageId, ItemData item);
	
	public ShortCutData createSkillShortcut(int slotId, int pageId, SkillData skill);
	
	public ShortCutData createActionShortcut(int slotId, int pageId, int actionId);
	
	// =========================================================
	// ======== L2PcInstance GET METHODS =======================
	// =========================================================
	
	public boolean isOnline();
	
	public boolean isOnline(boolean strict);
	
	public boolean isDead();
	
	public boolean isVisible();
	
	public void doRevive();
	
	public CharacterData getTarget();
	
	public String getPlayersName();
	
	public int getLevel();
	
	public int getPvpKills();
	
	public int getPkKills();
	
	public int getMaxHp();
	public int getMaxCp();
	public int getMaxMp();
	
	public void setCurrentHp(int hp);
	public void setCurrentCp(int cp);
	public void setCurrentMp(int mp);
	
	public double getCurrentHp();
	public double getCurrentCp();
	public double getCurrentMp();
	
	public void healPet();
	
	public void setTitle(String title, boolean updateVisible);
	
	/** returns true if player is not mage class */
	public boolean isMageClass();
	
	public int getClassIndex();
	
	public int getActiveClass();
	
	public String getClassName();
	
	public PartyData getParty();
	
	public boolean isFighter();
	
	/** returns true if player is of Priest class type (not nuker) */
	public boolean isPriest();
	
	/** returns true if player is of Mystic (nuke) class type, not healer of buffer */
	public boolean isMystic();
	
	public ClassType getClassType();
	
	public int getX();
	
	public int getY();
	
	public int getZ();
	
	public int getHeading();
	
	public int getReflectionId();
	
	public Reflection getReflection();
	
	public int getClanId();
	
	public boolean isGM();
	
	public String getIp();
	public String getHWID();
	public boolean getIpDualboxAllowed();
	public boolean getHWIDDualboxAllowed();
	
	public boolean isInJail();
	public boolean isInSiege();
	public boolean isInDuel();
	public boolean isInOlympiadMode();
	public int getKarma();
	public boolean isCursedWeaponEquipped();
	public boolean isBlocked();
	public boolean isParalyzed();
	public boolean isAfraid();
	public boolean isOlympiadRegistered();
	
	public void sitDown();
	
	public void standUp();
	
	public List<SkillData> getSkills();
	
	public List<Integer> getSkillIds();
	
	public double getPlanDistanceSq(int targetX, int targetY);
	
	public double getDistanceSq(int targetX, int targetY, int targetZ);
	
	// =========================================================
	// ======== EVENT RELATED GET/SET METHODS ==================
	// =========================================================

	public boolean isRegistered();
	
	public boolean isInEvent();
	
	public EventPlayerData getEventData();

	public void setNameColor(int color);
	
	public void setCanBuff(boolean canBuff);

	public boolean canBuff();
	
	public int getPlayersId();
	
	public int getKills();
	
	public int getDeaths();
	
	public int getScore();
	
	public int getStatus();
	
	public void raiseKills(int count);
	
	public void raiseDeaths(int count);
	
	public void raiseScore(int count);
	
	public void setScore(int count);
	
	public void setStatus(int count);
	
	public void setKills(int count);
	
	public void setDeaths(int count);
	
	public boolean isInFFAEvent();
	
	public void setIsRegisteredToMiniEvent(boolean b, MiniEventManager minievent, boolean updateGearScore);
	
	public MiniEventManager getRegisteredMiniEvent();
	
	public void setIsRegisteredToMainEvent(boolean b, EventType event, boolean updateGearScore);
	
	public EventType getRegisteredMainEvent();
	
	public MiniEventGame getActiveGame();
	
	public AbstractMainEvent getActiveEvent();
	
	public EventGame getEvent();
	
	public void setActiveGame(MiniEventGame game);
	
	public void setEventTeam(EventTeam team);
	
	public EventTeam getEventTeam();
	
	public int getTeamId();
	
	public Loc getOrigLoc();

	public void setIsSpectator(boolean _isSpectator);

	public boolean isSpectator();
	
	public boolean isEventRooted();
	
	public boolean isTitleUpdated();
	
	public void setTitleUpdated(boolean b);
	
	public ItemData getPaperdollItem(int slot);
	
	public void equipItem(ItemData item);
	
	public void unEquipItemInBodySlot(int slot);
	
	public void destroyItemByItemId(int id, int count);
	
	public void inventoryUpdate(ItemData item);
	
	public void addRadarMarker(int x, int y, int z);

	public void removeRadarMarker(int x, int y, int z);
	
	public void removeRadarAllMarkers();
	
	public void createRadar();
	
	public Radar getRadar();
	
	// =========================================================
	// ======== AFK Protection =================================
	// =========================================================
	
	public void disableAfkCheck(boolean b);

	public int getTotalTimeAfk();
	
	public boolean isAfk();
	
	public AfkChecker getAfkChecker();
	
	public CharacterData getCharacterData();
	
	public int getHealAmount();
	
	public void setHealAmount(int heal);
}
