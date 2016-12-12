package l2r.gameserver.network;

public class AccountData
{
	public static final AccountData DUMMY = new AccountData(true);
	private AccountData(boolean unusedLol)
	{
		accessLevel = -1;
		account = "NotFound";
		allowedHwids = "";
		allowedIps = "";
		banExpire = -1;
		bonus = -1;
		bonusExpire = -1;
		botReportPoints = -1;
		lastAccess = -1;
		lastIp = "0.0.0.0";
		lastServer = -1;
		points = -1;
	}
	
	public AccountData()
	{
		
	}
	
	public String account;
	public int accessLevel;
	public int banExpire;
	public String allowedIps;
	public String allowedHwids;
	public double bonus;
	public int bonusExpire;
	public int lastServer;
	public String lastIp;
	public int lastAccess;
	public int botReportPoints;
	public int points;
	
}
