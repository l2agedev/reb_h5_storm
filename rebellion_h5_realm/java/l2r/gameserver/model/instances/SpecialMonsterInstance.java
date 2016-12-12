package l2r.gameserver.model.instances;

import l2r.gameserver.templates.npc.NpcTemplate;

public class SpecialMonsterInstance extends MonsterInstance
{
	public SpecialMonsterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}