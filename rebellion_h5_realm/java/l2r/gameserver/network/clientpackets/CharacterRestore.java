package l2r.gameserver.network.clientpackets;

import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.serverpackets.CharacterSelectionInfo;

public class CharacterRestore extends L2GameClientPacket
{
	// cd
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		try
		{
			client.markRestoredChar(_charSlot);
		}
		catch(Exception e)
		{}
		CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}