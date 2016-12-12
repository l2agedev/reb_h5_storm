package l2r.gameserver.nexus_interface.delegate;


import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.nexus_engine.l2r.WeaponType;
import l2r.gameserver.nexus_engine.l2r.delegate.IItemData;
import l2r.gameserver.nexus_interface.Values;
import l2r.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.templates.item.ItemTemplate.Grade;
import l2r.gameserver.utils.ItemFunctions;

/**
 * @author hNoke
 *
 */
public class ItemData implements IItemData
{
	private ItemInstance _item;
	private ItemTemplate _itemTemplate;
	
	/** creates item template */
	public ItemData(int id)
	{
		_item = null;
		_itemTemplate = ItemHolder.getInstance().getTemplate(id);
	}
	
	/** creates a delegate for given ItemInstance */
	public ItemData(ItemInstance cha)
	{
		_item = cha;
		
		if(_item != null)
			_itemTemplate = _item.getTemplate();
	}
	
	/** creates NEW ItemInstance */
	public ItemData(int itemId, int count)
	{
		_item = ItemFunctions.createDummyItem(itemId);
		_item.setCount(count);
		
		if(_item != null)
			_itemTemplate = _item.getTemplate();
	}
	
	public ItemInstance getOwner()
	{
		return _item;
	}
	
	@Override
	public int getObjectId()
	{
		if(exists())
			return getOwner().getObjectId();
		return -1;
	}
	
	public ItemTemplate getTemplate()
	{
		return _itemTemplate;
	}
	
	@Override
	public boolean exists()
	{
		return _item != null;
	}
	
	@Override
	public boolean isEquipped()
	{
		if(exists() && _item.isEquipped())
			return true;
		return false;
	}
	
	@Override
	public int getItemId()
	{
		return _itemTemplate.getItemId();
	}
	
	@Override
	public String getItemName()
	{
		return _itemTemplate.getName();
	}
	
	@Override
	public int getEnchantLevel()
	{
		return _item != null ? _item.getEnchantLevel() : 0;
	}
	
	@Override
	public Grade getCrystalType()
	{
		return _itemTemplate.getCrystalType();
	}
	
	@Override
	public int getBodyPart()
	{
		return _itemTemplate.getBodyPart();
	}
	
	@Override
	public boolean isArmor()
	{
		return _itemTemplate.isArmor();
	}
	
	@Override
	public boolean isWeapon()
	{
		return _itemTemplate.isWeapon();
	}
	
	@Override
	public WeaponType getWeaponType()
	{
		if(isWeapon())
			return Values.getInstance().getWeaponType(this);
		else 
			return null;
	}
	
	@Override
	public boolean isType2Armor()
	{
		return _itemTemplate.getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR;
	}
	
	@Override
	public boolean isType2Weapon()
	{
		return _itemTemplate.getType2() == ItemTemplate.TYPE2_WEAPON;
	}
	
	@Override
	public boolean isType2Accessory()
	{
		return _itemTemplate.getType2() == ItemTemplate.TYPE2_ACCESSORY;
	}
	
	@Override
	public boolean isJewellery()
	{
		return _itemTemplate.getType2() == ItemTemplate.TYPE2_ACCESSORY;
	}
	
	@Override
	public boolean isPotion()
	{
		return _itemTemplate.getItemType() == EtcItemType.POTION;
	}
	
	@Override
	public boolean isScroll()
	{
		return _itemTemplate.getItemType() == EtcItemType.SCROLL;
	}
	
	@Override
	public boolean isPetCollar()
	{
		if(_item != null && _item.getTemplate().getItemType() == EtcItemType.PET_COLLAR)
			return true;
		return false;
	}
	
	@Override
	public String getTier()
	{
		String tier = null;
		//tier = _item.getTier();
		return tier;
	}
	
	@Override
	// temporary changes item's enchant level
	public void setEnchantLevel(int level)
	{
		if(_item.isArmor() || _item.isWeapon())
		{
			
		}
	}
	
	@Override
	// restores enchant level back to original value
	public void restoreEnchantLevel()
	{
		
	}
}
