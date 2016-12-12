package ai.den_of_evil;

import l2r.gameserver.ai.DefaultAI;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.scripts.Functions;

/**
 * @author VISTALL
 * @date 19:24/28.08.2011 Npc Id: 32026 Кричит в чат - если лвл ниже чем 37 включно
 */
public class HestuiGuard extends DefaultAI
{
	public HestuiGuard(NpcInstance actor)
	{
		super(actor);
		AI_TASK_DELAY_CURRENT = AI_TASK_ACTIVE_DELAY = AI_TASK_ATTACK_DELAY = 10000L;
		
	}
	
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		
		for (Player player : World.getAroundPlayers(actor))
		{
			if (player.getLevel() <= 37)
				Functions.npcSay(actor, NpcString.THIS_PLACE_IS_DANGEROUS_S1, player.getName());
		}
		
		return false;
	}
}
