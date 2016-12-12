package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.mail.Mail;
import l2r.gameserver.network.serverpackets.ExNoticePostArrived;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

public class Donate implements IVoicedCommandHandler
{
	private final HashMap<Integer, Attempt> _commandAttempts = new HashMap<>();

	private static final String[] COMMANDS =
	{
	    "donate"
	};

	private Attempt getAttempt(Player player)
	{
		final Attempt att = _commandAttempts.get(player.getObjectId());
		if (att == null)
		{
			final Attempt att2 = new Attempt();
			_commandAttempts.put(player.getObjectId(), att2);
			return att2;
		}
		return att;
	}

	private void sendHtml(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("command/donate.htm");
		player.sendPacket(html);
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if (args == null || args.length() == 0)
		{
			sendHtml(player);
			return true;
		}
		
		if (args.isEmpty() || !args.contains("@") || args.contains(" ") || !args.contains("."))
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Donation", "Incorrect email format. Please try again.");
			sendHtml(player);
			return false;
		}
		
		return retrieveDonation(args, player);
	}

	// (1e-9e NO Bonus)
	// (10€-24€ 10%+ Bonus)
	// (25€-99€ 15%+ Bonus)
	// (100€-199€ 20%+ Bonus)
	// (200€-299€ 25%+ Bonus)
	// (300€+ 35%+ Bonus)
	private synchronized final boolean retrieveDonation(String txn_id, Player player)
	{
		final Attempt attempt = getAttempt(player);
		if (!attempt.allowAttempt())
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Donation", "Please try again in few minutes. If this message still pop-up contact the server administrator.");
			return false;
		}

		if (txn_id == null || player == null)
			return false;

		try (Connection con = DatabaseFactory.getInstance().getConnection();
		     PreparedStatement st = con.prepareStatement("SELECT * FROM donations WHERE email=? AND retrieved=?"))
		{
			st.setString(1, txn_id);
			st.setString(2, "false");
			try (ResultSet rs = st.executeQuery())
			{
				int amount = 0;
				while (rs.next())
					amount += rs.getInt("amount");
				if (amount > 0)
				{
					int bonus = 1;
					for (Entry<Integer, Integer> bonuses : Config.DONATION_REWARD_BONUSES.entrySet())
					{
						if (amount >= bonuses.getKey())
						{
							bonus = bonuses.getValue();
							break;
						}
					}
					
					amount *= Config.DONATION_REWARD_MULTIPLIER_PER_EURO;
					if (ServerVariables.getBool("DonationBonusActive", false) && ServerVariables.getLong("DonationBonusTime") >= System.currentTimeMillis())
						amount += (amount * ServerVariables.getInt("DonationBonusPercent")) / 100; // % extra coins
					
					final int toGive = amount * (100 + bonus) / 100;
					
					Mail letter = new Mail();
					
					// Send a mail to the buyer.
					ItemInstance item = ItemFunctions.createItem(Config.DONATION_REWARD_ITEM_ID); // Gracian Coin
					letter.setSenderId(1);
					letter.setSenderName("Donate");
					letter.setReceiverId(player.getObjectId());
					letter.setReceiverName(player.getName());
					letter.setTopic("A gift for your donation has arrived!");
					
					
					letter.setBody("Thank you for supporting our server. You are rewarded with " + Util.formatAdena(toGive) + " coins. Enjoy your stay, and your reward :P");
					letter.setType(Mail.SenderType.NONE);
					letter.setUnread(true);
					letter.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
					
					item.setLocation(ItemInstance.ItemLocation.MAIL);
					item.setCount(toGive);
					item.save();
					letter.addAttachment(item);
					letter.save();
					
					if (player != null)
					{
						player.sendPacket(ExNoticePostArrived.STATIC_TRUE);
						player.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
					}
					
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Donation", "We have sent you email with a gift. Check it out :)");
					
					attempt.onAllow();

					try (PreparedStatement st2 = con.prepareStatement("UPDATE donations SET retrieved=?, retriever_ip=?, retriever_acc=?, retriever_char=?, retrieval_date=? WHERE email=?"))
					{
						st2.setString(1, "true");
						st2.setString(2, player.getIP());
						st2.setString(3, player.getAccountName());
						st2.setString(4, player.getName());
						st2.setString(5, formatDate(new Date(), "dd/MM/yyyy H:mm:ss"));
						st2.setString(6, txn_id);
						st2.executeUpdate();
					}
					return true;
				}
				else
				{
					attempt.onDeny();
					
					if (attempt.getTries() == 3)
						player.sendChatMessage(0, ChatType.TELL.ordinal(), "Donation", "You have been blocked for 3 minutes.");
					else
						player.sendChatMessage(0, ChatType.TELL.ordinal(), "Donation", "Sorry but i cannot find any data for the email you have typed. Plase try again later.");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		player.sendMessage("Please try again later or contact an Admin!");
		return false;
	}

	public static String formatDate(Date date, String format)
	{
		final DateFormat dateFormat = new SimpleDateFormat(format);
		if (date != null)
			return dateFormat.format(date);
		return null;
	}

	public static class Attempt
	{
		private int tries;
		private long banEx;

		public void onDeny()
		{
			if (++tries > 3)
			{
				banEx = System.currentTimeMillis() + 180000L;
				tries = 0;
			}
		}

		public void onAllow()
		{
			tries = 0;
			banEx = 0;
		}

		public boolean allowAttempt()
		{
			if (banEx > System.currentTimeMillis())
				return false;
			return true;
		}

		public int getTries()
		{
			return tries;
		}
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}
