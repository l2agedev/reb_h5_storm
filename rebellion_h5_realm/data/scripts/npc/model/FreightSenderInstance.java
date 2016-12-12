package npc.model;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.MerchantInstance;
import l2r.gameserver.network.serverpackets.PackageToList;
import l2r.gameserver.templates.npc.NpcTemplate;

/**
 * @author VISTALL
 * @date 20:32/16.05.2011
 */
public class FreightSenderInstance extends MerchantInstance
{

	public FreightSenderInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("deposit_items"))
			player.sendPacket(new PackageToList(player));
		else if(command.equalsIgnoreCase("withdraw_items"))
			player.showFreightWindow();
		else
			super.onBypassFeedback(player, command);
	}
}
