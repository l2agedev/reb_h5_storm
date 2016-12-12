package l2r.gameserver.model.base;

public enum RestartType
{
	TO_VILLAGE,
	TO_CLANHALL,
	TO_CASTLE,
	TO_FORTRESS,
	TO_FLAG,
	FIXED,
	AGATHION,
	JAIL;

	public static final RestartType[] VALUES = values();
}
