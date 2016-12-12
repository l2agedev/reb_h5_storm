package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.EnchantResult;
import l2r.gameserver.network.serverpackets.components.ChatType;

public class RequestExCancelEnchantItem extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar != null)
		{
			if (Config.SECURITY_ENABLED && Config.SECURITY_ENCHANT_ITEM_REMOVE_ENABLED && activeChar.getSecurity())
			{
				activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
				return;
			}
			
			activeChar.setEnchantScroll(null);
			activeChar.sendPacket(EnchantResult.CANCEL);
		}
	}
}