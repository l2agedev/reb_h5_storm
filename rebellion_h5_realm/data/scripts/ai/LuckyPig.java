package ai;

import java.util.List;

import l2r.gameserver.Config;
import l2r.gameserver.ai.DefaultAI;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.GetItem;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.SkillTable;

import npc.model.LuckyPigInstance;

/**
 * @author VISTALL
 * @date 14:53/25.04.2012
 */
public class LuckyPig extends DefaultAI
{
	
	private int _targetItemObjectId;
	private long _lastSay = 0;
	
	private int _displayId;
	
	public LuckyPig(NpcInstance actor)
	{
		super(actor);
		
		AI_TASK_ATTACK_DELAY = AI_TASK_ACTIVE_DELAY = AI_TASK_DELAY_CURRENT = 500L;
	}
	
	@Override
	protected void onEvtThink()
	{
		LuckyPigInstance actor = getActor();
		if (_lastSay < System.currentTimeMillis())
		{
			Functions.npcSayInRange(actor, 600, NpcString.LUCKPY_I_WANNA_EAT_ADENA);
			_lastSay = System.currentTimeMillis() + 10000L;
		}
		
		if (actor.isMoving())
			return;
		
		List<GameObject> item = World.getAroundObjects(actor, 500, 100);
		for (GameObject gameObject : item)
		{
			if (gameObject.isItem())
			{
				ItemInstance dropedItem = (ItemInstance) gameObject;
				
				if (dropedItem != null && dropedItem.isAdena() && dropedItem.getCount() < Config.MAX_ADENA_TO_EAT)
				{
					if (actor.getPickCount() == 10)
					{
						actor.meFull();
						return;
					}
					
					_targetItemObjectId = dropedItem.getObjectId();
					
					actor.moveToLocation(dropedItem.getLoc(), 10, true);
					return;
				}
			}
			else if (gameObject.isPlayer())
				actor.moveToLocation(gameObject.getLoc(), 100, true);
		}
		
		randomWalk();
	}
	
	@Override
	protected void onEvtArrived()
	{
		LuckyPigInstance actor = getActor();
		if (_targetItemObjectId > 0)
		{
			GameObject gameObject = World.getAroundObjectById(actor, _targetItemObjectId);
			if (gameObject == null || !gameObject.isItem())
			{
				_targetItemObjectId = 0;
				return;
			}
			
			ItemInstance item = (ItemInstance) gameObject;
			
			long count = item.getCount();
			pick(item);
			if (count < Config.ADENA_TO_EAT)
			{
				actor.altUseSkill(SkillTable.getInstance().getInfo(5758, 1), actor); // s_display_jackpot_firework
				Functions.npcSayInRange(actor, 600, NpcString.YUMMY);
				
				actor.incPickCount();
				
				if (actor.getPickCount() >= 2 && actor.getPickCount() < 5)
				{
					if (_displayId == 0)
					{
						actor.altUseSkill(SkillTable.getInstance().getInfo(23325, 1), actor); // s_g_display_luckpi_a
						_displayId++;
					}
					
				}
				else if (actor.getPickCount() >= 5)
				{
					if (_displayId == 1)
					{
						actor.altUseSkill(SkillTable.getInstance().getInfo(23325, 1), actor); // s_g_display_luckpi_a
						_displayId++;
					}
				}
			}
			else if (count < Config.MAX_ADENA_TO_EAT)
			{
				actor.altUseSkill(SkillTable.getInstance().getInfo(6037, 1), actor); // s_dispaly_soul_unleash1
				Functions.npcSayInRange(actor, 600, NpcString.GRRRR);
				
				actor.decPickCount();
				
				if (actor.getPickCount() >= 2 && actor.getPickCount() < 5)
				{
					if (_displayId == 2)
					{
						actor.altUseSkill(SkillTable.getInstance().getInfo(23326, 1), actor); // s_g_display_luckpi_b
						_displayId--;
					}
					
				}
				else if (actor.getPickCount() >= 5)
				{
					_displayId++;
				}
				else if (_displayId == 1)
				{
					actor.altUseSkill(SkillTable.getInstance().getInfo(23326, 1), actor); // s_g_display_luckpi_b
				}
			}
		}
	}
	
	private void pick(ItemInstance item)
	{
		LuckyPigInstance actor = getActor();
		
		actor.broadcastPacket(new GetItem(item, actor.getObjectId()));
		
		item.deleteMe();
		item.delete();
		
		_targetItemObjectId = 0;
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
	}
	
	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
	
	@Override
	public LuckyPigInstance getActor()
	{
		return (LuckyPigInstance) super.getActor();
	}
}
