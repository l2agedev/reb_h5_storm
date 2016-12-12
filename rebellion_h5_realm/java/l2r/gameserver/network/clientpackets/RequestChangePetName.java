package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.PetInstance;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.Util;

public class RequestChangePetName extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		PetInstance pet = activeChar.getPet() != null && activeChar.getPet().isPet() ? (PetInstance)activeChar.getPet() : null;
		if(pet == null)
			return;

		if(pet.isDefaultName())
		{
			if(_name.length() < 1 || _name.length() > 8)
			{
				activeChar.sendPacket(SystemMsg.YOUR_PETS_NAME_CAN_BE_UP_TO_8_CHARACTERS_IN_LENGTH);
				return;
			}
			
			if (!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE))
			{
				activeChar.sendPacket(SystemMsg.AN_INVALID_CHARACTER_IS_INCLUDED_IN_THE_PETS_NAME);
				return;
			}
			
			pet.setName(_name);
			pet.broadcastCharInfo();
			pet.updateControlItem();
		}
	}
}