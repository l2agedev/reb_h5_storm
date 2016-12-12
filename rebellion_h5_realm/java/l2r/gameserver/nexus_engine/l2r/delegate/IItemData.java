/**
 * 
 */
package l2r.gameserver.nexus_engine.l2r.delegate;

import l2r.gameserver.nexus_engine.l2r.WeaponType;
import l2r.gameserver.templates.item.ItemTemplate.Grade;

/**
 * @author hNoke
 *
 */
public interface IItemData
{
	public boolean exists();
	
	public boolean isEquipped();
	
	public int getObjectId();
	public int getItemId();
	public String getItemName();
	public int getEnchantLevel();
	public Grade getCrystalType();
	public int getBodyPart();
	
	public boolean isArmor();
	public boolean isWeapon();
	
	public WeaponType getWeaponType();
	
	public boolean isType2Armor();
	public boolean isType2Weapon();
	public boolean isType2Accessory();
	
	public boolean isJewellery();
	public boolean isPotion();
	public boolean isScroll();
	public boolean isPetCollar();

	public String getTier();

	public void setEnchantLevel(int level);
	public void restoreEnchantLevel();
}
