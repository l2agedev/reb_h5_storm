package l2r.gameserver.achievements;

import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

import java.util.HashMap;

/**
 * 
 * @author Midnex
 * @author Promo (htmls)
 * @rework Nik, Infern0
 *
 */
public class iAchievement
{
	private int _id;
	private int _level;
	private String _name;
	private int _cat;
	private String _icon;
	private String _desc;
	private int _needPoints;
	private String _type;
	private int _fame;

	public HashMap<Integer, Long> _rewards = new HashMap<>();

	public boolean maybeDone(int PlayerPoints)
	{
		if(isDone(PlayerPoints, false))
			return false;

		if(PlayerPoints == _needPoints)
			return true;
		
		return false;
	}

	public boolean isDone(int PlayerPoints, boolean lygu)
	{
		if(!lygu)
		{
			if(PlayerPoints > _needPoints)
				return true;
		}
		else if(PlayerPoints >= _needPoints)
			return true;
		
		return false;
	}

	public String getNotDoneHtml(Player pl, int playerPoints)
	{
		String oneAchievement = HtmCache.getInstance().getNotNull("achievements/oneAchievement.htm", pl);

		int greenbar = 24 * (playerPoints * 100 / _needPoints) / 100;
		if(greenbar < 0)
			greenbar = 0;

		if(greenbar > 24)
		{
			fix(pl, playerPoints);
			pl.sendMessage(new CustomMessage("l2r.gameserver.achievements.iachievement.applying_fix", pl));
			return "";
		}

		oneAchievement = oneAchievement.replaceFirst("%fame%", "" + _fame);
		oneAchievement = oneAchievement.replaceAll("%bar1%", "" + greenbar);
		oneAchievement = oneAchievement.replaceAll("%bar2%", "" + (24 - greenbar));

		oneAchievement = oneAchievement.replaceFirst("%cap1%", greenbar > 0 ? "Gauge_DF_Food_Left" : "Gauge_DF_Exp_bg_Left");
		oneAchievement = oneAchievement.replaceFirst("%cap2%", "Gauge_DF_Exp_bg_Right");

		oneAchievement = oneAchievement.replaceFirst("%desc%", _desc.replaceAll("%need%", "" + (_needPoints - playerPoints)));

		oneAchievement = oneAchievement.replaceFirst("%bg%", _id % 2 == 0 ? "090908" : "0f100f");
		oneAchievement = oneAchievement.replaceFirst("%icon%", _icon);
		oneAchievement = oneAchievement.replaceFirst("%name%", _name + " " + _level + "lvl");
		return oneAchievement;
	}

	private void fix(Player pl, int playerPoints)
	{
		iAchievement arc = Achievements.getInstance().GetAchievement(_id, pl.getAchievementLevelbyId(_id) + 1);
		if(arc != null && playerPoints > arc.getNeedPoints())
		{
			Achievements.getInstance().reward(pl, arc);
			fix(pl, playerPoints);
		}
	}

	public String getDoneHtml()
	{
		String oneAchievement = HtmCache.getInstance().getNullable("achievements/oneAchievement.htm", null);

		oneAchievement = oneAchievement.replaceFirst("%fame%", "" + _fame);
		oneAchievement = oneAchievement.replaceAll("%bar1%", "24");
		oneAchievement = oneAchievement.replaceAll("%bar2%", "0");

		oneAchievement = oneAchievement.replaceFirst("%cap1%", "Gauge_DF_Food_Left");
		oneAchievement = oneAchievement.replaceFirst("%cap2%", "Gauge_DF_Food_Right");

		oneAchievement = oneAchievement.replaceFirst("%desc%", "Done.");

		oneAchievement = oneAchievement.replaceFirst("%bg%", _id % 2 == 0 ? "090908" : "0f100f");
		oneAchievement = oneAchievement.replaceFirst("%icon%", _icon);
		oneAchievement = oneAchievement.replaceFirst("%name%", _name + " " + _level + "lvl");
		return oneAchievement;
	}

	public HashMap<Integer, Long> getRewards()
	{
		return _rewards;
	}

	public String getName()
	{
		return _name;
	}

	public String getDesc()
	{
		return _desc;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public String getType()
	{
		return _type;
	}

	public int getNeedPoints()
	{
		return _needPoints;
	}

	public int getCat()
	{
		return _cat;
	}

	public String getIcon()
	{
		return _icon;
	}

	public int getFame()
	{
		return _fame;
	}
	
	public void setIcon(String icon)
	{
		_icon = icon;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public void setDesc(String desc)
	{
		_desc = desc;
	}
	
	public void setFame(int fame)
	{
		_fame = fame;
	}
	
	public void setCategory(int cat)
	{
		_cat = cat;
	}
	
	public void setNeededPoints(int points)
	{
		_needPoints = points;
	}
	
	public void setType(String type)
	{
		_type = type;
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public void addReward(int itemid, long itemcount)
	{
		_rewards.put(itemid, itemcount);
	}
}
