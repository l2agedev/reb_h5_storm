package l2r.gameserver.network.clientpackets;

import l2r.gameserver.dao.SiegeClanDAO;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.EventHolder;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.events.EventType;
import l2r.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2r.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import l2r.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;
import l2r.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2r.gameserver.model.entity.events.impl.SiegeEvent;
import l2r.gameserver.model.entity.events.objects.SiegeClanObject;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.model.entity.residence.ClanHall;
import l2r.gameserver.model.entity.residence.Fortress;
import l2r.gameserver.model.entity.residence.Residence;
import l2r.gameserver.model.entity.residence.ResidenceType;
import l2r.gameserver.model.pledge.Alliance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.pledge.Privilege;
import l2r.gameserver.network.serverpackets.CastleSiegeAttackerList;
import l2r.gameserver.network.serverpackets.CastleSiegeDefenderList;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.item.ItemTemplate;

public class RequestJoinCastleSiege extends L2GameClientPacket
{
	private int _id;
	private boolean _isAttacker;
	private boolean _isJoining;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
		_isAttacker = readD() == 1;
		_isJoining = readD() == 1;
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (!player.hasPrivilege(Privilege.CS_FS_SIEGE_WAR))
		{
			player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		Residence residence = ResidenceHolder.getInstance().getResidence(_id);
		
		if (residence.getType() == ResidenceType.Castle)
			registerAtCastle(player, (Castle) residence, _isAttacker, _isJoining);
		else if (residence.getType() == ResidenceType.Fortress)
			registerAtFortress(player, (Fortress) residence, _isAttacker, _isJoining);
		else if (residence.getType() == ResidenceType.ClanHall && _isAttacker)
			registerAtClanHall(player, (ClanHall) residence, _isJoining);
	}
	
	public static void registerAtCastle(Player player, Castle castle, boolean attacker, boolean join)
	{
		CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
		
		Clan playerClan = player.getClan();
		
		if (playerClan == null)
			return;
		
		if(playerClan.isPlacedForDisband())
		{
			player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REQUESTED_THE_DISSOLUTION_OF_YOUR_CLAN);
			return;
		}
		
		SiegeClanObject siegeClan = null;
		if (attacker)
			siegeClan = siegeEvent.getSiegeClan(CastleSiegeEvent.ATTACKERS, playerClan);
		else
		{
			siegeClan = siegeEvent.getSiegeClan(CastleSiegeEvent.DEFENDERS, playerClan);
			if (siegeClan == null)
				siegeClan = siegeEvent.getSiegeClan(CastleSiegeEvent.DEFENDERS_WAITING, playerClan);
		}
		
		if (join)
		{
			Residence registeredCastle = null;
			for (Residence residence : ResidenceHolder.getInstance().getResidenceList(Castle.class))
			{
				SiegeClanObject tempCastle = residence.getSiegeEvent().getSiegeClan(CastleSiegeEvent.ATTACKERS, playerClan);
				
				if (tempCastle == null)
					tempCastle = residence.getSiegeEvent().getSiegeClan(CastleSiegeEvent.DEFENDERS, playerClan);
				
				if (tempCastle == null)
					tempCastle = residence.getSiegeEvent().getSiegeClan(CastleSiegeEvent.DEFENDERS_WAITING, playerClan);
				
				if (tempCastle != null)
					registeredCastle = residence;
			}
			
			if (attacker)
			{
				if (castle.getOwnerId() == playerClan.getClanId())
				{
					player.sendPacket(SystemMsg.CASTLE_OWNING_CLANS_ARE_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE);
					return;
				}
				
				Alliance alliance = playerClan.getAlliance();
				if (alliance != null)
				{
					for (Clan clan : alliance.getMembers())
					{
						if (clan.getCastle() == castle.getId())
						{
							player.sendPacket(SystemMsg.YOU_CANNOT_REGISTER_AS_AN_ATTACKER_BECAUSE_YOU_ARE_IN_AN_ALLIANCE_WITH_THE_CASTLE_OWNING_CLAN);
							return;
						}
					}
				}
				if (playerClan.getCastle() > 0)
				{
					player.sendPacket(SystemMsg.A_CLAN_THAT_OWNS_A_CASTLE_CANNOT_PARTICIPATE_IN_ANOTHER_SIEGE);
					return;
				}
				
				if (siegeClan != null)
				{
					player.sendPacket(SystemMsg.YOU_ARE_ALREADY_REGISTERED_TO_THE_ATTACKER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST);
					return;
				}
				
				if (playerClan.getLevel() < 5)
				{
					player.sendPacket(SystemMsg.ONLY_CLANS_OF_LEVEL_5_OR_HIGHER_MAY_REGISTER_FOR_A_CASTLE_SIEGE);
					return;
				}
				
				if (registeredCastle != null)
				{
					player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REQUESTED_A_CASTLE_SIEGE);
					return;
				}
				
				if (siegeEvent.isRegistrationOver())
				{
					player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
					return;
				}
				
				if (castle.getSiegeDate().getTimeInMillis() == 0)
				{
					player.sendPacket(SystemMsg.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATION_AND_CANCELLATION_CANNOT_BE_DONE);
					return;
				}
				
				int allSize = siegeEvent.getObjects(CastleSiegeEvent.ATTACKERS).size();
				if (allSize >= CastleSiegeEvent.MAX_SIEGE_CLANS)
				{
					player.sendPacket(SystemMsg.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_ATTACKER_SIDE);
					return;
				}
				
				Fortress fortress = player.getFortress();
				if (fortress != null && fortress.getCastleId() == castle.getId())
				{
					player.sendPacket(SystemMsg.SIEGE_REGISTRATION_IS_NOT_POSSIBLE_DUE_TO_YOUR_CASTLE_CONTRACT);
					return;
				}
				
				siegeClan = new SiegeClanObject(CastleSiegeEvent.ATTACKERS, playerClan, 0);
				siegeEvent.addObject(CastleSiegeEvent.ATTACKERS, siegeClan);
				
				SiegeClanDAO.getInstance().insert(castle, siegeClan);
				
				player.sendPacket(new CastleSiegeAttackerList(castle));
			}
			else
			{
				if (castle.getOwnerId() == 0)
					return;
				
				if (castle.getOwnerId() == playerClan.getClanId())
				{
					player.sendPacket(SystemMsg.CASTLE_OWNING_CLANS_ARE_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE);
					return;
				}
				
				if (playerClan.getCastle() > 0)
				{
					player.sendPacket(SystemMsg.A_CLAN_THAT_OWNS_A_CASTLE_CANNOT_PARTICIPATE_IN_ANOTHER_SIEGE);
					return;
				}
				
				if (siegeClan != null)
				{
					player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REGISTERED_TO_THE_DEFENDER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST);
					return;
				}
				
				if (playerClan.getLevel() < 5)
				{
					player.sendPacket(SystemMsg.ONLY_CLANS_OF_LEVEL_5_OR_HIGHER_MAY_REGISTER_FOR_A_CASTLE_SIEGE);
					return;
				}
				
				if (registeredCastle != null)
				{
					player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REQUESTED_A_CASTLE_SIEGE);
					return;
				}
				
				if (castle.getSiegeDate().getTimeInMillis() == 0)
				{
					player.sendPacket(SystemMsg.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATION_AND_CANCELLATION_CANNOT_BE_DONE);
					return;
				}
				
				if (siegeEvent.isRegistrationOver())
				{
					player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
					return;
				}
				
				siegeClan = new SiegeClanObject(CastleSiegeEvent.DEFENDERS_WAITING, playerClan, 0);
				siegeEvent.addObject(CastleSiegeEvent.DEFENDERS_WAITING, siegeClan);
				
				SiegeClanDAO.getInstance().insert(castle, siegeClan);
				
				player.sendPacket(new CastleSiegeDefenderList(castle));
			}
		}
		else
		{
			if (siegeClan == null)
				siegeClan = siegeEvent.getSiegeClan(CastleSiegeEvent.DEFENDERS_REFUSED, playerClan);
			
			if (siegeClan == null)
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_YET_REGISTERED_FOR_THE_CASTLE_SIEGE);
				return;
			}
			
			if (siegeEvent.isRegistrationOver())
			{
				player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
				return;
			}
			
			siegeEvent.removeObject(siegeClan.getType(), siegeClan);
			
			SiegeClanDAO.getInstance().delete(castle, siegeClan);
			if (siegeClan.getType() == SiegeEvent.ATTACKERS)
				player.sendPacket(new CastleSiegeAttackerList(castle));
			else
				player.sendPacket(new CastleSiegeDefenderList(castle));
		}
	}
	
	public static void registerAtFortress(Player player, Fortress fortres, boolean attacker, boolean join)
	{
		FortressSiegeEvent siegeEvent = fortres.getSiegeEvent();
		Clan playerClan = player.getClan();
		
		if (playerClan == null)
			return;
		
		SiegeClanObject siegeClan = siegeEvent.getSiegeClan(FortressSiegeEvent.ATTACKERS, playerClan);
		
		if(playerClan.isPlacedForDisband())
		{
			player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REQUESTED_THE_DISSOLUTION_OF_YOUR_CLAN);
			return;
		}
		
		if (join)
		{
			if (playerClan.getLevel() < 4)
			{
				player.sendPacket(SystemMsg.ONLY_CLANS_WHO_ARE_LEVEL_4_OR_ABOVE_CAN_REGISTER_FOR_BATTLE_AT_DEVASTATED_CASTLE_AND_FORTRESS_OF_THE_DEAD);
				return;
			}
			
			if (playerClan.getCastle() > 0)
			{
				Castle relatedCastle = null;
				for (Castle castle : fortres.getRelatedCastles())
					if (castle.getId() == playerClan.getCastle())
						relatedCastle = castle;
				
				if (relatedCastle != null)
				{
					if (fortres.getContractState() == Fortress.CONTRACT_WITH_CASTLE)
					{
						showChatWindow(player, "fortress_ordery022.htm");
						return;
					}
					
					if (relatedCastle.getSiegeEvent().isRegistrationOver())
					{
						showChatWindow(player, "fortress_ordery022.htm");
						return;
					}
				}
				else
				{
					showChatWindow(player, "fortress_ordery021.htm");
					return;
				}
			}
			
			for (Fortress ft : ResidenceHolder.getInstance().getResidenceList(Fortress.class))
				if (ft.getSiegeEvent().getSiegeClan(FortressSiegeEvent.ATTACKERS, playerClan) != null)
				{
					showChatWindow(player, "fortress_ordery006.htm");
					//player.sendMessage(player.isLangRus() ? "Вы уже зарегистрированы на одну осаду крепости, вам достаточно. В регистрации ОТКАЗАНО!" : "You are already registered on one siege of fortress, you are enough. It is SAID no in registration!");
					return;
				}
			
			// если у нас есть форт, запрещаем регатся на форт, если на носу осада своего форта(во избежания абуза, участия в 2 осадах)
			if (playerClan.getHasFortress() > 0 && fortres.getSiegeDate().getTimeInMillis() > 0)
			{
				//player.sendMessage(player.isLangRus() ? "Зачем вам чужие земли? Защитите сначала свои! В регистрации ОТКАЗАНО!" : "Why to you stranger earth? Protect it at first! It is SAID no in registration!");
				showChatWindow(player, "fortress_ordery006.htm");
				return;
			}
			
			DominionSiegeRunnerEvent runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
			if (runnerEvent.isRegistrationOver() || siegeEvent.isRegistrationOver())
			{
				showChatWindow(player, "fortress_ordery006.htm");
				return;
			}
			
			int attackersSize = siegeEvent.getObjects(SiegeEvent.ATTACKERS).size();
			if (attackersSize == 0 && !player.consumeItem(ItemTemplate.ITEM_ID_ADENA, 250000L))
			{
				player.sendMessage(player.isLangRus() ? "Недостаточно средств. Требуется 250000 Адены." : "Insufficient funds. Requires 250,000 Adena.");
				return;
			}
			
			Residence registeredCastle = null;
			for (Residence residence : ResidenceHolder.getInstance().getResidenceList(Fortress.class))
			{
				SiegeClanObject tempFortress = residence.getSiegeEvent().getSiegeClan(FortressSiegeEvent.ATTACKERS, playerClan);
				
				if (tempFortress == null)
					tempFortress = residence.getSiegeEvent().getSiegeClan(FortressSiegeEvent.DEFENDERS, playerClan);
				
				if (tempFortress != null)
					registeredCastle = residence;
			}
			
			if (registeredCastle != null)
			{
				player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REQUESTED_A_CASTLE_SIEGE);
				return;
			}
			
			siegeClan = new SiegeClanObject(FortressSiegeEvent.ATTACKERS, playerClan, 0);
			siegeEvent.addObject(FortressSiegeEvent.ATTACKERS, siegeClan);
			SiegeClanDAO.getInstance().insert(fortres, siegeClan);
			siegeEvent.reCalcNextTime(false);
			player.sendPacket(new SystemMessage2(SystemMsg.YOUR_CLAN_HAS_BEEN_REGISTERED_TO_S1S_FORTRESS_BATTLE).addResidenceName(fortres));
		}
		else
		{
			if (siegeClan != null)
			{
				siegeEvent.removeObject(FortressSiegeEvent.ATTACKERS, siegeClan);
				SiegeClanDAO.getInstance().delete(fortres, siegeClan);
				
				siegeEvent.reCalcNextTime(false);
			}
		}
		player.sendPacket(new CastleSiegeAttackerList(fortres));
	}
	
	public static void registerAtClanHall(Player player, ClanHall clanHall, boolean join)
	{
		ClanHallSiegeEvent siegeEvent = clanHall.getSiegeEvent();
		
		Clan playerClan = player.getClan();
		
		if (playerClan == null)
			return;
		
		if(playerClan.isPlacedForDisband())
		{
			player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REQUESTED_THE_DISSOLUTION_OF_YOUR_CLAN);
			return;
		}
		
		SiegeClanObject siegeClan = siegeEvent.getSiegeClan(CastleSiegeEvent.ATTACKERS, playerClan);
		
		if (join)
		{
			if (playerClan.getHasHideout() > 0)
			{
				player.sendPacket(SystemMsg.A_CLAN_THAT_OWNS_A_CLAN_HALL_MAY_NOT_PARTICIPATE_IN_A_CLAN_HALL_SIEGE);
				return;
			}
			
			if (siegeClan != null)
			{
				player.sendPacket(SystemMsg.YOU_ARE_ALREADY_REGISTERED_TO_THE_ATTACKER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST);
				return;
			}
			
			if (playerClan.getLevel() < 4)
			{
				player.sendPacket(SystemMsg.ONLY_CLANS_WHO_ARE_LEVEL_4_OR_ABOVE_CAN_REGISTER_FOR_BATTLE_AT_DEVASTATED_CASTLE_AND_FORTRESS_OF_THE_DEAD);
				return;
			}
			
			if (siegeEvent.isRegistrationOver())
			{
				player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
				return;
			}
			
			int allSize = siegeEvent.getObjects(ClanHallSiegeEvent.ATTACKERS).size();
			if (allSize >= CastleSiegeEvent.MAX_SIEGE_CLANS)
			{
				player.sendPacket(SystemMsg.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_ATTACKER_SIDE);
				return;
			}
			
			siegeClan = new SiegeClanObject(ClanHallSiegeEvent.ATTACKERS, playerClan, 0);
			siegeEvent.addObject(ClanHallSiegeEvent.ATTACKERS, siegeClan);
			
			SiegeClanDAO.getInstance().insert(clanHall, siegeClan);
		}
		else
		{
			if (siegeClan == null)
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_YET_REGISTERED_FOR_THE_CASTLE_SIEGE);
				return;
			}
			
			if (siegeEvent.isRegistrationOver())
			{
				player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
				return;
			}
			
			siegeEvent.removeObject(siegeClan.getType(), siegeClan);
			
			SiegeClanDAO.getInstance().delete(clanHall, siegeClan);
		}
		
		player.sendPacket(new CastleSiegeAttackerList(clanHall));
	}
	
	private static void showChatWindow(Player player, String htmlfile)
	{
		String htmltosend = HtmCache.getInstance().getNotNull("residence2/fortress/" + htmlfile, player);

		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(htmltosend);
		player.sendPacket(html);
	}
}
