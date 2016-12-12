package services;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.SkillTable;

/**
 * После смерти Антараса или Валакаса на 3 часа появляется Вестник Невитта С помощью «Вестника Невитта» можно получить баф «Сокрушение Дракона», на 3 часа увеличивающий время действия «Благословения Невитта».
 */

public class InvokerNevitHerald extends Functions
{
	
	public void getCrushingDragon()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		
		if (player == null || npc == null)
			return;
		
		if (player.getEffectList().getEffectsBySkillId(23312) != null)
		{
			show("default/4326-1.htm", player, npc);
			return;
		}
		
		npc.doCast(SkillTable.getInstance().getInfo(23312, 1), player, true);
	}
	
}
