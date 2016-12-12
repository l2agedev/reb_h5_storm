package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;

public class NetPing extends L2GameClientPacket 
{
    int clientId;
    int ping;
    int mtu;

    @Override
    protected void readImpl() 
    {
    	clientId = readD();
        ping = readD();
        mtu = readD();
    }
    @Override
    protected void runImpl() 
    {
    	Player activeChar = getClient().getActiveChar();
        if(activeChar == null)
        	return;
        
        activeChar.setPing(ping);
        activeChar.setMTU(mtu);
    }

    @Override
    public String getType() 
    {
        return "[C] B1 NetPing";
    }
}