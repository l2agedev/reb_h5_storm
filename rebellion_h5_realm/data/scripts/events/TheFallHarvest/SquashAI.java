package events.TheFallHarvest;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.reward.RewardItem;
import l2r.gameserver.model.reward.RewardItemResult;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Log;
import npc.model.SquashInstance;

public class SquashAI extends Fighter
{
	public class PolimorphTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			SquashInstance actor = getActor();
			if(actor == null)
				return;
			SimpleSpawner spawn = null;

			try
			{
				spawn = new SimpleSpawner(NpcHolder.getInstance().getTemplate(_npcId));
				spawn.setLoc(actor.getLoc());
				NpcInstance npc = spawn.doSpawn(true);
				npc.setAI(new SquashAI(npc));
				((SquashInstance) npc).setSpawner(actor.getSpawner());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			_timeToUnspawn = Long.MAX_VALUE;
			actor.deleteMe();
		}
	}

	protected static final RewardItem[] _dropList = new RewardItem[] 
	{ 
		new RewardItem(1539, 1, 5, 15000), // Greater Healing Potion
		new RewardItem(1374, 1, 3, 15000), // Greater Haste Potion

		new RewardItem(4411, 1, 1, 5000), // Echo Crystal - Theme of Journey
		new RewardItem(4412, 1, 1, 5000), // Echo Crystal - Theme of Battle
		new RewardItem(4413, 1, 1, 5000), // Echo Crystal - Theme of Love
		new RewardItem(4414, 1, 1, 5000), // Echo Crystal - Theme of Solitude
		new RewardItem(4415, 1, 1, 5000), // Echo Crystal - Theme of the Feast
		new RewardItem(4416, 1, 1, 5000), // Echo Crystal - Theme of Celebration
		new RewardItem(4417, 1, 1, 5000), // Echo Crystal - Theme of Comedy
		new RewardItem(5010, 1, 1, 5000), // Echo Crystal - Theme of Victory

		new RewardItem(1458, 10, 30, 13846), // Crystal: D-Grade 1.3%
		new RewardItem(1459, 10, 30, 3000), // Crystal: C-Grade  0.3%
		new RewardItem(1460, 10, 30, 1000), // Crystal: B-Grade  0.1%
		new RewardItem(1461, 10, 30, 600), // Crystal: A-Grade   0.06%
		new RewardItem(1462, 10, 30, 360), // Crystal: S-Grade   0.036%

		new RewardItem(4161, 1, 1, 5000), // Recipe: Blue Wolf Tunic
		new RewardItem(4182, 1, 1, 5000), // Recipe: Great Sword
		new RewardItem(4174, 1, 1, 5000), // Recipe:  Zubei's Boots
		new RewardItem(4166, 1, 1, 5000), // Recipe: Doom Helmet

		new RewardItem(8660, 1, 1, 1000), // Demon Horns        0.1%
		new RewardItem(8661, 1, 1, 1000), // Mask of Spirits    0.1%
		new RewardItem(4393, 1, 1, 300), // Calculator          0.03%
		new RewardItem(7836, 1, 1, 200), // Santa's Hat         0.02%
		new RewardItem(5590, 1, 1, 200), // Squeaking Shoes     0.02%
		new RewardItem(7058, 1, 1, 50), // Chrono Darbuka       0.005%
		new RewardItem(8350, 1, 1, 50), // Chrono Maracas       0.005%
		new RewardItem(5133, 1, 1, 50), // Chrono Unitus        0.005%
		new RewardItem(5817, 1, 1, 50), // Chrono Campana       0.005%
		new RewardItem(9140, 1, 1, 30), // Salvation Bow        0.003%

		// Призрачные аксессуары - шанс 0.01%
		new RewardItem(9177, 1, 1, 100), // Teddy Bear Hat - Blessed Resurrection Effect
		new RewardItem(9178, 1, 1, 100), // Piggy Hat - Blessed Resurrection Effect
		new RewardItem(9179, 1, 1, 100), // Jester Hat - Blessed Resurrection Effect
		new RewardItem(9180, 1, 1, 100), // Wizard's Hat - Blessed Resurrection Effect
		new RewardItem(9181, 1, 1, 100), // Dapper Cap - Blessed Resurrection Effect
		new RewardItem(9182, 1, 1, 100), // Romantic Chapeau - Blessed Resurrection Effect
		new RewardItem(9183, 1, 1, 100), // Iron Circlet - Blessed Resurrection Effect
		new RewardItem(9184, 1, 1, 100), // Teddy Bear Hat - Blessed Escape Effect
		new RewardItem(9185, 1, 1, 100), // Piggy Hat - Blessed Escape Effect
		new RewardItem(9186, 1, 1, 100), // Jester Hat - Blessed Escape Effect
		new RewardItem(9187, 1, 1, 100), // Wizard's Hat - Blessed Escape Effect
		new RewardItem(9188, 1, 1, 100), // Dapper Cap - Blessed Escape Effect
		new RewardItem(9189, 1, 1, 100), // Romantic Chapeau - Blessed Escape Effect
		new RewardItem(9190, 1, 1, 100), // Iron Circlet - Blessed Escape Effect
		new RewardItem(9191, 1, 1, 100), // Teddy Bear Hat - Big Head
		new RewardItem(9192, 1, 1, 100), // Piggy Hat - Big Head
		new RewardItem(9193, 1, 1, 100), // Jester Hat - Big Head
		new RewardItem(9194, 1, 1, 100), // Wizard Hat - Big Head
		new RewardItem(9195, 1, 1, 100), // Dapper Hat - Big Head
		new RewardItem(9196, 1, 1, 100), // Romantic Chapeau - Big Head
		new RewardItem(9197, 1, 1, 100), // Iron Circlet - Big Head
		new RewardItem(9198, 1, 1, 100), // Teddy Bear Hat - Firework
		new RewardItem(9199, 1, 1, 100), // Piggy Hat - Firework
		new RewardItem(9200, 1, 1, 100), // Jester Hat - Firework
		new RewardItem(9201, 1, 1, 100), // Wizard's Hat - Firework
		new RewardItem(9202, 1, 1, 100), // Dapper Hat - Firework
		new RewardItem(9203, 1, 1, 100), // Romantic Chapeau - Firework
		new RewardItem(9204, 1, 1, 100), // Iron Circlet - Firework

		new RewardItem(9146, 1, 3, 5000), // Scroll of Guidance        0.5%
		new RewardItem(9147, 1, 3, 5000), // Scroll of Death Whisper   0.5%
		new RewardItem(9148, 1, 3, 5000), // Scroll of Focus           0.5%
		new RewardItem(9149, 1, 3, 5000), // Scroll of Acumen          0.5%
		new RewardItem(9150, 1, 3, 5000), // Scroll of Haste           0.5%
		new RewardItem(9151, 1, 3, 5000), // Scroll of Agility         0.5%
		new RewardItem(9152, 1, 3, 5000), // Scroll of Empower         0.5%
		new RewardItem(9153, 1, 3, 5000), // Scroll of Might           0.5%
		new RewardItem(9154, 1, 3, 5000), // Scroll of Wind Walk       0.5%
		new RewardItem(9155, 1, 3, 5000), // Scroll of Shield          0.5%
		new RewardItem(9156, 1, 3, 2000), // BSoE                      0.2%
		new RewardItem(9157, 1, 3, 1000), // BRES                      0.1%

		new RewardItem(955, 1, 1, 400), // EWD          0.04%
		new RewardItem(956, 1, 1, 2000), // EAD         0.2%
		new RewardItem(951, 1, 1, 300), // EWC          0.03%
		new RewardItem(952, 1, 1, 1500), // EAC         0.15%
		new RewardItem(947, 1, 1, 200), // EWB          0.02%
		new RewardItem(948, 1, 1, 1000), // EAB         0.1%
		new RewardItem(729, 1, 1, 100), // EWA          0.01%
		new RewardItem(730, 1, 1, 500), // EAA          0.05%
		new RewardItem(959, 1, 1, 50), // EWS           0.005%
		new RewardItem(960, 1, 1, 300), // EAS          0.03%
	};

	public final static int Young_Squash = 12774;
	public final static int High_Quality_Squash = 12775;
	public final static int Low_Quality_Squash = 12776;
	public final static int Large_Young_Squash = 12777;
	public final static int High_Quality_Large_Squash = 12778;
	public final static int Low_Quality_Large_Squash = 12779;
	public final static int King_Squash = 13016;
	public final static int Emperor_Squash = 13017;

	public final static int Squash_Level_up = 4513;
	public final static int Squash_Poisoned = 4514;

	private static final String[] textOnSpawn = new String[] {
		"scripts.events.TheFallHarvest.SquashAI.textOnSpawn.0",
		"scripts.events.TheFallHarvest.SquashAI.textOnSpawn.1",
	"scripts.events.TheFallHarvest.SquashAI.textOnSpawn.2" };

	private static final String[] textOnAttack = new String[] {
		"Bites rat-a-tat... to change... body...!",
		"Ha ha, grew up! Completely on all!",
		"Cannot to aim all? Had a look all to flow out...",
		"Is that also calculated hit? Look for person which has the strength!",
		"Don't waste your time!",
		"Ha, this sound is really pleasant to hear?",
		"I eat your attack to grow!",
		"Time to hit again! Come again!",
	"Only useful music can open big pumpkin... It can not be opened with weapon!" };

	private static final String[] textTooFast = new String[] {
		"heh heh,looks well hit!",
		"yo yo? Your skill is mediocre?",
		"Time to hit again! Come again!",
		"I eat your attack to grow!",
		"Make an effort... to get down like this, I walked...",
		"What is this kind of degree to want to open me? Really is indulges in fantasy!",
		"Good fighting method. Evidently flies away the fly also can overcome.",
	"Strives to excel strength oh! But waste your time..." };

	private static final String[] textSuccess0 = new String[] {
		"The lovely pumpkin young fruit start to glisten when taken to the threshing ground! From now on will be able to grow healthy and strong!",
		"Oh, Haven't seen for a long time?",
		"Suddenly, thought as soon as to see my beautiful appearance?",
		"Well! This is something! Is the nectar?",
	"Refuels! Drink 5 bottles to be able to grow into the big pumpkin oh!" };

	private static final String[] textFail0 = new String[] {
		"If I drink nectar, I can grow up faster!",
		"Come, believe me, sprinkle a nectar! I can certainly turn the big pumpkin!!!",
	"Take nectar to come, pumpkin nectar!" };

	private static final String[] textSuccess1 = new String[] {
		"Wish the big pumpkin!",
		"completely became the recreation area! Really good!",
		"Guessed I am mature or am rotten?",
	"Nectar is just the best! Ha! Ha! Ha!" };

	private static final String[] textFail1 = new String[] {
		"oh! Randomly missed! Too quickly sprinkles the nectar?",
		"If I die like this, you only could get young pumpkin...",
		"Cultivate a bit faster! The good speech becomes the big pumpkin, the young pumpkin is not good!",
	"The such small pumpkin you all must eat? Bring the nectar, I can be bigger!" };

	private static final String[] textSuccess2 = new String[] {
		"Young pumpkin wishing! Has how already grown up?",
		"Already grew up! Quickly sneaked off...",
	"Graciousness, is very good. Come again to see, now felt more and more well" };

	private static final String[] textFail2 = new String[] {
		"Hey! Was not there! Here is! Here! Not because I can not properly care? Small!",
		"Wow, stops? Like this got down to have to thank",
		"Hungry for a nectar oh...",
	"Do you want the big pumpkin? But I like young pumpkin..." };

	private static final String[] textSuccess3 = new String[] {
		"Big pumpkin wishing! Ask, to sober!",
		"Rumble rumble... it's really tasty! Hasn't it?",
	"Cultivating me just to eat? Good, is casual your... not to give the manna on the suicide!" };

	private static final String[] textFail3 = new String[] {
		"Isn't it the water you add? What flavor?",
	"Master, rescue my... I don't have the nectar flavor, I must die..." };

	private static final String[] textSuccess4 = new String[] {
		"is very good, does extremely well! Knew what next step should make?",
	"If you catch me, I give you 10 million adena!!! Agree?" };

	private static final String[] textFail4 = new String[] { "Hungry for a nectar oh...", "If I drink nectar, I can grow up faster!" };

	private int _npcId;
	private int _nectar;
	private int _tryCount;
	private long _lastNectarUse;
	private long _timeToUnspawn;

	private ScheduledFuture<?> _polimorphTask;

	private static int NECTAR_REUSE = 3000;

	public SquashAI(NpcInstance actor)
	{
		super(actor);
		_npcId = getActor().getNpcId();
		Functions.npcSayCustomMessage(getActor(), textOnSpawn[Rnd.get(textOnSpawn.length)]);
		_timeToUnspawn = System.currentTimeMillis() + 120000;
	}

	@Override
	protected boolean thinkActive()
	{
		if(System.currentTimeMillis() > _timeToUnspawn)
		{
			_timeToUnspawn = Long.MAX_VALUE;
			if(_polimorphTask != null)
			{
				_polimorphTask.cancel(false);
				_polimorphTask = null;
			}

			SquashInstance actor = getActor();
			if(actor != null)
				actor.deleteMe();
		}

		return false;
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		SquashInstance actor = getActor();
		if(actor == null || skill.getId() != 2005)
			return;

		switch(_tryCount)
		{
			case 0:
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess0[Rnd.get(textSuccess0.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail0[Rnd.get(textFail0.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				break;
			case 1:
				if(System.currentTimeMillis() - _lastNectarUse < NECTAR_REUSE)
				{
					Functions.npcSay(actor, textTooFast[Rnd.get(textTooFast.length)]);
					return;
				}
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess1[Rnd.get(textSuccess1.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail1[Rnd.get(textFail1.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				break;
			case 2:
				if(System.currentTimeMillis() - _lastNectarUse < NECTAR_REUSE)
				{
					Functions.npcSay(actor, textTooFast[Rnd.get(textTooFast.length)]);
					return;
				}
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess2[Rnd.get(textSuccess2.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail2[Rnd.get(textFail2.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				break;
			case 3:
				if(System.currentTimeMillis() - _lastNectarUse < NECTAR_REUSE)
				{
					Functions.npcSay(actor, textTooFast[Rnd.get(textTooFast.length)]);
					return;
				}
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess3[Rnd.get(textSuccess3.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail3[Rnd.get(textFail3.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				break;
			case 4:
				if(System.currentTimeMillis() - _lastNectarUse < NECTAR_REUSE)
				{
					Functions.npcSay(actor, textTooFast[Rnd.get(textTooFast.length)]);
					return;
				}
				_tryCount++;
				_lastNectarUse = System.currentTimeMillis();
				if(Rnd.chance(50))
				{
					_nectar++;
					Functions.npcSay(actor, textSuccess4[Rnd.get(textSuccess4.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Level_up, 1, NECTAR_REUSE, 0));
				}
				else
				{
					Functions.npcSay(actor, textFail4[Rnd.get(textFail4.length)]);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, Squash_Poisoned, 1, NECTAR_REUSE, 0));
				}
				if(_npcId == Young_Squash)
				{
					if(_nectar < 3)
						_npcId = Low_Quality_Squash;
					else if(_nectar == 5)
						_npcId = King_Squash;
					else
						_npcId = High_Quality_Squash;
				}
				else if(_npcId == Large_Young_Squash)
					if(_nectar < 3)
						_npcId = Low_Quality_Large_Squash;
					else if(_nectar == 5)
						_npcId = Emperor_Squash;
					else
						_npcId = High_Quality_Large_Squash;

				_polimorphTask = ThreadPoolManager.getInstance().schedule(new PolimorphTask(), NECTAR_REUSE);
				break;
		}
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		SquashInstance actor = getActor();
		if(actor != null && Rnd.chance(5))
			Functions.npcSay(actor, textOnAttack[Rnd.get(textOnAttack.length)]);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_tryCount = -1;
		SquashInstance actor = getActor();
		if(actor == null)
			return;

		double dropMod = 1.5;

		switch(_npcId)
		{
			case Low_Quality_Squash:
				dropMod *= 1;
				Functions.npcSay(actor, "The pampkin opens!!!");
				Functions.npcSay(actor, "ya yo! Opens! Good thing many...");
				break;
			case High_Quality_Squash:
				dropMod *= 2;
				Functions.npcSay(actor, "The pampkin opens!!!");
				Functions.npcSay(actor, "ya yo! Opens! Good thing many...");
				break;
			case King_Squash:
				dropMod *= 4;
				Functions.npcSay(actor, "The pampkin opens!!!");
				Functions.npcSay(actor, "ya yo! Opens! Good thing many...");
				break;
			case Low_Quality_Large_Squash:
				dropMod *= 12.5;
				Functions.npcSay(actor, "The pampkin opens!!!");
				Functions.npcSay(actor, "ya yo! Opens! Good thing many...");
				break;
			case High_Quality_Large_Squash:
				dropMod *= 25;
				Functions.npcSay(actor, "The pampkin opens!!!");
				Functions.npcSay(actor, "ya yo! Opens! Good thing many...");
				break;
			case Emperor_Squash:
				dropMod *= 50;
				Functions.npcSay(actor, "The pampkin opens!!!");
				Functions.npcSay(actor, "ya yo! Opens! Good thing many...");
				break;
			default:
				dropMod *= 0;
				Functions.npcSay(actor, "Ouch, if I had died like this, you could obtain nothing!");
				Functions.npcSay(actor, "The news about my death shouldn't spread, oh!");
				break;
		}

		super.onEvtDead(actor);

		if(dropMod > 0)
		{
			if(_polimorphTask != null)
			{
				_polimorphTask.cancel(false);
				_polimorphTask = null;
				Log.addGame("TheFallHarvest :: Player " + actor.getSpawner().getName() + " tried to use cheat (SquashAI clone): killed " + actor + " after polymorfing started", "illegal-actions");
				return; // при таких вариантах ничего не даем
			}

			for(RewardItem d : _dropList)
			{
				List<RewardItemResult> itd = d.roll(null, dropMod);
				for(RewardItemResult i : itd)
					actor.dropItem(actor.getSpawner(), i.itemId, i.count);
			}
		}
	}

	@Override
	protected boolean randomAnimation()
	{
		return false;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	public SquashInstance getActor()
	{
		return (SquashInstance) super.getActor();
	}
}