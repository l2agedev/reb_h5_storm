package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.cache.ItemInfoCache;
import l2r.gameserver.dao.EmotionsTable;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.handler.admincommands.AdminCommandHandler;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2r.gameserver.instancemanager.PetitionManager;
import l2r.gameserver.listener.actor.player.impl.GmAnswerListener;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.entity.olympiad.OlympiadGame;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.matching.MatchingRoom;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.randoms.TradesHandler;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.AdminTable;
import l2r.gameserver.utils.AutoHuntingPunish;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.Strings;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Say2C extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(Say2C.class);

	/** RegExp для кэширования ссылок на предметы, пример ссылки: \b\tType=1 \tID=268484598 \tColor=0 \tUnderline=0 \tTitle=\u001BAdena\u001B\b */
	public static final Pattern EX_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+[\\s]+\tID=([0-9]+)[\\s]+\tColor=[0-9]+[\\s]+\tUnderline=[0-9]+[\\s]+\tTitle=\u001B(.[^\u001B]*)[^\b]");
	public static final Pattern SKIP_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+(.[^\b]*)[\b]");
	public static final Pattern CYRILIC_LANG_PATTERN = Pattern.compile(".*\\p{InCyrillic}.*");
	
	private String _text;
	private ChatType _type;
	private String _target;

	@Override
	protected void readImpl()
	{
		_text = readS(Config.CHAT_MESSAGE_MAX_LEN);
		_type = l2r.commons.lang.ArrayUtils.valid(ChatType.VALUES, readD());
		_target = _type == ChatType.TELL ? readS(Config.CNAME_MAXLEN) : null;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if (activeChar.isBeingPunished() && activeChar.getBotPunishType() == AutoHuntingPunish.Punish.CHATBAN)
		{
			if (activeChar.getPlayerPunish().canTalk() && activeChar.getBotPunishType() == AutoHuntingPunish.Punish.CHATBAN)
			{
				activeChar.endPunishment();
			}
			else if (activeChar.getBotPunishType() == AutoHuntingPunish.Punish.CHATBAN)
			{
				activeChar.sendPacket(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_CHATTING_WILL_BE_BLOCKED_FOR_10_MINUTES);
				return;
			}
		}

		if(_type == null || _text == null || _text.length() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		if (!NexusEvents.onSay(activeChar, _text, _type.ordinal()))
		{
			activeChar.sendActionFailed();
			return;
		}
		
		_text = _text.replaceAll("\\\\n", "\n");

		if(_text.contains("\n"))
		{
			String[] lines = _text.split("\n");
			_text = StringUtils.EMPTY;
			for(int i = 0; i < lines.length; i++)
			{
				lines[i] = lines[i].trim();
				if(lines[i].length() == 0)
					continue;
				if(_text.length() > 0)
					_text += "\n  >";
				_text += lines[i];
			}
		}

		if(_text.length() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}
		
		if(Functions.isEventStarted("events.Trivia.TriviaEvent"))
		{
			if(activeChar.getVar("trivia") == "on")
			{
				String answer = _text.trim();
				if(answer.length() > 0)
				{
					Object[] objects = { answer, activeChar };
					Functions.callScripts("events.Trivia.TriviaEvent", "checkAnswer", objects);
				}
			}
			
		}
		
		if (_text.startsWith(".") && (_text.equals(".respawn") || _text.equals(".suicide")) && NexusEvents.isInEvent(activeChar))
		{
			NexusEvents.trySuicide(activeChar);
			return;
		}
		
		if (_text.equals(".clean"))
		{
			activeChar.sendMessage("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
			return;
		}

		 // Handle shift-click item into .tradelist
		if (Config.ENABLE_TRADELIST_VOICE && (_text.toUpperCase().startsWith("WTS") || _text.toUpperCase().startsWith("WTB")))
			TradesHandler.tryPublishTrade(activeChar, _text);
		
		if (_text.startsWith(".") && _text.endsWith("findparty") && Config.PARTY_SEARCH_COMMANDS)
		{
			String fullcmd = _text.substring(1).trim();
			String command = fullcmd.split("\\s+")[0];
			String args = fullcmd.substring(command.length()).trim();

			if(command.length() > 0)
			{
				// then check for VoicedCommands
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
				if(vch != null)
				{
					
					if(_text.indexOf(8) >= 0)
						if(!checkActions(activeChar))
							return;
					
					vch.useVoicedCommand(command, activeChar, args);
					return;
				}
			}
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.command404", activeChar));
			return;
			
		}
		
		else if(_text.startsWith(".") && !Config.DISABLE_VOICED_COMMANDS) // Если доступны войс комманды по конфигу, обрабатываем их
		{
			String fullcmd = _text.substring(1).trim();
			String command = fullcmd.split("\\s+")[0];
			String args = fullcmd.substring(command.length()).trim();

			if(command.length() > 0 && !fullcmd.startsWith("."))
			{
				// then check for VoicedCommands
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
				if(vch != null)
				{
					vch.useVoicedCommand(command, activeChar, args);
					return;
				}
				else
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.command404", activeChar));
			}
		}
		
		else if(_text.startsWith(".") && _text.endsWith("offline") && Config.SERVICES_OFFLINE_TRADE_ALLOW && Config.DISABLE_VOICED_COMMANDS) // Если войс комманды не доступны, но включен оффлайн трей, обрабатываем его
		{
			String fullcmd = _text.substring(1).trim();
			String command = fullcmd.split("\\s+")[0];
			String args = fullcmd.substring(command.length()).trim();

			if(command.length() > 0)
			{
				// then check for VoicedCommands
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
				if(vch != null)
				{
					vch.useVoicedCommand(command, activeChar, args);
					return;
				}
			}
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.command404", activeChar));
			return;
		}
		
		if(Config.CHATFILTER_MIN_LEVEL > 0 && ArrayUtils.contains(Config.CHATFILTER_CHANNELS, _type.ordinal()) && activeChar.getLevel() < Config.CHATFILTER_MIN_LEVEL && !activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
		{
			if(Config.CHATFILTER_WORK_TYPE == 1)
				_type = ChatType.ALL;
			else if(Config.CHATFILTER_WORK_TYPE == 2)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ChatNotHavePermission", activeChar).addNumber(Config.CHATFILTER_MIN_LEVEL));
				return;
			}
		}

		boolean globalchat = _type != ChatType.ALLIANCE && _type != ChatType.CLAN && _type != ChatType.PARTY;

		if(globalchat && (Config.TRADE_CHATS_REPLACE_FROM_ALL && _type == ChatType.ALL) || (Config.TRADE_CHATS_REPLACE_FROM_SHOUT && _type == ChatType.SHOUT) || (Config.TRADE_CHATS_REPLACE_FROM_TRADE && _type == ChatType.TRADE))
		{
			for(String s : Config.TRADE_WORDS)
			{
				if(_text.contains(s))
				{
					_type = ChatType.TRADE;
					break;
				}
			}
		}

		if((globalchat || ArrayUtils.contains(Config.BAN_CHANNEL_LIST, _type.ordinal())) && activeChar.getNoChannel() != 0)
		{
			if(activeChar.getNoChannelRemained() > 0 || activeChar.getNoChannel() < 0)
			{
				if(activeChar.getNoChannel() > 0)
				{
					int timeRemained = Math.round(activeChar.getNoChannelRemained() / 60000);
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ChatBanned", activeChar).addNumber(timeRemained));
				}
				else
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ChatBannedPermanently", activeChar));
				}
				
				activeChar.sendActionFailed();
				return;
			}
			activeChar.updateNoChannel(0);
		}

		if(globalchat && !activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
		{
			if(Config.ABUSEWORD_REPLACE && Config.containsAbuseWord(_text))
			{
				for (Pattern regex : Config.ABUSEWORD_LIST)
					_text = regex.matcher(_text).replaceAll(Config.ABUSEWORD_REPLACE_STRING);
				
				activeChar.sendActionFailed();
			}
			else if(Config.ABUSEWORD_BANCHAT && Config.containsAbuseWord(_text))
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ChatBanned", activeChar).addNumber(Config.ABUSEWORD_BANTIME * 60));
				Log.addGame(activeChar + ": " + _text, "abuse");
				activeChar.updateNoChannel(Config.ABUSEWORD_BANTIME * 60000);
				activeChar.sendActionFailed();
				return;
			}
		}

		// Кэширование линков предметов
		Matcher m = EX_ITEM_LINK_PATTERN.matcher(_text);
		ItemInstance item;
		int objectId;

		while(m.find())
		{
			objectId = Integer.parseInt(m.group(1));
			item = activeChar.getInventory().getItemByObjectId(objectId);

			if(item == null)
			{
				activeChar.sendActionFailed();
				break;
			}

			ItemInfoCache.getInstance().put(item);
		}

		
		String translit = activeChar.getVar("translit");
		if(translit != null)
		{
			//Исключаем из транслитерации ссылки на предметы
			m = SKIP_ITEM_LINK_PATTERN.matcher(_text);
			StringBuilder sb = new StringBuilder();
			int end = 0;
			while(m.find())
			{
				sb.append(Strings.fromTranslit(_text.substring(end, end = m.start()), translit.equals("tl") ? 1 : 2));
				sb.append(_text.substring(end, end = m.end()));
			}

			_text = sb.append(Strings.fromTranslit(_text.substring(end, _text.length()), translit.equals("tl") ? 1 : 2)).toString();
		}

		int emotion = EmotionsTable.containsEmotion(_text);
		if(!activeChar.getVarB("emotions") && emotion != -1 && !World.getAroundPlayers(activeChar, 500, 300).isEmpty() && !activeChar.isInCombat() && !activeChar.isDead() && !activeChar.isCastingNow() && !activeChar.isSitting())
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), emotion));
		
		Say2 cs = new Say2(activeChar.getObjectId(), _type, activeChar.getName(), _text);

		int identifierForLog = 0;
		
		switch(_type)
		{
			case TELL:
				Player receiver = World.getPlayer(_target);
				if (receiver == null && activeChar.getPet() != null && activeChar.getPet().getName().equals(_target))
					receiver = activeChar;
				
				if(receiver != null)
				{
					if (Config.GM_PM_COMMANDS)
					{
						int index = _text.indexOf("//");
						if (index == 0)
						{
							String[] wordList = _text.substring(index+2).split(" ");
							if (wordList.length > 0)
							{
								
								// Build the bypass: //command targetname otherWords
								String bypass = "admin_" + wordList[0] +  " " + _target;
								for (int i = 1; i < wordList.length; i++)
									bypass += " " + wordList[i];

								if (!AdminTable.getInstance().hasAccess(bypass, activeChar.getAccessLevel()))
								{
									activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestBypassToServer.message1", activeChar));
									_log.warn("Character " + activeChar.getName() + " tried to use admin command " + bypass + ", without proper access level!");
									return;
								}
								
								if (AdminTable.getInstance().requireConfirm(bypass))
								{
									activeChar.ask(new l2r.gameserver.network.serverpackets.ConfirmDlg(SystemMsg.S1, 30000).addString("Are you sure you want execute command " + bypass), new GmAnswerListener(activeChar, bypass));
									return;
								}
								
								activeChar.setTarget(null); // Remove target!!! Many commands which are used with target wont be executed as it should.
								AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, bypass);
							}
							break;
						}
					}
					
					if (receiver.isInOfflineMode())
					{
						// Original Message: {0} is in offline trade mode.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.targetInOfflineTrade", activeChar).addString(receiver));
						activeChar.sendActionFailed();
					}
					else if(!receiver.isInBlockList(activeChar) && (!receiver.isBlockAll() || receiver.canAcceptPM(activeChar.getObjectId())))
					{
						if(!receiver.getMessageRefusal() || receiver.canAcceptPM(activeChar.getObjectId()))
						{
							if (activeChar.isBlockAll() || activeChar.getMessageRefusal()) // Allow to mutual talk with someone you want, while you are in msg refusal.
								activeChar.acceptPM(receiver.getObjectId());
							
							if(activeChar.antiFlood.canTell(receiver.getObjectId(), _text))
							{
								receiver.sendPacket(cs);
								cs = new Say2(activeChar.getObjectId(), _type, "->" + _target, _text);
								activeChar.sendPacket(cs);
							}
						}
						else if (receiver.canOverrideCond(PcCondOverride.CHAT_CONDITIONS) && receiver.getMessageRefusal() && !receiver.canAcceptPM(activeChar.getObjectId()))
							activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_IS_NOT_CURRENTLY_LOGGED_IN).addString(_target), ActionFail.STATIC);
						else
							activeChar.sendPacket(SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
					}
					else
						activeChar.sendPacket(SystemMsg.YOU_HAVE_BEEN_BLOCKED_FROM_CHATTING_WITH_THAT_CONTACT, ActionFail.STATIC);
				}
				else
					activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_IS_NOT_CURRENTLY_LOGGED_IN).addString(_target), ActionFail.STATIC);
				break;
			case SHOUT:
				if (!activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
				{
					if(activeChar.isCursedWeaponEquipped())
					{
						activeChar.sendPacket(SystemMsg.SHOUT_AND_TRADE_CHATTING_CANNOT_BE_USED_WHILE_POSSESSING_A_CURSED_WEAPON);
						return;
					}
					if(activeChar.isInObserverMode())
					{
						activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
						return;
					}

					if(!activeChar.antiFlood.canShout(_text, PremiumAccountsTable.getGlobalChat(activeChar)))
					{
						if (Config.GLOBAL_SHOUT && PremiumAccountsTable.getGlobalChat(activeChar))
						{
							// Original Message: Shout chat is allowed once per {0} seconds.
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ShoutChat1", activeChar).addNumber(Config.CHAT_SHOUT_TIME_DELAY / 2));
						}
						else
						{
							// Original Message: Shout chat is allowed once per {0} seconds.
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ShoutChat1", activeChar).addNumber(Config.CHAT_SHOUT_TIME_DELAY));
						}
						return;
					}

					if(Config.GLOBAL_SHOUT && activeChar.isInJail())
					{
						// Original Message: You may not use this chat while in jail.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ShoutChat3", activeChar));
						return;
					}
					
					if(activeChar.getPvpKills() < Config.PVP_COUNT_SHOUT)
					{
						// Original Message: Shout chat is allowed only for characters with at least {0} PvP kills.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ShoutChat2", activeChar).addNumber(Config.PVP_COUNT_SHOUT));
						return;
					}
					if(activeChar.getOnlineTime() < Config.ONLINE_TIME_SHOUT)
					{
						// Original Messsage: You character must have at least {0} hours online time to use shout chat.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ShoutChat4", activeChar).addNumber(Config.ONLINE_TIME_SHOUT / 360));
						return;
					}
					
					if(activeChar.getLevel() < Config.LEVEL_FOR_SHOUT)
					{
						// Original Message: You character must be level {0} or above to use this chat.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ShoutChat5", activeChar).addNumber(Config.LEVEL_FOR_SHOUT));						
						return;
					}
				}
				
				if(Config.GLOBAL_SHOUT)
					announce(activeChar, cs, _text);
				else
					shout(activeChar, cs, _text);

				activeChar.sendPacket(cs);
				break;
			case TRADE:
				if (!activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
				{
					if(activeChar.isCursedWeaponEquipped())
					{
						activeChar.sendPacket(SystemMsg.SHOUT_AND_TRADE_CHATTING_CANNOT_BE_USED_WHILE_POSSESSING_A_CURSED_WEAPON);
						return;
					}
					if(activeChar.isInObserverMode())
					{
						activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
						return;
					}

					if(!activeChar.antiFlood.canTrade(_text, PremiumAccountsTable.getGlobalChat(activeChar)))
					{
						if (Config.GLOBAL_TRADE_CHAT && PremiumAccountsTable.getGlobalChat(activeChar))
						{
							// Original Message: Trade chat is allowed only once per {0} seconds.
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.TradeChat1", activeChar).addNumber(Config.CHAT_TRADE_TIME_DELAY / 2));
						}
						else
							// Original Message: Trade chat is allowed only once per {0} seconds.
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.TradeChat1", activeChar).addNumber(Config.CHAT_TRADE_TIME_DELAY));
						return;
					}

					if(activeChar.getPvpKills() < Config.PVP_COUNT_TRADE)
					{
						// Original Message: Trade chat is allowed only for characters with at least {0} PvP kills.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.TradeChat2", activeChar).addNumber(Config.PVP_COUNT_TRADE));
						return;
					}

					if(activeChar.getOnlineTime() < Config.ONLINE_TIME_TRADE)
					{
						// Original Message: You character must have at least {0} hours online time to use trade chat.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.TradeChat3", activeChar).addNumber(Config.ONLINE_TIME_TRADE / 360));
						return;
					}
					
					if(activeChar.getLevel() < Config.LEVEL_FOR_TRADE)
					{
						// Original Messagee: You character must be level {0} or above to use Trade chat.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.TradeChat4", activeChar).addNumber(Config.LEVEL_FOR_TRADE));
						return;
					}
					
					if(Config.GLOBAL_TRADE_CHAT && activeChar.isInJail())
					{
						// Original Message: You may not use this chat while in jail.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ShoutChat3", activeChar));
						return;
					}
					
					if (Config.USE_TRADE_WORDS_ON_GLOBAL_CHAT && Config.GLOBAL_TRADE_CHAT)
					{
						boolean allowed = false;
						for (String stw : Config.TRADE_WORDS)
						{
							if (_text.toLowerCase().startsWith(stw))
							{
								allowed = true;
								break;
							}
						}
						
						if (!allowed)
						{
							// Original Message: This chat is for trade/sell/buy, For Example: (WTB/WTT/WTS Valakas Necklace.)
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.TradeChat5", activeChar));
							// Original Message: TradeWords: {0}.
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.TradeChat6", activeChar).addString(Config.TRADE_WORDS));
							return;
						}
					}
				}
				
				if(Config.GLOBAL_TRADE_CHAT)
					announce(activeChar, cs, _text);
				else
					shout(activeChar, cs, _text);

				activeChar.sendPacket(cs);
				break;
			case ALL:
				if(activeChar.isCursedWeaponEquipped())
					cs = new Say2(activeChar.getObjectId(), _type, activeChar.getTransformationName(), _text);

				List<Player> list = null;

				if(activeChar.isInObserverMode() && activeChar.getObserverRegion() != null && activeChar.getOlympiadObserveGame() != null)
				{
					OlympiadGame game = activeChar.getOlympiadObserveGame();
					if(game != null)
						list = game.getAllPlayers();
				}
				else if(activeChar.isInOlympiadMode())
				{
					OlympiadGame game = activeChar.getOlympiadGame();
					if(game != null)
						list = game.getAllPlayers();
				}
				else
					list = World.getAroundPlayers(activeChar);

				if(list != null)
					for(Player player : list)
					{
						if(player == activeChar || player.getReflection() != activeChar.getReflection() || player.isBlockAll() || player.isInBlockList(activeChar))
							continue;
						
						// Only russian language players can use this.
						if (player.getVarB("noEnglish", false) && !containsCyrillic(_text) && player.isLangRus())
							continue;
						
						// Only english language players can use this.
						if (player.getVarB("noCyrilic", false) && containsCyrillic(_text) && player.isLangEng())
							continue;
						
						player.sendPacket(cs);
					}

				activeChar.sendPacket(cs);
				break;
			case CLAN:
				if(activeChar.getClan() != null)
				{
					identifierForLog = activeChar.getClanId();
					
					activeChar.getClan().broadcastToOnlineMembers(cs);
				}
				break;
			case ALLIANCE:
				if(activeChar.getClan() != null && activeChar.getClan().getAlliance() != null)
				{
					identifierForLog = activeChar.getAllyId();
					
					activeChar.getClan().getAlliance().broadcastToOnlineMembers(cs);
				}
				break;
			case PARTY:
				if(activeChar.isInParty())
				{
					identifierForLog = activeChar.getPlayerGroup().hashCode();
					
					activeChar.getParty().sendPacket(cs);
				}
				break;
			case PARTY_ROOM:
				MatchingRoom room = activeChar.getMatchingRoom();
				if (room == null || room.getType() != MatchingRoom.PARTY_MATCHING)
					return;

				identifierForLog = room.getId();
				
				for (Player roomMember : room.getPlayers())
					if (activeChar.canTalkWith(roomMember))
						roomMember.sendPacket(cs);
				break;
			case COMMANDCHANNEL_ALL:
				if (activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
				{
					activeChar.getPlayerGroup().sendPacket(cs);
				}
				else
				{
					if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
					{
						activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
						return;
					}
					
					identifierForLog = activeChar.getPlayerGroup().hashCode();
					
					if(activeChar.getParty().getCommandChannel().getLeader() == activeChar)
						activeChar.getParty().getCommandChannel().sendPacket(cs);
					else
						activeChar.sendPacket(SystemMsg.ONLY_THE_COMMAND_CHANNEL_CREATOR_CAN_USE_THE_RAID_LEADER_TEXT);
				}
				break;
			case COMMANDCHANNEL_COMMANDER:
				if (activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
				{
					if (activeChar.getParty() != null && activeChar.getParty().getLeader() != null)
						activeChar.getPlayerGroup().sendPacket(activeChar.getParty().getLeader(), cs);
				}
				else
				{
					if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
					{
						activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
						return;
					}
					
					identifierForLog = activeChar.getParty().getCommandChannel().hashCode();
					
					if(activeChar.getParty().isLeader(activeChar))
						activeChar.getParty().getCommandChannel().broadcastToChannelPartyLeaders(cs);
					else
						activeChar.sendPacket(SystemMsg.ONLY_A_PARTY_LEADER_CAN_ACCESS_THE_COMMAND_CHANNEL);
				}
				break;
			case HERO_VOICE:
				if(activeChar.isHero() || activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
				{
					
					// Ограничение только для героев, гм-мы пускай говорят.
					if (!activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
					{
						if(activeChar.isInJail())
						{
							// Original Message: You may not use this chat while in jail.
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ShoutChat3", activeChar));
							return;
						}
						else if (!activeChar.antiFlood.canHero(_text))
						{
							// Original Message: Hero chat is allowed once per {0} seconds.
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.HeroChat1", activeChar).addNumber(Config.CHAT_HERO_TIME_DELAY));
							return;
						}
					}
					for(Player player : GameObjectsStorage.getAllPlayersForIterate())
						if(!player.isInBlockList(activeChar) && !player.isBlockAll())
							player.sendPacket(cs);
				}
				break;
			case PETITION_PLAYER:
			case PETITION_GM:
				if(!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
				{
					activeChar.sendPacket(new SystemMessage2(SystemMsg.YOU_ARE_CURRENTLY_NOT_IN_A_PETITION_CHAT));
					return;
				}

				PetitionManager.getInstance().sendActivePetitionMessage(activeChar, _text);
				break;
			case BATTLEFIELD:
				if (!activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS) && activeChar.getBattlefieldChatId() == 0)
					return;
				
				identifierForLog = activeChar.getBattlefieldChatId();
				
				for (Player player : GameObjectsStorage.getAllPlayersForIterate())
					if (!player.isInBlockList(activeChar) && !player.isBlockAll() && player.getBattlefieldChatId() == activeChar.getBattlefieldChatId())
						player.sendPacket(cs);
				break;
			case MPCC_ROOM:
				MatchingRoom mpccRoom = activeChar.getMatchingRoom();
				if(mpccRoom == null || mpccRoom.getType() != MatchingRoom.CC_MATCHING)
					return;

				identifierForLog = mpccRoom.getId();
				
				for (Player roomMember : mpccRoom.getPlayers())
					if (activeChar.canTalkWith(roomMember))
						roomMember.sendPacket(cs);
				break;
			default:
				_log.warn("Character " + activeChar.getName() + " used unknown chat type: " + _type.ordinal() + ".");
		}
		
		Log.LogChat(_type.name(), activeChar.getName(), _target, _text, identifierForLog);

		activeChar.getListeners().onSay(_type, _target, _text);
	}

	private static void shout(Player activeChar, Say2 cs, String text)
	{
		int rx = World.regionX(activeChar);
		int ry = World.regionY(activeChar);
		int offset = Config.SHOUT_OFFSET;

		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player == activeChar || activeChar.getReflection() != player.getReflection() || player.isBlockAll() || player.isInBlockList(activeChar))
				continue;

			// Only russian language players can use this.
			if (player.getVarB("noEnglish", false) && !containsCyrillic(text) && player.isLangRus())
				continue;
			
			// Only english language players can use this.
			if (player.getVarB("noCyrilic", false) && containsCyrillic(text) && player.isLangEng())
				continue;
			
			int tx = World.regionX(player);
			int ty = World.regionY(player);

			if(tx >= rx - offset && tx <= rx + offset && ty >= ry - offset && ty <= ry + offset || activeChar.isInRangeZ(player, Config.CHAT_RANGE))
				player.sendPacket(cs);
		}
	}

	private static void announce(Player activeChar, Say2 cs, String text)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player == activeChar || activeChar.getReflection() != player.getReflection() || player.isBlockAll() || player.isInBlockList(activeChar))
				continue;

			// Only russian language players can use this.
			if (player.getVarB("noEnglish", false) && !containsCyrillic(text) && player.isLangRus())
				continue;
			
			// Only english language players can use this.
			if (player.getVarB("noCyrilic", false) && containsCyrillic(text) && player.isLangEng())
				continue;
			
			player.sendPacket(cs);
		}
	}
	
	private boolean checkActions(Player owner)
	{
		int pos1 = -1;
		while((pos1 = _text.indexOf(8, pos1)) > -1)
		{
			int pos = _text.indexOf("ID=", pos1);
			if(pos == -1)
				return false;
			StringBuilder result = new StringBuilder(9);
			pos += 3;
			while(Character.isDigit(_text.charAt(pos)))
				result.append(_text.charAt(pos++));
			int id = Integer.parseInt(result.toString());

			if(id == 1000007 && _text.contains("Party looking for members"))
				return true;
			
			pos1 = _text.indexOf(8, pos) + 1;
			if(pos1 == 0) // missing ending tag
			{
				_log.info(getClient() + " sent invalid publish item msg! ID:" + id);
				return false;
			}
		}
		return true;
	}
	
	public static boolean containsCyrillic(String text)
	{
		return text.matches(CYRILIC_LANG_PATTERN.pattern());
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}