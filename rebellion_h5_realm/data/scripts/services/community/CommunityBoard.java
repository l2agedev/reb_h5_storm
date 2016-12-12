package services.community;

import l2r.gameserver.Config;
import l2r.gameserver.GameServer;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.achievements.Achievements;
import l2r.gameserver.achievements.PlayerTops;
import l2r.gameserver.auction.AuctionManager;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.dao.AccountsDAO;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.dao.PremiumAccountsTable.PremiumAccount;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.BuyListHolder;
import l2r.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import l2r.gameserver.data.xml.holder.MultiSellHolder;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.AccountData;
import l2r.gameserver.network.serverpackets.ExBuySellList;
import l2r.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import l2r.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import l2r.gameserver.network.serverpackets.HennaEquipList;
import l2r.gameserver.network.serverpackets.HennaUnequipList;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.randoms.PlayerShift;
import l2r.gameserver.randoms.Visuals;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.scripts.Scripts;
import l2r.gameserver.stats.Formulas;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2r.gameserver.utils.TimeUtils;
import l2r.gameserver.utils.Util;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunityBoard implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityBoard.class);

	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			_log.info("CommunityBoard: service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown()
	{}

	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_bbshome",
			"_bbslink",
			"_bbsmultisell",
			"_bbs_achievements",
			"_bbs_achievements_cat",
			"_maillist_0_1_0_",
			"_bbs_Auction",
			"_bbsmemo",
			"_bbssell",
			"_bbsaugment",
			"_bbsdeaugment",
			"_bbspage",
			"_bbs_see",
			"_bbs_get_ach",
			"_bbs_get_effects",
			"_bbs_henna_draw",
			"_bbs_henna_remove",
			"_bbs_augment_item",
			"_bbs_remove_augment",
			"_bbsclan",
			"_bbsVisual",
			"_bbsCCP",
			"_bbsGrandBoss"
		};
	}
	
	private static int ONLINE = 0;
	private static int OFFLINE = 0;
	private static int OFFLINE_BUFFER = 0;
	static
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			public void run()
			{
				ONLINE = GameObjectsStorage.getAllPlayersCount();
				OFFLINE = GameObjectsStorage.getAllOfflineCount(false);
				OFFLINE_BUFFER = GameObjectsStorage.getAllOfflineCount(false);
			}
		}, 1000, 30000);
	}
	
	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		String html = "";
		
		if("bbshome".equals(cmd))
		{
			StringTokenizer p = new StringTokenizer(Config.BBS_DEFAULT, "_");
			String dafault = p.nextToken();
			if(dafault.equals(cmd))
			{
				// Lets cleanup the code a bit....
				html = refreshIndex(html, player);
			}
			else
			{
				onBypassCommand(player, Config.BBS_DEFAULT);
				return;
			}
		}
		else if("bbslink".equals(cmd))
		{
			if (Config.ENABLE_DONATE_PAGE)
				html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/donate/donate-index.htm", player);
			else
			{
				if (Config.ALLOW_BSS_RAIDBOSS)
				{
					String bp = "_bbsraidboss";
					ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(bp);
					if (handler != null)
						handler.onBypassCommand(player, bp);

					return;
				}
				
				html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/index.htm", player);
			}
			
			html = html.replace("%currentTime%", TimeUtils.convertDateToString(System.currentTimeMillis()));
			html = html.replace("%uptime%", Util.formatTime(GameServer.getInstance().uptime()));
			html = html.replace("%serverRev%", GameServer.getInstance().getVersion().getRevisionNumber());
			html = html.replace("%buildDate%", GameServer.getInstance().getVersion().getBuildDate());
			html = html.replace("%Online%", String.valueOf(ONLINE));
			
		}
		else if(bypass.startsWith("_bbspage"))
		{
			String[] b = bypass.split(":");
			String page = b[1];
			html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/" + page + ".htm", player);
			
			// Prims - Remove tabs and enters to improve performance
			html = html.replace("\t", "");
			html = html.replace("\r\n", "");
			html = html.replace("\n", "");
		}
		else if(Config.COMUMNITY_ALLOW_BUY && bypass.startsWith("_bbsmultisell"))
		{
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String[] mBypass = st2.nextToken().split(":");
			String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
			if(pBypass != null)
			{
				ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
				if(handler != null)
					handler.onBypassCommand(player, pBypass);
			}

			if (Config.SECURITY_ENABLED && Config.SECURITY_TRADE_ENABLED && player.getSecurity())
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "SECURITY", (player.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
				return;
			}
			
			/*
			// Custom
			if (!PremiumAccountsTable.getGmShopOutsidePeace(player) && !player.isInZone(ZoneType.peace_zone) && !player.isInZone(ZoneType.RESIDENCE))
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Shop", (player.isLangRus() ? "Вы должны быть внутри города или ClanHall воспользоваться услугами магазина." : "You must be inside Town or ClanHall to use the shop."));
				return;
			}
			*/
			
			if (player.isCursedWeaponEquipped() ||  player.isInJail() || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.isFlying() || player.isTerritoryFlagEquipped())
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Shop", player.isLangRus() ? "Невозможно использовать в данный момент!" : "You can not use the buffer at this moment!");
				return;
			}
			
			int listId = Integer.parseInt(mBypass[1]);
			MultiSellHolder.getInstance().SeparateAndSend(listId, player, 0);
			return;
		}
		else if (bypass.startsWith("_bbs_achievements"))
		{
			if (!Config.ENABLE_ACHIEVEMENTS)
			{
				player.sendMessageS("Achievements are Disabled!", 5);
				return;
			}
			
			String[] cm = bypass.split(" ");
			
			Achievements.getInstance().usebypass(player, bypass, cm); 
			return;
		}
		else if(bypass.startsWith("_bbs_achievements_cat"))
		{
			if (!Config.ENABLE_ACHIEVEMENTS)
			{
				player.sendMessageS("Achievements are Disabled!", 5);
				return;
			}
			
			String[] cm = bypass.split(" ");

			int page = 0;
			if (cm.length < 1)
				page = 1;
			else
				page = Integer.parseInt(cm[2]);
			
			Achievements.getInstance().generatePage(player, Integer.parseInt(cm[1]), page);
			return;
		}
		else if (bypass.startsWith("_bbs_Auction") || bypass.startsWith("_maillist_0_1_0_"))
		{
			if (!Config.ENABLE_CUSTOM_AUCTION)
			{
				player.sendMessageS("Autions is Disabled!", 5);
				return;
			}
			
			AuctionManager.usebypass(player, bypass);
			return;
		}
		else if (bypass.startsWith("_bbsmemo"))
		{
			if (!Config.ENABLE_COMMUNITY_RANKING)
			{
				player.sendMessageS("Ranking is Disabled!", 5);
				return;
			}
			
			PlayerTops.getInstance().usebypass(player, bypass);
			return;
		}
		else if(Config.COMMUNITY_ALLOW_SELL && bypass.startsWith("_bbssell"))
		{
            StringTokenizer st2 = new StringTokenizer(bypass, ";");
            st2.nextToken();
            String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
            if(pBypass != null)
            {
                ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
                if(handler != null)
                    handler.onBypassCommand(player, pBypass);
            }
            
            if (player.isCursedWeaponEquipped() || player.isInJail() || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.isFlying() || player.isTerritoryFlagEquipped())
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Shop", player.isLangRus() ? "Невозможно использовать в данный момент!" : "You can not use the buffer at this moment!");
				return;
			}
            
            if (Config.SECURITY_ENABLED && Config.SECURITY_TRADE_ENABLED && player.getSecurity())
    		{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "SECURITY", (player.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
				return;
    		}
         		
			// Custom
			if (!PremiumAccountsTable.getGmShopOutsidePeace(player) && !player.isInZone(ZoneType.peace_zone) && !player.isInZone(ZoneType.RESIDENCE))
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Shop", (player.isLangRus() ? "Вы должны быть внутри города или ClanHall воспользоваться услугами магазина." : "You must be inside Town or ClanHall to use the shop."));
				return;
			}
			
			NpcTradeList list = BuyListHolder.getInstance().getBuyList(-1);
			player.sendPacket(new ExBuySellList.BuyList(list, player, 0.), new ExBuySellList.SellRefundList(player, false));
			return;
		}
		else if (bypass.startsWith("_bbsaugment") && Config.COMMUNITY_ALLOW_AUGMENT) 
		{
            player.sendPacket(Msg.SELECT_THE_ITEM_TO_BE_AUGMENTED, ExShowVariationMakeWindow.STATIC);
            return;
        } 
		else if (bypass.startsWith("_bbsdeaugment") && Config.COMMUNITY_ALLOW_AUGMENT) 
		{
            player.sendPacket(Msg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, ExShowVariationCancelWindow.STATIC);
            return;
        }
		else if(bypass.startsWith("_bbsscripts"))
		{
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String sBypass = st2.nextToken().substring(12);
			String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
			if(pBypass != null)
			{
				ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
				if(handler != null)
					handler.onBypassCommand(player, pBypass);
			}

			String[] word = sBypass.split("\\s+");
			String[] args = sBypass.substring(word[0].length()).trim().split("\\s+");
			String[] path = word[0].split(":");
			if(path.length != 2)
				return;

			Scripts.getInstance().callScripts(player, path[0], path[1], word.length == 1 ? new Object[] {} : new Object[] { args });
			return;
		}
		else if(bypass.startsWith("_bbs_see"))
		{
			String[] cm = bypass.split(" ");
			if(player.getTarget() != null && player.getTarget().isPlayer())
				PlayerShift.see(player.getTarget().getPlayer(), player, cm[1]);
			return;
		}
		else if(bypass.equalsIgnoreCase("_bbs_get_ach"))
		{
			if(player.getTarget() != null && player.getTarget().isPlayer())
				PlayerShift.getAchivmentLevels(player.getTarget().getPlayer(), player);
			return;
		}
		else if(bypass.equalsIgnoreCase("_bbs_get_effects"))
		{
			if(player.getTarget() != null && player.getTarget().isPlayer())
				PlayerShift.effects(player, player.getTarget().getPlayer());
			return;
		}
		else if(bypass.equalsIgnoreCase("_bbs_henna_draw"))
		{
			player.sendPacket(new HennaEquipList(player));
			html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/symbolmaker.htm", player);
			ShowBoard.separateAndSend(html, player);
			return;
		}
		else if(bypass.equalsIgnoreCase("_bbs_henna_remove"))
		{
			player.sendPacket(new HennaUnequipList(player));
			html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/symbolmaker.htm", player);
			ShowBoard.separateAndSend(html, player);
			return;
		}

		else if(bypass.equalsIgnoreCase("_bbs_augment_item"))
		{
			player.sendPacket(Msg.SELECT_THE_ITEM_TO_BE_AUGMENTED, ExShowVariationMakeWindow.STATIC);
			html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/blacksmithPushkin.htm", player);
			ShowBoard.separateAndSend(html, player);
			return;
		}
		else if(bypass.equalsIgnoreCase("_bbs_remove_augment"))
		{
			player.sendPacket(Msg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, ExShowVariationCancelWindow.STATIC);
			html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/blacksmithPushkin.htm", player);
			ShowBoard.separateAndSend(html, player);
			return;
		}
		else if(bypass.startsWith("_bbsclan") && Config.ENABLE_NEW_CLAN_CB)
		{
			NewClanCommunity.usebypass(player, bypass);
			return;
		}
		else if(bypass.equalsIgnoreCase("_bbsVisual"))
		{
			if (!Config.ENABLE_VISUAL_SYSTEM)
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "Visual transformation is disabled!");
				return;
			}
			
			if (!player.antiFlood.canPutVisual())
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Visuals", "Flood Protection, please try again in 10 sec.");
				return;
			}
			
			boolean haveNpc = Visuals.getInstance().getAllSpawnedNPCs().containsValue(player.getName());
			
			if (haveNpc)
			{
				Visuals.getInstance().destroyVisual(player.getName());
				html = refreshIndex(html, player);
				ShowBoard.separateAndSend(html, player);
			}
			else
				Visuals.getInstance().spawnVisual(player);
			
			return;
		}
		else if(bypass.equalsIgnoreCase("_bbsGrandBoss"))
		{
			html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/grandobssinfo.htm", player);
			
			html = html.replaceAll("%antharasSpawn%", "" + Config.FIXINTERVALOFANTHARAS_HOUR);
			html = html.replaceAll("%antharasSpawnRnd%", "" + Config.RANDOM_TIME_OF_ANTHARAS);
			html = html.replaceAll("%antharasDeathTime%", "" + getGrandBossStatus("AntharasDeath"));
			html = html.replaceAll("%valakasSpawn%", "" + Config.FIXINTERVALOFVALAKAS);
			html = html.replaceAll("%valakasSpawnRnd%", "" + Config.RANDOM_TIME_OF_VALAKAS);
			html = html.replaceAll("%valakasDeathTime%", "" + getGrandBossStatus("ValakasDeath"));
			html = html.replaceAll("%baiumSpawn%", "" + Config.FIXINTERVALOFBAIUM_HOUR);
			html = html.replaceAll("%baiumSpawnRnd%", "" + Config.RANDOMINTERVALOFBAIUM);
			html = html.replaceAll("%baiumDeathTime%", "" + getGrandBossStatus("BaiumDeath"));
			html = html.replaceAll("%sailrenSpawn%", "" + Config.FIXINTERVALOFSAILRENSPAWN_HOUR);
			html = html.replaceAll("%sailrenSpawnRnd%", "" + Config.RANDOMINTERVALOFSAILRENSPAWN);
			html = html.replaceAll("%sailrenDeathTime%", "" + getGrandBossStatus("SailrenDeath"));
			html = html.replaceAll("%orfenDeathTime%", "" + getGrandBossStatus("OrfenDeath"));
			html = html.replaceAll("%coreDeathTime%", "" + getGrandBossStatus("CoreDeath"));
			html = html.replaceAll("%BaylorDeathTime%", "" + getGrandBossStatus("BaylorDeath"));
			html = html.replaceAll("%BaylorSpawn%", "" + Config.FIXINTERVALOFBAYLORSPAWN_HOUR);
			html = html.replaceAll("%BaylorRnd%", "" + Config.RANDOMINTERVALOFBAYLORSPAWN);
			html = html.replaceAll("%queenAntDeathTime%", "" + getGrandBossStatus("QueenAntDeath"));
		}
		else if(bypass.equalsIgnoreCase("_bbsCCP"))
		{
			html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/ccp.htm", player);
			
			Creature target = null;
			
			double hpRegen = Formulas.calcHpRegen(player);
			double cpRegen = Formulas.calcCpRegen(player);
			double mpRegen = Formulas.calcMpRegen(player);
			double hpDrain = player.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0., target, null);
			double mpDrain = player.calcStat(Stats.ABSORB_DAMAGEMP_PERCENT, 0., target, null);
			double hpGain = player.calcStat(Stats.HEAL_EFFECTIVNESS, 100., target, null);
			double mpGain = player.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100., target, null);
			double critPerc = 2 * player.calcStat(Stats.CRITICAL_DAMAGE, target, null);
			double critStatic = player.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, null);
			double mCritRate = player.calcStat(Stats.MCRITICAL_RATE, target, null);
			double blowRate = player.calcStat(Stats.FATALBLOW_RATE, target, null);

			ItemInstance shld = player.getSecondaryWeaponInstance();
			boolean shield = shld != null && shld.getItemType() == WeaponType.NONE;

			double shieldDef = shield ? player.calcStat(Stats.SHIELD_DEFENCE, player.getTemplate().getBaseShldDef(), target, null) : 0.;
			double shieldRate = shield ? player.calcStat(Stats.SHIELD_RATE, target, null) : 0.;

			double fireResist = player.calcStat(Element.FIRE.getDefence(), 0., target, null);
			double windResist = player.calcStat(Element.WIND.getDefence(), 0., target, null);
			double waterResist = player.calcStat(Element.WATER.getDefence(), 0., target, null);
			double earthResist = player.calcStat(Element.EARTH.getDefence(), 0., target, null);
			double holyResist = player.calcStat(Element.HOLY.getDefence(), 0., target, null);
			double unholyResist = player.calcStat(Element.UNHOLY.getDefence(), 0., target, null);

			double bleedPower = player.calcStat(Stats.BLEED_POWER, target, null);
			double bleedResist = player.calcStat(Stats.BLEED_RESIST, target, null);
			double poisonPower = player.calcStat(Stats.POISON_POWER, target, null);
			double poisonResist = player.calcStat(Stats.POISON_RESIST, target, null);
			double stunPower = player.calcStat(Stats.STUN_POWER, target, null);
			double stunResist = player.calcStat(Stats.STUN_RESIST, target, null);
			double rootPower = player.calcStat(Stats.ROOT_POWER, target, null);
			double rootResist = player.calcStat(Stats.ROOT_RESIST, target, null);
			double sleepPower = player.calcStat(Stats.SLEEP_POWER, target, null);
			double sleepResist = player.calcStat(Stats.SLEEP_RESIST, target, null);
			double paralyzePower = player.calcStat(Stats.PARALYZE_POWER, target, null);
			double paralyzeResist = player.calcStat(Stats.PARALYZE_RESIST, target, null);
			double mentalPower = player.calcStat(Stats.MENTAL_POWER, target, null);
			double mentalResist = player.calcStat(Stats.MENTAL_RESIST, target, null);
			double debuffPower = player.calcStat(Stats.DEBUFF_POWER, target, null);
			double debuffResist = player.calcStat(Stats.DEBUFF_RESIST, target, null);
			double cancelPower = player.calcStat(Stats.CANCEL_POWER, target, null);
			double cancelResist = player.calcStat(Stats.CANCEL_RESIST, target, null);

			double swordResist = 100. - player.calcStat(Stats.SWORD_WPN_VULNERABILITY, target, null);
			double dualResist = 100. - player.calcStat(Stats.DUAL_WPN_VULNERABILITY, target, null);
			double bluntResist = 100. - player.calcStat(Stats.BLUNT_WPN_VULNERABILITY, target, null);
			double daggerResist = 100. - player.calcStat(Stats.DAGGER_WPN_VULNERABILITY, target, null);
			double bowResist = 100. - player.calcStat(Stats.BOW_WPN_VULNERABILITY, target, null);
			double crossbowResist = 100. - player.calcStat(Stats.CROSSBOW_WPN_VULNERABILITY, target, null);
			double poleResist = 100. - player.calcStat(Stats.POLE_WPN_VULNERABILITY, target, null);
			double fistResist = 100. - player.calcStat(Stats.FIST_WPN_VULNERABILITY, target, null);

			double critChanceResist = 100. - player.calcStat(Stats.CRIT_CHANCE_RECEPTIVE, target, null);
			double critDamResistStatic = player.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, target, null);
			double critDamResist = 100. - 100 * (player.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, 1., target, null) - critDamResistStatic);
			
			NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
			df.setMaximumFractionDigits(1);
			df.setMinimumFractionDigits(1);

			html = html.replace("%hpRegen%", df.format(hpRegen));
			html = html.replace("%cpRegen%", df.format(cpRegen));
			html = html.replace("%mpRegen%", df.format(mpRegen));
			html = html.replace("%hpDrain%", df.format(hpDrain));
			html = html.replace("%mpDrain%", df.format(mpDrain));
			html = html.replace("%hpGain%", df.format(hpGain));
			html = html.replace("%mpGain%", df.format(mpGain));
			html = html.replace("%critPerc%", df.format(critPerc));
			html = html.replace("%critStatic%", df.format(critStatic));
			html = html.replace("%mCritRate%", df.format(mCritRate));
			html = html.replace("%blowRate%", df.format(blowRate));
			html = html.replace("%shieldDef%", df.format(shieldDef));
			html = html.replace("%shieldRate%", df.format(shieldRate));
			html = html.replace("%fireResist%", df.format(fireResist));
			html = html.replace("%windResist%", df.format(windResist));
			html = html.replace("%waterResist%", df.format(waterResist));
			html = html.replace("%earthResist%", df.format(earthResist));
			html = html.replace("%holyResist%", df.format(holyResist));
			html = html.replace("%darkResist%", df.format(unholyResist));
			html = html.replace("%bleedPower%", df.format(bleedPower));
			html = html.replace("%bleedResist%", df.format(bleedResist));
			html = html.replace("%poisonPower%", df.format(poisonPower));
			html = html.replace("%poisonResist%", df.format(poisonResist));
			html = html.replace("%stunPower%", df.format(stunPower));
			html = html.replace("%stunResist%", df.format(stunResist));
			html = html.replace("%rootPower%", df.format(rootPower));
			html = html.replace("%rootResist%", df.format(rootResist));
			html = html.replace("%sleepPower%", df.format(sleepPower));
			html = html.replace("%sleepResist%", df.format(sleepResist));
			html = html.replace("%paralyzePower%", df.format(paralyzePower));
			html = html.replace("%paralyzeResist%", df.format(paralyzeResist));
			html = html.replace("%mentalPower%", df.format(mentalPower));
			html = html.replace("%mentalResist%", df.format(mentalResist));
			html = html.replace("%debuffPower%", df.format(debuffPower));
			html = html.replace("%debuffResist%", df.format(debuffResist));
			html = html.replace("%cancelPower%", df.format(cancelPower));
			html = html.replace("%cancelResist%", df.format(cancelResist));
			html = html.replace("%swordResist%", df.format(swordResist));
			html = html.replace("%dualResist%", df.format(dualResist));
			html = html.replace("%bluntResist%", df.format(bluntResist));
			html = html.replace("%daggerResist%", df.format(daggerResist));
			html = html.replace("%bowResist%", df.format(bowResist));
			html = html.replace("%crossbowResist%", df.format(crossbowResist));
			html = html.replace("%fistResist%", df.format(fistResist));
			html = html.replace("%poleResist%", df.format(poleResist));
			html = html.replace("%critChanceResist%", df.format(critChanceResist));
			html = html.replace("%critDamResist%", df.format(critDamResist));
		}
		
		ShowBoard.separateAndSend(html, player);
	}

	private String refreshIndex(String html, Player player)
	{	
		html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/index.htm", player);
		
		html = html.replace("%currentTime%", "" + TimeUtils.convertDateToString(System.currentTimeMillis()));
		html = html.replace("%uptime%", Util.formatTime(GameServer.getInstance().uptime()));
		html = html.replace("%serverRev%", GameServer.getInstance().getVersion().getRevisionNumber());
		html = html.replace("%buildDate%", GameServer.getInstance().getVersion().getBuildDate());
		html = html.replace("%Online%",  String.valueOf(ONLINE));
		html = html.replace("%Offline%", String.valueOf(OFFLINE));
		html = html.replace("%Offbuff%", String.valueOf(OFFLINE_BUFFER));
		html = html.replace("%playerClssName%", "" + player.getClassId().getName(player));
		html = html.replace("%playerIP%", "" + player.getIP());
		html = html.replace("%playerName%", "" + player.getName() + " (" + player.getLevel() + ")");
		html = html.replace("%noble%", player.isNoble() ? "Yes" : "No");
		
		PremiumAccount template = PremiumAccountsTable.getPremiumAccount(player);
		if (template == PremiumAccountsTable.DEFAULT_PREMIUM_ACCOUNT)
			html = html.replace("%premium%", "<a action=\"bypass _bbslink\">Buy Premium</a>");
		else
			html = html.replace("%premium%", "<font color=\"D7DF01\">" + template.getTemplate().name + "</font> - " + TimeUtils.minutesToFullString((int) (template.getTimeLeftInMilis() / 60000), true, true, false, false) + " left.");
		
		if (PremiumAccountsTable.isPremium(player))
		{
			int timeRemaning = (int) (PremiumAccountsTable.getPremiumAccount(player).getTimeLeftInMilis() / 1000);
			String premiumName = PremiumAccountsTable.getPremiumAccount(player).getTemplate().name;
			html = html.replace("%premium%", "" + premiumName + "" + TimeUtils.getConvertedTime(timeRemaning));
		}
			
		String clanName = "No";
		if(player.getClan() != null)
			clanName = player.getClan().getName() + " (" + player.getClan().getLevel() + ")";
		
		html = html.replace("%playerClan%", "" + clanName);
		html = html.replace("%serverTime%", "" + TimeUtils.getTimeInServer());
		html = html.replace("%onlineTime%", "" + Util.formatTime((int) player.getOnlineTime()));
		
		AccountData data =  AccountsDAO.getAccountData(player.getAccountName());
		String hwid = "";
		
		if (data != null)
			hwid = data.allowedHwids;
		
		html = html.replace("%hwid%", "" + hwid != "" ? "<font color=\"FF8000\"><a action=\"bypass -h user_lock \">Bind HWID</a></font>" : "<font color=\"64FE2E\">HWID Protected</font>");
		html = html.replace("%security%", player.getSecurityPassword() == null ? "<font color=\"FE2E2E\"><a action=\"bypass -h user_security \">Set Security</a></font>" : "<font color=\"64FE2E\">Secured</font>");
		
		return html;
	
	}
	
	private String getGrandBossStatus(String boss)
	{
		SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("HH:** dd/MM/yyyy");
		
		long date = 0;
		date = ServerVariables.getLong(boss, 0);
		
		if (date == 0)
			return "Status: <font color=\"74DF00\">Alive</font>";
		else
			return "Last Death: <font color=\"df0101\">" + SIMPLE_FORMAT.format(date) + "</font>";
	}
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{}
}
