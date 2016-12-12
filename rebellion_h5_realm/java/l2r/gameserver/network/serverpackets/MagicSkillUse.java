package l2r.gameserver.network.serverpackets;

import l2r.commons.geometry.Point3D;
import l2r.gameserver.model.Creature;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Format:   dddddddddh [h] h [ddd]
 * Пример пакета:
 * 48
 * 86 99 00 4F  86 99 00 4F
 * EF 08 00 00  01 00 00 00
 * 00 00 00 00  00 00 00 00
 * F9 B5 FF FF  7D E0 01 00  68 F3 FF FF
 * 00 00 00 00
 */
public class MagicSkillUse extends L2GameServerPacket
{
	private int _targetId;
	private int _skillId;
	private int _skillLevel;
	private int _hitTime;
	private int _reuseDelay;
	private int _chaId;
	private Point3D _castLoc;
	private Point3D _targetLoc; // Gets the client object of targetObjId and puts it in this loc.
	private final List<Integer> _unknown = Collections.<Integer>emptyList();
	private List<Point3D> _groundLocations = Collections.<Point3D>emptyList(); // This should be used for skills such as Volcano, Cyclone, Raging Waves, Gehenna, etc.

	public MagicSkillUse(Creature cha, Creature target, int skillId, int skillLevel, int hitTime, long reuseDelay)
	{
		_chaId = cha.getObjectId();
		_targetId = target.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = (int)reuseDelay;
		_castLoc = cha.getLoc();
		_targetLoc = target.getLoc();
		if (cha.isPlayer())
			_groundLocations = cha.getPlayer().getGroundSkillLoc() != null ? Arrays.<Point3D>asList(cha.getPlayer().getGroundSkillLoc()) : Collections.<Point3D> emptyList();
	}

	public MagicSkillUse(Creature cha, int skillId, int skillLevel, int hitTime, long reuseDelay)
	{
		_chaId = cha.getObjectId();
		_targetId = cha.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = (int)reuseDelay;
		_castLoc = _targetLoc = cha.getLoc();
		if (cha.isPlayer())
			_groundLocations = cha.getPlayer().getGroundSkillLoc() != null ? Arrays.<Point3D>asList(cha.getPlayer().getGroundSkillLoc()) : Collections.<Point3D> emptyList();
	}

	public MagicSkillUse(int x, int y, int z, Creature owner, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		_chaId = owner.getObjectId();
		_targetId = owner.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		_castLoc = _targetLoc = new Point3D(x, y, z);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x48);
		writeD(_chaId);
		writeD(_targetId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_hitTime);
		writeD(_reuseDelay);
		writeD(_castLoc.x);
		writeD(_castLoc.y);
		writeD(_castLoc.z);
		writeH(_unknown.size()); // TODO: Implement me!
		for (int unknown : _unknown)
		{
			writeH(unknown);
		}
		writeH(_groundLocations.size());
		for (Point3D point : _groundLocations)
		{
			writeD(point.x);
			writeD(point.y);
			writeD(point.z);
		}
		writeD(_targetLoc.x);
		writeD(_targetLoc.y);
		writeD(_targetLoc.z);
	}
}