package ai.PaganTemplete;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.utils.Location;

/**
 *         - AI для монстра Triols Layperson (22142).
 *         - Если увидел игрока в радиусе 500, если его пати состовляет больше 4 мемберов.
 *         - Тогда выбрасывает на рандомные координаты первого увидевшего игрока.
 *         - AI проверен и работает.
 */
public class TriolsLayperson extends Fighter
{
	private boolean _tele = true;

	public static final Location[] locs = {new Location( -16128, -35888, -10726), new Location( -17029, -39617, -10724), new Location( -15729, -42001, -10724)};

	public TriolsLayperson(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		for(Player player : World.getAroundPlayers(actor, 500, 500))
		{
			if(player == null || !player.isInParty())
				continue;

			if(player.getParty().size() >= 5 && _tele)
			{
				_tele = false;
				player.teleToLocation(Rnd.get(locs));
			}
		}

		return true;
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_tele = true;
		super.onEvtDead(killer);
	}
}