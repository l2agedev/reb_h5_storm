package npc.model;

import java.util.concurrent.Future;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectTasks;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.npc.NpcTemplate;

/**
 * @author VISTALL
 * @date 23:34/28.04.2012
 */
public class PrizeLuckyPigInstance extends MonsterInstance
{
	
	private static final int NEOLITHIC_B = 14678;
	private static final int NEOLITHIC_A = 14679;
	private static final int NEOLITHIC_S = 14680;
	private static final int NEOLITHIC_S80 = 14681;
	
	private static final int TOPLS_52 = 8755;
	
	private static final int REDSOULCRY_11 = 5577;
	private static final int GREENSOULCRY_11 = 5578;
	private static final int BLUESOULCRY_11 = 5579;
	
	private static final int FIRE_CRYSTAL = 9552;
	private static final int WATER_CRYSTAL = 9553;
	private static final int EARTH_CRYSTAL = 9554;
	private static final int WIND_CRYSTAL = 9555;
	private static final int DARK_CRYSTAL = 9556;
	private static final int UNHOLY_CRYSTAL = 9557;
	
	private int _pickCount;
	private int _pigLevel;
	
	private int _temp;
	
	private Future<?> _task;
	
	public PrizeLuckyPigInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void calculateRewards(Creature lastAttacker)
	{
		if (!lastAttacker.isPlayable())
			return;
		
		if (!Config.ENABLE_LUCKY_PIGS)
			return;
		
		_task.cancel(false);
		
		switch (_pigLevel)
		{
			case 52:
				if (_pickCount >= 5)
					dropItem(lastAttacker.getPlayer(), NEOLITHIC_B, 1);
				else if (_pickCount >= 2 && _pickCount < 5)
					dropItem(lastAttacker.getPlayer(), TOPLS_52, 2);
				else
					dropItem(lastAttacker.getPlayer(), TOPLS_52, 1);
				break;
			case 70:
				if (_pickCount >= 5)
					dropItem(lastAttacker.getPlayer(), NEOLITHIC_A, 1);
				else if (_pickCount >= 2 && _pickCount < 5)
				{
					if (_temp == 2)
						dropItem(lastAttacker.getPlayer(), REDSOULCRY_11, 2);
					else if (_temp == 1)
						dropItem(lastAttacker.getPlayer(), GREENSOULCRY_11, 2);
					else
						dropItem(lastAttacker.getPlayer(), BLUESOULCRY_11, 2);
				}
				else
				{
					if (_temp == 2)
						dropItem(lastAttacker.getPlayer(), REDSOULCRY_11, 1);
					else if (_temp == 1)
						dropItem(lastAttacker.getPlayer(), GREENSOULCRY_11, 1);
					else
						dropItem(lastAttacker.getPlayer(), BLUESOULCRY_11, 1);
				}
				break;
			case 80:
				if (_pickCount >= 5)
					dropItem(lastAttacker.getPlayer(), NEOLITHIC_S, 1);
				else if (_pickCount >= 2 && _pickCount < 5)
				{
					if (_temp == 5)
						dropItem(lastAttacker.getPlayer(), FIRE_CRYSTAL, 2);
					else if (_temp == 4)
						dropItem(lastAttacker.getPlayer(), WATER_CRYSTAL, 2);
					else if (_temp == 3)
						dropItem(lastAttacker.getPlayer(), EARTH_CRYSTAL, 2);
					else if (_temp == 2)
						dropItem(lastAttacker.getPlayer(), WIND_CRYSTAL, 2);
					else if (_temp == 1)
						dropItem(lastAttacker.getPlayer(), DARK_CRYSTAL, 2);
					else
						dropItem(lastAttacker.getPlayer(), UNHOLY_CRYSTAL, 2);
				}
				else
				{
					if (_temp == 5)
						dropItem(lastAttacker.getPlayer(), FIRE_CRYSTAL, 1);
					else if (_temp == 4)
						dropItem(lastAttacker.getPlayer(), WATER_CRYSTAL, 1);
					else if (_temp == 3)
						dropItem(lastAttacker.getPlayer(), EARTH_CRYSTAL, 1);
					else if (_temp == 2)
						dropItem(lastAttacker.getPlayer(), WIND_CRYSTAL, 1);
					else if (_temp == 1)
						dropItem(lastAttacker.getPlayer(), DARK_CRYSTAL, 1);
					else
						dropItem(lastAttacker.getPlayer(), UNHOLY_CRYSTAL, 1);
				}
				break;
			case 85:
				if (_pickCount >= 5)
					dropItem(lastAttacker.getPlayer(), NEOLITHIC_S80, 1);
				else if (_pickCount >= 2 && _pickCount < 5)
				{
					if (_temp == 5)
						dropItem(lastAttacker.getPlayer(), FIRE_CRYSTAL, 2);
					else if (_temp == 4)
						dropItem(lastAttacker.getPlayer(), WATER_CRYSTAL, 2);
					else if (_temp == 3)
						dropItem(lastAttacker.getPlayer(), EARTH_CRYSTAL, 2);
					else if (_temp == 2)
						dropItem(lastAttacker.getPlayer(), WIND_CRYSTAL, 2);
					else if (_temp == 1)
						dropItem(lastAttacker.getPlayer(), DARK_CRYSTAL, 2);
					else
						dropItem(lastAttacker.getPlayer(), UNHOLY_CRYSTAL, 2);
				}
				else
				{
					if (_temp == 5)
						dropItem(lastAttacker.getPlayer(), FIRE_CRYSTAL, 1);
					else if (_temp == 4)
						dropItem(lastAttacker.getPlayer(), WATER_CRYSTAL, 1);
					else if (_temp == 3)
						dropItem(lastAttacker.getPlayer(), EARTH_CRYSTAL, 1);
					else if (_temp == 2)
						dropItem(lastAttacker.getPlayer(), WIND_CRYSTAL, 1);
					else if (_temp == 1)
						dropItem(lastAttacker.getPlayer(), DARK_CRYSTAL, 1);
					else
						dropItem(lastAttacker.getPlayer(), UNHOLY_CRYSTAL, 1);
				}
				break;
		}
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		_temp = Rnd.get(0, 5);
		
		if (_temp == 0)
			Functions.npcSayInRange(this, 600, NpcString.OH_MY_WINGS_DISAPPEARED_ARE_YOU_GONNA_HIT_ME_IF_YOU_HIT_ME_ILL_THROW_UP_EVERYTHING_THAT_I_ATE);
		else
			Functions.npcSayInRange(this, 600, NpcString.OH_MY_WINGS_ACK_ARE_YOU_GONNA_HIT_ME_SCARY_SCARY_IF_YOU_HIT_ME_SOMETHING_BAD_IS_GOING_HAPPEN);
		
		_task = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(this), 600000L);
	}
	
	public void setPickCount(int pickCount)
	{
		_pickCount = pickCount;
	}
	
	public void setPigLevel(int pigLevel)
	{
		_pigLevel = pigLevel;
	}
}
