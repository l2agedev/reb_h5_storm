package ai.dragonvalley;

import l2r.gameserver.Config;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.AggroList.HateInfo;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.PlayerGroup;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.tables.SpawnTable;

import java.util.Map;

import quests._456_DontKnowDontCare;

public class DrakeBosses extends Fighter
{
	public DrakeBosses(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance corpse = null;
		switch (getActor().getNpcId())
		{
			case 25725:
				corpse = SpawnTable.spawnSingle(32884, getActor().getLoc(), 300000);
				break;
			case 25726:
				corpse = SpawnTable.spawnSingle(32885, getActor().getLoc(), 300000);
				break;
			case 25727:
				corpse = SpawnTable.spawnSingle(32886, getActor().getLoc(), 300000);
				break;
		}
		
		if (killer != null && corpse != null)
		{
			final Player player = killer.getPlayer();
			if (player != null)
			{
				PlayerGroup pg = player.getPlayerGroup();
				if (pg != null)
				{
					QuestState qs;
					Map<Playable, HateInfo> aggro = getActor().getAggroList().getPlayableMap();
					for (Player pl : pg)
					{
						if (pl != null && !pl.isDead() && aggro.containsKey(pl) && getActor().isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE) || getActor().isInRangeZ(killer, Config.ALT_PARTY_DISTRIBUTION_RANGE))
						{
							qs = pl.getQuestState(_456_DontKnowDontCare.class);
							if (qs != null && qs.getCond() == 1)
								qs.set("RaidKilled", corpse.getObjectId());
						}
					}
				}
			}
		}
		super.onEvtDead(killer);
		getActor().endDecayTask();
	}
}