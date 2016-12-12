package services.community;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Party;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SubClass;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.network.serverpackets.ConfirmDlg;
import l2r.gameserver.network.serverpackets.JoinParty;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.utils.Util;

public class PartyMatchingBoard extends Functions implements ScriptFile, ICommunityBoardHandler
{

	@Override
	public void onLoad()
	{
		CommunityBoardManager.getInstance().registerHandler(this);
	}
	
	@Override
	public void onReload()
	{
		CommunityBoardManager.getInstance().removeHandler(this);
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_bbspartymatch"
		};
	}
	
	private static final int CHECKED_COUNT = 9;// last checked + 1
	private static final int MAX_PER_PAGE = 13;
	
	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		// Bypass: bbsmemo_class_sort_asc_charpage_char_classpage sometimes _invClassId
		
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String mainStringToken = st.nextToken();// bbsmemo
		if (mainStringToken.equals("bbspartymatch"))
		{
			if (!st.hasMoreTokens())
				showMainPage(player, 0, 0, 0, 0, 0);
			else
			{
				int classesSortType = Integer.parseInt(st.nextToken());
				int sortType = Integer.parseInt(st.nextToken());
				int asc = Integer.parseInt(st.nextToken());
				int page = Integer.parseInt(st.nextToken());
				int charObjId = Integer.parseInt(st.nextToken());
				showMainPage(player, classesSortType, sortType, asc, page, charObjId);
				
				if (st.hasMoreTokens())
				{
					int nextNumber = Integer.parseInt(st.nextToken());
					
					if (nextNumber == -1)// Show/Hide on list
					{
						player.setPartyMatchingVisible();
						
						if (player.isPartyMatchingVisible())
							player.sendMessage("You are now visible on Party Matching list!");
						else
							player.sendMessage("You are NO LONGER visible on Party Matching list!");
						
						showMainPage(player, classesSortType, sortType, asc, page, charObjId);
					}
					else
					// Invite to party
					{
						Player invited = GameObjectsStorage.getPlayer(charObjId);
						if (invited != null && player != invited && invited.getParty() == null)
						{
							String partyMsg = canJoinParty(invited);
							if (partyMsg.isEmpty())
							{
								ConfirmDlg packet = new ConfirmDlg(SystemMsg.S1, 60000).addString("Do you want to join " + player.getName() + " party?");
								invited.ask(packet, new InviteAnswer(invited, player));
								player.sendMessage("Invitation has been sent!");
							}
							else
								player.sendMessage(partyMsg);
						}
					}
				}
			}
		}
	}
	
	private void showMainPage(Player player, int classesSelected, int sortType, int asc, int page, int charObjId)
	{
		String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/partymatching.htm", player);
		html = html.replace("%characters%", getCharacters(player, sortType, asc, classesSelected, page, charObjId));
		html = html.replace("%visible%", player.isPartyMatchingVisible() ? "Hide from list" : "Show on list");
		html = replace(html, classesSelected, sortType, asc, page, charObjId);
		
		for (int i = 0; i < CHECKED_COUNT; i++)
			html = html.replace("%checked" + i + "%", getChecked(i, classesSelected));
		
		// html = BbsUtil.htmlAll(html, player);
		ShowBoard.separateAndSend(html, player);
	}
	
	private String replace(String text, int classesSelected, int sortType, int asc, int page, int charObjId)
	{
		text = text.replace("%class%", String.valueOf(classesSelected));
		text = text.replace("%sort%", String.valueOf(sortType));
		text = text.replace("%asc%", String.valueOf(asc));
		text = text.replace("%asc2%", String.valueOf(asc == 0 ? 1 : 0));
		text = text.replace("%page%", String.valueOf(page));
		text = text.replace("%char%", String.valueOf(charObjId));
		return text;
	}
	
	private String getCharacters(Player visitor, int charSort, int asc, int classesSelected, int page, int charToView)
	{
		StringBuilder builder = new StringBuilder();
		List<Player> allPlayers = getPlayerList(visitor, charSort, asc, classesSelected);
		int badCharacters = 0;
		boolean isThereNextPage = true;
		
		for (int i = MAX_PER_PAGE * page; i < (MAX_PER_PAGE + badCharacters + page * MAX_PER_PAGE); i++)
		{
			if (allPlayers.size() <= i)
			{
				isThereNextPage = false;
				break;
			}
			Player player = allPlayers.get(i);
			
			if (!isClassTestPassed(player, classesSelected))
			{
				badCharacters++;
				continue;
			}
			
			builder.append("<table bgcolor=").append(getLineColor(i)).append(" width=780 height=30 border=0 cellpadding=0 cellspacing=0><tr>");
			builder.append("<td width=185><center><font color=").append(getTextColor(i)).append(">").append(player.getName() + "</font></center></td>");
			builder.append("<td width=135><center><font color=").append(getTextColor(i)).append(">").append(Util.getFullClassName(player.getClassId().getId()) + "</font></center></td>");
			builder.append("<td width=80><center><font color=").append(getTextColor(i)).append(">").append(player.getLevel() + "</font></center></td>");
			builder.append("<td width=80><center><font color=").append(getTextColor(i)).append(">").append(player.getBaseClassId() == player.getActiveClassId() ? "Yes" : "No").append("</font></center></td>");
			builder.append("<td width=180><center><font color=").append(getTextColor(i)).append(">").append(player.getClan() != null ? player.getClan().getName() : "<br>").append("</font></center></td>");
			
			if (!player.equals(visitor) || player.getParty() != null)
				builder.append("<td width=120><center><button value=\"Invite\" action=\"bypass _bbspartymatch_%class%_%sort%_%asc%_%page%_").append(player.getObjectId()).append("_0\" width=70 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"><center></td>");
			else
				builder.append("<td width=120><br></td>");
			
			builder.append("</tr></table>");
		}
		builder.append("<center><table><tr>");
		if (page > 0)
			builder.append("<td><button value=\"Prev\" action=\"bypass _bbspartymatch_%class%_%sort%_%asc%_").append(page - 1).append("_%char%\" width=80 height=18 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		if (isThereNextPage)
			builder.append("<td><button value=\"Next\" action=\"bypass _bbspartymatch_%class%_%sort%_%asc%_").append(page + 1).append("_%char%\" width=80 height=18 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		builder.append("</tr></table></center>");
		
		return builder.toString();
	}
	
	private boolean containsClass(ClassId[] group, int clazz)
	{
		for (ClassId classInGroup : group)
			if (clazz == classInGroup.getId())
				return true;
		return false;
	}
	
	private boolean isClassTestPassed(Player player, int classesSelected)
	{
		for (ClassId clazz : getNeededClasses(classesSelected))
		{
			if (clazz.getId() == player.getClassId().getId() || clazz.getParent(0) != null && clazz.getParent(0).getId() == player.getClassId().getId())
				return true;
		}
		return false;
	}
	
	private List<Player> getPlayerList(Player player, int sortType, int asc, int classesSelected)
	{
		List<Player> allPlayers = new ArrayList<Player>();
		if (classesSelected == 8)// Party
		{
			if (player.getParty() == null)
				allPlayers.add(player);
			else
				for (Player member : player.getParty().getMembers())
					allPlayers.add(member);
			Collections.sort(allPlayers, new CharComparator(sortType, classesSelected, asc));
			return allPlayers;
		}
		
		for (Player singlePlayer : GameObjectsStorage.getAllPlayersForIterate())
			if (canJoinParty(singlePlayer).isEmpty())
			{
				if (!isClassTestPassed(singlePlayer, classesSelected))
					continue;
				allPlayers.add(singlePlayer);
			}
		Collections.sort(allPlayers, new CharComparator(sortType, classesSelected, asc));
		return allPlayers;
	}
	
	private class CharComparator implements Comparator<Player>
	{
		int _type;
		int _classType;
		int _asc;
		
		private CharComparator(int sortType, int classType, int asc)
		{
			_type = sortType;
			_classType = classType;
			_asc = asc;
		}
		
		@Override
		public int compare(Player o1, Player o2)
		{
			if (_asc == 1)
			{
				Player temp = o1;
				o1 = o2;
				o2 = temp;
			}
			if (_type == 0)// Name
				return o1.getName().compareTo(o2.getName());
			if (_type == 1)// lvl
				return ((Integer) getMaxLevel(o2, _classType)).compareTo(((Integer) getMaxLevel(o1, _classType)));
			if (_type == 2)// unlocks
				return ((Integer) getUnlocksSize(o2, _classType)).compareTo(((Integer) getUnlocksSize(o1, _classType)));
			return 0;
		}
		
	}
	
	private int getMaxLevel(Player player, int classSortType)
	{
		ClassId[] group = getNeededClasses(classSortType);
		int maxLevel = 0;
		for (SubClass sub : player.getSubClasses().values())
		{
			if (!containsClass(group, sub.getClassId()))
				continue;
			int level = Experience.getLevel(sub.getExp());
			if (level > maxLevel)
				maxLevel = level;
		}
		return maxLevel;
	}
	
	private int getUnlocksSize(Player player, int classSortType)
	{
		return player.getSubClasses().size();
	}
	
	private static String canJoinParty(Player player)
	{
		//TODO: punsh time for requests.
		if (player.isGM())
			return "Don't invite GMs...";
		
		if (player.getParty() != null)
			return "This character already found a party!";
		
		if (player.isInOfflineMode())
			return "This character is offline!";
		
		if (player.isInOlympiadMode())
			return "This character is currently fighting in the Olympiad!";
		
		if (player.isInObserverMode())
			return "This character is currently observing an Olympiad Match!";
		
		if (player.getCursedWeaponEquippedId() != 0)
			return "Players with Cursed Weapons cannot join party!";
		
		if (!player.isPartyMatchingVisible())
			return "Player doesnt want to join any party!";
		
		if (player.getPrivateStoreType() > 0)
			return "Players that have Private Store, cannot join partys!";
		
		return "";
	}
	
	private ClassId[] getNeededClasses(int type)
	{
		switch (type)
		{
			case 0:// All
				return ClassId.values();
			case 1:// Buffers
				ClassId[] classes =
				{
					ClassId.hierophant,
					ClassId.evaSaint,
					ClassId.shillienSaint,
					ClassId.dominator,
					ClassId.doomcryer
				};
				return classes;
			case 2:// BD
				return new ClassId[]
				{
					ClassId.spectralDancer
				};
			case 3:// SWS
				return new ClassId[]
				{
					ClassId.swordMuse
				};
			case 4:// Healers
				return new ClassId[]
				{
					ClassId.cardinal,
					ClassId.evaSaint,
					ClassId.shillienSaint
				};
			case 5:// Tanks
				return new ClassId[]
				{
					ClassId.phoenixKnight,
					ClassId.hellKnight,
					ClassId.evaTemplar,
					ClassId.shillienTemplar
				};
			case 6:// Mage DD
				return new ClassId[]
				{
					ClassId.archmage,
					ClassId.soultaker,
					ClassId.arcanaLord,
					ClassId.mysticMuse,
					ClassId.elementalMaster,
					ClassId.stormScreamer,
					ClassId.spectralMaster,
					ClassId.dominator,
					ClassId.doombringer
				};
			case 7:// Fighter DD
				return new ClassId[]
				{
					ClassId.dreadnought,
					ClassId.duelist,
					ClassId.adventurer,
					ClassId.sagittarius,
					ClassId.windRider,
					ClassId.moonlightSentinel,
					ClassId.ghostHunter,
					ClassId.ghostSentinel,
					ClassId.titan,
					ClassId.grandKhauatari,
					ClassId.fortuneSeeker,
					ClassId.maestro
				};
		}
		return ClassId.values();
	}
	
	private String getChecked(int i, int classSortType)
	{
		if (classSortType == i)
			return "L2UI.Checkbox_checked";
		else
			return "L2UI.CheckBox";
	}
	
	private String getLineColor(int i)
	{
		if (i % 2 == 0)
			return "18191e";
		else
			return "22181a";
	}
	
	private String getTextColor(int i)
	{
		if (i % 2 == 0)
			return "8f3d3f";
		else
			return "327b39";
	}
	
	public static class InviteAnswer implements OnAnswerListener
	{
		private Player _invited;
		private Player _inviter;
		
		public InviteAnswer(Player invited, Player inviter)
		{
			_invited = invited;
			_inviter = inviter;
		}
		
		@Override
		public void sayYes()
		{
			String inviteMsg = canJoinParty(_invited);
			if (!inviteMsg.isEmpty())
			{
				_inviter.sendMessage(inviteMsg);
				return;
			}
			
			// Joining Party
			Party party = _inviter.getParty();
			if (party == null)
			{
				_inviter.setParty(party = new Party(_inviter, 0));
			}
			
			_invited.joinParty(party);
			_invited.sendPacket(new JoinParty(2, _invited.getPet() != null));
			_inviter.sendPacket(new JoinParty(1, _invited.getPet() != null));
		}
		
		@Override
		public void sayNo()
		{
			_inviter.sendMessage(_invited.getName() + " cancelled party join request!");
		}
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
}
