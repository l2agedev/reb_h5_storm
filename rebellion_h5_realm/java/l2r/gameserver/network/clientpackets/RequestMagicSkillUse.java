package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Skill.SkillType;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.items.attachment.FlagItemAttachment;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.tables.SkillTable;

public class RequestMagicSkillUse extends L2GameClientPacket
{
	private Integer _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	/**
	 * packet type id 0x39
	 * format:		cddc
	 */
	@Override
	protected void readImpl()
	{
		_magicId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		activeChar.setActive();

		if(activeChar.isOutOfControl() && !activeChar.canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
		{
			activeChar.sendActionFailed();
			return;
		}

		Skill skill = SkillTable.getInstance().getInfo(_magicId, activeChar.getSkillLevel(_magicId));
		if (activeChar.isPendingOlyEnd())
		{
			if (skill != null && skill.isOffensive())
			{
				activeChar.sendActionFailed();
				return;
			}
		}
		
		if(skill != null)
		{
			if (activeChar.isUnActiveSkill(skill.getId()))
				return;

			if(!(skill.isActive() || skill.isToggle()))
				return;

			// If Alternate rule Karma punishment is set to true, forbid skill Return to player with Karma
			if (!activeChar.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && (skill.getSkillType() == SkillType.RECALL) && !Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && (activeChar.getKarma() > 0))
				return;
						
			FlagItemAttachment attachment = activeChar.getActiveWeaponFlagAttachment();
			if(attachment != null && !attachment.canCast(activeChar, skill))
			{
				activeChar.sendActionFailed();
				return;
			}

			if (!activeChar.canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
			{
				boolean allow = true;
				// В режиме трансформации доступны только скилы трансформы
				if (activeChar.getTransformation() != 0)
				{
					if (NexusEvents.isInEvent(activeChar))
					{
						int allowSkill = NexusEvents.allowTransformationSkill(activeChar, skill);
						
						if (allowSkill == -1)
							allow = false;
						else if (allowSkill == 0)
						{
							if (!activeChar.getAllSkills().contains(skill))
								allow = false;
						}
					}
					else if (!activeChar.getAllSkills().contains(skill))
						allow = false;
				}
				
				if (!allow)
				{
					activeChar.sendPacket(ActionFail.STATIC);
					return;
				}
			}
			
			if(skill.isToggle())
				if(activeChar.getEffectList().getEffectsBySkill(skill) != null)
				{
					activeChar.getEffectList().stopEffect(skill.getId());
					activeChar.sendActionFailed();
					return;
				}

			Creature target = skill.getAimingTarget(activeChar, activeChar.getTarget());

			activeChar.setGroundSkillLoc(null);
			activeChar.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
		}
		else
			activeChar.sendActionFailed();
	}
}