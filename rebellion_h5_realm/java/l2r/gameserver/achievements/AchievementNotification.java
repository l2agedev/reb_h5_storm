package l2r.gameserver.achievements;

import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.TutorialCloseHtml;
import l2r.gameserver.network.serverpackets.TutorialShowHtml;
import l2r.gameserver.network.serverpackets.TutorialShowQuestionMark;

/**
 * 
 * @author Midnex
 * @author Promo (htmls)
 * @rework Nik, Infern0
 *
 */
public class AchievementNotification
{
	public static void update(Player player, int type)
	{	
		iAchievement iAch = player.getAchievementByType(TypeConverter(type + 100));
		if(iAch == null)
			return;

		int points = Achievements.getInstance().getRealPoints(player.getCounters().getPoints(TypeConverter(type + 100)), iAch.getId(), iAch.getLevel() - 1);

		if(iAch.isDone(points, true))
		{
			closeTutorialHTML(player);
			return;
		}

		if(!player.achievement_nf_open)
			showQuestionMark(player, type + 100);
		else
			showTutorialHTML(player, type + 100);
	}

	public static final void onTutorialQuestionMark(Player player, int number)
	{
		showTutorialHTML(player, number);
	}

	public static final void showQuestionMark(Player player, int type)
	{
		player.sendPacket(new TutorialShowQuestionMark(type));
	}

	private static void showTutorialHTML(Player player, int type)
	{
		player.achievement_nf_open = true;
		String text = HtmCache.getInstance().getNotNull("achievements/Notification.htm", player);
				
		iAchievement ooo = player.getAchievementByType(TypeConverter(type));

		if(ooo == null)
		{
			closeTutorialHTML(player);
			return;
		}

		int points = Achievements.getInstance().getRealPoints(player.getCounters().getPoints(TypeConverter(type)), ooo.getId(), ooo.getLevel() - 1);

		if(ooo.getNeedPoints() - points == 0)
		{
			closeTutorialHTML(player);
			return;
		}

		text = text.replaceFirst("%fame%", "" + ooo.getFame());
		text = text.replaceAll("%desc%", ooo.getDesc().replaceAll("%need%", "" + (ooo.getNeedPoints() - points)));
		text = text.replaceAll("%name%", ooo.getName() + " " + ooo.getLevel() + " lvl");
		text = text.replaceAll("%icon%", ooo.getIcon());

		int greenbar = 248 * (points * 100 / ooo.getNeedPoints()) / 100;
		if(greenbar < 0)
			greenbar = 0;

		text = text.replaceAll("%bar1%", "" + greenbar);
		text = text.replaceAll("%bar2%", "" + (248 - greenbar));

		player.sendPacket(new TutorialShowHtml(text));
	}

	public static void closeTutorialHTML(Player player)
	{
		player.achievement_nf_open = false;
		player.sendPacket(TutorialCloseHtml.STATIC);
	}

	public static String TypeConverter(int type)
	{
		switch(type)
		{
			case 100:
				return "mobkill";
			case 200:
				return "";
			case 300:
				return "";
		}
		return "";
	}
}
