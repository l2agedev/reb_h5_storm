package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ManufactureItem;
import l2r.gameserver.network.serverpackets.ConfirmDlg;
import l2r.gameserver.network.serverpackets.RecipeShopMsg;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class RequestRecipeShopListSet extends L2GameClientPacket
{
	private int[] _recipes;
	private long[] _prices;
	private int _count;

	@Override
	protected void readImpl()
	{
		_count = readD();
		if(_count * 12 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}
		_recipes = new int[_count];
		_prices = new long[_count];
		for(int i = 0; i < _count; i++)
		{
			_recipes[i] = readD();
			_prices[i] = readQ();
			if(_prices[i] < 0)
			{
				_count = 0;
				return;
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player manufacturer = getClient().getActiveChar();
		if(manufacturer == null || _count == 0)
			return;

		if(!manufacturer.canOpenPrivateStore(Player.STORE_PRIVATE_MANUFACTURE))
		{
			manufacturer.sendActionFailed();
			return;
		}

		if(_count > Config.MAX_PVTCRAFT_SLOTS)
		{
			manufacturer.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}

		List<ManufactureItem> createList = new CopyOnWriteArrayList<ManufactureItem>();
		for(int i = 0; i < _count; i++)
		{
			int recipeId = _recipes[i];
			long price = _prices[i];
			if(!manufacturer.findRecipe(recipeId))
				continue;

			ManufactureItem mi = new ManufactureItem(recipeId, price);
			createList.add(mi);
		}

		if(!createList.isEmpty())
		{
			manufacturer.setCreateList(createList);
			manufacturer.saveTradeList();
			manufacturer.setPrivateStoreType(Player.STORE_PRIVATE_MANUFACTURE);
			manufacturer.broadcastPacket(new RecipeShopMsg(manufacturer));
			manufacturer.sitDown(null);
			manufacturer.broadcastCharInfo();
			
			if (Config.SERVICES_OFFLINE_TRADE_ALLOW)
			{
				manufacturer.ask(new ConfirmDlg(SystemMsg.S1, 10000).addString("Do you want to go in offline store mode?"), new OnAnswerListener()
				{
					@Override
					public void sayYes()
					{
						IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("offline");
						if(vch != null && manufacturer != null)
							vch.useVoicedCommand("offline", manufacturer, "");
					}

					@Override
					public void sayNo()
					{
					}
				});
			}
		}

		manufacturer.sendActionFailed();
	}
}