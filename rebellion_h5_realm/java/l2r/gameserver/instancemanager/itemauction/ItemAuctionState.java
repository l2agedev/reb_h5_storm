package l2r.gameserver.instancemanager.itemauction;

import l2r.commons.lang.ArrayUtils;

public enum ItemAuctionState
{
	CREATED,
	STARTED,
	FINISHED;

	public static final ItemAuctionState stateForStateId(int stateId)
	{
		return ArrayUtils.valid(values(), stateId);
	}
}