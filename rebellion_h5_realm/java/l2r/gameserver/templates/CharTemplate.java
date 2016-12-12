package l2r.gameserver.templates;

import l2r.gameserver.templates.item.WeaponTemplate.WeaponType;

public class CharTemplate
{
	private final static int[] EMPTY_ATTRIBUTES = new int[6];
	
	private final int baseSTR;
	private final int baseCON;
	private final int baseDEX;
	private final int baseINT;
	private final int baseWIT;
	private final int baseMEN;
	private final double baseHpMax;
	private final double baseCpMax;
	private final double baseMpMax;

	/** HP Regen base */
	private final double baseHpReg;

	/** MP Regen base */
	private final double baseMpReg;

	/** CP Regen base */
	private final double baseCpReg;

	private final int basePAtk;
	private final int baseMAtk;
	private final int basePDef;
	private final int baseMDef;
	private final int basePAtkSpd;
	private final int baseMAtkSpd;
	private final int baseShldDef;
	private final int baseAtkRange;
	private final int baseShldRate;
	private final int baseCritRate;
	private final int baseRunSpd;
	private final int baseWalkSpd;
	private final int baseWaterRunSpd;
	private final int baseWaterWalkSpd;

	private final int[] baseAttributeAttack;
	private final int[] baseAttributeDefence;
	
	private final WeaponType baseAttackType;
	
	private final double collisionRadius;
	private final double collisionHeight;

	public CharTemplate(StatsSet set)
	{
		baseSTR = set.getInteger("baseSTR");
		baseCON = set.getInteger("baseCON");
		baseDEX = set.getInteger("baseDEX");
		baseINT = set.getInteger("baseINT");
		baseWIT = set.getInteger("baseWIT");
		baseMEN = set.getInteger("baseMEN");
		baseHpMax = set.getDouble("baseHpMax");
		baseCpMax = set.getDouble("baseCpMax");
		baseMpMax = set.getDouble("baseMpMax");
		baseHpReg = set.getDouble("baseHpReg");
		baseCpReg = set.getDouble("baseCpReg");
		baseMpReg = set.getDouble("baseMpReg");
		basePAtk = set.getInteger("basePAtk");
		baseMAtk = set.getInteger("baseMAtk");
		basePDef = set.getInteger("basePDef");
		baseMDef = set.getInteger("baseMDef");
		basePAtkSpd = set.getInteger("basePAtkSpd");
		baseMAtkSpd = set.getInteger("baseMAtkSpd");
		baseShldDef = set.getInteger("baseShldDef");
		baseAtkRange = set.getInteger("baseAtkRange");
		baseShldRate = set.getInteger("baseShldRate");
		baseCritRate = set.getInteger("baseCritRate");
		baseRunSpd = set.getInteger("baseRunSpd");
		baseWalkSpd = set.getInteger("baseWalkSpd");
		baseWaterRunSpd = set.getInteger("baseWaterRunSpd", 50);
		baseWaterWalkSpd = set.getInteger("baseWaterWalkSpd", 50);
		
		baseAttributeAttack = set.getIntegerArray("baseAttributeAttack", EMPTY_ATTRIBUTES);
		baseAttributeDefence = set.getIntegerArray("baseAttributeDefence", EMPTY_ATTRIBUTES);
		
		baseAttackType = WeaponType.valueOf(set.getString("baseAttackType", "NONE").toUpperCase());
		
		// Geometry
		collisionRadius = set.getDouble("collision_radius", 5);
		collisionHeight = set.getDouble("collision_height", 5);
	}

	public int getBaseSTR()
	{
		return baseSTR;
	}
	
	public int getBaseCON()
	{
		return baseCON;
	}
	
	public int getBaseDEX()
	{
		return baseDEX;
	}
	
	public int getBaseINT()
	{
		return baseINT;
	}
	
	public int getBaseWIT()
	{
		return baseWIT;
	}
	
	public int getBaseMEN()
	{
		return baseMEN;
	}
	
	public double getBaseHpMax()
	{
		return baseHpMax;
	}
	
	public double getBaseCpMax()
	{
		return baseCpMax;
	}
	
	public double getBaseMpMax()
	{
		return baseMpMax;
	}
	
	public double getBaseHpReg()
	{
		return baseHpReg;
	}
	
	public double getBaseMpReg()
	{
		return baseMpReg;
	}
	
	public double getBaseCpReg()
	{
		return baseCpReg;
	}
	
	public int getBasePAtk()
	{
		return basePAtk;
	}
	
	public int getBaseMAtk()
	{
		return baseMAtk;
	}
	
	public int getBasePDef()
	{
		return basePDef;
	}
	
	public int getBaseMDef()
	{
		return baseMDef;
	}
	
	public int getBasePAtkSpd()
	{
		return basePAtkSpd;
	}
	
	public int getBaseMAtkSpd()
	{
		return baseMAtkSpd;
	}
	
	public int getBaseShldDef()
	{
		return baseShldDef;
	}
	
	public int getBaseAtkRange()
	{
		return baseAtkRange;
	}
	
	public int getBaseShldRate()
	{
		return baseShldRate;
	}
	
	public int getBaseCritRate()
	{
		return baseCritRate;
	}
	
	public int getBaseRunSpd()
	{
		return baseRunSpd;
	}
	
	public int getBaseWalkSpd()
	{
		return baseWalkSpd;
	}
	
	public int getBaseWaterRunSpd()
	{
		return baseWaterRunSpd;
	}
	
	public int getBaseWaterWalkSpd()
	{
		return baseWaterWalkSpd;
	}
	
	public int[] getBaseAttributeAttack()
	{
		return baseAttributeAttack;
	}
	
	public int[] getBaseAttributeDefence()
	{
		return baseAttributeDefence;
	}
	
	public double getCollisionRadius()
	{
		return collisionRadius;
	}
	
	public double getCollisionHeight()
	{
		return collisionHeight;
	}
	
	public WeaponType getBaseAttackType()
	{
		return baseAttackType;
	}
	
	public int getNpcId()
	{
		return 0;
	}

	public static StatsSet getEmptyStatsSet()
	{
		StatsSet npcDat = new StatsSet();
		npcDat.set("baseSTR", 0);
		npcDat.set("baseCON", 0);
		npcDat.set("baseDEX", 0);
		npcDat.set("baseINT", 0);
		npcDat.set("baseWIT", 0);
		npcDat.set("baseMEN", 0);
		npcDat.set("baseHpMax", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("baseMpMax", 0);
		npcDat.set("baseHpReg", 3.e-3f);
		npcDat.set("baseCpReg", 0);
		npcDat.set("baseMpReg", 3.e-3f);
		npcDat.set("basePAtk", 0);
		npcDat.set("baseMAtk", 0);
		npcDat.set("basePDef", 100);
		npcDat.set("baseMDef", 100);
		npcDat.set("basePAtkSpd", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("baseShldDef", 0);
		npcDat.set("baseAtkRange", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseCritRate", 0);
		npcDat.set("baseRunSpd", 0);
		npcDat.set("baseWalkSpd", 0);
		npcDat.set("baseWaterRunSpd", 0);
		npcDat.set("baseWaterWalkSpd", 0);
		npcDat.set("baseAttackType", "NONE");
		return npcDat;
	}
}