package l2r.gameserver.nexus_interface;


import l2r.gameserver.model.actor.instances.player.ShortCut;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_engine.l2r.IValues;
import l2r.gameserver.nexus_engine.l2r.WeaponType;
import l2r.gameserver.nexus_interface.delegate.ItemData;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.templates.item.ItemTemplate;

public class Values implements IValues
{
	public void load()
	{
		CallBack.getInstance().setValues(this);
	}
	
	@Override
	public int PAPERDOLL_UNDER() { return Inventory.PAPERDOLL_UNDER; }
	@Override
	public int PAPERDOLL_HEAD() { return Inventory.PAPERDOLL_HEAD; }
	@Override
	public int PAPERDOLL_HAIR() { return Inventory.PAPERDOLL_HAIR; }
	@Override
	public int PAPERDOLL_HAIR2() { return Inventory.PAPERDOLL_DHAIR; }
	@Override
	public int PAPERDOLL_NECK() { return Inventory.PAPERDOLL_NECK; }
	@Override
	public int PAPERDOLL_RHAND() { return Inventory.PAPERDOLL_RHAND; }
	@Override
	public int PAPERDOLL_CHEST() { return Inventory.PAPERDOLL_CHEST; }
	@Override
	public int PAPERDOLL_LHAND() { return Inventory.PAPERDOLL_LHAND; }
	@Override
	public int PAPERDOLL_REAR() { return Inventory.PAPERDOLL_REAR; }
	@Override
	public int PAPERDOLL_LEAR() { return Inventory.PAPERDOLL_LEAR; }
	@Override
	public int PAPERDOLL_GLOVES() { return Inventory.PAPERDOLL_GLOVES; }
	@Override
	public int PAPERDOLL_LEGS() { return Inventory.PAPERDOLL_LEGS; }
	@Override
	public int PAPERDOLL_FEET() { return Inventory.PAPERDOLL_FEET; }
	@Override
	public int PAPERDOLL_RFINGER() { return Inventory.PAPERDOLL_RFINGER; }
	@Override
	public int PAPERDOLL_LFINGER() { return Inventory.PAPERDOLL_LFINGER; }
	@Override
	public int PAPERDOLL_LBRACELET() { return Inventory.PAPERDOLL_LBRACELET; }
	@Override
	public int PAPERDOLL_RBRACELET() { return Inventory.PAPERDOLL_RBRACELET; }
	@Override
	public int PAPERDOLL_DECO1() { return Inventory.PAPERDOLL_DECO1; }
	@Override
	public int PAPERDOLL_DECO2() { return Inventory.PAPERDOLL_DECO2; }
	@Override
	public int PAPERDOLL_DECO3() { return Inventory.PAPERDOLL_DECO3; }
	@Override
	public int PAPERDOLL_DECO4() { return Inventory.PAPERDOLL_DECO4; }
	@Override
	public int PAPERDOLL_DECO5() { return Inventory.PAPERDOLL_DECO5; }
	@Override
	public int PAPERDOLL_DECO6() { return Inventory.PAPERDOLL_DECO6; }
	@Override
	public int PAPERDOLL_CLOAK() { return Inventory.PAPERDOLL_BACK; }
	@Override
	public int PAPERDOLL_BELT() { return Inventory.PAPERDOLL_BELT; }
	@Override
	public int PAPERDOLL_TOTALSLOTS() { return Inventory.PAPERDOLL_MAX; }
	
	@Override
	public int SLOT_NONE() { return ItemTemplate.SLOT_NONE; }
	@Override
	public int SLOT_UNDERWEAR() { return ItemTemplate.SLOT_UNDERWEAR; }
	@Override
	public int SLOT_R_EAR() { return ItemTemplate.SLOT_R_EAR; }
	@Override
	public int SLOT_L_EAR() { return ItemTemplate.SLOT_L_EAR; }
	@Override
	public int SLOT_NECK() { return ItemTemplate.SLOT_NECK; }
	@Override
	public int SLOT_R_FINGER() { return ItemTemplate.SLOT_R_FINGER; }
	@Override
	public int SLOT_L_FINGER() { return ItemTemplate.SLOT_L_FINGER; }
	@Override
	public int SLOT_HEAD() { return ItemTemplate.SLOT_HEAD; }
	@Override
	public int SLOT_R_HAND() { return ItemTemplate.SLOT_R_HAND; }
	@Override
	public int SLOT_L_HAND() { return ItemTemplate.SLOT_L_HAND; }
	@Override
	public int SLOT_GLOVES() { return ItemTemplate.SLOT_GLOVES; }
	@Override
	public int SLOT_CHEST() { return ItemTemplate.SLOT_CHEST; }
	@Override
	public int SLOT_LEGS() { return ItemTemplate.SLOT_LEGS; }
	@Override
	public int SLOT_FEET() { return ItemTemplate.SLOT_FEET; }
	@Override
	public int SLOT_BACK() { return ItemTemplate.SLOT_BACK; }
	@Override
	public int SLOT_LR_HAND() { return ItemTemplate.SLOT_LR_HAND; }
	@Override
	public int SLOT_FULL_ARMOR() { return ItemTemplate.SLOT_FULL_ARMOR; }
	@Override
	public int SLOT_HAIR() { return ItemTemplate.SLOT_HAIR; }
	@Override
	public int SLOT_ALLDRESS() { return ItemTemplate.SLOT_FORMAL_WEAR; }
	@Override
	public int SLOT_HAIR2() { return ItemTemplate.SLOT_HAIRALL; }
	@Override
	public int SLOT_HAIRALL() { return ItemTemplate.SLOT_HAIRALL; }
	@Override
	public int SLOT_R_BRACELET() { return ItemTemplate.SLOT_R_BRACELET; }
	@Override
	public int SLOT_L_BRACELET() { return ItemTemplate.SLOT_L_BRACELET; }
	@Override
	public int SLOT_DECO() { return ItemTemplate.SLOT_DECO; }
	@Override
	public int SLOT_BELT() { return ItemTemplate.SLOT_BELT; }
	@Override
	public int SLOT_WOLF() { return ItemTemplate.SLOT_WOLF; }
	@Override
	public int SLOT_HATCHLING() { return ItemTemplate.SLOT_HATCHLING; }
	@Override
	public int SLOT_STRIDER() { return ItemTemplate.SLOT_STRIDER; }
	@Override
	public int SLOT_BABYPET() { return ItemTemplate.SLOT_BABYPET; }
	@Override
	public int SLOT_GREATWOLF() { return ItemTemplate.SLOT_GWOLF; }
	
	@Override
	public int CRYSTAL_NONE() { return ItemTemplate.CRYSTAL_NONE; }
	@Override
	public int CRYSTAL_D() { return ItemTemplate.CRYSTAL_D; }
	@Override
	public int CRYSTAL_C() { return ItemTemplate.CRYSTAL_C; }
	@Override
	public int CRYSTAL_B() { return ItemTemplate.CRYSTAL_B; }
	@Override
	public int CRYSTAL_A() { return ItemTemplate.CRYSTAL_A; }
	@Override
	public int CRYSTAL_S() { return ItemTemplate.CRYSTAL_S; }
	@Override
	public int CRYSTAL_S80() { return ItemTemplate.CRYSTAL_S; }
	@Override
	public int CRYSTAL_S84() { return ItemTemplate.CRYSTAL_S; }
	
	@Override
	public int TYPE_ITEM() { return ShortCut.TYPE_ITEM; }
	@Override
	public int TYPE_SKILL() { return ShortCut.TYPE_SKILL; }
	@Override
	public int TYPE_ACTION() { return ShortCut.TYPE_ACTION; }
	@Override
	public int TYPE_MACRO() { return ShortCut.TYPE_MACRO; }
	@Override
	public int TYPE_RECIPE() { return ShortCut.TYPE_RECIPE; }
	@Override
	public int TYPE_TPBOOKMARK() { return ShortCut.TYPE_TPBOOKMARK; }
	
	public WeaponType getWeaponType(ItemData item)
	{
		switch(item.getWeaponType())
		{
			case SWORD:
				return WeaponType.SWORD;
			case BLUNT:
				return WeaponType.BLUNT;
			case DAGGER:
				return WeaponType.DAGGER;
			case BOW:
				return WeaponType.BOW;
			case POLE:
				return WeaponType.POLE;
			case NONE:
				return WeaponType.NONE;
			case DUAL:
				return WeaponType.DUAL;
			case ETC:
				return WeaponType.ETC;
			case FIST:
				return WeaponType.FIST;
			case DUALFIST:
				return WeaponType.DUALFIST;
			case FISHINGROD:
				return WeaponType.FISHINGROD;
			case RAPIER:
				return WeaponType.RAPIER;
			case ANCIENTSWORD:
				return WeaponType.ANCIENTSWORD;
			case CROSSBOW:
				return WeaponType.CROSSBOW;
			case FLAG:
				return WeaponType.FLAG;
			case OWNTHING:
				return WeaponType.OWNTHING;
			case DUALDAGGER:
				return WeaponType.DUALDAGGER;
			case BIGBLUNT:
				return WeaponType.BIGBLUNT;
			case BIGSWORD:
				return WeaponType.BIGSWORD;
			default:
				return null;
		}
	}
	
	@Override
	public int ABNORMAL_NULL() { return AbnormalEffect.NULL.getMask(); }
	@Override
	public int ABNORMAL_BLEEDING() { return AbnormalEffect.BLEEDING.getMask(); }
	@Override
	public int ABNORMAL_POISON() { return AbnormalEffect.POISON.getMask(); }
	@Override
	public int ABNORMAL_REDCIRCLE() { return AbnormalEffect.REDCIRCLE.getMask(); }
	@Override
	public int ABNORMAL_ICE() { return AbnormalEffect.ICE.getMask(); }
	@Override
	public int ABNORMAL_WIND() { return AbnormalEffect.AFFRAID.getMask(); }
	@Override
	public int ABNORMAL_FEAR() { return AbnormalEffect.CONFUSED.getMask(); }
	@Override
	public int ABNORMAL_STUN() { return AbnormalEffect.STUN.getMask(); }
	@Override
	public int ABNORMAL_SLEEP() { return AbnormalEffect.SLEEP.getMask(); }
	@Override
	public int ABNORMAL_MUTED() { return AbnormalEffect.MUTED.getMask(); }
	@Override
	public int ABNORMAL_ROOT() { return AbnormalEffect.ROOT.getMask(); }
	@Override
	public int ABNORMAL_HOLD_1() { return AbnormalEffect.HOLD_1.getMask(); }
	@Override
	public int ABNORMAL_HOLD_2() { return AbnormalEffect.HOLD_2.getMask(); }
	@Override
	public int ABNORMAL_UNKNOWN_13() { return AbnormalEffect.UNKNOWN_13.getMask(); }
	@Override
	public int ABNORMAL_BIG_HEAD() { return AbnormalEffect.BIG_HEAD.getMask(); }
	@Override
	public int ABNORMAL_FLAME() { return AbnormalEffect.FLAME.getMask(); }
	@Override
	public int ABNORMAL_UNKNOWN_16() { return AbnormalEffect.UNKNOWN_16.getMask(); }
	@Override
	public int ABNORMAL_GROW() { return AbnormalEffect.GROW.getMask(); }
	@Override
	public int ABNORMAL_FLOATING_ROOT() { return AbnormalEffect.FLOATING_ROOT.getMask(); }
	@Override
	public int ABNORMAL_DANCE_STUNNED() { return AbnormalEffect.DANCE_STUNNED.getMask(); }
	@Override
	public int ABNORMAL_FIREROOT_STUN() { return AbnormalEffect.FIREROOT_STUN.getMask(); }
	@Override
	public int ABNORMAL_STEALTH() { return AbnormalEffect.STEALTH.getMask(); }
	@Override
	public int ABNORMAL_IMPRISIONING_1() { return AbnormalEffect.IMPRISIONING_1.getMask(); }
	@Override
	public int ABNORMAL_IMPRISIONING_2() { return AbnormalEffect.IMPRISIONING_2.getMask(); }
	@Override
	public int ABNORMAL_MAGIC_CIRCLE() { return AbnormalEffect.MAGIC_CIRCLE.getMask(); }
	@Override
	public int ABNORMAL_ICE2() { return AbnormalEffect.ICE2.getMask(); }
	@Override
	public int ABNORMAL_EARTHQUAKE() { return AbnormalEffect.EARTHQUAKE.getMask(); }
	@Override
	public int ABNORMAL_UNKNOWN_27() { return AbnormalEffect.UNKNOWN_27.getMask(); }
	@Override
	public int ABNORMAL_INVULNERABLE() { return AbnormalEffect.INVULNERABLE.getMask(); }
	@Override
	public int ABNORMAL_VITALITY() { return AbnormalEffect.VITALITY.getMask(); }
	@Override
	public int ABNORMAL_REAL_TARGET() { return AbnormalEffect.REAL_TARGET.getMask(); }
	@Override
	public int ABNORMAL_DEATH_MARK() { return AbnormalEffect.DEATH_MARK.getMask(); }
	@Override
	public int ABNORMAL_SKULL_FEAR() { return AbnormalEffect.SOUL_SHOCK.getMask(); }
	//CONFUSED("confused", 0x0020.getMask(); }
	
	// special effects
	@Override
	public int ABNORMAL_S_INVINCIBLE() { return AbnormalEffect.S_INVULNERABLE.getMask(); }
	@Override
	public int ABNORMAL_S_AIR_STUN() { return AbnormalEffect.S_AIR_STUN.getMask(); }
	@Override
	public int ABNORMAL_S_AIR_ROOT() { return AbnormalEffect.S_AIR_ROOT.getMask(); }
	@Override
	public int ABNORMAL_S_BAGUETTE_SWORD() { return AbnormalEffect.S_BAGUETTE_SWORD.getMask(); }
	@Override
	public int ABNORMAL_S_YELLOW_AFFRO() { return AbnormalEffect.S_YELLOW_AFFRO.getMask(); }
	@Override
	public int ABNORMAL_S_PINK_AFFRO() { return AbnormalEffect.S_PINK_AFFRO.getMask(); }
	@Override
	public int ABNORMAL_S_BLACK_AFFRO() { return AbnormalEffect.S_BLACK_AFFRO.getMask(); }
	@Override
	public int ABNORMAL_S_UNKNOWN8() { return AbnormalEffect.AVE_VP_KEEP.getMask(); }
	@Override
	public int ABNORMAL_S_STIGMA_SHILIEN() { return AbnormalEffect.S_STIGMA.getMask(); }
	@Override
	public int ABNORMAL_S_STAKATOROOT() { return AbnormalEffect.FLOATING_ROOT.getMask(); }
	@Override
	public int ABNORMAL_S_FREEZING() { return AbnormalEffect.DANCE_STUNNED.getMask(); }
	@Override
	public int ABNORMAL_S_VESPER() { return AbnormalEffect.FIREROOT_STUN.getMask(); }
	
	// event effects
	@Override
	public int ABNORMAL_E_AFRO_1() { return AbnormalEffect.E_AFRO_1.getMask(); }
	@Override
	public int ABNORMAL_E_AFRO_2() { return AbnormalEffect.E_AFRO_2.getMask(); }
	@Override
	public int ABNORMAL_E_AFRO_3() { return AbnormalEffect.E_AFRO_3.getMask(); }
	@Override
	public int ABNORMAL_E_EVASWRATH() { return AbnormalEffect.E_EVASWRATH.getMask(); }
	@Override
	public int ABNORMAL_E_HEADPHONE() { return AbnormalEffect.E_HEADPHONE.getMask(); }
	@Override
	public int ABNORMAL_E_VESPER_1() { return AbnormalEffect.E_VESPER_1.getMask(); }
	@Override
	public int ABNORMAL_E_VESPER_2() { return AbnormalEffect.E_VESPER_2.getMask(); }
	@Override
	public int ABNORMAL_E_VESPER_3() { return AbnormalEffect.E_VESPER_3.getMask(); }
	
	public static final Values getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final Values _instance = new Values();
	}
}
