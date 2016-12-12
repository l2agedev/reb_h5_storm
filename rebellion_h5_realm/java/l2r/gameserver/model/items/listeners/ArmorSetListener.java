package l2r.gameserver.model.items.listeners;

import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.xml.holder.ArmorSetsHolder;
import l2r.gameserver.listener.inventory.OnEquipListener;
import l2r.gameserver.model.ArmorSet;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.SkillList;

import java.util.ArrayList;
import java.util.List;

public final class ArmorSetListener implements OnEquipListener
{
	private static final ArmorSetListener _instance = new ArmorSetListener();

	public static ArmorSetListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return;

		Player player = (Player) actor;

		// checks if player worns chest item
		ItemInstance chestItem = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if(chestItem == null)
			return;

		// checks if there is armorset for chest item that player worns
		ArmorSet armorSet = ArmorSetsHolder.getInstance().getArmorSet(chestItem.getItemId());
		if(armorSet == null)
			return;

		boolean update = false;
		// checks if equipped item is part of set
		if(armorSet.containItem(slot, item.getItemId()))
		{
			if(armorSet.containAll(player))
			{
				List<Skill> skills = armorSet.getSkills();
				for(Skill skill : skills)
				{
					player.addSkill(skill, false);
					update = true;
				}

				if(armorSet.containShield(player)) // has shield from set
				{
					skills = armorSet.getShieldSkills();
					for(Skill skill : skills)
					{
						player.addSkill(skill, false);
						update = true;
					}
				}
				if(armorSet.isEnchanted6(player)) // has all parts of set enchanted to 6 or more
				{
					skills = armorSet.getEnchant6skills();
					for(Skill skill : skills)
					{
						player.addSkill(skill, false);
						update = true;
					}
				}
			}
		}
		else if(armorSet.containShield(item.getItemId()))
			if(armorSet.containAll(player))
			{
				List<Skill> skills = armorSet.getShieldSkills();
				for(Skill skill : skills)
				{
					player.addSkill(skill, false);
					update = true;
				}
			}

		if(update)
		{
			player.sendPacket(new SkillList(player));
			player.updateStats();
		}
	}

	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return;

		Player player = (Player) actor;

		boolean remove = false;
		List<Skill> removeSkillId1 = new ArrayList<Skill>(1); // set skill
		List<Skill> removeSkillId2 = new ArrayList<Skill>(1); // shield skill
		List<Skill> removeSkillId3 = new ArrayList<Skill>(1); // enchant +6 skill

		if(slot == Inventory.PAPERDOLL_CHEST)
		{
			ArmorSet armorSet = ArmorSetsHolder.getInstance().getArmorSet(item.getItemId());
			if(armorSet == null)
				return;

			remove = true;
			removeSkillId1 = armorSet.getSkills();
			removeSkillId2 = armorSet.getShieldSkills();
			removeSkillId3 = armorSet.getEnchant6skills();

		}
		else
		{
			ItemInstance chestItem = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if(chestItem == null)
				return;

			ArmorSet armorSet = ArmorSetsHolder.getInstance().getArmorSet(chestItem.getItemId());
			if(armorSet == null)
				return;

			if(armorSet.containItem(slot, item.getItemId())) // removed part of set
			{
				remove = true;
				removeSkillId1 = armorSet.getSkills();
				removeSkillId2 = armorSet.getShieldSkills();
				removeSkillId3 = armorSet.getEnchant6skills();
			}
			else if(armorSet.containShield(item.getItemId())) // removed shield
			{
				remove = true;
				removeSkillId2 = armorSet.getShieldSkills();
			}
		}

		boolean update = false;
		if(remove)
		{
			for(Skill skill : removeSkillId1)
			{
				player.removeSkill(skill, false);
				update = true;
			}
			for(Skill skill : removeSkillId2)
			{
				player.removeSkill(skill);
				update = true;
			}
			for(Skill skill : removeSkillId3)
			{
				player.removeSkill(skill);
				update = true;
			}
		}

		if(update)
		{
			if(!player.getInventory().isRefreshed())
			{
				// При снятии вещей из состава S80 или S84 сета снимаем плащ
				if(!player.getOpenCloak() && player.getInventory().setPaperdollItem(Inventory.PAPERDOLL_BACK, null) != null)
					player.sendPacket(Msg.THE_CLOAK_EQUIP_HAS_BEEN_REMOVED_BECAUSE_THE_ARMOR_SET_EQUIP_HAS_BEEN_REMOVED);
			}

			player.sendPacket(new SkillList(player));
			player.updateStats();
		}
	}
}