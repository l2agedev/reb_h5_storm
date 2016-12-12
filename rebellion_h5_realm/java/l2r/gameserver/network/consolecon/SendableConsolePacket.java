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
package l2r.gameserver.network.consolecon;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.SubClass;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.pledge.UnitMember;
import l2r.gameserver.tables.SkillTreeTable;

import java.util.List;

import org.inc.nionetcore.SendableNioNetPacket;

/**
 * @author Forsaiken
 */
public abstract class SendableConsolePacket extends SendableNioNetPacket<Console>
{
	public static final byte COMBAT_TYPE_PATK = 0;
	public static final byte COMBAT_TYPE_PDEF = 1;
	public static final byte COMBAT_TYPE_ACCURACY = 2;
	public static final byte COMBAT_TYPE_CRIT_RATE = 3;
	public static final byte COMBAT_TYPE_ATK_SPD = 4;
	public static final byte COMBAT_TYPE_MATK = 5;
	public static final byte COMBAT_TYPE_MDEF = 6;
	public static final byte COMBAT_TYPE_EVASION = 7;
	public static final byte COMBAT_TYPE_SPEED = 8;
	public static final byte COMBAT_TYPE_CASTING_SPEED = 9;
	
	public static final byte STAT_STR = 10;
	public static final byte STAT_INT = 11;
	public static final byte STAT_DEX = 12;
	public static final byte STAT_WIT = 13;
	public static final byte STAT_CON = 14;
	public static final byte STAT_MEN = 15;
	
	public static final byte SOCIAL_KARMA = 16;
	public static final byte SOCIAL_PVP = 17;
	public static final byte SOCIAL_PK = 18;
	public static final byte SOCIAL_FAME = 19;
	public static final byte SOCIAL_RECOMMENT_HAVE = 20;
	public static final byte SOCIAL_RECOMMENT_LEFT = 21;
	
	public static final byte ATTRIBUTE_ATK_TYPE = 22;
	public static final byte ATTRIBUTE_FIRE = 23;
	public static final byte ATTRIBUTE_WIND = 24;
	public static final byte ATTRIBUTE_HOLY = 25;
	
	public static final byte ATTRIBUTE_ATK_VALUE = 26;
	public static final byte ATTRIBUTE_WATER = 27;
	public static final byte ATTRIBUTE_EARTH = 28;
	public static final byte ATTRIBUTE_UNHOLY = 29;
	
	protected SendableConsolePacket()
	{
		
	}
	
	@Override
	public final boolean write()
	{
		try
		{
			writeImpl();
			return true;
		}
		catch (final Throwable e)
		{
			e.printStackTrace();
			
			final Console console = super.getClient();
			if (console != null)
				console.close(null);
			
			return false;
		}
	}
	
	protected abstract void writeImpl();
	
	protected final void writePlayers(final Player[] players, final boolean full)
	{
		super.writeH(players.length);
		for (int i = 0; i < players.length; i++)
		{
			writePlayer(players[i], full);
		}
	}
	
	protected final void writePlayer(final Player player, final boolean full)
	{
		super.writeD(player.getObjectId());
		super.writeS(player.getName());
		if (full) super.writeS(player.getTitle());
		super.writeC(player.getLevel());
		super.writeC(player.getRace().ordinal());
		if (full)
		{
			int bitSet = 0;
			final int BIT_SET_NOBLE = 1 << 0;
			final int BIT_SET_HERO = 1 << 1;
			if (player.isNoble())
				bitSet |= BIT_SET_NOBLE;
			
			if (player.isHero())
				bitSet |= BIT_SET_HERO;
			
			super.writeC(bitSet);
		}
		
		super.writeC(player.getActiveClassId());
		if (full)
		{
			super.writeC(player.getBaseClassId());
			
			final int[][] subClasses = getSubClasses(player);
			super.writeC(subClasses.length);
			for (final int[] subClassIdLevel : subClasses)
			{
				super.writeC(subClassIdLevel[0]);
				super.writeC(subClassIdLevel[1]);
			}
		}
	}
	
	private final int[][] getSubClasses(final Player player)
	{
		if (player.getSubClasses().isEmpty())
			return new int[0][0];
		
		final int[][] subClasses = new int[player.getSubClasses().size() ][2];
		int i = 0;
		for (SubClass subClass : player.getSubClasses().values())
		{
			if (subClass == null)// || subClass.getClassIndex() != (i + 1))
			{
				System.out.println("Failed getting sub classes for player " + player.getName() + ", sub class invalid " + i);
				return new int[0][0];
			}
			
			subClasses[i][0] = subClass.getClassId();
			subClasses[i++][1] = subClass.getLevel();
		}
		
		return subClasses;
	}
	
	protected final void writeClanMember(final UnitMember member)
	{
		super.writeD(member.getObjectId());
		super.writeS(member.getName());
		super.writeC(member.getLevel());
		super.writeC(1);//super.writeC(member.getRaceOrdinal()); TODO: Fix?
		super.writeC(member.getClassId());
	}
	
	protected final void writeStat(final byte type, final Player player)
	{
		final int value;
		
		switch (type)
		{
			case COMBAT_TYPE_PATK: value = player.getPAtk(null); break;
			case COMBAT_TYPE_PDEF: value = player.getPDef(null); break;
			case COMBAT_TYPE_ACCURACY: value = player.getAccuracy(); break;
			case COMBAT_TYPE_CRIT_RATE: value = player.getCriticalHit(null, null); break;
			case COMBAT_TYPE_ATK_SPD: value = player.getPAtkSpd(); break;
			case COMBAT_TYPE_MATK: value = player.getMAtk(null, null); break;
			case COMBAT_TYPE_MDEF: value = player.getMDef(null, null); break;
			case COMBAT_TYPE_EVASION: value = player.getEvasionRate(null); break;
			case COMBAT_TYPE_SPEED: value = player.isRunning() ? player.getRunSpeed() : player.getWalkSpeed(); break;
			case COMBAT_TYPE_CASTING_SPEED: value = player.getMAtkSpd(); break;
			case STAT_STR: value = player.getSTR(); break;
			case STAT_INT: value = player.getINT(); break;
			case STAT_DEX: value = player.getDEX(); break;
			case STAT_WIT: value = player.getWIT(); break;
			case STAT_CON: value = player.getCON(); break;
			case STAT_MEN: value = player.getMEN(); break;
			case SOCIAL_KARMA: value = player.getKarma(); break;
			case SOCIAL_PVP: value = player.getPvpKills(); break;
			case SOCIAL_PK: value = player.getPkKills(); break;
			case SOCIAL_FAME: value = player.getFame(); break;
			case SOCIAL_RECOMMENT_HAVE: value = player.getRecomHave(); break;
			case SOCIAL_RECOMMENT_LEFT: value = player.getRecomLeft(); break;
			case ATTRIBUTE_ATK_TYPE: value = player.getAttackElement().getId(); break;
			case ATTRIBUTE_FIRE: value = player.getDefence(Element.FIRE); break;
			case ATTRIBUTE_WIND: value = player.getDefence(Element.WIND); break;
			case ATTRIBUTE_HOLY: value = player.getDefence(Element.HOLY); break;
			case ATTRIBUTE_ATK_VALUE: value = player.getAttackElement().getAttack() != null ? (int) player.calcStat(player.getAttackElement().getAttack(), 0) : 0; break;
			case ATTRIBUTE_WATER: value = player.getDefence(Element.WATER); break;
			case ATTRIBUTE_EARTH: value = player.getDefence(Element.EARTH); break;
			case ATTRIBUTE_UNHOLY: value = player.getDefence(Element.UNHOLY); break;
			default: value = -1; break;
		}
		
		super.writeC(type);
		super.writeH(value);
	}
	
	protected final void writeStats(final Player player)
	{
		for (byte i = 0; i < 30; i++)
		{
			writeStat(i, player);
		}
	}
	
	protected final void writeSkill(final Skill skill)
	{
		super.writeH(skill.getId());
		super.writeH(SkillTreeTable.unconvertEnchantLevel(skill.getId(), skill.getLevel()));
	}
	
	protected final void writeSkills(final Skill[] skills)
	{
		super.writeH(skills.length);
		for (int i = 0; i < skills.length; i++)
		{
			writeSkill(skills[i]);
		}
	}
	
	protected final void writeItem(final ItemInstance item)
	{
		super.writeD(item.getItemId());
		super.writeQ(item.getCount());
		super.writeD(item.getObjectId());
		super.writeH(item.getEnchantLevel());
		super.writeD(item.getAugmentationId());
		
		//TODO: Forsaiken
		//super.writeC(item.getElementType());
		//super.writeH(item.getElementPower());
		
		// Support will be added once we go full 2.5
		super.writeC(0);
		super.writeH(0);
	}
	
	protected final void writeItems(final ItemInstance[] items)
	{
		super.writeH(items.length);
		for (int i = 0; i < items.length; i++)
		{
			writeItem(items[i]);
		}
	}
	
	protected final void writeClan(final Clan clan)
	{
		if (clan != null)
		{
			super.writeD(clan.getClanId());
			super.writeS(clan.getName());
			super.writeD(clan.getAllyId());
			super.writeS(clan.getAlliance() != null ? clan.getAlliance().getAllyName() : "NONE");
			super.writeD(clan.getLeaderId());
			
			final List<UnitMember> members = clan.getAllMembers();
			super.writeH(members.size());
			for (final UnitMember member : members)
			{
				writeClanMember(member);
			}
		}
		else
		{
			super.writeD(0x00);
		}
	}
}