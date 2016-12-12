package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.RecipeBookItemList;

public class RequestRecipeBookOpen extends L2GameClientPacket
{
	private boolean isDwarvenCraft;

	@Override
	protected void readImpl()
	{
		if(_buf.hasRemaining())
			isDwarvenCraft = readD() == 0;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		sendPacket(new RecipeBookItemList(activeChar, isDwarvenCraft));
	}
}