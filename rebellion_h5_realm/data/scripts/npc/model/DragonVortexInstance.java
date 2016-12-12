package npc.model;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SpawnTable;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;

/**
 * @author pchayka
 */

public final class DragonVortexInstance extends NpcInstance
{
	private final int[] bosses = { 
		25718, // Emerald Horn
		25719, // Dust Rider
		25720, // Bleeding Fly
		25721, // Blackdagger Wing
		25722, // Shadow Summoner
		25723, // Spike Slasher
		25724 // Muscle Bomber
		};
	//private int _bossOID = 0;
	private long _timer = 0L;
	
	public DragonVortexInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.startsWith("request_boss"))
		{
			if(_timer + 30000 > System.currentTimeMillis())
			{
				showChatWindow(player, "default/32871-3.htm");
				return;
			}
			
			/*
			GameObject obj = GameObjectsStorage.findObject(_bossOID);
			if (obj != null && obj.isNpc() && Util.contains(bosses, ((NpcInstance)obj).getNpcId()))
			{
				showChatWindow(player, "default/32871-3.htm");
				return;
			}
			*/
			if(ItemFunctions.getItemCount(player, 17248) > 0)
			{
				ItemFunctions.removeItem(player, 17248, 1, true);
				Location loc = player.getLoc().findPointToStay(300, 1000);
				
				int bossId = Rnd.get(bosses);
				/*
				int bossId = 0;
				int random = Rnd.get(100);
				
					 if (random < 4) bossId = bosses[0]; // Emerald Horn
				else if (random < 12) bossId = bosses[1]; // Dust Rider
				else if (random < 25) bossId = bosses[2]; // Bleeding Fly
				else if (random < 43) bossId = bosses[3]; // Blackdagger Wing
				else if (random < 55) bossId = bosses[4]; // Shadow Summoner
				else if (random < 75) bossId = bosses[5];  // Spike Slasher
				else if (random < 100)	bossId = bosses[6]; // Muscle Bomber
				*/
				
				//_bossOID = SpawnTable.spawnSingle(bossId, loc.getX(), loc.getY(), loc.getZ(), Config.DV_RB_DESPAWN * 60000).getObjectId();
				
				SpawnTable.spawnSingle(bossId, loc.getX(), loc.getY(), loc.getZ(), Config.DV_RB_DESPAWN * 60000);
				_timer = System.currentTimeMillis();
				
				showChatWindow(player, "default/32871-1.htm");
			}
			else
				showChatWindow(player, "default/32871-2.htm");
		}
		else
			super.onBypassFeedback(player, command);
	}
}