/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom;

/*
import l2r.gameserver.Config;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Location;

public class TreasureGoblin extends Quest implements ScriptFile
{
	final private static int NPC_ID = 120050;
	
	public TreasureGoblin()
	{
		super(false);
		addAttackId(NPC_ID);
		addKillId(NPC_ID);
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		Player player = st.getPlayer();
		st.cancelQuestTimer("adenaDrop");
		if (npc.getNpcId() == NPC_ID && Config.ENABLE_ACHIEVEMENTS)
		{
			player.getCounters().addPoint("_treasure_goblin_kills");
		}
		// TODO: onkill start looking for new random location over the map...
		//TODO: Rewards
		return null;
	}
	
	@Override
	public String onAttack(NpcInstance npc, QuestState st)
	{
		Player player = st.getPlayer();
		
		reduceNpcHp(22, npc, player);
		
		st.startQuestTimer("adenaDrop", 500, npc);
		st.startQuestTimer("startRunning", 100, npc);
		st.startQuestTimer("unspawnNpc", 30000, npc);
		st.startQuestTimer("showNumber", 19000, npc);
		return null;
	}
	
	@Override
	public String onMoveFinished(NpcInstance npc)
	{
		double closestDist = 0;
		Player closetPlayer = null;
		for (Player player : World.getAroundPlayers(npc, 3000, 3000)) // There is a player nearby, RUN!
		{
			double dist = Location.calculateDistance(npc, player, false);
			if ((dist < closestDist) || (closestDist == 0))
			{
				closestDist = dist;
				closetPlayer = player;
			}
		}
		
		startQuestTimer("startRunning", 500, npc, closetPlayer);
		return null;
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		
		if (event.equalsIgnoreCase("adenaDrop"))
		{
			if (npc.isRunning())
			{
				ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), 57);
				item.setCount(getRandom(10000, 100000));
				item.dro
				item.spawnMe(npc.getX(), npc.getY(), npc.getZ()+5);
			}
			startQuestTimer("adenaDrop", 400, npc, player);
		}
		else if (event.equalsIgnoreCase("startRunning"))
		{
			if (npc.isMovementDisabled())
				return null;
			
			npc.setRunning();
			
			int x = npc.getX();
			int y = npc.getY();
			int z = npc.getZ();
			
			if (player != null && Location.checkIfInRange(10000, npc, player, false)) // Run away from the player
			{
				if (player.getX() < x) x += 300;
				else x -= 300;
				if (player.getY() < y) y += 300;
				else y -= 300;
			}
			else
			{
				x += getRandom(-300, 300);
				y += getRandom(-300, 300);
			}
			
			if (Config.GEODATA > 0)
			{
				Location destiny = GeoData.getInstance().moveCheck(npc.getX(), npc.getY(), npc.getZ(), x, y, z, npc.getInstanceId());
				x = destiny.getX();
				y = destiny.getY();
			}
			
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(x, y, z, npc.getHeading()));
		}
		else if (event.equalsIgnoreCase("unspawnNpc"))
		{
			if (npc != null)
			{
				npc.broadcastPacket(new MagicSkillUse(npc, npc, 2036, 1, 800, 800)); // Blessed SOE
				npc.deleteMe();
			}
		}
		else if (event.startsWith("showNumber"))
		{
			if (npc == null)
				return null;
			
			try
			{
				if (event.length() < 11)
					startQuestTimer("showNumber 10", 1000, npc, player);
				
				int num = Integer.parseInt(event.substring(11));
				if (num < 1 || num > 20)
					return null;
				
				showNumberOnHead(npc, num);
				
				num--;
				startQuestTimer("showNumber "+num, 1000, npc, player);
			}
			catch (Exception e) {}
		}
		
		return "";
	}
	
	private void reduceNpcHp(int percent, NpcInstance npc, Player player)
	{
		double hpToReduce = npc.getMaxHp();
		hpToReduce *= (percent/100.0);
		
		npc.setCurrentHp(npc.getCurrentHp() - hpToReduce, false);
		
		if (npc.getCurrentHp() < 1)
			npc.doDie(player);
	}
	
	private void showNumberOnHead(NpcInstance npc, int num)
	{
		num = Math.min(num, 20);
		int skillId = 23096 + num;
		npc.broadcastPacket(new MagicSkillUse(npc, npc, skillId, 1, 300, 300));
	}

	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReload() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShutdown() {
		// TODO Auto-generated method stub
		
	}
}
*/