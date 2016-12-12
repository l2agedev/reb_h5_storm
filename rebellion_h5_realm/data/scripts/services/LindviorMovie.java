package services;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.network.serverpackets.ExStartScenePlayer;
import l2r.gameserver.scripts.ScriptFile;

import java.util.List;


/**
 * Раз в 3 часа на всей территории базы альянса на Грации всем внутри зоны показывается мувик
 *
 * @author pchayka
 */

public class LindviorMovie implements ScriptFile
{
	private static long movieDelay = 3 * 60 * 60 * 1000L; // показывать мувик раз в n часов

	@Override
	public void onLoad()
	{
		Zone zone = ZoneHolder.getZone("[keucereus_alliance_base_town_peace]");
		zone.setActive(true);

		ThreadPoolManager.getInstance().scheduleAtFixedRate(new ShowLindviorMovie(zone), movieDelay, movieDelay);
	}

	public class ShowLindviorMovie extends RunnableImpl
	{
		Zone _zone;

		public ShowLindviorMovie(Zone zone)
		{
			_zone = zone;
		}

		@Override
		public void runImpl() throws Exception
		{
			List<Player> insideZoners = _zone.getInsidePlayers();

			if(insideZoners != null && !insideZoners.isEmpty())
				for(Player player : insideZoners)
					if(!player.isInBoat() && !player.isInFlyingTransform())
						player.showQuestMovie(ExStartScenePlayer.SCENE_LINDVIOR);
		}
	}

	@Override
	public void onReload()
	{
	}

	@Override
	public void onShutdown()
	{
	}
}
