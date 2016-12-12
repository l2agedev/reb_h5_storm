package l2r.gameserver.model.items.listeners;

import l2r.gameserver.listener.inventory.OnEquipListener;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.SkillCoolTime;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.stats.Formulas;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.item.ItemTemplate;

public final class ItemSkillsListener implements OnEquipListener
{
	private static final ItemSkillsListener _instance = new ItemSkillsListener();

	public static ItemSkillsListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		Player player = (Player)actor;

		Skill[] itemSkills = null;
		Skill enchant4Skill = null;

		ItemTemplate it = item.getTemplate();

		itemSkills = it.getAttachedSkills();

		enchant4Skill = it.getEnchant4Skill();

		player.removeTriggers(it);

		if(itemSkills != null && itemSkills.length > 0)
			for(Skill itemSkill : itemSkills)
				if(itemSkill.getId() >= 26046 && itemSkill.getId() <= 26048)
				{
					int level = player.getSkillLevel(itemSkill.getId());
					int newlevel = level - 1;
					if(newlevel > 0)
						player.addSkill(SkillTable.getInstance().getInfo(itemSkill.getId(), newlevel), false);
					else
						player.removeSkillById(itemSkill.getId());
				}
				else
				{
					player.removeSkill(itemSkill, false);
				}

		if(enchant4Skill != null)
			player.removeSkill(enchant4Skill, false);

		if(itemSkills.length > 0 || enchant4Skill != null)
		{
			player.sendPacket(new SkillList(player));
			player.updateStats();
		}
	}

	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		Player player = (Player)actor;

		Skill[] itemSkills = null;
		Skill enchant4Skill = null;

		ItemTemplate it = item.getTemplate();

		itemSkills = it.getAttachedSkills();

		if(item.getEnchantLevel() >= 4)
			enchant4Skill = it.getEnchant4Skill();

		// Weapon penalty
		if(it.getType2() == ItemTemplate.TYPE2_WEAPON && player.getWeaponsExpertisePenalty() > 0)
			return;

		// Accessory penalty
		if(it.getType2() == ItemTemplate.TYPE2_ACCESSORY && item.getCrystalType().ordinal() > player.getExpertiseIndex())
			return;
		
		player.addTriggers(it);

		boolean needSendInfo = false;
		if(itemSkills.length > 0)
			for(Skill itemSkill : itemSkills)
				if(itemSkill.getId() >= 26046 && itemSkill.getId() <= 26048)
				{
					int level = player.getSkillLevel(itemSkill.getId());
					int newlevel = level;
					if(level > 0)
					{
						if(SkillTable.getInstance().getInfo(itemSkill.getId(), level + 1) != null)
							newlevel = level + 1;
					}
					else
						newlevel = 1;
					if(newlevel != level)
					{
						player.addSkill(SkillTable.getInstance().getInfo(itemSkill.getId(), newlevel), false);
					}
				}
				else if(player.getSkillLevel(itemSkill.getId()) < itemSkill.getLevel())
				{
					player.addSkill(itemSkill, false);
					
					if (itemSkill.isActive())
					{
						long reuseDelay = it.getEquipReuseDelay();
						if (reuseDelay < 0)
							reuseDelay = Math.min(Formulas.calcSkillReuseDelay(player, itemSkill), 30000);
						
						if (reuseDelay > 0 && !player.isSkillDisabled(itemSkill))
						{
							player.disableSkill(itemSkill, reuseDelay);
							needSendInfo = true;
						}
					}
				}

		if(enchant4Skill != null)
			player.addSkill(enchant4Skill, false);

		if(itemSkills.length > 0 || enchant4Skill != null)
		{
			player.sendPacket(new SkillList(player));
			player.updateStats();
			if (needSendInfo)
				player.sendPacket(new SkillCoolTime(player));
		}
	}
}