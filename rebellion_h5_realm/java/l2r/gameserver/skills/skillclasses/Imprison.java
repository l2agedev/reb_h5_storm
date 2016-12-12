package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.utils.AutoBan;
import l2r.gameserver.utils.TimeUtils;

import java.util.List;

public class Imprison extends Skill
{
	public Imprison(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(target != null)
			{
                if (!target.isPlayer())
					continue;

                Player player = target.getPlayer();
				AutoBan.doJailPlayer(player, (int)getPower() * 1000L, false);
				player.sendMessage(new CustomMessage("l2r.gameserver.skills.skillclasses.Imprison", player).addCharName(activeChar).addString(TimeUtils.minutesToFullString((int)getPower() / 60)));
			}
		}
	}
}