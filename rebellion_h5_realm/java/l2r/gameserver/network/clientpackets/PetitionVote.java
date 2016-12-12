package l2r.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;

/**
 * format: ddS
 */
public class PetitionVote extends L2GameClientPacket
{
	int _type, _unk1;
	String _petitionText;

	@Override
	protected void runImpl()
	{}

	@Override
	protected void readImpl()
	{
		try
		{
			_type = readD();
			_unk1 = readD(); // possible always zero
			_petitionText = readS(4096);
			// not done
		}
		catch (BufferUnderflowException bue) {} // Bugged Client
	}
}