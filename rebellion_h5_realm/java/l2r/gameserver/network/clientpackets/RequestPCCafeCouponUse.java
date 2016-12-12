package l2r.gameserver.network.clientpackets;

/**
 * format: chS
 */
public class RequestPCCafeCouponUse extends L2GameClientPacket
{
	// format: (ch)S
	String _unknown;

	@Override
	protected void readImpl()
	{
		_unknown = readS();
		System.out.println(_unknown); // Coupon Code -> XXXX-XXXX-XXXX-XXXX-XXXX
	}

	@Override
	protected void runImpl()
	{
		//TODO not implemented
	}
}