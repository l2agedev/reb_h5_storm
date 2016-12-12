package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill.SkillType;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.skills.EffectType;

public class RequestDispel extends L2GameClientPacket
{
	private int _objectId, _id, _level;

	@Override
	protected void readImpl() throws Exception
	{
		_objectId = readD();
		_id = readD();
		_level = readD();
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.getObjectId() != _objectId && activeChar.getPet() == null)
			return;

		Creature target = activeChar;
		if(activeChar.getObjectId() != _objectId)
			target = activeChar.getPet();
		
		boolean canOverride = activeChar.canOverrideCond(PcCondOverride.SKILL_CONDITIONS);

		for(Effect e : target.getEffectList().getAllEffects())
			if(e.getDisplayId() == _id && e.getDisplayLevel() == _level)
				if(canOverride || (!e.isOffensive() && (!e.getSkill().isMusic() || Config.ALT_DISPEL_MUSIC) && e.getSkill().isSelfDispellable() && e.getSkill().getSkillType() != SkillType.TRANSFORMATION && e.getTemplate().getEffectType() != EffectType.Hourglass))
					e.exit();
				else
					return;
	}
}