package l2r.gameserver.achievements;

import l2r.gameserver.data.htm.HtmCache;

import java.util.List;

import javolution.util.FastTable;

/**
 * 
 * @author Midnex
 * @author Promo (htmls)
 * @rework Nik, Infern0
 *
 */
public class IAchievementCategory
{
	private List<iAchievement> _achivments = new FastTable<>();
	private int _id;
	private String _html;
	private String _name;
	private String _icon;
	private String _desc;

	public String getHtml(int totalPlayerLevel)
	{
		int greenbar = 0;

		if(totalPlayerLevel > 0)
			greenbar = calculteBar(24, totalPlayerLevel);
		
		String temp = HtmCache.getInstance().getNullable("achievements/AchievementsCat.htm", null);
		
		temp = temp.replaceFirst("%bg%", getId() % 2 == 0 ? "090908" : "0f100f");
		temp = temp.replaceFirst("%desc%", getDesc());
		temp = temp.replaceFirst("%icon%", getIcon());
		temp = temp.replaceFirst("%name%", getName());
		temp = temp.replaceFirst("%id%", "" + getId());

		temp = temp.replaceFirst("%caps1%", greenbar > 0 ? "Gauge_DF_Food_Left" : "Gauge_DF_Exp_bg_Left");
		temp = temp.replaceFirst("%caps2%", greenbar >= 24 ? "Gauge_DF_Food_Right" : "Gauge_DF_Exp_bg_Right");

		temp = temp.replaceAll("%bar1%", "" + greenbar);
		temp = temp.replaceAll("%bar2%", "" + (24 - greenbar));
		return temp;
	}

	public int calculteBar(int barmax, int totalPlayerLevel)
	{
		int c = barmax * (totalPlayerLevel * 100 / _achivments.size()) / 100;

		if(c >= barmax)
			return barmax;
		return c;
	}

	public int getId()
	{
		return _id;
	}

	public List<iAchievement> getAchievments()
	{
		return _achivments;
	}

	public String getDesc()
	{
		return _desc;
	}

	public String getIcon()
	{
		return _icon;
	}

	public String getName()
	{
		return _name;
	}
	
	public String getHtml()
	{
		return _html;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public void setAchievements(List<iAchievement> achievement)
	{
		_achivments = achievement;
	}
	
	public void setDesc(String desc)
	{
		_desc = desc;
	}
	
	public void setIcon(String icon)
	{
		_icon = icon;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public void setHtml(String html)
	{
		_html = html;
	}
}
