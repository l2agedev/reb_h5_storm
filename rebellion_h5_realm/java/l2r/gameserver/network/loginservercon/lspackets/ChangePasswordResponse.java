package l2r.gameserver.network.loginservercon.lspackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.ReceivablePacket;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;


public class ChangePasswordResponse extends ReceivablePacket
{
	String account;
	boolean changed;

	@Override
	public void readImpl()
	{
		account = readS();
		changed = readD() == 1;
	}
	
	@Override
	protected void runImpl()
	{
		GameClient client = AuthServerCommunication.getInstance().removeWaitingClient(account);
		if(client == null)
			return;
		
		Player activeChar = client.getActiveChar();

		if(activeChar == null)
			return;
		
		if(changed)
			Functions.show(new CustomMessage("scripts.commands.user.password.ResultTrue", activeChar), activeChar);
		else
			Functions.show(new CustomMessage("scripts.commands.user.password.ResultFalse", activeChar), activeChar);
	}
}