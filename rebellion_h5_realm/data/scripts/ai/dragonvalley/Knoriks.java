package ai.dragonvalley;

import l2r.commons.util.Rnd;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Location;

public class Knoriks extends Patrollers
{
	private static int KNORIKS_ACTIVATE_SKILL_CHANGE = 5; // chance for activate skill
	private static int KNORIKS_SEARCH_RADIUS = 600; // search around players
	private static int KNORIKS_SKILL_DBUFF_ID = 6744; // dbuff id (Dark Storm)
	
	public Knoriks(NpcInstance actor)
	{
		super(actor);
		_points = new Location[]
		{
			new Location(140639, 114460, -3729),
			new Location(142095, 114336, -3729),
			new Location(142664, 113707, -3720),
			new Location(143831, 114596, -3720),
			new Location(144783, 114876, -3720),
			new Location(147197, 116761, -3704),
			new Location(148227, 117724, -3712),
			new Location(143007, 108951, -3953),
			new Location(141716, 109380, -3953),
			new Location(140653, 112072, -3720),
			new Location(141162, 113050, -3729),
			new Location(141877, 112263, -3720),
			new Location(142752, 111555, -3953),
			new Location(142114, 109445, -3953),
			new Location(140890, 109445, -3953),
			new Location(147895, 110043, -3946),
			new Location(145685, 109132, -3953),
			new Location(144924, 108192, -3928),
			new Location(144558, 107723, -3953),
			new Location(142480, 119635, -3921),
			new Location(142960, 117501, -3921),
			new Location(142026, 117331, -3912),
			new Location(141238, 117792, -3912),
			new Location(140220, 119893, -3912),
			new Location(140697, 120780, -3896),
			new Location(141939, 121539, -3921),
			new Location(143250, 121372, -3921),
			new Location(145334, 121799, -3921),
			new Location(146081, 121987, -3921),
			new Location(146278, 121463, -3912),
			new Location(145785, 120344, -3912),
			new Location(152585, 110480, -5529),
			new Location(153524, 110822, -5529),
			new Location(153325, 111607, -5529),
			new Location(150988, 111181, -5520),
			new Location(149511, 111160, -5496),
			new Location(149330, 110708, -5448),
			new Location(150386, 109580, -5136),
			new Location(152942, 109299, -5161),
			new Location(153303, 108403, -5152),
			new Location(152669, 107732, -5120),
			new Location(150735, 107365, -4776),
			new Location(149713, 108362, -4536),
			new Location(148849, 108131, -4312),
			new Location(147699, 107247, -4056),
			new Location(146847, 107845, -3872),
			new Location(146442, 109417, -3432),
			new Location(146901, 116472, -3703),
			new Location(148652, 115744, -3721),
			new Location(149213, 114571, -3721),
			new Location(148744, 112355, -3721),
			new Location(148104, 112099, -3721),
			new Location(145757, 112813, -3721),
			new Location(145688, 112943, -3721),
			new Location(144569, 114637, -3711),
			new Location(143071, 114050, -3721),
			new Location(142351, 113780, -3721),
			new Location(141899, 114565, -3721),
			new Location(140915, 114253, -3708),
			new Location(140743, 112383, -3721),
			new Location(140476, 111103, -3945),
			new Location(141182, 109756, -3945),
			new Location(142932, 108579, -3945),
			new Location(142620, 107144, -3945),
			new Location(144119, 107449, -3945),
			new Location(145247, 108941, -3945),
			new Location(147232, 109791, -3948),
			new Location(145501, 109010, -3945),
			new Location(143502, 107242, -3945),
			new Location(142311, 107213, -3945),
			new Location(140624, 107331, -3945),
			new Location(140784, 109603, -3945),
			new Location(140540, 111948, -3721),
			new Location(140845, 114262, -3707),
			new Location(142023, 114407, -3721),
			new Location(142449, 113612, -3703),
			new Location(145190, 115247, -3721)
		};
	}
	
	@Override
	public void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		
		super.onEvtAttacked(attacker, damage);
		
		if (!attacker.isPlayer() && actor.getDistance(attacker) > KNORIKS_SEARCH_RADIUS)
			return;
		
		if (actor.isDead() || !Rnd.chance(KNORIKS_ACTIVATE_SKILL_CHANGE))
			return;
		
		if (attacker != null)
			actor.doCast(SkillTable.getInstance().getInfo(KNORIKS_SKILL_DBUFF_ID, 1), attacker, false);
	}
}
