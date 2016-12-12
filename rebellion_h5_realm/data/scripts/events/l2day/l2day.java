package events.l2day;

import l2r.gameserver.model.reward.RewardItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class l2day extends LettersCollection
{
	// Misc
	private static int GIANT_CODEX = 6622;
	private static int GIANT_CODEX_MASTERY = 9627;
	private static int BSOE = 3958;
	private static int BSOR = 3959;
	
	// Crystals
	private static int FIRE_CRYSTAL = 9552;
	private static int DARK_CRYSTAL = 9556;
	private static int WIND_CRYSTAL = 9555;
	private static int WATER_CRYSTAL = 9553;
	private static int HOLY_CRYSTAL = 9557;
	private static int EARTH_CRYSTAL = 9554;
	
	
	// Boss jelews
	private static int RING_OF_ANT_QUIEEN_3DAYS = 20204;
	private static int RING_OF_ANT_QUIEEN_7DAYS = 20205;
	private static int RING_OF_ANT_QUIEEN_15DAYS = 20206;
	private static int ZAKEN_EARRING_3DAYS = 20207;
	private static int ZAKEN_EARRING_7DAYS = 20208;
	private static int ZAKEN_EARRING_15DAYS = 20209;
	private static int BAIUM_RING_3DAYS = 22304;
	private static int BAIUM_RING_7DAYS = 22305;
	private static int ANTHARAS_EARRING_3DAYS = 22302;
	private static int ANTHARAS_EARRING_7DAYS = 22303;
	private static int EARRING_OF_ORFEN = 6661;
	private static int RING_OF_CORE = 6662;
	private static int FRINTEZZA_NECKLACE = 8191;

	static
	{
		_name = "l2day";
		_msgStarted = "scripts.events.l2day.AnnounceEventStarted";
		_msgEnded = "scripts.events.l2day.AnnounceEventStoped";

		EVENT_MANAGERS = new int[][] {
				{ 19541, 145419, -3103, 30419 },
				{ 147485, -59049, -2980, 9138 },
				{ 109947, 218176, -3543, 63079 },
				{ -81363, 151611, -3121, 42910 },
				{ 144741, 28846, -2453, 2059 },
				{ 44192, -48481, -796, 23331 },
				{ -13889, 122999, -3109, 40099 },
				{ 116278, 75498, -2713, 12022 },
				{ 82029, 55936, -1519, 58708 },
				{ 147142, 28555, -2261, 59402 },
				{ 82153, 148390, -3466, 57344 }, };

		_words.put("LineageII", new Integer[][] { { L, 1 }, { I, 1 }, { N, 1 }, { E, 1 }, { A, 1 }, { G, 1 }, { II, 1 } });
		_rewards.put("LineageII", new RewardItem[]
		{
			// Misc
			new RewardItem(BSOE, 1, 3, 95000),
			new RewardItem(BSOR, 1, 3, 90000),
			
			// Attribute stones
			new RewardItem(9546, 1, 6, 88000),
			new RewardItem(9547, 1, 6, 85000),
			new RewardItem(9548, 1, 6, 82000),
			new RewardItem(9549, 1, 6, 79000),
			new RewardItem(9550, 1, 6, 76000),
			new RewardItem(9551, 1, 6, 73000),
			
			// Attribute Crystals
			new RewardItem(FIRE_CRYSTAL, 1, 2, 70000),
			new RewardItem(DARK_CRYSTAL, 1, 2, 67000),
			new RewardItem(WIND_CRYSTAL, 1, 2, 65000),
			new RewardItem(WATER_CRYSTAL, 1, 2, 62000),
			new RewardItem(HOLY_CRYSTAL, 1, 2, 59000),
			new RewardItem(EARTH_CRYSTAL, 1, 2, 56000),
			
			new RewardItem(EARRING_OF_ORFEN, 1, 1, 15000),
			new RewardItem(RING_OF_CORE, 1, 1, 18000),
			
			new RewardItem(GIANT_CODEX, 1, 10, 50000),
			new RewardItem(GIANT_CODEX_MASTERY, 1, 3, 40000)
		});

		_words.put("ICan", new Integer[][] { { I, 1 }, { C, 1 }, { A, 1 }, { N, 1 } });
		_rewards.put("ICan", new RewardItem[]
		{
			// Attribute stones
			new RewardItem(9546, 1, 3, 90000),
			new RewardItem(9547, 1, 3, 90000),
			new RewardItem(9548, 1, 3, 90000),
			new RewardItem(9549, 1, 3, 90000),
			new RewardItem(9550, 1, 3, 90000),
			new RewardItem(9551, 1, 3, 90000),
			
			// Boss jewels
			new RewardItem(RING_OF_ANT_QUIEEN_3DAYS, 1, 1, 10000),
			new RewardItem(RING_OF_ANT_QUIEEN_7DAYS, 1, 1, 4000),
			
			new RewardItem(ZAKEN_EARRING_3DAYS, 1, 1, 18000),
			new RewardItem(ZAKEN_EARRING_7DAYS, 1, 1, 5000),
			
			new RewardItem(BAIUM_RING_3DAYS, 1, 1, 1000),
			
			new RewardItem(EARRING_OF_ORFEN, 1, 1, 18000),
			new RewardItem(RING_OF_CORE, 1, 1, 15000),
			
			new RewardItem(FRINTEZZA_NECKLACE, 1, 1, 500)
		});

		_words.put("Rich", new Integer[][] { { R, 1 }, { I, 1 }, { C, 1 }, { H, 1 } });
		_rewards.put("Rich", new RewardItem[]
		{
			// Boss jewels
			new RewardItem(RING_OF_ANT_QUIEEN_3DAYS, 1, 1, 7000),
			new RewardItem(RING_OF_ANT_QUIEEN_7DAYS, 1, 1, 4000),
			new RewardItem(RING_OF_ANT_QUIEEN_15DAYS, 1, 1, 3000),
			
			new RewardItem(ZAKEN_EARRING_3DAYS, 1, 1, 5000),
			new RewardItem(ZAKEN_EARRING_7DAYS, 1, 1, 2000),
			new RewardItem(ZAKEN_EARRING_15DAYS, 1, 1, 1000),
			
			new RewardItem(BAIUM_RING_3DAYS, 1, 1, 1000),
			new RewardItem(BAIUM_RING_7DAYS, 1, 1, 800),
			
			new RewardItem(ANTHARAS_EARRING_3DAYS, 1, 1, 4000),
			new RewardItem(ANTHARAS_EARRING_7DAYS, 1, 1, 1000),
			
			new RewardItem(EARRING_OF_ORFEN, 1, 1, 18000),
			new RewardItem(RING_OF_CORE, 1, 1, 15000),
			
			new RewardItem(FRINTEZZA_NECKLACE, 1, 1, 400)
		});

		// Балансируем дроплист на базе используемых слов
		Map<Integer, Integer> temp = new HashMap<Integer, Integer>();
		for(Integer[][] ii : _words.values())
			for(Integer[] i : ii)
			{
				Integer curr = temp.get(i[0]);
				if(curr == null)
					temp.put(i[0], i[1]);
				else
					temp.put(i[0], curr + i[1]);
			}
		letters = new int[temp.size()][2];
		int i = 0;
		for(Entry<Integer, Integer> e : temp.entrySet())
			letters[i++] = new int[] { e.getKey(), e.getValue() };
	}
}