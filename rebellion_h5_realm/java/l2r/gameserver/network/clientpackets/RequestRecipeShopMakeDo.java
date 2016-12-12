package l2r.gameserver.network.clientpackets;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.RecipeHolder;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Recipe;
import l2r.gameserver.model.RecipeComponent;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.ManufactureItem;
import l2r.gameserver.network.serverpackets.RecipeShopItemInfo;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.ItemFunctions;

public class RequestRecipeShopMakeDo extends L2GameClientPacket
{
	private int _manufacturerId;
	private int _recipeId;
	private long _price;

	@Override
	protected void readImpl()
	{
		_manufacturerId = readD();
		_recipeId = readD();
		_price = readQ();
	}

	@Override
	protected void runImpl()
	{
		Player buyer = getClient().getActiveChar();
		if(buyer == null)
			return;

		if(buyer.isActionsDisabled())
		{
			buyer.sendActionFailed();
			return;
		}

		if(buyer.isInStoreMode())
		{
			buyer.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(buyer.isInTrade())
		{
			buyer.sendActionFailed();
			return;
		}

		if(buyer.isFishing())
		{
			buyer.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			return;
		}

		if(!buyer.getAccessLevel().allowTransaction())
		{
			buyer.sendPacket(SystemMsg.SOME_LINEAGE_II_FEATURES_HAVE_BEEN_LIMITED_FOR_FREE_TRIALS_____);
			return;
		}

		Player manufacturer = (Player) buyer.getVisibleObject(_manufacturerId);
		if(manufacturer == null || manufacturer.getPrivateStoreType() != Player.STORE_PRIVATE_MANUFACTURE || !manufacturer.isInRangeZ(buyer, Creature.INTERACTION_DISTANCE))
		{
			buyer.sendActionFailed();
			return;
		}

		Recipe recipeList = null;
		for(ManufactureItem mi : manufacturer.getCreateList())
			if(mi.getRecipeId() == _recipeId)
				if(_price == mi.getCost())
				{
					recipeList = RecipeHolder.getInstance().getRecipeByRecipeId(_recipeId);
					break;
				}

		if(recipeList == null)
		{
			buyer.sendActionFailed();
			return;
		}

		int success = 0;

		if(recipeList.getRecipes().length == 0)
		{
			manufacturer.sendMessage(new CustomMessage("l2r.gameserver.RecipeController.NoRecipe", manufacturer).addString(recipeList.getRecipeName()));
			buyer.sendMessage(new CustomMessage("l2r.gameserver.RecipeController.NoRecipe", manufacturer).addString(recipeList.getRecipeName()));
			return;
		}

		if(!manufacturer.findRecipe(_recipeId))
		{
			buyer.sendActionFailed();
			return;
		}

		if(manufacturer.getCurrentMp() < recipeList.getMpCost())
		{
			manufacturer.sendPacket(SystemMsg.NOT_ENOUGH_MP);
			buyer.sendPacket(SystemMsg.NOT_ENOUGH_MP, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
			return;
		}

		buyer.getInventory().writeLock();
		try
		{
			if(buyer.getAdena() < _price)
			{
				buyer.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
				return;
			}

			RecipeComponent[] recipes = recipeList.getRecipes();

			for(RecipeComponent recipe : recipes)
			{
				if(recipe.getQuantity() == 0)
					continue;

				ItemInstance item = buyer.getInventory().getItemByItemId(recipe.getItemId());

				if(item == null || recipe.getQuantity() > item.getCount())
				{
					buyer.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_MATERIALS_TO_PERFORM_THAT_ACTION, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
					return;
				}
			}

			if(!buyer.reduceAdena(_price, false))
			{
				buyer.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
				return;
			}

			for(RecipeComponent recipe : recipes)
				if(recipe.getQuantity() != 0)
				{
					buyer.getInventory().destroyItemByItemId(recipe.getItemId(), recipe.getQuantity());
					//TODO audit
					buyer.sendPacket(SystemMessage2.removeItems(recipe.getItemId(), recipe.getQuantity()));
				}

			long tax = manufacturer.getTax(_price);
			if(tax > 0)
			{
				_price -= tax;
				manufacturer.sendMessage(new CustomMessage("trade.HavePaidTax", manufacturer).addNumber(tax));
			}

			manufacturer.addAdena(_price);
		}
		finally
		{
			buyer.getInventory().writeUnlock();
		}

		manufacturer.sendMessage(new CustomMessage("l2r.gameserver.RecipeController.GotOrder", manufacturer).addString(recipeList.getRecipeName()));

		manufacturer.reduceCurrentMp(recipeList.getMpCost(), null);
		manufacturer.sendStatusUpdate(false, false, StatusUpdate.CUR_MP);

		int tryCount = 1, successCount = 0;
		if(Rnd.chance(Config.CRAFT_DOUBLECRAFT_CHANCE))
			tryCount++;

		for(int i = 0; i < tryCount; i++)
			if(Rnd.chance(recipeList.getSuccessRate()))
			{
				int itemId = recipeList.getFoundation() != 0 ? Rnd.chance(Config.CRAFT_MASTERWORK_CHANCE) ? recipeList.getFoundation() : recipeList.getItemId() : recipeList.getItemId();
				long count = recipeList.getCount();
				ItemFunctions.addItem(buyer, itemId, count, true);
				success = 1;
				successCount++;
			}

		SystemMessage2 sm;
		if(successCount == 0)
		{
			sm = new SystemMessage2(SystemMsg.C1_HAS_FAILED_TO_CREATE_S2_AT_THE_PRICE_OF_S3_ADENA);
			sm.addString(manufacturer.getName());
			sm.addItemName(recipeList.getItemId());
			sm.addInteger(_price);
			buyer.sendPacket(sm);

			sm = new SystemMessage2(SystemMsg.YOUR_ATTEMPT_TO_CREATE_S2_FOR_C1_AT_THE_PRICE_OF_S3_ADENA_HAS_FAILED);
			sm.addString(buyer.getName());
			sm.addItemName(recipeList.getItemId());
			sm.addInteger(_price);
			manufacturer.sendPacket(sm);

		}
		else if(recipeList.getCount() > 1 || successCount > 1)
		{
			sm = new SystemMessage2(SystemMsg.C1_CREATED_S2_S3_AT_THE_PRICE_OF_S4_ADENA);
			sm.addString(manufacturer.getName());
			sm.addItemName(recipeList.getItemId());
			sm.addInteger(recipeList.getCount() * successCount);
			sm.addInteger(_price);
			buyer.sendPacket(sm);

			sm = new SystemMessage2(SystemMsg.S2_S3_HAVE_BEEN_SOLD_TO_C1_FOR_S4_ADENA);
			sm.addString(buyer.getName());
			sm.addItemName(recipeList.getItemId());
			sm.addInteger(recipeList.getCount() * successCount);
			sm.addInteger(_price);
			manufacturer.sendPacket(sm);

		}
		else
		{
			sm = new SystemMessage2(SystemMsg.C1_CREATED_S2_AFTER_RECEIVING_S3_ADENA);
			sm.addString(manufacturer.getName());
			sm.addItemName(recipeList.getItemId());
			sm.addInteger(_price);
			buyer.sendPacket(sm);

			sm = new SystemMessage2(SystemMsg.S2_IS_SOLD_TO_C1_FOR_THE_PRICE_OF_S3_ADENA);
			sm.addString(buyer.getName());
			sm.addItemName(recipeList.getItemId());
			sm.addInteger(_price);
			manufacturer.sendPacket(sm);
		}

		buyer.sendChanges();
		buyer.sendPacket(new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
	}
}