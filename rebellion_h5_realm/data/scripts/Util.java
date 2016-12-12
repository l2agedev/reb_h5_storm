import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SubClass;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.entity.SevenSigns;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.randoms.Wyverntransport;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.mapregion.DomainArea;
import l2r.gameserver.utils.Location;

public class Util extends Functions
{
	public void Gatekeeper(String[] param)
	{
		if(param.length < 4)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		long price = Long.parseLong(param[param.length - 1]);

		if(!NpcInstance.canBypassCheck(player, player.getLastNpc()))
			return;

		if(price > 0 && player.getAdena() < price)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		if(player.getMountType() == 2)
		{
			player.sendMessage(new CustomMessage("scripts.util.wyvern", player));
			return;
		}

		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && player.getKarma() > 0) //karma
		{
			player.sendMessage(new CustomMessage("scripts.util.nokarma", player));
			return;
		}
		
		if (player.isInJail())
		{
			player.sendMessage(new CustomMessage("scripts.util.jail", player));
			return;
		}
		
		if(player.isTerritoryFlagEquipped() || player.isCombatFlagEquipped())
		{
			player.sendPacket(Msg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
			return;
		}
		
		/* Затычка, npc Mozella не ТПшит чаров уровень которых превышает заданный в конфиге
		 * Off Like >= 56 lvl, данные по ограничению lvl'a устанавливаются в altsettings.properties.
		 */
		if(player.getLastNpc() != null)
		{
			int npcId = player.getLastNpc().getNpcId();
			switch(npcId)
			{
				case 30483:
					if(player.getLevel() >= Config.CRUMA_GATEKEEPER_LVL)
					{
						show("teleporter/30483-no.htm", player);
						return;
					}
					break;
				case 32864:
				case 32865:
				case 32866:
				case 32867:
				case 32868:
				case 32869:
				case 32870:
					if(player.isInCombat())
					{
						show("You cannot use this NPC while in combat.", player);
						return;
					}
					if(player.getLevel() < 80)
					{
						show("teleporter/"+npcId+"-no.htm", player);
						return;
					}
					break;
			}
		}

		int x = Integer.parseInt(param[0]);
		int y = Integer.parseInt(param[1]);
		int z = Integer.parseInt(param[2]);
		//int castleId = param.length > 4 ? Integer.parseInt(param[3]) : 0;

		Location pos = Location.findPointToStay(x, y, z, 50, 100, player.getGeoIndex());
		
		if(player.getReflection().isDefault())
		{
			DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, pos);
			if(domain != null)
			{
				Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, domain.getId());
				if (castle != null && castle.getSiegeEvent().isInProgress())
				{
					player.sendPacket(Msg.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
					return;
				}
			}
		}
		
		if(price > 0)
			player.reduceAdena(price, true);
		
		// Protection zones script.
		if (player.isInZonePeace())
		{
			player.setVar("EnterUrban", 0, -1);
			player.setVar("EnterHellbound", 0, -1);
			player.setVar("EnterBaium", 0, -1);
			player.setVar("EnterAntharas", 0, -1);
			player.setVar("EnterValakas", 0, -1);
		}
		
		player.teleToLocation(pos);
	}

	public void SSGatekeeper(String[] param)
	{
		if(param.length < 4)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		int type = Integer.parseInt(param[3]);

		if(!NpcInstance.canBypassCheck(player, player.getLastNpc()))
			return;

		if (player.isInJail())
		{
			player.sendMessage(new CustomMessage("scripts.util.jail", player));
			return;
		}
		
		if (!Config.ALLOW_FARM_IN_SEVENSIGN_IF_NOT_REGGED)
		{
			if(type > 0)
			{
				int player_cabal = SevenSigns.getInstance().getPlayerCabal(player);
				int period = SevenSigns.getInstance().getCurrentPeriod();
				if(period == SevenSigns.PERIOD_COMPETITION && player_cabal == SevenSigns.CABAL_NULL)
				{
					player.sendPacket(Msg.USED_ONLY_DURING_A_QUEST_EVENT_PERIOD);
					return;
				}

				int winner;
				if(period == SevenSigns.PERIOD_SEAL_VALIDATION && (winner = SevenSigns.getInstance().getCabalHighestScore()) != SevenSigns.CABAL_NULL)
				{
					if(winner != player_cabal)
						return;
					if(type == 1 && SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE) != player_cabal)
						return;
					if(type == 2 && SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_GNOSIS) != player_cabal)
						return;
				}
			}
		}
		
		player.teleToLocation(Integer.parseInt(param[0]), Integer.parseInt(param[1]), Integer.parseInt(param[2]));
	}

	public void QuestGatekeeper(String[] param)
	{
		if(param.length < 5)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		if (player.isInJail())
		{
			player.sendMessage(new CustomMessage("scripts.util.jail", player));
			return;
		}
		
		long count = Long.parseLong(param[3]);
		int item = Integer.parseInt(param[4]);

		if(!NpcInstance.canBypassCheck(player, player.getLastNpc()))
			return;

		if(count > 0)
		{
			if(!player.getInventory().destroyItemByItemId(item, count))
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
				return;
			}
			player.sendPacket(SystemMessage2.removeItems(item, count));
		}

		int x = Integer.parseInt(param[0]);
		int y = Integer.parseInt(param[1]);
		int z = Integer.parseInt(param[2]);

		Location pos = Location.findPointToStay(x, y, z, 20, 70, player.getGeoIndex());

		player.teleToLocation(pos);
	}

	public void WyvernGatekeeper(String[] param)
	{
		if(param.length < 4)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		long price = Long.parseLong(param[param.length - 1]);
		
		if(price > 0 && player.getAdena() < price)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		if(player.isMounted() || player.getTransformation() > 0)
		{
			player.sendMessage(new CustomMessage("scripts.util.jail.noteleport", player));
			return;
		}

		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && player.getKarma() > 0) //karma
		{
			player.sendMessage(new CustomMessage("scripts.util.jail.noteleport.karma", player));
			return;
		}
		
		if(player.isTerritoryFlagEquipped() || player.isCombatFlagEquipped())
		{
			player.sendPacket(Msg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
			return;
		}
		
		int x = Integer.parseInt(param[0]);
		int y = Integer.parseInt(param[1]);
		int z = Integer.parseInt(param[2]);
		//int castleId = param.length > 4 ? Integer.parseInt(param[3]) : 0;

		Location pos = Location.findPointToStay(x, y, z, 50, 100, player.getGeoIndex());
		
		if(player.getReflection().isDefault())
		{
			DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, pos);
			if(domain != null)
			{
				Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, domain.getId());
				if (castle != null && castle.getSiegeEvent().isInProgress())
				{
					player.sendPacket(Msg.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
					return;
				}
			}
		}
		
		if(price > 0)
			player.reduceAdena(price, true);

		Wyverntransport.getInstance().wyvernTeleport(player, pos);
	}
	
	public void ReflectionGatekeeper(String[] param)
	{
		if(param.length < 5)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		player.setReflection(Integer.parseInt(param[4]));

		Gatekeeper(param);
	}

	/**
	 * Используется для телепортации за Newbie Token, проверяет уровень и передает
	 * параметры в QuestGatekeeper
	 */
	public void TokenJump(String[] param)
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(player.getLevel() <= 19)
			QuestGatekeeper(param);
		else
			show(player.isLangRus() ? "Только для новичков." : "Only for newbies.", player);
	}

	public void NoblessTeleport()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(player.isNoble() || Config.ALLOW_NOBLE_TP_TO_ALL)
			show("scripts/noble.htm", player);
		else
			show("scripts/nobleteleporter-no.htm", player);
	}

	public void PayPage(String[] param)
	{
		if(param.length < 2)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		String page = param[0];
		int item = Integer.parseInt(param[1]);
		long price = Long.parseLong(param[2]);

		if(getItemCount(player, item) < price)
		{
			player.sendPacket(item == 57 ? Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA : SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}

		removeItem(player, item, price);
		show(page, player);
	}
	
	public void MakeEchoCrystal(String[] param)
	{
		if(param.length < 2)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		if(!NpcInstance.canBypassCheck(player, player.getLastNpc()))
			return;

		int crystal = Integer.parseInt(param[0]);
		int score = Integer.parseInt(param[1]);

		if(crystal < 4411 || crystal > 4417)
			return;

		if(getItemCount(player, score) == 0)
		{
			player.getLastNpc().onBypassFeedback(player, "Chat 1");
			return;
		}

		if(getItemCount(player, 57) < 200)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		removeItem(player, 57, 200);
		addItem(player, crystal, 1);
	}

	public void TakeNewbieWeaponCoupon()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.ALT_ALLOW_SHADOW_WEAPONS)
		{
			show(new CustomMessage("common.Disabled", player), player);
			return;
		}
		if(player.getLevel() > 19 || player.getClassId().getLevel() > 1)
		{
			show(player.isLangRus() ? "Ваш уровень слишком высок!" : "Your level is too high!", player);
			return;
		}
		if(player.getLevel() < 6)
		{
			show(player.isLangRus() ? "Ваш уровень слишком низок!" : "Your level is too low!", player);
			return;
		}
		if(player.getVarB("newbieweapon"))
		{
			show(player.isLangRus() ? "Вы уже получили свой новичка оружие!" : "You already got your newbie weapon!", player);
			return;
		}
		addItem(player, 7832, 5);
		player.setVar("newbieweapon", "true", -1);
	}

	public void TakeAdventurersArmorCoupon()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.ALT_ALLOW_SHADOW_WEAPONS)
		{
			show(new CustomMessage("common.Disabled", player), player);
			return;
		}
		if(player.getLevel() > 39 || player.getClassId().getLevel() > 2)
		{
			show(player.isLangRus() ? "Ваш уровень слишком высок!" : "Your level is too high!", player);
			return;
		}
		if(player.getLevel() < 20 || player.getClassId().getLevel() < 2)
		{
			show(player.isLangRus() ? "Ваш уровень слишком низок!" : "Your level is too low!", player);
			return;
		}
		if(player.getVarB("newbiearmor"))
		{
			show(player.isLangRus() ? "Вы уже получили свой новичка оружие!" : "You already got your newbie weapon!", player);
			return;
		}
		addItem(player, 7833, 1);
		player.setVar("newbiearmor", "true", -1);
	}

	public void enter_dc()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(!NpcInstance.canBypassCheck(player, npc))
			return;

		player.setVar("DCBackCoords", player.getLoc().toXYZString(), -1);
		player.teleToLocation(-114582, -152635, -6742);
	}

	public void exit_dc()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(!NpcInstance.canBypassCheck(player, npc))
			return;

		String var = player.getVar("DCBackCoords");
		if(var == null || var.isEmpty())
		{
			player.teleToLocation(new Location(43768, -48232, -800), 0);
			return;
		}
		player.teleToLocation(Location.parseLoc(var), 0);
		player.unsetVar("DCBackCoords");
	}
	
	public void CommunityCert65()
	{
		Player player = getSelf();
		if(player == null)
			return;
		
		SubClass clzz = player.getActiveClass();
		if (!checkCertificationCondition(65, SubClass.CERTIFICATION_65))
			return;
		
		Functions.addItem(player, 10280, 1);
		clzz.addCertification(SubClass.CERTIFICATION_65);
		player.store(true);
		player.sendChatMessage(0, ChatType.TELL.ordinal(), "Career Manager", "You have earned Certification Emergent Level 65.");
		communityNextPage(player, "_bbscareer;certification;");
	}
	
	public void CommunityCert70()
	{
		Player player = getSelf();
		if(player == null)
			return;
		
		SubClass clzz = player.getActiveClass();
		if (!checkCertificationCondition(70, SubClass.CERTIFICATION_70))
			return;
		
		Functions.addItem(player, 10280, 1);
		clzz.addCertification(SubClass.CERTIFICATION_70);
		player.store(true);
		player.sendChatMessage(0, ChatType.TELL.ordinal(), "Career Manager", "You have earned Certification Emergent Level 70.");
		communityNextPage(player, "_bbscareer;certification;");
	}
	
	public void CommunityCert75Class()
	{
		Player player = getSelf();
		if(player == null)
			return;
		
		SubClass clzz = player.getActiveClass();
		if (!checkCertificationCondition(75, SubClass.CERTIFICATION_75))
			return;
		
		ClassId cl = ClassId.VALUES[clzz.getClassId()];
		if (cl.getType2() == null)
			return;
		
		Functions.addItem(player, cl.getType2().getCertificateId(), 1);
		clzz.addCertification(SubClass.CERTIFICATION_75);
		player.store(true);
		player.sendChatMessage(0, ChatType.TELL.ordinal(), "Career Manager", "You have earned Certification Class Specific Level 75.");
		communityNextPage(player, "_bbscareer;certification;");
	}
	
	public void CommunityCert75Master()
	{
		Player player = getSelf();
		if(player == null)
			return;
		
		SubClass clzz = player.getActiveClass();
		if (!checkCertificationCondition(75, SubClass.CERTIFICATION_75))
			return;
		
		Functions.addItem(player, 10612, 1);
		clzz.addCertification(SubClass.CERTIFICATION_75);
		player.store(true);
		player.sendChatMessage(0, ChatType.TELL.ordinal(), "Career Manager", "You have earned Certification Master Level 75.");
		communityNextPage(player, "_bbscareer;certification;");
	}
	
	public void CommunityCert80()
	{
		Player player = getSelf();
		if(player == null)
			return;
		
		SubClass clzz = player.getActiveClass();
		if (!checkCertificationCondition(80, SubClass.CERTIFICATION_80))
			return;
		
		ClassId cl = ClassId.VALUES[clzz.getClassId()];
		if (cl.getType2() == null)
			return;
		
		Functions.addItem(player, cl.getType2().getTransformationId(), 1);
		clzz.addCertification(SubClass.CERTIFICATION_80);
		player.store(true);
		player.sendChatMessage(0, ChatType.TELL.ordinal(), "Career Manager", "You have earned Certification Transformation Level 80.");
		communityNextPage(player, "_bbscareer;certification;");
	}
	
	private boolean checkCertificationCondition(int requiredLevel, int certificationIndex)
	{
		Player player = getSelf();
		if (player == null)
			return false;
		
		boolean failed = false;
		if (player.getLevel() < requiredLevel)
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Career Manager", "Your Level is too low to earn certification books.");
			player.sendMessage("Your level is too low!");
			failed = true;
		}
		
		SubClass clazz = player.getActiveClass();
		if (!failed && clazz.isCertificationGet(certificationIndex))
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Career Manager", "You already have this Certification!");
			failed = true;
		}
		
		if (failed)
		{
			communityNextPage(player, "_bbscareer;");
			return false;
		}
		
		return true;
	}
	
	private static void communityNextPage(Player player, String link)
	{
		ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(link);
		if (handler != null)
			handler.onBypassCommand(player, link);
	}
}