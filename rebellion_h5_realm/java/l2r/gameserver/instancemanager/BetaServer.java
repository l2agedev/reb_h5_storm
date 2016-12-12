package l2r.gameserver.instancemanager;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.BuyListHolder;
import l2r.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2r.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2r.gameserver.listener.skills.OnSkillEnchantListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.actor.instances.player.ShortCut;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.model.base.EnchantSkillLearn;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.network.serverpackets.ExBuySellList;
import l2r.gameserver.network.serverpackets.ExEnchantSkillInfo;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.ShortCutRegister;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.tables.SkillTreeTable;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.TimeUtils;
import l2r.gameserver.utils.Util;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Future;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * 
 * @author Infern0
 * @date 14.05.2016
 *
 */
public class BetaServer implements OnPlayerEnterListener, OnSkillEnchantListener, IVoicedCommandHandler
{
	private static final Logger _log = LoggerFactory.getLogger(BetaServer.class);
	private static BetaServer _instance;

	private static Calendar _startDate = Calendar.getInstance();
	private static Calendar _endDate = Calendar.getInstance();
	
	private static boolean _accessGmshop;
	private static boolean _maxLevel;
	private static boolean _autoNoble;
	private static boolean _autoHero;
	private static boolean _maxSp ;
	private static boolean _noClanPenaltys;
	private static boolean _maxEnchantSkill;
	private static int _itemCreationEnchant;
	private static long _adenaOnLogin;
	private static String _spawnPoint;
	
	private Future<?> _announceTask = null;
	
	private int ANNOUNCE_DELAY = 3600000; // 60 minutes
	
	public static BetaServer getInstance()
	{
		if(_instance == null)
			_instance = new BetaServer();
		return _instance;
	}
	
	public void reload()
	{
		_log.info("Beta Server: Reloading...");
		load();
		
		String startDate = TimeUtils.toSimpleFormat(_startDate);
		String endDate = TimeUtils.toSimpleFormat(_endDate);
		
		if (!isBetaServerAvailable())
		{
			VoicedCommandHandler.getInstance().removeVoicedCommandHandler(this);
			_log.info("Beta Server: Disabled... Date has passed - [Start Date: " + startDate + " End Date: " + endDate);
			return;
		}
		
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
		
		startAnnounceTask(ANNOUNCE_DELAY);
		
		String activeFor = Util.formatTime((int) ((_endDate.getTimeInMillis() - System.currentTimeMillis()) / 1000));
		_log.info("Beta Server: Successfuly reloaded. Active for " + activeFor + " | Start Date: " + startDate + " End Date: " + endDate);
	}

	public BetaServer()
	{
		_log.info("Beta Server: Loading...");
		load();
		
		String startDate = TimeUtils.toSimpleFormat(_startDate);
		String endDate = TimeUtils.toSimpleFormat(_endDate);
		
		if (!isBetaServerAvailable())
		{
			VoicedCommandHandler.getInstance().removeVoicedCommandHandler(this);
			_log.info("Beta Server: Disabled... Date has passed - [Start Date: " + startDate + " End Date: " + endDate);
			return;
		}
		
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
		
		// Start spam ...
		startAnnounceTask(ANNOUNCE_DELAY);
		
		String activeFor = Util.formatTime((int) ((_endDate.getTimeInMillis() - System.currentTimeMillis()) / 1000));
		_log.info("Beta Server: Successfuly reloaded. Active for " + activeFor + " | Start Date: " + startDate + " End Date: " + endDate);
		
	}

	private void startAnnounceTask(int delay)
	{
		stopAnnounceTask();
		_announceTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new announceTask(), 30000, delay);
	}
	
	private void stopAnnounceTask()
	{
		if (_announceTask != null)
			_announceTask.cancel(true);
		
		_announceTask = null;
	}
			
	private void load()
	{
		CharListenerList.addGlobal(this);
		
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);

			File file = Config.getFile("config/BetaServer.xml");
			if(!file.exists())
			{
				_log.warn("Beta Server: NO FILE at " + Config.getFile("config/BetaServer.xml").getAbsolutePath());
				return;
			}

			Document doc = factory.newDocumentBuilder().parse(file);
			for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				if("list".equalsIgnoreCase(n.getNodeName()))
				{
					for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if("settings".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							Node att;
							
							if ((att = attrs.getNamedItem("startDate")) == null)
							{
								_log.warn("Beta Server: Missing [startDate].. terminate loading...");
								break;
							}
							
							_startDate.setTimeInMillis(TimeUtils.getMillisecondsFromString(att.getNodeValue()));
							
							if ((att = attrs.getNamedItem("endDate")) == null)
							{
								_log.warn("Beta Server: Missing  [endDate].. terminate loading...");
								break;
							}
							
							_endDate.setTimeInMillis(TimeUtils.getMillisecondsFromString(att.getNodeValue()));
							
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("canGmshop".equalsIgnoreCase(cd.getNodeName()))
								{
									Node accessGmshop = cd.getAttributes().getNamedItem("val");
									_accessGmshop = accessGmshop != null ? Boolean.parseBoolean(accessGmshop.getNodeValue()) : false;
								}
								else if ("maxLevel".equalsIgnoreCase(cd.getNodeName()))
								{
									Node maxLevel = cd.getAttributes().getNamedItem("val");
									_maxLevel = maxLevel != null ? Boolean.parseBoolean(maxLevel.getNodeValue()) : false;
								}
								else if ("maxSp".equalsIgnoreCase(cd.getNodeName()))
								{
									Node maxSp = cd.getAttributes().getNamedItem("val");
									_maxSp = maxSp != null ? Boolean.parseBoolean(maxSp.getNodeValue()) : false;
								}
								else if ("autoNoble".equalsIgnoreCase(cd.getNodeName()))
								{
									Node autoNoble = cd.getAttributes().getNamedItem("val");
									_autoNoble = autoNoble != null ? Boolean.parseBoolean(autoNoble.getNodeValue()) : false;
								}
								else if ("autoHero".equalsIgnoreCase(cd.getNodeName()))
								{
									Node autoHero = cd.getAttributes().getNamedItem("val");
									_autoHero = autoHero != null ? Boolean.parseBoolean(autoHero.getNodeValue()) : false;
								}
								else if ("noClanPenalty".equalsIgnoreCase(cd.getNodeName()))
								{
									Node noClanPenalty = cd.getAttributes().getNamedItem("val");
									_noClanPenaltys = noClanPenalty != null ? Boolean.parseBoolean(noClanPenalty.getNodeValue()) : false;
								}
								else if ("maxSkillEnchant".equalsIgnoreCase(cd.getNodeName()))
								{
									Node maxSkillEnchant = cd.getAttributes().getNamedItem("val");
									_maxEnchantSkill = maxSkillEnchant != null ? Boolean.parseBoolean(maxSkillEnchant.getNodeValue()) : false;
								}
								else if ("itemCreationEnchant".equalsIgnoreCase(cd.getNodeName()))
								{
									Node itemCreationEnchant = cd.getAttributes().getNamedItem("val");
									_itemCreationEnchant = itemCreationEnchant != null ? Integer.parseInt(itemCreationEnchant.getNodeValue()) : 0;
								}
								else if ("adenaOnLogin".equalsIgnoreCase(cd.getNodeName()))
								{
									Node adenaOnLogin = cd.getAttributes().getNamedItem("val");
									_adenaOnLogin = adenaOnLogin != null ? Long.parseLong(adenaOnLogin.getNodeValue()) : 0;
								}
								else if ("spawnPoint".equalsIgnoreCase(cd.getNodeName()))
								{
									Node spawnPoint = cd.getAttributes().getNamedItem("val");
									_spawnPoint = spawnPoint != null ? spawnPoint.getNodeValue() : "";
								}
							}
						}
					}
				}
			_log.info("Beta Server: Data loaded from xml....");
		}
		catch(Exception e)
		{
			_log.warn("Beta Server: Error parsing BetaServer.xml file. " + e);
		}
	}
	
	public static boolean canAccessGmshop()
	{
		return _accessGmshop;
	}
	
	public static boolean canMaxLevel()
	{
		return _maxLevel;
	}
	
	public static boolean canAutoNoble()
	{
		return _autoNoble;
	}
	
	public static boolean canAutoHero()
	{
		return _autoHero;
	}
	
	public static boolean canMaxSp()
	{
		return _maxSp;
	}
	
	public static boolean canRemovePenalty()
	{
		return _noClanPenaltys;
	}
	
	public static boolean canMaxEnchantSkill()
	{
		return _maxEnchantSkill;
	}
	
	public static int getItemCreationEnchant()
	{
		return _itemCreationEnchant;
	}
	
	public static long getLoginAdena()
	{
		return _adenaOnLogin;
	}

	public static String getSpawnPoint()
	{
		return _spawnPoint;
	}
	
	public static boolean isBetaServerAvailable()
	{
		Date currentTime = Calendar.getInstance().getTime();
		return currentTime.after(_startDate.getTime()) && currentTime.before(_endDate.getTime());
	}
	
	public static Calendar getBetaStartDate()
	{
		return _startDate;
	}
	
	public static Calendar getBetaEndDate()
	{
		return _endDate;
	}
	
	@Override
	public void onPlayerEnter(Player player)
	{
		if (player == null)
			return;
		
		if (!isBetaServerAvailable())
			return;
		
		ThreadPoolManager.getInstance().schedule(new sendPlayerStatus(player), 5000); // 5 sec delay before start task.
	}

	@Override
	public void onSkillEnchant(Player player, Skill skill, boolean success, boolean safeEnchant)
	{
		if (player == null)
			return;
		
		if (!isBetaServerAvailable())
			return;
		
		if (_maxEnchantSkill && skill != null)
		{
			EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(skill.getId(), skill.getLevel());
			if(sl == null)
				return;

			int slevel = player.getSkillLevel(skill.getId());
			if(slevel == -1)
				return;

			int enchantLevel = SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), skill.getLevel(), sl.getMaxLevel());

			// already knows the skill with this level
			if(slevel >= enchantLevel)
				return;
			
			if(slevel == sl.getBaseLevel() ? skill.getId() % 100 != 1 : slevel != enchantLevel - 1)
				return;

			skill = SkillTable.getInstance().getInfo(skill.getId(), enchantLevel);
			if(skill == null)
				return;
			
			// Max the skill level.
			skill = SkillTable.getInstance().getInfo(skill.getId(), enchantLevel + sl.getMaxLevel() - 1);
			
			player.addSkill(skill, true);
			
			// Update shortcuts
			for(ShortCut sc : player.getAllShortCuts())
				if(sc.getId() == skill.getId() && sc.getType() == ShortCut.TYPE_SKILL)
				{
					ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skill.getLevel(), 1);
					player.sendPacket(new ShortCutRegister(player, newsc));
					player.registerShortCut(newsc);
				}
			
			player.sendPacket(new ExEnchantSkillInfo(skill.getId(), player.getSkillDisplayLevel(skill.getLevel())));
			player.sendMessage("Your skill " + skill.getName() + " received maximum enchant.");
		}
	}

	private class announceTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if (!isBetaServerAvailable())
				return;
			
			// Last 3 hours spam beta end.
			int timer = (int) ((_endDate.getTimeInMillis() - System.currentTimeMillis()) / 1000); // time in seconds
			timer = Math.max(0, timer);
			if (timer > 10800)
			{
				Announcements.getInstance().announceToAll("Server is in Beta Test Mode until: " + TimeUtils.toSimpleFormat(_endDate));
				return;
			}
			
			// if time goes below 3 hours start new faster task, and start announce when the beta will end.
			startAnnounceTask(1000);
			
			// time in seconds
			switch(timer)
			{
				case 10800:
				case 7200:
				case 3600:
					Announcements.getInstance().announceToAll("Beta Test Mode will be turned OFF after " + TimeUtils.minutesToFullString(timer, true, false, true, true));
					break;
				case 1800:
				case 900:
				case 600:
				case 300:
				case 240:
				case 180:
				case 120:
				case 60:
					Announcements.getInstance().announceToAll("Beta Test Mode will be turned OFF after " + TimeUtils.minutesToFullString(timer, true, false, false, true));
					break;
				case 0:
					Announcements.getInstance().announceToAll("Beta Test has officially finished! Thanks to everyone who participated.");
					stopAnnounceTask();
					return;
			}
		}
	}
	
	private class sendPlayerStatus extends RunnableImpl
	{
		private Player _player;
		
		public sendPlayerStatus(Player player)
		{
			_player = player;
		}
		
		@Override
		public void runImpl()
		{
			if (_player == null || !_player.isOnline())
				return;

			if (isBetaServerAvailable())
			{
				_player.sendMessageS("Server is currently in Beta Test Mode. Until " + TimeUtils.toSimpleFormat(_endDate), 10); // Screen Message
				
				_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "Welcome to L2 World of Ages.");
				_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "Server is currently in Beta Test Mode!");
				_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "Beta will be available until " + TimeUtils.toSimpleFormat(_endDate));
				_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "For questions and issues contact the server staff via petition or forum.");
				_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "Happy farming :)");
				

				if (!_spawnPoint.isEmpty() && _player.getLevel() == 1 && _maxLevel)
				{
					Location loc = Location.parseLoc(_spawnPoint);
					if (loc != null)
						_player.teleToLocation(loc, 0);
				}
				
				if (_accessGmshop)
				{
					_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "~~~~~~~~~~~~~~~~");
					_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "You can access the GMShop by typing .gmshop");
					_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "~~~~~~~~~~~~~~~~");
				}
				
				if (_maxSp && _player.getSp() != Integer.MAX_VALUE)
				{
					_player.setSp(Integer.MAX_VALUE);
					_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "Your SP has been increased to maximum.");
					_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "~~~~~~~~~~~~~~~~");
				}
				
				if (_maxLevel && _player.getLevel() != Experience.getMaxLevel())
				{
					long exp_add = Experience.LEVEL[Experience.getMaxLevel()] - _player.getExp();
					_player.addExpAndSp(exp_add, 0);
					_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "Your level has been set to " + Experience.getMaxLevel());
					_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "~~~~~~~~~~~~~~~~");
					
					if (_player.getClassId().level() != 4)
					{
						// send class cb in 5 sec, due teleporting
						ThreadPoolManager.getInstance().schedule(new RunnableImpl()
						{
							@Override
							public void runImpl()
							{
								Util.communityNextPage(_player, "_bbscareer;");
							}
						}, 5000);
					}
				}
				
				if (_autoHero && !_player.isHero())
				{
					_player.setHero(true);
					_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "You have received Hero status.");
					_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "~~~~~~~~~~~~~~~~");
				}
				
				if (_autoNoble && !_player.isNoble())
				{
					Olympiad.addNoble(_player);
					_player.setNoble(true);
					_player.updatePledgeClass();
					_player.updateNobleSkills();
					_player.sendPacket(new SkillList(_player));
					_player.broadcastUserInfo(true);
					_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "You have received noble status.");
					_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "~~~~~~~~~~~~~~~~");
				}
				
				if (_adenaOnLogin != 0)
				{
					long playerAdena = _player.getAdena();
					if (playerAdena < _adenaOnLogin)
						_player.addAdena(_adenaOnLogin - playerAdena);
					_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "Your adena was set to " + Util.formatAdena(_adenaOnLogin));
					_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "~~~~~~~~~~~~~~~~");
				}
				
				if (_noClanPenaltys)
				{
					if (_player.getDeleteClanTime() > 0)
					{
						_player.setDeleteClanTime(0);
						_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "Cleaned clan create penalty...");
					}
					if (_player.getLeaveClanTime() > 0)
					{
						_player.setLeaveClanTime(0);
						_player.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "Beta", "Cleaned clan join penatly...");
					}
				}
				
				_player.broadcastUserInfo(true);
			}
		}
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if (activeChar == null)
			return false;
		
		if (!isBetaServerAvailable())
			return false;
		
		if (_accessGmshop)
		{
			if (command.equalsIgnoreCase("gmshop"))
			{
				if(args != null && !args.isEmpty())
				{
					String[] param = args.split(" ");
					if (param.length == 1)
					{
						String html = HtmCache.getInstance().getNullable("BetaStore/" + param[0], activeChar);
						if (html != null)
						{
							NpcHtmlMessage npchtml = new NpcHtmlMessage(0);
							npchtml.setHtml(html);
							activeChar.sendPacket(npchtml);
						}
					}
				}
				else
					activeChar.sendPacket(new NpcHtmlMessage(5).setFile("BetaStore/gmshops.htm"));
			}
			else if (command.equalsIgnoreCase("buy") && args != null)
				handleBuyRequest(activeChar, args);
		}
		
		return true;
	}

	private void handleBuyRequest(Player activeChar, String value)
	{
		int val = -1;

		try
		{
			val = Integer.parseInt(value);
		}
		catch(Exception e)
		{
		}

		NpcTradeList list = BuyListHolder.getInstance().getBuyList(val);

		if(list != null && list.getNpcId() == -1) // only gmshop use -1.
			activeChar.sendPacket(new ExBuySellList.BuyList(list, activeChar, 0.), new ExBuySellList.SellRefundList(activeChar, false));

		activeChar.sendActionFailed();
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
	
	private static final String[] _voicedCommands =
	{
		"gmshop",
		"buy"
	};
}