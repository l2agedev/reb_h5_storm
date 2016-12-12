package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.base.EnchantSkillLearn;
import l2r.gameserver.network.serverpackets.ExEnchantSkillInfo;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.tables.SkillTreeTable;

public class RequestExEnchantSkillInfo extends L2GameClientPacket
{
	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_skillLvl > 100)
		{
			EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
			if(sl == null)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExEnchantSkillInfo.message1", activeChar));
				return;
			}

			Skill skill = SkillTable.getInstance().getInfo(_skillId, SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), _skillLvl, sl.getMaxLevel()));

			if(skill == null || skill.getId() != _skillId)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExEnchantSkillInfo.message2", activeChar));
				return;
			}

			if(activeChar.getSkillLevel(_skillId) != skill.getLevel())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExEnchantSkillInfo.message3", activeChar));
				return;
			}
		}
		else if(activeChar.getSkillLevel(_skillId) != _skillLvl)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExEnchantSkillInfo.message3", activeChar));
			return;
		}

		sendPacket(new ExEnchantSkillInfo(_skillId, _skillLvl));
	}
}