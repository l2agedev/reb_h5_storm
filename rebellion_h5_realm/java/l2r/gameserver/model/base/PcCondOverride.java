/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.base;

/**
 * @author UnAfraid
 */
public enum PcCondOverride
{
	MAX_STATS_VALUE("Override maximum stats conditions"),
	ITEM_USE_CONDITIONS("Override item conditions that prevents you from using it"),
	ITEM_DESTROY_CONDITIONS("Override item destroy conditions"),
	ITEM_DROP_CONDITIONS("Override item drop conditions"),
	ITEM_TRADE_CONDITIONS("Override item conditions that involve player trading"),
	ITEM_SHOP_CONDITIONS("Override item conditions that involve NPC shopping"),
	SKILL_CONDITIONS("Cast skills while skipping consequences and required conditions"),
	SKILL_REUSE_CONDITIONS("Cast skills without checking reuse"),
	SKILL_TARGET_CONDITIONS("Cast skills without checking target conditions"),
	ZONE_CONDITIONS("Override zone conditions"),
	CASTLE_CONDITIONS("Override castle conditions"),
	CHAT_CONDITIONS("Override chat conditions"),
	INSTANCE_CONDITIONS("Override instance conditions"),
	QUEST_CONDITIONS("Override quest conditions"),
	DEATH_PENALTY("Override death penalty conditions"),
	SEE_ALL_PLAYERS("Can see hidden players"),
	HERO_AURA_CONDITIONS("Appear to everyone as a hero"),
	TELEPORT_BOOKMARK_CONDITIONS("Override My Teleports conditions"),
	HTML_ACTION_CONDITIONS("Override HTMLs that have special part for GMs"),
	DEBUG_CONDITIONS("View debug messages"),
	SKILL_ENCHANT_CONDITIONS("Can Enchant skill with one request");
	
	private final long _mask;
	private final String _descr;
	
	private PcCondOverride(String descr)
	{
		_mask = 1L << ordinal();
		_descr = descr;
	}
	
	public long getMask()
	{
		return _mask;
	}
	
	public String getDescription()
	{
		return _descr;
	}
	
	public static PcCondOverride getCondOverride(int ordinal)
	{
		try
		{
			return values()[ordinal];
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public static long getAllExceptionsMask()
	{
		long result = 0L;
		for (PcCondOverride ex : values())
		{
			result |= ex.getMask();
		}
		return result;
	}
}
