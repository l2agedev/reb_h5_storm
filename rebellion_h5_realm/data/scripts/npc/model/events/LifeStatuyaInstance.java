package npc.model.events;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.templates.npc.NpcTemplate;

/**
 * @author PaInKiLlEr
 */
public class LifeStatuyaInstance extends MonsterInstance
{
	public LifeStatuyaInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		// loadSql();
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return true;
	}
}