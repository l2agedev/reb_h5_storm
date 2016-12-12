package events.CustomDropItems;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.listener.actor.OnDeathListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomDropItems extends Functions implements ScriptFile, OnDeathListener
{
	private static final Logger _log = LoggerFactory.getLogger(CustomDropItems.class);

	private static boolean ALLOW_MIN_MAX_PLAYER_LVL = Config.CDItemsAllowMinMaxPlayerLvl;
	private static final int MIN_PLAYER_LVL = Config.CDItemsMinPlayerLvl;
	private static final int MAX_PLAYER_LVL = Config.CDItemsMaxPlayerLvl;
	private static boolean ALLOW_MIN_MAX_MOB_LVL = Config.CDItemsAllowMinMaxMobLvl;
	private static final int MIN_MOB_LVL = Config.CDItemsMinMobLvl;
	private static final int MAX_MOB_LVL = Config.CDItemsMaxMobLvl;
	private static boolean ALLOW_ONLY_RB_DROPS = Config.CDItemsAllowOnlyRbDrops;
	private static boolean _active = false;

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		if(Config.AllowCustomDropItems)
		{
			_active = true;
			_log.info("Loaded Event: CustomDropItems [state: activated]");
		}
		else
			_log.info("Loaded Event: CustomDropItems [state: deactivated]");
	}

	@Override
	public void onReload()
	{
	
	}

	@Override
	public void onShutdown()
	{
	
	}

	@Override
	public void onDeath(Creature cha, Creature killer)
	{
		if (cha == null || killer == null)
			return;
		
		// No rewards in instances. Dark Clound Mansion exploit.
		if (killer.getReflectionId() != ReflectionManager.DEFAULT_ID || cha.getReflectionId() != ReflectionManager.DEFAULT_ID)
			return;
		
		if(!ALLOW_ONLY_RB_DROPS)
		{
			if((ALLOW_MIN_MAX_PLAYER_LVL && checkValidate(killer, cha, true, false)) && (ALLOW_MIN_MAX_MOB_LVL && checkValidate(killer, cha, false, true)))
			{
				dropItemMob(cha, killer);
			}
			else if((ALLOW_MIN_MAX_PLAYER_LVL && checkValidate(killer, cha, true, false)) && !ALLOW_MIN_MAX_MOB_LVL)
			{
				dropItemMob(cha, killer);
			}
			else if(!ALLOW_MIN_MAX_PLAYER_LVL && (ALLOW_MIN_MAX_MOB_LVL && checkValidate(killer, cha, false, true)))
			{
				dropItemMob(cha, killer);
			}
			else if(!ALLOW_MIN_MAX_PLAYER_LVL && !ALLOW_MIN_MAX_MOB_LVL)
			{
				dropItemMob(cha, killer);
			}
			else
				return;
		}
		else if(ALLOW_ONLY_RB_DROPS && (cha.isRaid() || cha.isBoss()))
		{
			if((ALLOW_MIN_MAX_PLAYER_LVL && checkValidate(killer, cha, true, false)) && (ALLOW_MIN_MAX_MOB_LVL && checkValidate(killer, cha, false, true)))
			{
				dropItemRb(cha, killer);
			}
			else if((ALLOW_MIN_MAX_PLAYER_LVL && checkValidate(killer, cha, true, false)) && !ALLOW_MIN_MAX_MOB_LVL)
			{
				dropItemRb(cha, killer);
			}
			else if(!ALLOW_MIN_MAX_PLAYER_LVL && (ALLOW_MIN_MAX_MOB_LVL && checkValidate(killer, cha, false, true)))
			{
				dropItemRb(cha, killer);
			}
			else if(!ALLOW_MIN_MAX_PLAYER_LVL && !ALLOW_MIN_MAX_MOB_LVL)
			{
				dropItemRb(cha, killer);
			}
			else
				return;
		}
		else
			return;
	}

	private boolean checkValidate(Creature killer, Creature mob, boolean lvlPlayer, boolean lvlMob)
	{
		if(mob == null || killer == null)
			return false;

		if(lvlPlayer && (killer.getLevel() >= MIN_PLAYER_LVL && killer.getLevel() <= MAX_PLAYER_LVL))
			return true;

		if(lvlMob && (mob.getLevel() >= MIN_MOB_LVL && mob.getLevel() <= MAX_MOB_LVL))
			return true;

		return false;
	}

	private void dropItemMob(Creature cha, Creature killer)
	{
		if(_active && SimpleCheckDrop(cha, killer))
			for (String rewards : Config.CUSTOM_DROP_ITEMS)
			{
				String[] reward2 = rewards.split(",");
				
				int id = Integer.parseInt(reward2[0]);
				long mincount = Long.parseLong(reward2[1]);
				long maxcount = Long.parseLong(reward2[2]);
				int chance = Integer.parseInt(reward2[3]);
				
				if (Rnd.get(100) < chance)
					((MonsterInstance) cha).dropItem(killer.getPlayer(), id, Rnd.get(mincount, maxcount));
			}
		else
			return;
	}
	
	private void dropItemRb(Creature cha, Creature killer)
	{
		if(_active)
			for (String rewards : Config.CUSTOM_DROP_ITEMS)
			{
				String[] reward2 = rewards.split(",");
				
				int id = Integer.parseInt(reward2[0]);
				long mincount = Long.parseLong(reward2[1]);
				long maxcount = Long.parseLong(reward2[2]);
				int chance = Integer.parseInt(reward2[3]);
				
				if (Rnd.get(100) < chance)
					((NpcInstance) cha).dropItem(killer.getPlayer(), id, Rnd.get(mincount, maxcount));
			}
		else
			return;
	}
}