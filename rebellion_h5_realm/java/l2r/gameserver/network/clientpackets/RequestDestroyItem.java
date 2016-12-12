package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ExGMViewQuestItemList;
import l2r.gameserver.network.serverpackets.GMHennaInfo;
import l2r.gameserver.network.serverpackets.GMViewItemList;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.tables.PetDataTable;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Log;
public class RequestDestroyItem extends L2GameClientPacket
{
	private int _objectId;
	private long _count;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if (Config.SECURITY_ENABLED && Config.SECURITY_ITEM_DESTROY_ENABLED && activeChar.getSecurity())
		{
			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			return;
		}
		
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		long count = _count;

		ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if (item == null && activeChar.canOverrideCond(PcCondOverride.ITEM_DESTROY_CONDITIONS))	// Support for GMs deleting items from alt+g inventory.
		{
			for (Player player : GameObjectsStorage.getAllPlayersForIterate()) // There is no way to get item by objectId!!! Or im very stupid to not know such.
			{
				if ((item = player.getInventory().getItemByObjectId(_objectId)) != null)
					break;
			}
		}
		
		if(item == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if (!activeChar.canOverrideCond(PcCondOverride.ITEM_DESTROY_CONDITIONS) && !NexusEvents.canDestroyItem(activeChar, item))
		{
			activeChar.sendPacket(SystemMsg.THIS_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		
		if(count < 1)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DESTROY_IT_BECAUSE_THE_NUMBER_IS_INCORRECT);
			return;
		}

		if(!activeChar.canOverrideCond(PcCondOverride.ITEM_DESTROY_CONDITIONS) && item.isHeroWeapon())
		{
			activeChar.sendPacket(SystemMsg.HERO_WEAPONS_CANNOT_BE_DESTROYED);
			return;
		}

		if(activeChar.getPet() != null && activeChar.getPet().getControlItemObjId() == item.getObjectId())
		{
			activeChar.sendPacket(SystemMsg.THE_PET_HAS_BEEN_SUMMONED_AND_CANNOT_BE_DELETED);
			return;
		}

		if(!activeChar.canOverrideCond(PcCondOverride.ITEM_DESTROY_CONDITIONS) && !item.canBeDestroyed(activeChar))
		{
			activeChar.sendPacket(SystemMsg.THIS_ITEM_CANNOT_BE_DISCARDED);
			return;
		}

		if(_count > item.getCount())
			count = item.getCount();

		boolean crystallize = item.canBeCrystallized(activeChar);

		int crystalId = item.getTemplate().getCrystalType().cry;
		int crystalAmount = item.getTemplate().getCrystalCount();
		if(crystallize)
		{
			int level = activeChar.getSkillLevel(Skill.SKILL_CRYSTALLIZE);
			if(level < 1 || crystalId - ItemTemplate.CRYSTAL_D + 1 > level)
				crystallize = false;
		}

		Log.LogItem(activeChar, Log.Delete, item, count);

		if (item.getOwnerId() == activeChar.getObjectId())
		{
			if (!activeChar.getInventory().destroyItemByObjectId(_objectId, count))
			{			
				activeChar.sendActionFailed();
				return;
			}
		}
		else // Support for GM item deletion through Alt+G inventory.
		{
			Player owner = World.getPlayer(item.getOwnerId());
			if (owner != null)
			{
				// If item is successfully deleted, show updated target inventory.
				if (owner.getInventory().destroyItemByObjectId(_objectId, count))
				{
					ItemInstance[] items = owner.getInventory().getItems();
					int questSize = 0;
					for(ItemInstance i : items)
						if(i.getTemplate().isQuest())
							questSize ++;
					
					activeChar.sendPacket(new GMViewItemList(owner, items, items.length - questSize));
					activeChar.sendPacket(new ExGMViewQuestItemList(owner, items, questSize));
					activeChar.sendPacket(new GMHennaInfo(owner));
				}
				else
				{	
					activeChar.sendActionFailed();
					return;
				}
			}	
		}

		// При удалении ошейника, удалить пета
		if(PetDataTable.isPetControlItem(item))
			PetDataTable.deletePet(item, activeChar);

		if(crystallize)
		{
			activeChar.sendPacket(SystemMsg.THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED);
			ItemFunctions.addItem(activeChar, crystalId, crystalAmount, true);
		}
		else
			activeChar.sendPacket(SystemMessage2.removeItems(item.getItemId(), count));

		activeChar.sendChanges();
	}
}