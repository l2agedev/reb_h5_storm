package l2r.gameserver.nexus_engine.debug;

import l2r.gameserver.nexus_engine.events.engine.main.base.MainEventInstanceType;
import l2r.gameserver.nexus_engine.events.engine.team.EventTeam;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_engine.l2r.ClassType;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import javolution.util.FastTable;
import javolution.util.FastMap;

/** 
 * @author hNoke
 * official testing class for hNoke when he wanna have some fun
 */
public class Test
{
	@SuppressWarnings("unused")
	private static int next = 0;
	public static FastMap<MainEventInstanceType, FastTable<String>> _tempPlayers;
	
	public static List<EventTeam> _teams;
	public static int _vipsCount = 5;
	public static int _chooseFromTopPercent = 30;
	

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		//partyPlayers();
	}
	
	protected static void partyPlayers()
	{
////		List<PlayerEventInfo> toBePartied = new FastTable<PlayerEventInfo>();
////		toBePartied.add(new PlayerEventInfo(createPlayer(1), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(2), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(3), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(4), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(5), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(6), 1, ClassType.Fighter));
////		
////		toBePartied.add(new PlayerEventInfo(createPlayer(7), 1, ClassType.Mystic));
////		toBePartied.add(new PlayerEventInfo(createPlayer(8), 1, ClassType.Mystic));
////		toBePartied.add(new PlayerEventInfo(createPlayer(9), 1, ClassType.Mystic));
////		toBePartied.add(new PlayerEventInfo(createPlayer(10), 1, ClassType.Mystic));
////		toBePartied.add(new PlayerEventInfo(createPlayer(11), 1, ClassType.Mystic));
////		
////		toBePartied.add(new PlayerEventInfo(createPlayer(12), 1, ClassType.Priest));
////		toBePartied.add(new PlayerEventInfo(createPlayer(13), 1, ClassType.Priest));
////		toBePartied.add(new PlayerEventInfo(createPlayer(14), 1, ClassType.Priest));
////		toBePartied.add(new PlayerEventInfo(createPlayer(15), 1, ClassType.Priest));
////		
////		toBePartied.add(new PlayerEventInfo(createPlayer(1), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(2), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(3), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(4), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(5), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(6), 1, ClassType.Fighter));
////		
////		toBePartied.add(new PlayerEventInfo(createPlayer(7), 1, ClassType.Mystic));
////		toBePartied.add(new PlayerEventInfo(createPlayer(8), 1, ClassType.Mystic));
////		toBePartied.add(new PlayerEventInfo(createPlayer(9), 1, ClassType.Mystic));
////		toBePartied.add(new PlayerEventInfo(createPlayer(10), 1, ClassType.Mystic));
////		toBePartied.add(new PlayerEventInfo(createPlayer(11), 1, ClassType.Mystic));
////		
////		toBePartied.add(new PlayerEventInfo(createPlayer(12), 1, ClassType.Priest));
////		toBePartied.add(new PlayerEventInfo(createPlayer(13), 1, ClassType.Priest));
////		toBePartied.add(new PlayerEventInfo(createPlayer(14), 1, ClassType.Priest));
////		toBePartied.add(new PlayerEventInfo(createPlayer(15), 1, ClassType.Priest));
////		
////		toBePartied.add(new PlayerEventInfo(createPlayer(1), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(2), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(3), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(4), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(5), 1, ClassType.Fighter));
////		toBePartied.add(new PlayerEventInfo(createPlayer(6), 1, ClassType.Fighter));
////		
////		toBePartied.add(new PlayerEventInfo(createPlayer(7), 1, ClassType.Mystic));
////		toBePartied.add(new PlayerEventInfo(createPlayer(8), 1, ClassType.Mystic));
////		toBePartied.add(new PlayerEventInfo(createPlayer(9), 1, ClassType.Mystic));
////		toBePartied.add(new PlayerEventInfo(createPlayer(10), 1, ClassType.Mystic));
////		toBePartied.add(new PlayerEventInfo(createPlayer(11), 1, ClassType.Mystic));
////		
////		toBePartied.add(new PlayerEventInfo(createPlayer(12), 1, ClassType.Priest));
////		toBePartied.add(new PlayerEventInfo(createPlayer(13), 1, ClassType.Priest));
////		toBePartied.add(new PlayerEventInfo(createPlayer(14), 1, ClassType.Priest));
////		toBePartied.add(new PlayerEventInfo(createPlayer(15), 1, ClassType.Priest));
//		
//		final int partySize = 9;
//		
//		FastMap<ClassType, FastTable<PlayerEventInfo>> players;
//		FastTable<PlayerEventInfo> toParty;
//		
//		int totalCount;
//		int healersCount;
//		int partiesCount;
//		
//		totalCount = 0;
//		players = new FastMap<ClassType, FastTable<PlayerEventInfo>>();
//		
//		for(ClassType classType : ClassType.values())
//			players.put(classType, new FastTable<PlayerEventInfo>());
//		
//		for(PlayerEventInfo player : toBePartied)
//		{
//			players.get(player.getClassType()).add(player);
//			totalCount ++;
//		}
//		
//		// sort players by level before dividing to parties
//		for(List<PlayerEventInfo> pls : players.values())
//			Collections.sort(pls, compareByLevels);
//		
//		healersCount = players.get(ClassType.Priest).size();
//		
//		partiesCount = (int) Math.ceil((double) totalCount / (double) partySize);
//		
//		toParty = new FastTable<PlayerEventInfo>();
//		PlayerEventInfo pi;
//		
//		int healersToGive = (int)Math.ceil((double)healersCount / (double)partiesCount);
//		if(healersToGive == 0)
//			healersToGive = 1;
//		
//		for(int i = 0; i < partiesCount; i++)
//		{
//			if(healersCount > 0)
//			{
//				for(int h = 0; h < healersToGive && healersCount >= healersToGive; h ++)
//				{
//					for(PlayerEventInfo p : players.get(ClassType.Priest))
//					{
//						System.out.println(p.getPlayersName() + " - healer");
//					}
//					
//					
//					pi = players.get(ClassType.Priest).head().getNext().getValue();
//					System.out.println("chosen: " + pi.getPlayersName());
//					
//					if(pi == null)
//					{
//						NexusLoader.debug("SHIT HAPPENED, hnoke knows and needs to fix it.");
//						pi = players.get(ClassType.Priest).head().getNext().getValue();
//					}
//					
//					toParty.add(pi);
//					players.get(ClassType.Priest).remove(pi);
//					
//					healersCount --;
//				}
//			}
//			
//			boolean b = false;
//			boolean added;
//			while(toParty.size() < partySize)
//			{
//				added = false;
//				for(PlayerEventInfo fighter : players.get((b ? ClassType.Mystic : ClassType.Fighter)))
//				{
//					toParty.add(fighter);
//					players.get((b ? ClassType.Mystic : ClassType.Fighter)).remove(fighter);
//					added = true;
//					break;
//				}
//				
//				b = !b;
//				
//				if(!added)
//				{
//					for(PlayerEventInfo mystic : players.get((b ? ClassType.Mystic : ClassType.Fighter)))
//					{
//						toParty.add(mystic);
//						players.get((b ? ClassType.Mystic : ClassType.Fighter)).remove(mystic);
//						added = true;
//						break;
//					}
//				}
//				
//				if(!added)// no other fighters / mystics available
//				{
//					if(healersCount > 0) // fill the rest of the party with healers
//					{
//						for(PlayerEventInfo healer : players.get(ClassType.Priest))
//						{
//							toParty.add(healer);
//							players.get(ClassType.Priest).remove(healer);
//							added = true;
//							healersCount --;
//							break;
//						}
//					}
//					else // or leave this party unfinished
//						break;
//				}
//				else continue;
//			}
//			
//			partyPlayers(toParty);
//			toParty.clear();
//		}
	}
	
	protected static void partyPlayers(List<PlayerEventInfo> party)
	{
		System.out.println("... partying ");
		
		for(PlayerEventInfo player : party)
		{
			System.out.println(player.getPlayersName() + ", type = " + player.getClassType().toString());
		}
	}
	
	protected static void selectVips(int instanceId, int count)
	{
//		List<PlayerEventInfo> newVips = new FastTable<PlayerEventInfo>();
//		List<PlayerEventInfo> temp = new FastTable<PlayerEventInfo>();
//		for(EventTeam team : _teams)
//		{
//			if(count == -1)
//				count = _vipsCount;
//			
//			if(count > team.getPlayers().size())
//				count = team.getPlayers().size();
//			
//			for(PlayerEventInfo player : team.getPlayers())
//			{
//				temp.add(player);
//			}
//			
//			String s = "Level";
//			
//			Collections.sort(temp, compareByLevels);
//			if(s.startsWith("PvPs"))
//				Collections.sort(temp, compareByPvps);
//			
//			int from = 0;
//			int to = (int) Math.ceil(temp.size() * ((double)_chooseFromTopPercent / (double)100));
//			System.out.println("choosing VIPs in top players interval: " + from + " - " + to);
//			
//			PlayerEventInfo newVip;
//			
//			int i = 0;
//			while(count > 0 && i < temp.size())
//			{
//				System.out.println("count " + count + " I = " + i);
//				System.out.println("sublist size = " + temp.subList(from, to).size());
//				if(i >= temp.subList(from, to).size())
//				{
//					to ++;
//					System.out.println("******** raising TO - " + to);
//				}
//				
//				newVip = temp.subList(from, to).get(Rnd.get(temp.subList(from, to).size()));
//				System.out.println("///// received - " + newVip.getPlayersName());
//				if(Rnd.get(3) == 0)
//				{
//					temp.remove(newVip); //ADDED
//					newVips.add(newVip);
//					count --; //ADDED
//				}
//				
//				i++;
//			}
//			
//			temp.clear();
//		}
//		
//		for(PlayerEventInfo player : newVips)
//		{
//			System.out.println("Player " + player.getPlayersName() + " with level " + player.getLevel() + " marked as VIP and teleported!");
//		}
	}
	
	/** compares PlayerEventInfo collection by their levels - descending */
	public static Comparator<PlayerEventInfo> compareByLevels = new Comparator<PlayerEventInfo>()
	{
		@Override
		public int compare(PlayerEventInfo o1, PlayerEventInfo o2)
		{
			int level1 = o1.getLevel();
			int level2 = o2.getLevel();
			
			return level1 == level2 ? 0 : level1 < level2 ? 1 : -1;
		}
	};
	
	/** compares PlayerEventInfo collection by their pvp kills - descending */
	public static Comparator<PlayerEventInfo> compareByPvps = new Comparator<PlayerEventInfo>()
	{
		@Override
		public int compare(PlayerEventInfo o1, PlayerEventInfo o2)
		{
			int pvp1 = o1.getPvpKills();
			int pvp2 = o2.getPvpKills();
			
			return pvp1 == pvp2 ? 0 : pvp1 < pvp2 ? 1 : -1;
		}
	};
	
	protected static void dividePlayersToTeams(int instanceId, FastTable<PlayerEventInfo> players, int teamsCount)
	{
		//"LevelOnly", "PvPsAndLevel"
		
		int team1 = 0;
		int team2 = 0;
		int team3 = 0;
		int team4 = 0;
		
		int healers1 = 0;
		int healers2 = 0;
		int healers3 = 0;
		int healers4 = 0;
		
		int mages1 = 0;
		int mages2 = 0;
		int mages3 = 0;
		int mages4 = 0;
		
		int fighters1 = 0;
		int fighters2 = 0;
		int fighters3 = 0;
		int fighters4 = 0;
		
		int divided = 0;
		int teamId;
		PlayerEventInfo player = null;
		
		String type = "LevelOnly";
		
		// sort players
		Collections.sort(players, compareByLevels);
		if(type.startsWith("PvPs"))
			Collections.sort(players, compareByPvps);
		
		// divide players into a map by their ClassType
		FastMap<ClassType, FastTable<PlayerEventInfo>> sortedPlayers = new FastMap<ClassType, FastTable<PlayerEventInfo>>();
		
		for(ClassType classType : ClassType.values())
			sortedPlayers.put(classType, new FastTable<PlayerEventInfo>());
		
		for(PlayerEventInfo pi : players)
			sortedPlayers.get(pi.getClassType()).add(pi);
		
		for(Entry<ClassType, FastTable<PlayerEventInfo>> e : sortedPlayers.entrySet())
		{
			System.out.println(e.getKey().toString() + " has " + e.getValue().size() + " players");
		}
		
		// to teams: first divide healers to teams
		if(true)
		{
			int healersCount = sortedPlayers.get(ClassType.Priest).size();
			
			teamId = 0;
			int index = 0;
			while(healersCount > 0)
			{
				teamId ++;
				divided++;
				
				switch(teamId)
				{
					case 1: team1 ++; healers1++; break;
					case 2: team2 ++; healers2++; break;
					case 3: team3 ++; healers3++; break;
					case 4: team4 ++; healers4++; break;
				}
				
				healersCount --;
				
				player = sortedPlayers.get(ClassType.Priest).get(index++);
				
				//player.onEventStart(this);
				//_teams.get(instanceId).get(teamId).addPlayer(player, true);
				
				System.out.println(player.getClassType().toString() + " " + player.getPlayersName() + " -  goes to team " + teamId);
				
				sortedPlayers.get(ClassType.Priest).remove(player);
				
				if(teamId >= teamsCount)
					teamId = 0;
			}
		}
		
		// add the rest of players
		teamId = 0;
		for(Entry<ClassType, FastTable<PlayerEventInfo>> e : sortedPlayers.entrySet())
		{
			for(PlayerEventInfo pi : e.getValue())
			{
				if(team1 < team2)
					teamId = 1;
				else if(team2 < team1)
					teamId = 2;
				else
					teamId = CallBack.getInstance().getOut().random(1,2);

				
				divided++;
				
				switch(teamId)
				{
					case 1: team1 ++; if(pi.getClassType() == ClassType.Fighter) fighters1++; else mages1++; break;
					case 2: team2 ++; if(pi.getClassType() == ClassType.Fighter) fighters2++; else mages2++; break;
					case 3: team3 ++; if(pi.getClassType() == ClassType.Fighter) fighters3++; else mages3++; break;
					case 4: team4 ++; if(pi.getClassType() == ClassType.Fighter) fighters4++; else mages4++; break;
				}
				
				System.out.println(pi.getClassType().toString() + " " + pi.getPlayersName() + " -  goes to team " + teamId);
				
				//pi.onEventStart(this);
				//_teams.get(instanceId).get(teamId).addPlayer(pi, true);
				
				if(teamId == teamsCount)
					teamId = 0;
			}
		}
		
		System.out.println("divided: " + divided);
		
		System.out.println("team1: " + team1 + " ::: " + healers1 + " healers, " + mages1 + " mages, " + fighters1 + " fighters.");
		System.out.println("team2: " + team2 + " ::: " + healers2 + " healers, " + mages2 + " mages, " + fighters2 + " fighters.");
		System.out.println("team3: " + team3 + " ::: " + healers3 + " healers, " + mages3 + " mages, " + fighters3 + " fighters.");
		System.out.println("team4: " + team4 + " ::: " + healers4 + " healers, " + mages4 + " mages, " + fighters4 + " fighters.");
	}
	
	/*protected static void createParties(int partySize)
	{
		//TODO: adjust and test for different values !!
		// and check it copied to core properly
		
		EventTeam team = new EventTeam(1);
		for(int i = 0; i < 40; i++)
			team.addPlayer(getPlayer(ClassType.Fighter), false);
		
		for(int i = 0; i < 40; i++)
			team.addPlayer(getPlayer(ClassType.Mystic), false);
		
		for(int i = 0; i < 19; i++)
			team.addPlayer(getPlayer(ClassType.Priest), false);
		
		FastMap<ClassType, FastTable<PlayerEventInfo>> players;
		FastTable<PlayerEventInfo> toParty;
		
		int totalCount;
		int healersCount;
		int partiesCount;
		
		totalCount = 0;
		players = new FastMap<ClassType, FastTable<PlayerEventInfo>>();
		for(PlayerEventInfo player : team.getPlayers())
		{
			if(!players.containsKey(player.getClassType()))
				players.put(player.getClassType(), new FastTable<PlayerEventInfo>());
			
			players.get(player.getClassType()).add(player);
			totalCount ++;
		}
		
		System.out.println("TotalCount: " + totalCount);
		
		// sort players by level before dividing to parties
		for(List<PlayerEventInfo> pls : players.values())
			Collections.sort(pls, new Comparator<PlayerEventInfo>()
			{
				@Override
				public int compare(PlayerEventInfo o1, PlayerEventInfo o2)
				{
					int level1 = o1.getLevel();
					int level2 = o2.getLevel();
					
					return level1 == level2 ? 0 : level1 < level2 ? 1 : -1;
				}
			});
		
		healersCount = players.get(ClassType.Priest).size();
		System.out.println("HealersCount: " + healersCount);
		
		partiesCount = (int) Math.ceil((double) totalCount / (double) partySize);
		System.out.println("PartiesCount: " + partiesCount);
		
		toParty = new FastTable<PlayerEventInfo>();
		PlayerEventInfo pi;
		
		int healersToGive = (int)Math.ceil((double)healersCount / (double)partiesCount);
		if(healersToGive == 0)
			healersToGive = 1;
		
		for(int i = 0; i < partiesCount; i++)
		{
			int healers = 0;
			int fighters = 0;
			int mages = 0;
			
			if(healersCount > 0)
			{
				System.out.println("HealersToGive " + healersToGive + " (" + (double)healersCount + " / " + (double)partiesCount + ")");
				
				for(int h = 0; h < healersToGive && healersCount >= healersToGive; h ++)
				{
					pi = players.get(ClassType.Priest).head().getValue();
					
					toParty.add(pi);
					players.get(ClassType.Priest).remove(pi);
					healers ++;
					
					healersCount --;
				}
			}
			
			boolean b = false;
			boolean added;
			
			// test
			
			while(toParty.size() < partySize)
			{
				added = false;
				for(PlayerEventInfo fighter : players.get((b ? ClassType.Mystic : ClassType.Fighter)))
				{
					toParty.add(fighter);
					players.get((b ? ClassType.Mystic : ClassType.Fighter)).remove(fighter);
					
					if(b)
						mages ++;
					else
						fighters ++;
					
					added = true;
					break;
				}
				
				b = !b;
				
				if(!added)
				{
					for(PlayerEventInfo mystic : players.get((b ? ClassType.Mystic : ClassType.Fighter)))
					{
						toParty.add(mystic);
						players.get((b ? ClassType.Mystic : ClassType.Fighter)).remove(mystic);
						
						if(b)
							mages ++;
						else
							fighters ++;
						
						added = true;
						break;
					}
				}
				
				if(!added)// no other fighters / mystics available
				{
					if(healersCount > 0) // fill the rest of the party with healers
					{
						for(PlayerEventInfo healer : players.get(ClassType.Priest))
						{
							toParty.add(healer);
							players.get(ClassType.Priest).remove(healer);
							healers ++;
							added = true;
							healersCount --;
							break;
						}
					}
					else // or leave this party unfinished
						break;
				}
				else continue;
			}
			
			System.out.println("================= PARTY " + (i + 1));
			System.out.println("Players: " + toParty.size());
			System.out.println("* Healers: " + healers);
			System.out.println("* Fighters: " + fighters);
			System.out.println("* Mages: " + mages);
			
			toParty.clear();
		}
	}*/
	
	@SuppressWarnings("deprecation")
	protected static void reorganizeInstances()
	{
		List<MainEventInstanceType> sameStrenghtInstances = new FastTable<MainEventInstanceType>();
		
		// strenght interval = 1-10
		for(int currentStrenght = 1; currentStrenght <= 10; currentStrenght ++)
		{
			for(Entry<MainEventInstanceType, FastTable<String>> e : _tempPlayers.entrySet())
			{
				if(isFull(e.getKey()))
					continue;
				
				if(e.getKey().getTempRate() == currentStrenght)
				{
					sameStrenghtInstances.add(e.getKey());
				}
			}
			
			Collections.sort(sameStrenghtInstances, new Comparator<MainEventInstanceType>()
			{
				@Override
				public int compare(MainEventInstanceType i1, MainEventInstanceType i2)
				{
					int neededPlayers1 = i1.getMinPlayers() - _tempPlayers.get(i1).size();
					int neededPlayers2 = i2.getMinPlayers() - _tempPlayers.get(i2).size();
					
					return neededPlayers1 == neededPlayers2 ? 0 : neededPlayers1 < neededPlayers2 ? -1 : 1;
				}
			});
			
			reorganize(sameStrenghtInstances);
			sameStrenghtInstances.clear();
		}
	}
	
	protected static void init()
	{
		FastTable<String> players;
		int id = 0;
		
		// 1 
		id ++;
		players = new FastTable<String>();
		for(int i = 0; i < 4; i++)
			players.add(createPlayer(id));
		_tempPlayers.put(new MainEventInstanceType(id, null, "INSTANCE " + id, "Instance " + id, "", 10, 15, 1), players);
		
		/*id ++;
		players = new FastTable<String>();
		for(int i = 0; i < 9; i++)
			players.add(createPlayer(id));
		_tempPlayers.put(new MainEventInstanceType(id, null, "INSTANCE " + id, "Instance " + id, "", 10, 15, 1), players);
		
		id ++;
		players = new FastTable<String>();
		for(int i = 0; i < 2; i++)
			players.add(createPlayer(id));
		_tempPlayers.put(new MainEventInstanceType(id, null, "INSTANCE " + id, "Instance " + id, "", 10, 15, 1), players);
		
		id ++;
		players = new FastTable<String>();
		for(int i = 0; i < 1; i++)
			players.add(createPlayer(id));
		_tempPlayers.put(new MainEventInstanceType(id, null, "INSTANCE " + id, "Instance " + id, "", 10, 15, 1), players);
		
		id ++;
		players = new FastTable<String>();
		for(int i = 0; i < 7; i++)
			players.add(createPlayer(id));
		_tempPlayers.put(new MainEventInstanceType(id, null, "INSTANCE " + id, "Instance " + id, "", 10, 15, 1), players);*/
		
		// 2 
		
		id ++;
		players = new FastTable<String>();
		for(int i = 0; i < 6; i++)
			players.add(createPlayer(id));
		_tempPlayers.put(new MainEventInstanceType(id, null, "INSTANCE " + id, "Instance " + id, "", 10, 16, 2), players);
		
		/*id ++;
		players = new FastTable<String>();
		for(int i = 0; i < 15; i++)
			players.add(createPlayer(id));
		_tempPlayers.put(new MainEventInstanceType(id, null, "INSTANCE " + id, "Instance " + id, "", 10, 15, 2), players);
		
		id ++;
		players = new FastTable<String>();
		for(int i = 0; i < 12; i++)
			players.add(createPlayer(id));
		_tempPlayers.put(new MainEventInstanceType(id, null, "INSTANCE " + id, "Instance " + id, "", 10, 13, 2), players);
		
		id ++;
		players = new FastTable<String>();
		for(int i = 0; i < 10; i++)
			players.add(createPlayer(id));
		_tempPlayers.put(new MainEventInstanceType(id, null, "INSTANCE " + id, "Instance " + id, "", 10, 13, 2), players);
		
		id ++;
		players = new FastTable<String>();
		for(int i = 0; i < 10; i++)
			players.add(createPlayer(id));
		_tempPlayers.put(new MainEventInstanceType(id, null, "INSTANCE " + id, "Instance " + id, "", 10, 13, 2), players);*/
	}
	
	protected static String createPlayer(int id)
	{
		return ("Player " + (id + 1));
	}
	
	@SuppressWarnings({
		"deprecation",
		"unused"
	})
	protected static void reorganize(List<MainEventInstanceType> instances)
	{
		boolean full;
		int count , sum, toMove, moved;
		
		for(MainEventInstanceType instance : instances)
		{
			System.out.println("======================");
			System.out.println("*** " + instance.getName() + " processing");
			
			if(hasEnoughtPlayers(instance)) // instance already full
			{
				System.out.println("" + instance.getName() + " already full");
				instances.remove(instance);
				continue;
			}
			else
			{
				count = _tempPlayers.get(instance).size();
				toMove = instance.getMinPlayers() - count;
				
				System.out.println("" + instance.getName() + " not full - players (" + count + "), toMove (" + toMove + ")");
				
				for(MainEventInstanceType possibleInstance : instances)
				{
					if(possibleInstance == instance)
						continue;
					
					moved = movePlayers(instance, possibleInstance, toMove);
					toMove -= moved;
					
					System.out.println("- from '" + possibleInstance.getName() + "' moved " + moved + ", still need to move " + toMove);
					
					if(toMove == 0)
					{
						System.out.println("- '" + instance.getName() + "' ready to run!");
						instances.remove(instance);
						break;
					}
					else if(toMove > 0) 
						continue;
				}
			}
		}
		
		if(!instances.isEmpty())
		{
			int minPlayers = Integer.MAX_VALUE;
			MainEventInstanceType inst = null;
			for(MainEventInstanceType instance : instances)
			{
				if(instance.getMinPlayers() < minPlayers)
				{
					minPlayers = instance.getMinPlayers();
					inst = instance;
				}
			}
			
			System.out.println("*** - moving all players to instance " + inst.getName());
			
			for(MainEventInstanceType instance : instances)
			{
				if(instance != inst)
				{
					movePlayers(inst, instance, -1);
				}
			}
			
			System.out.println("*** Done, instance " + inst.getName() + " has " + _tempPlayers.get(inst).size() + " players.");
		}
	}
	
	/** returns the count of players moved */
	protected static int movePlayers(MainEventInstanceType target, MainEventInstanceType source, int count)
	{
		if(count == 0) return 0;
		
		int moved = 0;
		for(String player : _tempPlayers.get(source))
		{
			// move player
			_tempPlayers.get(target).add(player);
			_tempPlayers.get(source).remove(player);
			
			moved ++;
			
			if(count != -1 && moved >= count)
				break;
		}
		return moved;
	}
	
	@SuppressWarnings("deprecation")
	protected static boolean hasEnoughtPlayers(MainEventInstanceType instance)
	{
		return _tempPlayers.get(instance).size() >= instance.getMinPlayers();
	}
	
	@SuppressWarnings("deprecation")
	protected static boolean isFull(MainEventInstanceType instance)
	{
		return _tempPlayers.get(instance).size() >= instance.getMaxPlayers();
	}
	
	@SuppressWarnings("deprecation")
	protected static void dividePlayers()
	{
		System.out.println("********* Reorganize Players ************");
		
		reorganizeInstances();
		
		System.out.println("********* Divide Players ************");
		
		List<MainEventInstanceType> notEnoughtPlayersInstances = new FastTable<MainEventInstanceType>();
		
		for(Entry<MainEventInstanceType, FastTable<String>> e : _tempPlayers.entrySet())
		{
			// not enought players for this instance
			if(e.getValue().size() < e.getKey().getMinPlayers())
			{
				System.out.println("/ adding instance " + e.getKey().getName() + " [" + e.getKey().getTempRate() + "] to notEnoughtPlayersInstance");
				notEnoughtPlayersInstances.add(e.getKey());
				continue;
			}
			else System.out.println("/ instance " + e.getKey().getName() + " [" + e.getKey().getTempRate() + "] has enought players");
		}
		
		boolean joinStrongerInstIfNeeded;
		int maxDiff;
		int strenght;
		int playersCount;
		
		// temp
		int sumPlayers;
		int max;
		int toMove;
		
		List<MainEventInstanceType> fixed = new FastTable<MainEventInstanceType>();
		
		for(MainEventInstanceType currentInstance : notEnoughtPlayersInstances)
		{
			if(currentInstance == null || fixed.contains(currentInstance))
				continue;
			
			System.out.println("================== " + currentInstance.getName());
			
			strenght = currentInstance.getTempRate();
			playersCount = _tempPlayers.get(currentInstance).size();
			
			joinStrongerInstIfNeeded = true;
			maxDiff = 2;
			
			System.out.println("*** current instance " + currentInstance.getName() + "[" + currentInstance.getTempRate() + "] - playersCount (" + playersCount + "), strenght (" + strenght + ")");
			
			for(MainEventInstanceType possibleInstance : notEnoughtPlayersInstances)
			{
				if(possibleInstance == null || fixed.contains(possibleInstance) || possibleInstance == currentInstance)
					continue;
				
				playersCount = _tempPlayers.get(currentInstance).size();
				
				if(possibleInstance.getTempRate() == strenght)
				{
					if(_tempPlayers.get(possibleInstance).size() + playersCount >= possibleInstance.getMinPlayers())
						System.out.println("How could have this happened? (" + currentInstance.getName() + ", " + possibleInstance.getName() + ")");
					
					continue;
				}
				
				if(joinStrongerInstIfNeeded && possibleInstance.getTempRate() > strenght)
				{
					if(possibleInstance.getTempRate() - strenght <= maxDiff)
					{
						System.out.println("/// possible instance " + possibleInstance.getName() + "[" + possibleInstance.getTempRate() + "] - playersCount (" + _tempPlayers.get(possibleInstance).size() + "), strenght (" + possibleInstance.getTempRate() + ")");
						
						sumPlayers = _tempPlayers.get(possibleInstance).size() + playersCount;
						
						System.out.println("sum = " + sumPlayers);
						
						// moving players from currentInstance to possibleInstance
						if(sumPlayers >= possibleInstance.getMinPlayers())
						{
							max = possibleInstance.getMaxPlayers(); // TODO
							if(sumPlayers > max)
								toMove = max - _tempPlayers.get(possibleInstance).size();
							else
								toMove = _tempPlayers.get(currentInstance).size();
							
							System.out.println("moving " + toMove + " players from " + currentInstance.getName() + " to " + possibleInstance.getName());
							
							movePlayers(possibleInstance, currentInstance, toMove);
							
							System.out.println("size of " + possibleInstance.getName()  + " is now " + _tempPlayers.get(possibleInstance).size());
							
							if(_tempPlayers.get(possibleInstance).size() >= possibleInstance.getMinPlayers()) // TODO
							{
								System.out.println(possibleInstance.getName() + " removed from notEnoughtPlayersInstances.");
								fixed.add(possibleInstance);
							}
						}
						else // both instances still have together not enought players
						{
							continue;
						}
					}
				}
			}
		}
		
		for(MainEventInstanceType currentInstance : notEnoughtPlayersInstances)
		{
			playersCount = _tempPlayers.get(currentInstance).size();
			if(playersCount == 0) 
				continue;
			
			strenght = currentInstance.getTempRate(); // TODO
			
			joinStrongerInstIfNeeded = true; // TODO
			maxDiff = 2; // TODO
			
			for(MainEventInstanceType fixedInstance : fixed)
			{
				if(joinStrongerInstIfNeeded && fixedInstance.getTempRate() > strenght) // TODO
				{
					if(fixedInstance.getTempRate() - strenght <= maxDiff) // TODO
					{
						sumPlayers = _tempPlayers.get(fixedInstance).size();
						if(sumPlayers < fixedInstance.getMaxPlayers()) // TODO
						{
							toMove = fixedInstance.getMaxPlayers() - _tempPlayers.get(fixedInstance).size(); // TODO
							movePlayers(fixedInstance, currentInstance, toMove);
							continue;
						}
					}
				}
			}
		}
		
		for(MainEventInstanceType toRemove : fixed)
			notEnoughtPlayersInstances.remove(toRemove);
		
		for(Entry<MainEventInstanceType, FastTable<String>> e : _tempPlayers.entrySet())
		{
			playersCount = e.getValue().size();
			if(playersCount == 0) 
				continue;
			
			strenght = e.getKey().getTempRate(); // TODO
			
			joinStrongerInstIfNeeded = true; // TODO
			maxDiff = 2; // TODO
			
			if(hasEnoughtPlayers(e.getKey()))
				continue;
			
			System.out.println(e.getKey().getName() + " has BEFORE SAME LEVEL DIVIDE " + playersCount + " players");
			
			for(Entry<MainEventInstanceType, FastTable<String>> inst : _tempPlayers.entrySet())
			{
				System.out.println("////// Instance " + inst.getKey().getName() + "[" + inst.getKey().getTempRate() + "] has " + inst.getValue().size());
			}
			
			System.out.println(" DIVIDED //////////");
			
			// try to divide again into all same-strenght instances
			int temp;
			while(playersCount > 0)
			{
				temp = playersCount;
				
				for(Entry<MainEventInstanceType, FastTable<String>> i : _tempPlayers.entrySet())
				{
					if(playersCount <= 0) 
						break;
					
					if(!hasEnoughtPlayers(i.getKey()))
						continue;
					
					if(i.getKey().getTempRate() == strenght)
					{
						int canMove = i.getKey().getMaxPlayers() - i.getValue().size();
						if(canMove <= 0)
							continue;
						
						if(movePlayers(i.getKey(), e.getKey(), 1) == 1)
						{
							playersCount --;
							continue;
						}
					}
				}
				
				// all possible same-strenght instances are full already
				if(playersCount == temp)
					break;
			}
			
			for(Entry<MainEventInstanceType, FastTable<String>> inst : _tempPlayers.entrySet())
			{
				System.out.println("////// Instance " + inst.getKey().getName() + "[" + inst.getKey().getTempRate() + "] has " + inst.getValue().size());
			}
			
			System.out.println(e.getKey().getName() + " has AFTER SAME LEVEL DIVIDE " + playersCount + " players");
			
			if(playersCount <= 0 || !joinStrongerInstIfNeeded)
				continue;
			
			while(playersCount > 0)
			{
				temp = playersCount;
				
				for(Entry<MainEventInstanceType, FastTable<String>> i : _tempPlayers.entrySet())
				{
					if(playersCount <= 0) 
						break;
					
					if(!hasEnoughtPlayers(i.getKey()))
						continue;
					
					if(i.getKey().getTempRate() > strenght)
					{
						if(i.getKey().getTempRate() - strenght <= maxDiff)
						{
							int canMove = i.getKey().getMaxPlayers() - i.getValue().size();
							if(canMove <= 0)
								continue;
							
							if(movePlayers(i.getKey(), e.getKey(), 1) == 1)
							{
								playersCount --;
								continue;
							}
						}
					}
				}
				
				if(playersCount == temp)
					break;
			}
			
			System.out.println(e.getKey().getName() + " has IN THE END " + playersCount + " players");
			
			continue;
		}
		
		
		for(MainEventInstanceType inst : notEnoughtPlayersInstances)
		{
			System.out.println("Not enought players for instance " + inst.getName() + " (" + _tempPlayers.get(inst).size() + "), instance removed; " + _tempPlayers.get(inst).size() + " players unregistered");
			_tempPlayers.remove(inst);
		}
		
		for(Entry<MainEventInstanceType, FastTable<String>> inst : _tempPlayers.entrySet())
		{
			System.out.println("Instance " + inst.getKey().getName() + "[" + inst.getKey().getTempRate() + "] has " + inst.getValue().size());
		}
	}
}
