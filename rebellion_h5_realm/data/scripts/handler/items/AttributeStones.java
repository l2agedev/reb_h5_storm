package handler.items;

import l2r.gameserver.cache.Msg;
import l2r.gameserver.handler.items.ItemHandler;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ConfirmDlg;
import l2r.gameserver.network.serverpackets.ExChooseInventoryAttributeItem;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.ScriptFile;

public class AttributeStones extends ScriptItemHandler implements ScriptFile
{
	private static final int[] _itemIds = {
		9546,
		9547,
		9548,
		9549,
		9550,
		9551,
		9552,
		9553,
		9554,
		9555,
		9556,
		9557,
		9558,
		9563,
		9561,
		9560,
		9562,
		9559,
		9567,
		9566,
		9568,
		9565,
		9564,
		9569,
		10521,
		10522,
		10523,
		10524,
		10525,
		10526};

	@Override
	public boolean useItem(Playable playable, final ItemInstance item, final boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		
		final Player player = (Player) playable;

		if(player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			player.sendPacket(Msg.YOU_CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			return false;
		}

		if(player.getEnchantScroll() != null)
			return false;

		if (ctrl)
		{
			player.ask(new ConfirmDlg(SystemMsg.S1, 10000).addString("Do you wanna use Batch item Attribute Function?"), new OnAnswerListener()
			{
				@Override
				public void sayYes()
				{
					player.sendMessage(new CustomMessage("scripts.handler.items.AttributeStones.message1", player).addNumber(item.getCount()));
					player.setEnchantScroll(item, (int) item.getCount()); // Custom ctrl click to attribute on max.
					player.sendPacket(Msg.PLEASE_SELECT_ITEM_TO_ADD_ELEMENTAL_POWER);
					player.sendPacket(new ExChooseInventoryAttributeItem(item));
					return;
				}

				@Override
				public void sayNo()
				{
					player.sendMessage("You are using normal attribute function. One attribute stone/crystal will be used each time.");
				}
			});
		}
		
		player.setEnchantScroll(item, 0);
		
		player.sendPacket(Msg.PLEASE_SELECT_ITEM_TO_ADD_ELEMENTAL_POWER);
		player.sendPacket(new ExChooseInventoryAttributeItem(item));
		return true;
	}

	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}


	@Override
	public boolean pickupItem(Playable playable, ItemInstance item)
	{
		return true;
	}

	@Override
	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	@Override
	public void onReload()
	{

	}

	@Override
	public void onShutdown()
	{

	}
}