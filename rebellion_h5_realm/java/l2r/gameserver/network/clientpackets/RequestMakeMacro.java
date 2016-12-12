package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.actor.instances.player.Macro;
import l2r.gameserver.model.actor.instances.player.Macro.L2MacroCmd;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

/**
 * packet type id 0xcd
 *
 * sample
 *
 * cd
 * d // id
 * S // macro name
 * S // unknown  desc
 * S // unknown  acronym
 * c // icon
 * c // count
 *
 * c // entry
 * c // type
 * d // skill id
 * c // shortcut id
 * S // command name
 *
 * format:		cdSSScc (ccdcS)
 */
public class RequestMakeMacro extends L2GameClientPacket
{
	private Macro _macro;

	@Override
	protected void readImpl()
	{
		int _id = readD();
		String _name = readS(32);
		String _desc = readS(64);
		String _acronym = readS(4);
		int _icon = readC();
		int _count = readC();
		if(_count > 12)
			_count = 12;
		L2MacroCmd[] commands = new L2MacroCmd[_count];
		for(int i = 0; i < _count; i++)
		{
			int entry = readC();
			int type = readC(); // 1 = skill, 3 = action, 4 = shortcut
			int d1 = readD(); // skill or page number for shortcuts
			int d2 = readC();
			String command = readS().replace(";", "").replace(",", "");
			if (type == 1 && Config.ALLOW_MACROS_REUSE_BUG)
			{
				entry = 3;
				command = ".useskill " + d1;
			}
			commands[i] = new L2MacroCmd(entry, type, d1, d2, command);
		}
		_macro = new Macro(_id, _icon, _name, _desc, _acronym, commands);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if ((Config.SECURITY_ENABLED && Config.SECURITY_ADD_MACRO) && activeChar.getSecurity())
		{
			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			return;
		}
		
		if(activeChar.getMacroses().getAllMacroses().length > 48)
		{
			activeChar.sendPacket(SystemMsg.YOU_MAY_CREATE_UP_TO_48_MACROS);
			return;
		}

		if(_macro.name.length() == 0)
		{
			activeChar.sendPacket(SystemMsg.ENTER_THE_NAME_OF_THE_MACRO);
			return;
		}

		if(_macro.descr.length() > 32)
		{
			activeChar.sendPacket(SystemMsg.MACRO_DESCRIPTIONS_MAY_CONTAIN_UP_TO_32_CHARACTERS);
			return;
		}

		activeChar.registerMacro(_macro);
	}
}