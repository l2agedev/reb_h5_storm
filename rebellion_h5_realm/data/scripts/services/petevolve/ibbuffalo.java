package services.petevolve;

import l2r.commons.dao.JdbcEntityState;
import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.PetDataTable;
import l2r.gameserver.tables.PetDataTable.Pet;

/**
 * User: darkevil
 * Date: 04.06.2008
 * Time: 1:06:26
 */
public class ibbuffalo extends Functions
{
	private static final int BABY_BUFFALO = PetDataTable.BABY_BUFFALO_ID;
	private static final int BABY_BUFFALO_PANPIPE = Pet.BABY_BUFFALO.getControlItemId();
	private static final int IN_BABY_BUFFALO_NECKLACE = Pet.IMPROVED_BABY_BUFFALO.getControlItemId();

	public void evolve()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;
		Summon pl_pet = player.getPet();
		if(player.getInventory().getItemByItemId(BABY_BUFFALO_PANPIPE) == null)
		{
			show("scripts/services/petevolve/no_item.htm", player, npc);
			return;
		}
		if(pl_pet == null || pl_pet.isDead())
		{
			show("scripts/services/petevolve/evolve_no.htm", player, npc);
			return;
		}
		if(pl_pet.getNpcId() != BABY_BUFFALO)
		{
			show("scripts/services/petevolve/no_pet.htm", player, npc);
			return;
		}
		if(Config.ALT_IMPROVED_PETS_LIMITED_USE && player.isMageClass())
		{
			show("scripts/services/petevolve/no_class_w.htm", player, npc);
			return;
		}
		if(pl_pet.getLevel() < 55)
		{
			show("scripts/services/petevolve/no_level.htm", player, npc);
			return;
		}

		int controlItemId = player.getPet().getControlItemObjId();
		player.getPet().unSummon();

		ItemInstance control = player.getInventory().getItemByObjectId(controlItemId);
		control.setItemId(IN_BABY_BUFFALO_NECKLACE);
		control.setEnchantLevel(Pet.IMPROVED_BABY_BUFFALO.getMinLevel());
		control.setJdbcState(JdbcEntityState.UPDATED);
		control.update();
		player.sendItemList(false);

		show("scripts/services/petevolve/yes_pet.htm", player, npc);
	}
}