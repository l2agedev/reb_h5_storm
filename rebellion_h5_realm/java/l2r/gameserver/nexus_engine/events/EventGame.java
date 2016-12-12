package l2r.gameserver.nexus_engine.events;

import l2r.gameserver.nexus_engine.events.engine.EventManager.DisconnectedPlayerData;
import l2r.gameserver.nexus_engine.events.engine.base.EventPlayerData;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.CharacterData;
import l2r.gameserver.nexus_interface.delegate.ItemData;
import l2r.gameserver.nexus_interface.delegate.NpcData;
import l2r.gameserver.nexus_interface.delegate.SkillData;

/**
 * @author hNoke
 * a runnable event match (MiniEventGame, AbstractMainEvent)
 */
public interface EventGame
{
	public EventPlayerData createPlayerData(PlayerEventInfo player);
	public EventPlayerData getPlayerData(PlayerEventInfo player);
	
	public void clearEvent();
	
	public boolean canAttack(PlayerEventInfo player, CharacterData target);
	public boolean onAttack(CharacterData cha, CharacterData target);
	public boolean canSupport(PlayerEventInfo player, CharacterData target);
	public void onKill(PlayerEventInfo player, CharacterData target);
	public void onDie(PlayerEventInfo player, CharacterData killer);
	public void onDamageGive(CharacterData cha, CharacterData target, int damage, boolean isDOT);
	public void onDisconnect(PlayerEventInfo player);
	public boolean addDisconnectedPlayer(PlayerEventInfo player, DisconnectedPlayerData data);
	public boolean onSay(PlayerEventInfo player, String text, int channel);
	public boolean onNpcAction(PlayerEventInfo player, NpcData npc);
	
	public boolean canUseItem(PlayerEventInfo player, ItemData item);
	public void onItemUse(PlayerEventInfo player, ItemData item);
	
	public boolean canUseSkill(PlayerEventInfo player, SkillData skill);
	public void onSkillUse(PlayerEventInfo player, SkillData skill);
	
	public boolean canDestroyItem(PlayerEventInfo player, ItemData item);
	public boolean canInviteToParty(PlayerEventInfo player, PlayerEventInfo target);
	public boolean canTransform(PlayerEventInfo player);
	public boolean canBeDisarmed(PlayerEventInfo player);
	public int allowTransformationSkill(PlayerEventInfo playerEventInfo, SkillData skillData);
	public boolean canSaveShortcuts(PlayerEventInfo player);
	
	public int isSkillOffensive(SkillData skill);
	public boolean isSkillNeutral(SkillData skill);
	
	public void playerWentAfk(PlayerEventInfo player, boolean warningOnly, int afkTime);
	public void playerReturnedFromAfk(PlayerEventInfo player);

	public double getMaxGearScore();
}
