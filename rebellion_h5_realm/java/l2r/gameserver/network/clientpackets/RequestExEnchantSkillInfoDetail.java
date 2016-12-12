package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.EnchantSkillLearn;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.tables.SkillTreeTable;

public final class RequestExEnchantSkillInfoDetail extends L2GameClientPacket
{
	private static final int TYPE_NORMAL_ENCHANT = 0;
	private static final int TYPE_SAFE_ENCHANT = 1;
	private static final int TYPE_UNTRAIN_ENCHANT = 2;
	private static final int TYPE_CHANGE_ENCHANT = 3;

	private int _type;
	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
		_type = readD();
		_skillId = readD();
		_skillLvl = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if (Config.SECURITY_ENABLED && Config.SECURITY_ENCHANT_SKILL_ENABLED && activeChar.getSecurity())
		{
			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			return;
		}
		
		if (activeChar.isBusy())
			return;
		
		if (activeChar.getTransformation() != 0 || activeChar.isMounted() || Olympiad.isRegisteredInComp(activeChar) || activeChar.isInCombat())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExEnchantSkillInfoDetail.message1", activeChar));
			return;
		}

		if(activeChar.getLevel() < 76 || activeChar.getClassId().getLevel() < 4)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExEnchantSkillInfoDetail.message2", activeChar));
			return;
		}

		int bookId = 0;
		int sp = 0;
		int adenaCount = 0;
		double spMult = SkillTreeTable.NORMAL_ENCHANT_COST_MULTIPLIER;

		EnchantSkillLearn esd = null;

		switch(_type)
		{
			case TYPE_NORMAL_ENCHANT:
				if(_skillLvl % 100 == 1)
					bookId = SkillTreeTable.NORMAL_ENCHANT_BOOK;
				esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
				break;
			case TYPE_SAFE_ENCHANT:
				bookId = SkillTreeTable.SAFE_ENCHANT_BOOK;
				esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
				spMult = SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
				break;
			case TYPE_UNTRAIN_ENCHANT:
				bookId = SkillTreeTable.UNTRAIN_ENCHANT_BOOK;
				esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl + 1);
				break;
			case TYPE_CHANGE_ENCHANT:
				bookId = SkillTreeTable.CHANGE_ENCHANT_BOOK;
				esd = SkillTreeTable.getEnchantsForChange(_skillId, _skillLvl).get(0);
				spMult = 1f / SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
				break;
		}

		if(esd == null)
			return;

		spMult *= esd.getCostMult();
		int[] cost = esd.getCost();

		sp = (int) (cost[1] * spMult);

		if(_type != TYPE_UNTRAIN_ENCHANT)
			adenaCount = (int) (cost[0] * spMult);

		// send skill enchantment detail
		activeChar.sendPacket(new ExEnchantSkillInfoDetail(_skillId, _skillLvl, sp, esd.getRate(activeChar), bookId, adenaCount));
	}
}