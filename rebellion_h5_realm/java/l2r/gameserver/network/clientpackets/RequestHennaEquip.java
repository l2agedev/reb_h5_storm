package l2r.gameserver.network.clientpackets;

import l2r.gameserver.data.xml.holder.HennaHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.network.serverpackets.HennaEquipList;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.randoms.PvPCharacterIntro;
import l2r.gameserver.templates.Henna;

public class RequestHennaEquip extends L2GameClientPacket
{
	private int _symbolId;

	/**
	 * packet type id 0x6F
	 * format:		cd
	 */
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Henna temp = HennaHolder.getInstance().getHenna(_symbolId);
		if(temp == null || (!temp.isForThisClass(player) && !player.canOverrideCond(PcCondOverride.ITEM_USE_CONDITIONS)))
		{
			player.sendPacket(SystemMsg.THE_SYMBOL_CANNOT_BE_DRAWN);
			return;
		}
		
		if (PvPCharacterIntro.getInstance().getCharactertStep(player).equals("dyes"))
		{
			player.sendPacket(SystemMsg.THE_SYMBOL_HAS_BEEN_ADDED);
			player.addHenna(temp);
			
			if(player.getHennaEmptySlots() == 0)
			{
				PvPCharacterIntro.getInstance().showTutorialHTML("teleport", player);
				PvPCharacterIntro.getInstance().setCharacterStep(player, "teleport");
				return;
			}
			
			player.sendPacket(new HennaEquipList(player, false));
			return;
		}
		
		long countDye = player.getInventory().getCountOf(temp.getDyeId());
		if(countDye < temp.getDrawCount())
		{
			player.sendPacket(SystemMsg.THE_SYMBOL_CANNOT_BE_DRAWN);
			return;
		}

		if(!player.reduceAdena(temp.getPrice(), true))
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		if(player.consumeItem(temp.getDyeId(), temp.getDrawCount()))
		{
			player.sendPacket(SystemMsg.THE_SYMBOL_HAS_BEEN_ADDED);
			player.addHenna(temp);
		}
	}
}