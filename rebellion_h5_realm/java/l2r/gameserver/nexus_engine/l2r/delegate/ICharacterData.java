package l2r.gameserver.nexus_engine.l2r.delegate;

import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.CharacterData;
import l2r.gameserver.nexus_interface.delegate.DoorData;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.utils.Location;

/**
 * @author hNoke
 *
 */
public interface ICharacterData
{
	public String getName();
	public int getObjectId();
	
	public Location getLoc();
	
	public double getPlanDistanceSq(int targetX, int targetY);
	
	public boolean isDoor();
	public DoorData getDoorData();
	
	public void startAbnormalEffect(AbnormalEffect mask);
	public void stopAbnormalEffect(AbnormalEffect mask);
	
	public void creatureSay(int channel, String charName, String text);
	public void doDie(CharacterData killer);
	public boolean isDead();
	
	/** returns null if _owner is NOT L2Playable */
	public PlayerEventInfo getEventInfo();
}
