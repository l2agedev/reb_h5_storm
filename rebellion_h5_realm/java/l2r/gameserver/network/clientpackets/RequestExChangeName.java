package l2r.gameserver.network.clientpackets;

public class RequestExChangeName extends L2GameClientPacket
{
	int unk1;
	String name;
	int unk2;
	
	@Override
	protected void readImpl()
	{
		unk1 = readD();
		name = readS();
		unk2 = readD();
	}

	@Override
	protected void runImpl()
	{
		//TODO not implemented
	}
}