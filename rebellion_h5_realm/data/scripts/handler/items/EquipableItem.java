package handler.items;

import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.handler.items.ItemHandler;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.ItemFunctions;

import gnu.trove.set.hash.TIntHashSet;

public class EquipableItem extends ScriptItemHandler implements ScriptFile
{
	private int[] _itemIds;


	@Override
	public boolean pickupItem(Playable playable, ItemInstance item)
	{
		return true;
	}

	@Override
	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	@Override
	public void onReload()
	{

	}

	@Override
	public void onShutdown()
	{

	}

	public EquipableItem()
	{
		TIntHashSet set = new TIntHashSet();
		for(ItemTemplate template : ItemHolder.getInstance().getAllTemplates())
		{
			if(template == null)
				continue;
			if(template.isEquipable())
				set.add(template.getItemId());
		}
		_itemIds = set.toArray();
	}

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(!playable.isPlayer())
			return false;
		Player player = playable.getPlayer();
		if(player.isCastingNow())
		{
			player.sendPacket(Msg.YOU_MAY_NOT_EQUIP_ITEMS_WHILE_CASTING_OR_PERFORMING_A_SKILL);
			return false;
		}

		if (Config.SECURITY_ENABLED && Config.SECURITY_ITEM_UNEQUIP && player.getSecurity())
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (player.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			return false;
		}
		
		// Нельзя снимать/одевать любое снаряжение при этих условиях
		if(player.isStunned() || player.isSleeping() || player.isParalyzed() || player.isAlikeDead() || player.isWeaponEquipBlocked())
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return false;
		}

		int bodyPart = item.getBodyPart();

		if(bodyPart == ItemTemplate.SLOT_LR_HAND || bodyPart == ItemTemplate.SLOT_L_HAND || bodyPart == ItemTemplate.SLOT_R_HAND)
		{
			// Нельзя снимать/одевать оружие, сидя на пете
			// Нельзя снимать/одевать проклятое оружие и флаги
			// Нельзя одевать/снимать оружие/щит/сигил, управляя кораблем
			if(player.isMounted() || player.isCursedWeaponEquipped() || player.getActiveWeaponFlagAttachment() != null || player.isClanAirShipDriver())
			{
				player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
				return false;
			}
		}

		// Нельзя снимать/одевать проклятое оружие
		if(item.isCursed())
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return false;
		}
		
		// Don't allow weapon/shield hero equipment during Olympiads
		if(player.isInOlympiadMode() && item.isHeroWeapon())
		{
			player.sendActionFailed();
			return false;
		}

		if(item.isEquipped())
		{
			ItemInstance weapon = player.getActiveWeaponInstance();
			if(item == weapon)
			{
				player.abortAttack(true, true);
				player.abortCast(true, true);
			}
			player.sendDisarmMessage(item);
			player.getInventory().unEquipItem(item);
			return false;
		}

		L2GameServerPacket p = ItemFunctions.checkIfCanEquip(player, item);
		if(p != null)
		{
			player.sendPacket(p);
			return false;
		}

		player.getInventory().equipItem(item);
		if(!item.isEquipped())
		{
			player.sendActionFailed();
			return false;
		}

		SystemMessage sm;
		if(item.getEnchantLevel() > 0)
		{
			sm = new SystemMessage(SystemMessage.EQUIPPED__S1_S2);
			sm.addNumber(item.getEnchantLevel());
			sm.addItemName(item.getItemId());
		}
		else
			sm = new SystemMessage(SystemMessage.YOU_HAVE_EQUIPPED_YOUR_S1).addItemName(item.getItemId());

		player.sendPacket(sm);
		return true;
	}

	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
