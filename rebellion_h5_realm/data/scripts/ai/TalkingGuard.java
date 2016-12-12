package ai;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.ai.Guard;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.model.AggroList;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.scripts.Functions;

public class TalkingGuard extends Guard implements Runnable
{
	private boolean _crazyState;
	private long _lastAggroSay;
	private long _lastNormalSay;
	private static final int _crazyChance = Config.TalkGuardChance;
	private static final int _sayNormalChance = Config.TalkNormalChance;
	private static final long _sayNormalPeriod = Config.TalkNormalPeriod * 6000;
	private static final long _sayAggroPeriod = Config.TalkAggroPeriod * 6000;
	
	// Фразы, которые может произнести гвард, когда начинает атаковать пк
	private static final String[] _sayAggroText_ru =
	{
		"{name}, никуда не уходи, сейчас я тебя чуть-чуть убью!",
		"{name}, я тебя зарэжу, мамой клянусь!",
		"Ля-ля-ля, я сошел с ума. Сейчас всех буду убивать!",
		"Сколько я зарезал, сколько перерезал, сколько я народу загубил! Будешь ты, {name}, еще одним в этом списке!",
		"Я ужас, летящий на крыльях ночи! Я жвачка, прилипшая к вашей подошве! Я... Короче, {name}, сейчас я тебя буду убивать!",
		"Я страх, трепещущий в ночи! Я хитроумный замок от подвала правосудия! Я любимец удачи! Я Чёрный Гвард!",
		"Ух ты, моя будущая жертва. Это я к тебе обращаюсь, {name}! Не делай вид что ты не при делах!",
		"Ура! За родину, за всех моих собратьев! Готовься к смерти, {name}!",
		"{name}, кошелек или жизнь?",
		"{name}, просто умри, не осложняй мне жизнь!",
		"{name}, как ты предпочитаешь умереть? Быстро и легко или же медленно и мучительно?",
		"{name}, пвп или засцал?",
		"{name}, я убью тебя нежно.",
		"{name}, я тебя порву как Тузик грелку!",
		"Готовься к смерти, {name}!",
		"{name}, ты дерешся как девчонка!",
		"{name}, помолись перед смертью! Хотя... уже не успеешь!"
	};
	
	private static final String[] _sayAggroText_en =
	{
		"{name}, do not go away, now I'm going to kill you a little bit!",
		"{name}, I'll slaughter you!",
		"La-la-la, I'm crazy. Now I will kill you all!",
		"How much I slaughtered, how much slashed, how many people I killed! Will you, {name}, be another on this list!",
		"I'm terrified of flying on the wings of the night! I'm cud sticking to your soles! In short, {name}, I'm going to kill you now!",
		"I fear, trembling in the night! I dodgy lock the basement of justice! I have a favorite good luck! I am a Black Guard!",
		"Hey you, my future victim. This is me talking to you, {name}! Do not pretend that you're not in the business!",
		"Hooray! For their country, for all my brethren! Prepare to die, {name}!",
		"{name}, Trick or Treat?",
		"{name}, just die, do not complicate my life!",
		"{name}, how do you like to die? Quickly and easily, or slowly and painfully?",
		"{name}, PVP, or you pissed your pants?",
		"{name}, I'll kill you softly.",
		"{name}, I'll tear you as a pillow.",
		"Prepare to die, {name}!",
		"{name}, You fight like a girl!",
		"{name}, pray before death! Although... you do not have time!"
	};
	
	// Фразы, которые может произнести гвард, адресуя их проходящим мимо игрокам мужского пола
	private static final String[] _sayNormalTextM_ru =
	{
		"{name}, есть чо?",
		"{name}, превед!",
		"{name}, привет!",
		"{name}, привет пративный.",
		"{name}, дай оружие на минутку, хочу скрин сделать.",
		"{name}, удачной охоты.",
		"{name}, в чем сила, брат?",
		"{name}, больше фрагов тебе.",
		"{name}, ты мне в кошмарах снился.",
		"{name}, я тебя знаю - тебя давно разыскивают за убийства невинных монстров.",
		"{name}, пвп или засцал?",
		"{name}, у тебя кошелек выпал.",
		"{name}, не пойду я с тобой на свидание, даже не проси.",
		"Всем чмоке в этом чате."
	};
	
	private static final String[] _sayNormalTextM_en =
	{
		"{name}, you there?",
		"{name}, hello!",
		"{name}, hi!",
		"{name}, hello nasty.",
		"{name}, give your weapon for a moment, I want to make a screen.",
		"{name}, successful hunt.",
		"{name}, what force, brother?",
		"{name}, more kills for you.",
		"{name}, you give me nightmares.",
		"{name}, I know you - you are long wanted for the murder of innocent monsters.",
		"{name}, PVP, or you pissed your pants?",
		"{name}, your wallet fell down.",
		"{name}, I will not go with you on a date, do not even ask.",
		"Smack all in this chat."
	};
	
	// Фразы, которые может произнести гвард, адресуя их проходящим мимо игрокам женского пола
	private static final String[] _sayNormalTextF_ru =
	{
		"{name}, привет красавица.",
		"{name}, ух ты, какие у тебя... э... глаза.",
		"{name}, не хочешь погулять с настоящим мачо?",
		"{name}, привет!",
		"{name}, дай потрогать... э... ну вобщем дай чего нибудь потрогать.",
		"{name}, не женское это дело - врагов убивать.",
		"{name}, у тебя верх порвался, не свети... глазками.",
		"{name}, ах какие булочки...",
		"{name}, ах какие ножки...",
		"{name}, да ты красотка однако.",
		"{name}, вах, какая женщина, мне бы такую.",
		"{name}, а что вы делаете сегодня вечером?",
		"{name}, вы согласны что с точки зрения банальной эрудиции, не всякий локально селектированный индивидуум способен игнорировать тенденции потенциальных эмоций и паритетно аллоцировать амбивалентные кванты логистики, экстрагируемой с учетом антропоморфности эвристического генезиса?",
		"{name}, предлагаю руку и сердце. И кошелек после свадьбы."
	};
	
	private static final String[] _sayNormalTextF_en =
	{
		"{name}, Hello beautiful.",
		"{name}, Wow, what are your... e... eyes.",
		"{name}, do not want to hang out with this macho?",
		"{name}, hi!",
		"{name}, let me touch you... on.... Well in the generally forbidden places.",
		"{name}, to kill enemies is not a woman's thing.",
		"{name}, you broke the top, no light... eyes.",
		"{name}, oh what buns...",
		"{name}, oh what legs...",
		"{name}, yes, you are a babe.",
		"{name}, woow, what a woman, I would have you.",
		"{name}, and.. what are you doing tonight?",
		"{name}, You agree that from the point of banal erudition, not every individual is able to locally-selected tendency to ignore the potential of emotions and parity allotsirovat ambivalent quanta logistics extractable given anthropomorphic heuristic genesis?",
		"{name}, offer his hand and heart. And after the wedding - your purse."
	};
	
	public TalkingGuard(NpcInstance actor)
	{
		super(actor);
		MAX_PURSUE_RANGE = 600;
		_crazyState = false;
		_lastAggroSay = 0;
		_lastNormalSay = 0;
	}
	
	@Override
	protected void onEvtSpawn()
	{
		_lastAggroSay = 0;
		_lastNormalSay = 0;
		_crazyState = Rnd.chance(_crazyChance) ? true : false;
		super.onEvtSpawn();
	}
	
	@Override
	public boolean checkAggression(Creature target)
	{
		if (_crazyState)
		{
			NpcInstance actor = getActor();
			Player player = target.getPlayer();
			if (actor == null || actor.isDead() || player == null)
				return false;
			if (player.isGM())
				return false;
			if (Rnd.chance(_sayNormalChance))
			{
				if (target.isPlayer() && target.getKarma() <= 0 && (_lastNormalSay + _sayNormalPeriod < System.currentTimeMillis()) && actor.isInRange(target, 250L))
				{
					if (target.getPlayer().isLangRus())
					{
						Functions.npcSay(actor, target.getPlayer().getSex() == 0 ? _sayNormalTextM_ru[Rnd.get(_sayNormalTextM_ru.length)].replace("{name}", target.getName()) : _sayNormalTextF_ru[Rnd.get(_sayNormalTextF_ru.length)].replace("{name}", target.getName()));
					}
					else
					{
						Functions.npcSay(actor, target.getPlayer().getSex() == 0 ? _sayNormalTextM_en[Rnd.get(_sayNormalTextM_en.length)].replace("{name}", target.getName()) : _sayNormalTextF_en[Rnd.get(_sayNormalTextF_en.length)].replace("{name}", target.getName()));
					}
					_lastNormalSay = System.currentTimeMillis();
				}
			}
			
			if (target.getKarma() <= 0)
				return false;
			if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
				return false;
			if (_globalAggro < 0L)
				return false;
			AggroList.AggroInfo ai = actor.getAggroList().get(target);
			if (ai != null && ai.hate > 0)
			{
				if (!target.isInRangeZ(actor.getSpawnedLoc(), MAX_PURSUE_RANGE))
					return false;
			}
			else if (!target.isInRangeZ(actor.getSpawnedLoc(), 600))
				return false;
			if (target.isPlayable() && !canSeeInSilentMove((Playable) target))
				return false;
			if (!GeoEngine.canSeeTarget(actor, target, false))
				return false;
			if (target.isPlayer() && ((Player) target).isInvisible())
				return false;
			if ((target.isSummon() || target.isPet()) && target.getPlayer() != null)
				actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
			actor.getAggroList().addDamageHate(target, 0, 2);
			startRunningTask(2000);
			if (_lastAggroSay + _sayAggroPeriod < System.currentTimeMillis())
			{
				if (target.getPlayer().isLangRus())
				{
					Functions.npcSay(actor, _sayAggroText_ru[Rnd.get(_sayAggroText_ru.length)].replace("{name}", target.getPlayer().getName()));
				}
				else
				{
					Functions.npcSay(actor, _sayAggroText_en[Rnd.get(_sayAggroText_en.length)].replace("{name}", target.getPlayer().getName()));
				}
				_lastAggroSay = System.currentTimeMillis();
			}
			
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			return true;
		}
		else
		{
			super.checkAggression(target);
		}
		return false;
	}
}