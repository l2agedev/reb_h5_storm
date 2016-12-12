package l2r.gameserver.network.clientpackets;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.actor.instances.player.ShortCut;
import l2r.gameserver.model.base.EnchantSkillLearn;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.network.serverpackets.ExEnchantSkillInfo;
import l2r.gameserver.network.serverpackets.ExEnchantSkillResult;
import l2r.gameserver.network.serverpackets.ShortCutRegister;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.tables.SkillTreeTable;
import l2r.gameserver.utils.Log;

public class RequestExEnchantSkill extends L2GameClientPacket
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

		if (Config.SECURITY_ENABLED && Config.SECURITY_ENCHANT_SKILL_ENABLED && activeChar.getSecurity())
		{
			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
				return;
		}
		
		if (activeChar.isBusy())
			return;
		
		if (activeChar.getTransformation() != 0 || activeChar.isMounted() || Olympiad.isRegisteredInComp(activeChar) || activeChar.isInCombat() || activeChar.isInBoat())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExEnchantSkill.message1", activeChar));
			return;
		}

		if(activeChar.getLevel() < 76 || activeChar.getClassId().getLevel() < 4)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExEnchantSkill.message2", activeChar));
			return;
		}

		EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
		if(sl == null)
			return;

		int slevel = activeChar.getSkillLevel(_skillId);
		if(slevel == -1)
			return;

		int enchantLevel = SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), _skillLvl, sl.getMaxLevel());

		// already knows the skill with this level
		if(slevel >= enchantLevel)
			return;

		// Можем ли мы перейти с текущего уровня скилла на данную заточку
		if(slevel == sl.getBaseLevel() ? _skillLvl % 100 != 1 : slevel != enchantLevel - 1)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExEnchantSkill.message3", activeChar));
			return;
		}

		Skill skill = SkillTable.getInstance().getInfo(_skillId, enchantLevel);
		if(skill == null)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExEnchantSkill.message4", activeChar));
			return;
		}

		int[] cost = sl.getCost();
		int requiredSp = cost[1] * SkillTreeTable.NORMAL_ENCHANT_COST_MULTIPLIER * sl.getCostMult();
		int requiredAdena = cost[0] * SkillTreeTable.NORMAL_ENCHANT_COST_MULTIPLIER * sl.getCostMult();
		int rate = sl.getRate(activeChar);

		if(activeChar.getSp() < requiredSp)
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			return;
		}

		if(activeChar.getAdena() < requiredAdena)
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		if(_skillLvl % 100 == 1) // only first lvl requires book (101, 201, 301 ...)
		{
			if(Functions.getItemCount(activeChar, SkillTreeTable.NORMAL_ENCHANT_BOOK) == 0)
			{
				activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
			Functions.removeItem(activeChar, SkillTreeTable.NORMAL_ENCHANT_BOOK, 1);
		}

		activeChar.addExpAndSp(0, -requiredSp);
		activeChar.sendPacket(new SystemMessage2(SystemMsg.YOUR_SP_HAS_DECREASED_BY_S1).addInteger(requiredSp));
		Functions.removeItem(activeChar, 57, requiredAdena);
		
		boolean success = false;
		if(Rnd.chance(rate))
		{
			success = true;
			activeChar.sendPacket(new SystemMessage2(SystemMsg.SKILL_ENCHANT_WAS_SUCCESSFUL_S1_HAS_BEEN_ENCHANTED).addSkillName(_skillId, _skillLvl), new SkillList(activeChar), new ExEnchantSkillResult(1));
			Log.addGame(activeChar.getName() + "|Successfully enchanted|" + _skillId + "|to+" + _skillLvl + "|" + rate, "enchant_skills");
		}
		else
		{
			skill = SkillTable.getInstance().getInfo(_skillId, sl.getBaseLevel());
			activeChar.sendPacket(new SystemMessage(SystemMessage.FAILED_IN_ENCHANTING_SKILL_S1).addSkillName(_skillId, _skillLvl), new ExEnchantSkillResult(0));
			Log.addGame(activeChar.getName() + "|Failed to enchant|" + _skillId + "|to+" + _skillLvl + "|" + rate, "enchant_skills");
		}
		
		activeChar.addSkill(skill, true);
		
		//Listener
		activeChar.getListeners().onSkillEnchant(skill, success, false);
		
		updateSkillShortcuts(activeChar, _skillId, _skillLvl);
		activeChar.sendPacket(new ExEnchantSkillInfo(_skillId, activeChar.getSkillDisplayLevel(_skillId)));
	}

	protected static void updateSkillShortcuts(Player player, int skillId, int skillLevel)
	{
		for(ShortCut sc : player.getAllShortCuts())
			if(sc.getId() == skillId && sc.getType() == ShortCut.TYPE_SKILL)
			{
				ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLevel, 1);
				player.sendPacket(new ShortCutRegister(player, newsc));
				player.registerShortCut(newsc);
			}
	}
}