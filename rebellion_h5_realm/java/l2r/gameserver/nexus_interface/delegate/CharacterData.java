package l2r.gameserver.nexus_interface.delegate;


import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.instances.DoorInstance;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.nexus_engine.l2r.delegate.ICharacterData;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.utils.Location;

/**
 * @author hNoke
 *
 */
public class CharacterData extends ObjectData implements ICharacterData
{
	protected Creature _owner;
	
	public CharacterData(Creature cha)
	{
		super(cha);
		_owner = cha;
	}
	
	@Override
	public Creature getOwner()
	{
		return _owner;
	}
	
	@Override
	public double getPlanDistanceSq(int targetX, int targetY)
	{
		return _owner.getDistance(targetX, targetY);
	}
	
	@Override
	public Location getLoc()
	{
		return new Location(_owner.getX(), _owner.getY(), _owner.getZ(), _owner.getHeading());
	}
	
	@Override
	public int getObjectId()
	{
		return _owner.getObjectId();
	}
	
	@Override
	public boolean isDoor()
	{
		return _owner instanceof DoorInstance;
	}
	
	@Override
	public DoorData getDoorData()
	{
		return isDoor() ? new DoorData((DoorInstance) _owner) : null;
	}
	
	@Override
	public void startAbnormalEffect(AbnormalEffect ae)
	{
		_owner.startAbnormalEffect(ae);
	}
	
	@Override
	public void stopAbnormalEffect(AbnormalEffect ae)
	{
		_owner.stopAbnormalEffect(ae);
	}
	
	/** returns null if _owner is NOT L2Playable */
	@Override
	public PlayerEventInfo getEventInfo()
	{
		if(_owner != null && _owner.isPlayable())
			return ((Playable)_owner).getPlayer().getEventInfo();
		return null;
	}
	
	@Override
	public String getName()
	{
		return _owner.getName();
	}
	
	@Override
	public void creatureSay(int channel, String charName, String text)
	{
		_owner.broadcastPacket(new CreatureSay(_owner.getObjectId(), channel, charName, text));
	}
	
	@Override
	public void doDie(CharacterData killer)
	{
		_owner.reduceCurrentHp(_owner.getCurrentHp()*2, killer.getOwner(), null, true, true, true, false, false, false, true);
	}
	
	@Override
	public boolean isDead()
	{
		return _owner.isDead();
	}

}
