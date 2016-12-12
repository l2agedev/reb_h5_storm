package ai;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2r.commons.util.Rnd;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Location;

/**
 * @author Diamond
 */
public class CrystallineGolem extends Fighter
{
	private static final int CORAL_GARDEN_SECRETGATE = 24220026; // Tears Door

	private static final int Crystal_Fragment = 9693;

	private ItemInstance itemToConsume = null;
	private Location lastPoint = null;
	
	private static String[] phrases_eat = new String[]
	{
		"Yum, yum!!!",
		"Give me! ",
		" I want more Cystal Fragments! ",
		" My Crystals! ",
		" More! ",
		" Food! "
	};
	
	private static final String[] phrases_idle = new String[]
	{
		"Hello is anyone there?",
		"I am hungry! ",
		" I want some Cystal Fragments! ",
		" Where are my Crystals?"
	};
	
	private static TIntObjectHashMap<Info> instanceInfo = new TIntObjectHashMap<Info>();

	private static class Info
	{
		boolean stage1 = false;
		boolean stage2 = false;
	}

	public CrystallineGolem(NpcInstance actor)
	{
		super(actor);
		actor.setHasChatWindow(false);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return true;

		if(_def_think)
		{
			doTask();
			return true;
		}

		if(itemToConsume != null)
			if(itemToConsume.isVisible())
			{
				itemToConsume.deleteMe();
				itemToConsume = null;
			}
			else
			{
				itemToConsume = null;
				Functions.npcSay(actor, phrases_idle[Rnd.get(phrases_idle.length)]);
				actor.setWalking();
				addTaskMove(lastPoint, true);
				lastPoint = null;
				return true;
			}

		Info info = instanceInfo.get(actor.getReflectionId());
		if(info == null)
		{
			info = new Info();
			instanceInfo.put(actor.getReflectionId(), info);
		}

		boolean opened = info.stage1 && info.stage2;

		if(!info.stage1)
		{
			int dx = actor.getX() - 142999;
			int dy = actor.getY() - 151671;
			if(dx * dx + dy * dy < 10000)
			{
				actor.broadcastPacket(new MagicSkillUse(actor, actor, 5441, 1, 1, 0));
				info.stage1 = true;
			}
		}

		if(!info.stage2)
		{
			int dx = actor.getX() - 139494;
			int dy = actor.getY() - 151668;
			if(dx * dx + dy * dy < 10000)
			{
				actor.broadcastPacket(new MagicSkillUse(actor, actor, 5441, 1, 1, 0));
				info.stage2 = true;
			}
		}

		if(!opened && info.stage1 && info.stage2)
			actor.getReflection().openDoor(CORAL_GARDEN_SECRETGATE);

		if(Rnd.chance(10))
			for(GameObject obj : World.getAroundObjects(actor, 300, 200))
				if(obj.isItem())
				{
					ItemInstance item = (ItemInstance) obj;
					if(item.getItemId() == Crystal_Fragment)
					{
						if (Rnd.chance(50))
							Functions.npcSay(actor, phrases_eat[Rnd.get(phrases_eat.length)]);
						itemToConsume = item;
						lastPoint = actor.getLoc();
						actor.setRunning();
						addTaskMove(item.getLoc(), false);
						return true;
					}
				}

		if(randomAnimation())
			return true;

		return false;
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}