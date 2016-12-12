package l2r.gameserver.stats;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.items.ItemInstance;

public final class Env
{
	public Creature character;
	public Creature target;
	public ItemInstance item;
	public Skill skill;
	public double value;
	public int chargedShot;
	public double minChance;
	public double maxChance;

	public Env()
	{}

	public Env(Creature cha, Creature tar, Skill sk)
	{
		character = cha;
		target = tar;
		skill = sk;
	}
}
