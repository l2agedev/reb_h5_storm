package l2r.gameserver.skills.skillclasses;

import l2r.commons.util.Rnd;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.xml.holder.FishDataHolder;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Fishing;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.World;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.templates.item.WeaponTemplate;
import l2r.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2r.gameserver.templates.item.support.FishGrade;
import l2r.gameserver.templates.item.support.FishGroup;
import l2r.gameserver.templates.item.support.FishTemplate;
import l2r.gameserver.templates.item.support.LureTemplate;
import l2r.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;

public class FishingSkill extends Skill
{
	public FishingSkill(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Player player = (Player) activeChar;

		if(player.getSkillLevel(SKILL_FISHING_MASTERY) == -1)
			return false;

		if(player.isFishing())
		{
			player.stopFishing();
			player.sendPacket(SystemMsg.CANCELS_FISHING);
			return false;
		}

		if(player.isInBoat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_FISH_WHILE_RIDING_AS_A_PASSENGER_OF_A_BOAT__ITS_AGAINST_THE_RULES);
			return false;
		}

		if(player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_FISH_WHILE_USING_A_RECIPE_BOOK_PRIVATE_MANUFACTURE_OR_PRIVATE_STORE);
			return false;
		}

		if(!player.isInZone(ZoneType.FISHING))
		{
			player.sendPacket(SystemMsg.YOU_CANT_FISH_HERE);
			return false;
		}

		if(player.isInWater())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_FISH_WHILE_UNDER_WATER);
			return false;
		}
		
		WeaponTemplate weaponItem = player.getActiveWeaponItem();
		if(weaponItem == null || weaponItem.getItemType() != WeaponType.ROD)
		{
			//Fishing poles are not installed
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_A_FISHING_POLE_EQUIPPED);
			return false;
		}

		ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if(lure == null || lure.getCount() < 1)
		{
			player.sendPacket(SystemMsg.YOU_MUST_PUT_BAIT_ON_YOUR_HOOK_BEFORE_YOU_CAN_FISH);
			return false;
		}

		//Вычисляем координаты поплавка
		int rnd = Rnd.get(50) + 150;
		double angle = Location.convertHeadingToDegree(player.getHeading());
		double radian = Math.toRadians(angle - 90);
		double sin = Math.sin(radian);
		double cos = Math.cos(radian);
		int x1 = -(int) (sin * rnd);
		int y1 = (int) (cos * rnd);
		int x = player.getX() + x1;
		int y = player.getY() + y1;
		//z - уровень карты
		int z = GeoEngine.getHeight(x, y, player.getZ(), player.getGeoIndex());

		//Проверяем, что поплавок оказался в воде
		boolean isInWater = false;
		List<Zone> zones = new ArrayList<Zone>();
		World.getZones(zones, new Location(x, y, z), player.getReflection());
		for(Zone zone : zones)
			if ((zone.getType() == ZoneType.FISHING) || (zone.getType() == ZoneType.water))
			{
				//z - уровень воды
				z = zone.getTerritory().getZmax();
				isInWater = true;
				break;
			}

		if(!isInWater)
		{
			player.sendPacket(SystemMsg.YOU_CANT_FISH_HERE);
			return false;
		}

		LureTemplate lureTemplate = FishDataHolder.getInstance().getLure(lure.getItemId());
		if(lureTemplate == null)
		{
			player.sendPacket(Msg.BAITS_ARE_NOT_PUT_ON_A_HOOK);
			return false;
		}
		
		player.getFishing().setFishLoc(new Location(x, y, z));
		player.getFishing().setLureId(lureTemplate);

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature caster, List<Creature> targets)
	{
		if(caster == null || !caster.isPlayer())
			return;

		Player player = (Player) caster;

		ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

		if(lure == null || lure.getCount() < 1)
		{
			player.sendPacket(SystemMsg.YOU_MUST_PUT_BAIT_ON_YOUR_HOOK_BEFORE_YOU_CAN_FISH);
			return;
		}
		Zone zone = player.getZone(ZoneType.FISHING);
		if(zone == null)
			return;
		
		LureTemplate lureTemplate = player.getFishing().getLure();
		if(lureTemplate == null)
		{
			player.sendPacket(Msg.BAITS_ARE_NOT_PUT_ON_A_HOOK);
			return;
		}

		int lvl = Fishing.getRandomFishLvl(player);
		FishGrade grade = lureTemplate.getGrade();
		FishGroup group = lureTemplate.getGroup(grade);

		List<FishTemplate> fishs = FishDataHolder.getInstance().getFish(group, grade, lvl);
		if(fishs == null || fishs.size() == 0)
		{
			player.sendPacket(SystemMsg.SYSTEM_ERROR);
			return;
		}

		if(!player.getInventory().destroyItemByObjectId(player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1L))
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_BAIT);
			return;
		}

		int check = Rnd.get(fishs.size());
		FishTemplate fish = fishs.get(check);

		player.startFishing(fish, lureTemplate);
	}
}