package events.CtF_New;

import java.util.HashMap;

import l2r.gameserver.utils.GArray;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Util;

public class Configs  implements Comparable<Object>
{
	public boolean ENABLED;
	public long START_TIME = 0;
	public int TIME_TO_END_BATTLE = 120;
	public int TIME_MAGE_SUPPORT = 10;
	public int MIN_PARTICIPANTS = 2;
	public int MAX_PARTICIPANTS = 10;
	public int TEAM_COUNTS = 0;
	public String ST_REWARD_ITEM_ID = "";
	public String ST_REWARD_COUNT = "";
	public GArray<Integer> LIST_MAGE_MAG_SUPPORT = new GArray<Integer>();
	public GArray<Integer> LIST_MAGE_FAITER_SUPPORT = new GArray<Integer>();
	public GArray<Location> TEAM_COORDS = new GArray<Location>();
	public GArray<Location> FLAG_COORDS = new GArray<Location>();
	public GArray<String> TEAM_NAME = new GArray<String>();
	public boolean STOP_ALL_EFFECTS = true;
	public boolean ALLOW_TAKE_ITEM = false;
	public boolean ALLOW_KILL_BONUS = false;
	public boolean ALLOW_HERO_WEAPONS = true;
	public int KILL_BONUS_ID = 0;
	public int KILL_BONUS_COUNT = 0;
	public int TAKE_ITEM_ID = 0;
	public int TAKE_COUNT = 0;
	public int RESURRECTION_TIME = 0;
	public int NEED_SCORE = 0;
	public int MIN_LEVEL = 1;
	public int MAX_LEVEL = 85;
	public int TIME_TO_START_BATTLE = 10;
	public HashMap<Integer, Boolean> DOORS = new HashMap<Integer, Boolean>();
	public int PAUSE_TIME = 5;
	public String RESTRICT_ITEMS = "";
	public String RESTRICT_SKILLS = "";

	public int compareTo(Object obj)
	{
		Configs tmp = (Configs)obj;
		return START_TIME < tmp.START_TIME ? -1 : START_TIME > tmp.START_TIME ? 1 : 0;
	}

	public int[] getRestictId()
	{
		return getIntArray(RESTRICT_ITEMS);
	}

	public int[] getRestictSkillId()
	{
		return getIntArray(RESTRICT_SKILLS);
	}
	
	public int[] getRewardId()
	{
		return getIntArray(ST_REWARD_ITEM_ID);
	}

	public int[] getRewardCount()
	{
		return getIntArray(ST_REWARD_COUNT);
	}

    private int[] getIntArray(String name)
	{
         return Util.parseCommaSeparatedIntegerArray(name);
    }
}