package l2r.gameserver.network.serverpackets;

public class ClientAction extends L2GameServerPacket
{
	public enum ClientActionType
	{
		NEXT_TARGET, // 4
		PICKUP, // 5
		ASSIST // 6
	}
	
	private ClientActionType _action;
	
	public ClientAction(ClientActionType action)
	{
		_action = action;
	}
	@Override
	protected void writeImpl()
	{
		writeC(0x8F);
		writeD(_action.ordinal() + 4); // Small hack for the enum. Others dont work.
	}
}