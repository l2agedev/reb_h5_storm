package quests;

import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.network.serverpackets.components.NpcString;

/**
 * @author VISTALL
 * @date 15:56/12.04.2011
 */
public class _734_PierceThroughAShield extends Dominion_KillSpecialUnitQuest
{
	public _734_PierceThroughAShield()
	{
		super();
	}

	@Override
	protected NpcString startNpcString()
	{
		return NpcString.DEFEAT_S1_ENEMY_KNIGHTS;
	}

	@Override
	protected NpcString progressNpcString()
	{
		return NpcString.YOU_HAVE_DEFEATED_S2_OF_S1_KNIGHTS;
	}

	@Override
	protected NpcString doneNpcString()
	{
		return NpcString.YOU_WEAKENED_THE_ENEMYS_DEFENSE;
	}

	@Override
	protected int getRandomMin()
	{
		return 10;
	}

	@Override
	protected int getRandomMax()
	{
		return 15;
	}

	@Override
	protected ClassId[] getTargetClassIds()
	{
		return new ClassId[]
		{
				ClassId.paladin,
				ClassId.darkAvenger,
				ClassId.templeKnight,
				ClassId.shillienKnight,
				ClassId.phoenixKnight,
				ClassId.hellKnight,
				ClassId.evaTemplar,
				ClassId.shillienTemplar
		};
	}
}
