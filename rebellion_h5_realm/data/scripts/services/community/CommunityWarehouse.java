package services.community;

import l2r.gameserver.Config;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.scripts.ScriptFile;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author RuleZzz
 */
public class CommunityWarehouse implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityWarehouse.class);
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_bbswarehouse",
		};
	}
	
	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		if (!Config.ALLOW_BBS_WAREHOUSE)
			return;
		
		if (player == null)
			return;
		
		if (!player.isGM())
		{
			if (!Config.BBS_WAREHOUSE_ALLOW_PK && player.getKarma() > 0)
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Warehouse", (player.isLangRus() ? "PK нельзя использовать склад" : "PK can not use a warehouse"));
				return;
			}
			
			if (Config.SECURITY_ENABLED && Config.SECURITY_TRADE_ENABLED && player.getSecurity())
			{
				player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Warehouse", (player.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
				return;
			}
			
			if (PremiumAccountsTable.getCareerOutsidePeace(player))
			{
				// I cant think of any... what should block you from using warehouse outside becase besides others?
			}
			else
			{				
				if (!player.isInZone(ZoneType.peace_zone) && !player.isInZone(ZoneType.RESIDENCE))
				{
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Warehouse", (player.isLangRus() ? "Вы должны быть в зону мира, чтобы использовать эту функцию." : "You must be inside peace zone to use this function."));
					return;
				}
			}
			
			if (player.isCursedWeaponEquipped() || player.isInJail() || player.getReflectionId() != ReflectionManager.DEFAULT_ID /* || player.getPvpFlag() != 0*/ || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.isFlying() || player.isTerritoryFlagEquipped() || player.isInZone(ZoneType.no_escape) || player.isInZone(ZoneType.SIEGE) || player.isInZone(ZoneType.epic))
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Warehouse", "You cannot use Warehouse due restrictions. Please try again later.");
				return;
			}
		}

		StringTokenizer st = new StringTokenizer(bypass, ":");
		st.nextToken();
		String action = st.hasMoreTokens() ? st.nextToken() : "";
		if (action.equalsIgnoreCase("private_deposit"))
			player.showDepositWindow();
		else if (action.equalsIgnoreCase("private_retrieve"))
			player.showRetrieveWindow(getVal(st.nextToken()));
		else if (action.equalsIgnoreCase("clan_deposit"))
			player.showDepositWindowClan();
		else if (action.equalsIgnoreCase("clan_retrieve"))
			player.showWithdrawWindowClan(getVal(st.nextToken()));
		showMain(player);
	}
	
	private int getVal(String name)
	{
		name = name.trim();
		if (name.equalsIgnoreCase("Оружие") || name.equalsIgnoreCase("Weapon") || name.equalsIgnoreCase("1"))
			return 1;
		else if (name.equalsIgnoreCase("Броня") || name.equalsIgnoreCase("Armor") || name.equalsIgnoreCase("2"))
			return 2;
		else if (name.equalsIgnoreCase("Бижутерия") || name.equalsIgnoreCase("Jewelry") || name.equalsIgnoreCase("3"))
			return 3;
		else if (name.equalsIgnoreCase("Украшения") || name.equalsIgnoreCase("Accessory") || name.equalsIgnoreCase("4"))
			return 4;
		else if (name.equalsIgnoreCase("Предметы снабжения") || name.equalsIgnoreCase("Consumable") || name.equalsIgnoreCase("5"))
			return 5;
		else if (name.equalsIgnoreCase("Материалы") || name.equalsIgnoreCase("Material") || name.equalsIgnoreCase("6"))
			return 6;
		else if (name.equalsIgnoreCase("Ключевые материалы") || name.equalsIgnoreCase("Key Material") || name.equalsIgnoreCase("7"))
			return 7;
		else if (name.equalsIgnoreCase("Рецепты") || name.equalsIgnoreCase("Recipe") || name.equalsIgnoreCase("8"))
			return 8;
		else if (name.equalsIgnoreCase("Книги") || name.equalsIgnoreCase("Books") || name.equalsIgnoreCase("9"))
			return 9;
		else if (name.equalsIgnoreCase("Разное") || name.equalsIgnoreCase("Misc") || name.equalsIgnoreCase("10"))
			return 10;
		else if (name.equalsIgnoreCase("Прочее") || name.equalsIgnoreCase("Other") || name.equalsIgnoreCase("11"))
			return 11;
		
		return 0;
	}
	
	private void showMain(Player player)
	{
		if (player == null)
			return;
		String htm = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/warehouse.htm", player);
		StringBuilder sb = new StringBuilder();
		htm = htm.replace("<?content?>", sb.toString());
		ShowBoard.separateAndSend(htm, player);
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
	
	@Override
	public void onLoad()
	{
		if (Config.COMMUNITYBOARD_ENABLED)
		{
			_log.info("CommunityBoard: Warehouse loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}
	
	@Override
	public void onReload()
	{
		if (Config.COMMUNITYBOARD_ENABLED)
			CommunityBoardManager.getInstance().removeHandler(this);
	}
	
	@Override
	public void onShutdown()
	{
	}
	
}
