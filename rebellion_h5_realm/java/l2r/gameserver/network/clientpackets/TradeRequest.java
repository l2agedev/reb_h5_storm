package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Request;
import l2r.gameserver.model.Request.L2RequestType;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.network.serverpackets.SendTradeRequest;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.utils.Util;

public class TradeRequest extends L2GameClientPacket
{
	//Format: cd
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if (Config.SECURITY_ENABLED && Config.SECURITY_TRADE_ENABLED && activeChar.getSecurity())
		{
			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			return;
		}
		
		if(!activeChar.getAccessLevel().allowTransaction())
		{
			activeChar.sendPacket(SystemMsg.SOME_LINEAGE_II_FEATURES_HAVE_BEEN_LIMITED_FOR_FREE_TRIALS_);
			activeChar.sendActionFailed();
			return;
		}
		
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendPacket(SystemMsg.YOU_ARE_ALREADY_TRADING_WITH_SOMEONE);
			return;
		}

		if(activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(SystemMsg.WAITING_FOR_ANOTHER_REPLY);
			return;
		}
		
		if (!activeChar.canOverrideCond(PcCondOverride.ITEM_TRADE_CONDITIONS))
		{
			if(activeChar.isOutOfControl())
			{
				activeChar.sendActionFailed();
				return;
			}
			
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (activeChar.getKarma() > 0))
			{
				activeChar.sendMessage("You cannot trade while you are in a chaotic state.");
				return;
			}
			
			if(activeChar.isFishing())
			{
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
				return;
			}
			
			if (NexusEvents.isInEvent(activeChar))
			{
				activeChar.sendMessage("Cannot make trade while in event.");
				return;
			}
			
			if (activeChar.isInCombat())
			{
				activeChar.sendMessage("Cannot request trade while you are in combat.");
				return;
			}
			
			String tradeBan = activeChar.getVar("tradeBan");
			if(tradeBan != null && (tradeBan.equals("-1") || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
			{
				if(tradeBan.equals("-1"))
					activeChar.sendMessage(new CustomMessage("common.TradeBannedPermanently", activeChar));
				else
					activeChar.sendMessage(new CustomMessage("common.TradeBanned", activeChar).addString(Util.formatTime((int)(Long.parseLong(tradeBan) / 1000L - System.currentTimeMillis() / 1000L))));
				return;
			}
		}

		GameObject target = activeChar.getVisibleObject(_objectId);
		if(target == null || !target.isPlayer() || target == activeChar)
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return;
		}
		
		Player reciever = target.getPlayer();

		if (!activeChar.canOverrideCond(PcCondOverride.ITEM_TRADE_CONDITIONS))
		{
			if(!activeChar.isInRangeZ(target, Creature.INTERACTION_DISTANCE))
			{
				activeChar.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
				return;
			}
			
			if(!reciever.getAccessLevel().allowTransaction())
			{
				activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
				return;
			}

			if (Config.SECURITY_ENABLED && Config.SECURITY_TRADE_ENABLED && reciever.getSecurity())
			{
				reciever.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (reciever.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
				return;
			}
			
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (reciever.getKarma() > 0))
			{
				activeChar.sendMessage("You cannot request a trade while your target is in a chaotic state.");
				return;
			}
			
			if (activeChar.isInJail() || reciever.isInJail())
			{
				activeChar.sendMessage("You cannot trade while you are in in Jail.");
				return;
			}
			
			String tradeBan = reciever.getVar("tradeBan");
			if(tradeBan != null && (tradeBan.equals("-1") || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
			{
				activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
				return;
			}

			if(reciever.isInBlockList(activeChar))
			{
				activeChar.sendPacket(SystemMsg.YOU_HAVE_BEEN_BLOCKED_FROM_CHATTING_WITH_THAT_CONTACT);
				return;
			}

			if(reciever.getTradeRefusal() || reciever.isBusy())
			{
				activeChar.sendPacket(new SystemMessage2(SystemMsg.C1_IS_ON_ANOTHER_TASK).addString(reciever.getName()));
				return;
			}
		}

		new Request(L2RequestType.TRADE_REQUEST, activeChar, reciever).setTimeout(10000L);
		reciever.sendPacket(new SendTradeRequest(activeChar.getObjectId()));
		activeChar.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_REQUESTED_A_TRADE_WITH_C1).addString(reciever.getName()));
	}
}