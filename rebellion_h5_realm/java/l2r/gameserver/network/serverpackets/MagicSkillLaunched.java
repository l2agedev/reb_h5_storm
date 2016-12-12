package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Creature;

import java.util.Collection;


public class MagicSkillLaunched extends L2GameServerPacket
{
	private final int _casterId;
	private final int _skillId;
	private final int _skillLevel;
	private final Creature[] _targets;

	public MagicSkillLaunched(int casterId, int skillId, int skillLevel, Creature ... targets)
	{
		_casterId = casterId;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_targets = targets;
	}

	public MagicSkillLaunched(int casterId, int skillId, int skillLevel, Collection<Creature> targets)
	{
		_casterId = casterId;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_targets = targets.toArray(new Creature[0]);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x54);
		writeD(_casterId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_targets.length);
		if (_targets != null && _targets.length != 0)
		{
			for (Creature target : _targets)
			{
				if (target != null)
					writeD(target.getObjectId());
			}
		}
	}
}