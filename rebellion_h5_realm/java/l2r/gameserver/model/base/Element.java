package l2r.gameserver.model.base;

import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.stats.Stats;

import java.util.Arrays;


public enum Element
{
	FIRE(0, Stats.ATTACK_FIRE, Stats.DEFENCE_FIRE),
	WATER(1, Stats.ATTACK_WATER, Stats.DEFENCE_WATER),
	WIND(2, Stats.ATTACK_WIND, Stats.DEFENCE_WIND),
	EARTH(3, Stats.ATTACK_EARTH, Stats.DEFENCE_EARTH),
	HOLY(4, Stats.ATTACK_HOLY, Stats.DEFENCE_HOLY),
	UNHOLY(5, Stats.ATTACK_UNHOLY, Stats.DEFENCE_UNHOLY),
	NONE(-2, null, null);

	/** Массив элементов без NONE **/
	public final static Element[] VALUES = Arrays.copyOf(values(), 6);

	private final int id;
	private final Stats attack;
	private final Stats defence;

	private Element(int id, Stats attack, Stats defence)
	{
		this.id = id;
		this.attack = attack;
		this.defence = defence;
	}

	public int getId()
	{
		return id;
	}

	public Stats getAttack()
	{
		return attack;
	}

	public Stats getDefence()
	{
		return defence;
	}

	public static Element getElementById(int id)
	{
		for(Element e : VALUES)
			if(e.getId() == id)
				return e;
		return NONE;
	}

	/**
	 * Возвращает противоположный тип элемента
	 * @return значение элемента
	 */
	public static Element getReverseElement(Element element)
	{
		switch(element)
		{
			case WATER:
				return FIRE;
			case FIRE:
				return WATER;
			case WIND:
				return EARTH;
			case EARTH:
				return WIND;
			case HOLY:
				return UNHOLY;
			case UNHOLY:
				return HOLY;
		}

		return NONE;
	}
	
	/**
	 * @param attr attribute ordinal.
	 * @param item the item whose attributes will be checked.
	 * @return True if there is no opposite attribute to the given.
	 */
	public static boolean canAttributeArmor(int attr, ItemInstance item)
	{
		return canAttributeArmor(getElementById(attr), item);
	}
	
	/**
	 * @param item the item whose attributes will be checked.
	 * @return True if there is no opposite attribute to the given.
	 */
	public static boolean canAttributeArmor(Element ele, ItemInstance item)
	{
		switch (ele)
		{
			case FIRE: return item.getDefenceWater() == 0;
			case WATER: return item.getDefenceFire() == 0;
			case WIND: return item.getDefenceEarth() == 0;
			case EARTH: return item.getDefenceWind() == 0;
			case UNHOLY: return item.getDefenceUnholy() == 0;
			case HOLY: return item.getDefenceHoly() == 0;
			default: return true;
		}
	}

	public static Element getElementByName(String name)
	{
		for(Element e : VALUES)
			if(e.name().equalsIgnoreCase(name))
				return e;
		return NONE;
	}
}
