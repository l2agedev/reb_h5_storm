package l2r.gameserver.network.serverpackets;

public class ExEventMatchMessage extends L2GameServerPacket
{
	public enum EventMatchMessage
	{
		CUSTOM_TEXT,
		FINISH,
		START,
		GAME_OVER,
		ONE,
		TWO,
		THREE,
		FOUR,
		FIVE
	}
	
	String _message;
	EventMatchMessage _type;
	
	/**
	 * 
	 * @param message - 77 max chars prefered, else the message gets cut-off.
	 */
	public ExEventMatchMessage(String message)
	{
		_message = message;
		_type = EventMatchMessage.CUSTOM_TEXT;
	}
	
	public ExEventMatchMessage(EventMatchMessage message)
	{
		_message = "";
		_type = message;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(0x0F);
		writeC(_type.ordinal()); // 0 - custom text, 1 - Finish, 2 - Start, 3 - Game Over, 4 - 1, 5 - 2, 6 - 3, 7 - 4, 8 - 5
		writeS(_message);
	}
}