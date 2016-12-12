package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.actor.instances.player.ShortCut;
import l2r.gameserver.network.serverpackets.ShortCutRegister;
import l2r.gameserver.nexus_interface.NexusEvents;

public class RequestShortCutReg extends L2GameClientPacket
{
	private int _type, _id, _slot, _page, _lvl, _characterType;

	@Override
	protected void readImpl()
	{
		_type = readD();
		int slot = readD();
		_id = readD();
		_lvl = readD();
		_characterType = readD();

		_slot = slot % 12;
		_page = slot / 12;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_page < 0 || _page > ShortCut.PAGE_MAX)
		{
			activeChar.sendActionFailed();
			return;
		}

		if (_type <= 0 || _type > ShortCut.TYPE_MAX)
		{
			activeChar.sendActionFailed();
			return;
		}
		
		boolean saveToDb = true;
		
		if (NexusEvents.isInEvent(activeChar))
		{
			if (!NexusEvents.canSaveShortcuts(activeChar))
			{
				saveToDb = false;
			}
		}
		
		ShortCut shortCut = new ShortCut(_slot, _page, _type, _id, _lvl, _characterType);
		activeChar.sendPacket(new ShortCutRegister(activeChar, shortCut));
		activeChar.registerShortCut(shortCut, saveToDb);
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}