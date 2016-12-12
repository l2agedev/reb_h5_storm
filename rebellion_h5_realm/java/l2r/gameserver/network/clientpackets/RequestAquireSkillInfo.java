package l2r.gameserver.network.clientpackets;

import l2r.commons.lang.ArrayUtils;
import l2r.gameserver.data.xml.holder.SkillAcquireHolder;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SkillLearn;
import l2r.gameserver.model.base.AcquireType;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.AcquireSkillInfo;
import l2r.gameserver.tables.SkillTable;

/**
 * Reworked: VISTALL
 */
public class RequestAquireSkillInfo extends L2GameClientPacket
{
	private int _id;
	private int _level;
	private AcquireType _type;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_type = ArrayUtils.valid(AcquireType.VALUES, readD());
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || player.getTransformation() != 0 || SkillTable.getInstance().getInfo(_id, _level) == null || _type == null)
			return;

		NpcInstance trainer = player.getLastNpc();
		if((trainer == null || player.getDistance(trainer.getX(), trainer.getY()) > Creature.INTERACTION_DISTANCE) && !player.canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
			return;

		SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, _id, _level, _type);
		if(skillLearn == null)
			return;

		sendPacket(new AcquireSkillInfo(_type, skillLearn));
	}
}