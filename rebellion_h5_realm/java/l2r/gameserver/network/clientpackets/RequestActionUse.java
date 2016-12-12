package l2r.gameserver.network.clientpackets;


import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Request;
import l2r.gameserver.model.Request.L2RequestType;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.World;
import l2r.gameserver.model.entity.boat.ClanAirShip;
import l2r.gameserver.model.instances.PetBabyInstance;
import l2r.gameserver.model.instances.StaticObjectInstance;
import l2r.gameserver.model.instances.residences.SiegeFlagInstance;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.ExAirShipTeleportList;
import l2r.gameserver.network.serverpackets.ExAskCoupleAction;
import l2r.gameserver.network.serverpackets.MyTargetSelected;
import l2r.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import l2r.gameserver.network.serverpackets.PrivateStoreManageListSell;
import l2r.gameserver.network.serverpackets.RecipeShopManageList;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.ValidateLocation;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.PetDataTable;
import l2r.gameserver.tables.PetSkillsTable;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.AutoHuntingPunish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * packet type id 0x56
 * format:		cddc
 */
public class RequestActionUse extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestActionUse.class);

	private int _actionId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	private static final int PLAYER_ACTION = 0;
	private static final int PET_ACTION = 1;
	private static final int PET_SKILL = 2;
	private static final int SOCIAL_ACTION = 3;
	private static final int COUPLE_ACTION = 4;
	
	/* type:
	 * 0 - action
	 * 1 - pet action
	 * 2 - pet skill
	 * 3 - social
	 * 4 - couple social
	 * 
	 * transform:
	 * 0 для любых разрешено
	 * 1 разрешено для некоторых
	 * 2 запрещено для всех
	 */
	public static enum Action
	{
		// Действия персонажей
		ACTION0(0, PLAYER_ACTION, 0, 1), // Сесть/встать
		ACTION1(1, PLAYER_ACTION, 0, 0), // Изменить тип передвижения, шаг/бег
		ACTION7(7, PLAYER_ACTION, 0, 1), // Next Target
		ACTION10(10, PLAYER_ACTION, 0, 1), // Запрос на создание приватного магазина продажи
		ACTION28(28, PLAYER_ACTION, 0, 1), // Запрос на создание приватного магазина покупки
		ACTION37(37, PLAYER_ACTION, 0, 1), // Создание магазина Common Craft
		ACTION38(38, PLAYER_ACTION, 0, 1), // Mount
		ACTION51(51, PLAYER_ACTION, 0, 1), // Создание магазина Dwarven Craft
		ACTION61(61, PLAYER_ACTION, 0, 1), // Запрос на создание приватного магазина продажи (Package)
		ACTION96(96, PLAYER_ACTION, 0, 1), // Quit Party Command Channel?
		ACTION97(97, PLAYER_ACTION, 0, 1), // Request Party Command Channel Info?
		ACTION65(65, PLAYER_ACTION, 0, 1), // Auto Hunting Reporting
		ACTION67(67, PLAYER_ACTION, 0, 1), // Steer. Allows you to control the Airship.
		ACTION68(68, PLAYER_ACTION, 0, 1), // Cancel Control. Relinquishes control of the Airship.
		ACTION69(69, PLAYER_ACTION, 0, 1), // Destination Map. Choose from pre-designated locations.
		ACTION70(70, PLAYER_ACTION, 0, 1), // Exit Airship. Disembarks from the Airship.

		// Действия петов
		ACTION15(15, PET_ACTION, 0, 0), // Pet Follow
		ACTION16(16, PET_ACTION, 0, 0), // Атака петом
		ACTION17(17, PET_ACTION, 0, 0), // Отмена действия у пета
		ACTION19(19, PET_ACTION, 0, 0), // Отзыв пета
		ACTION21(21, PET_ACTION, 0, 0), // Pet Follow
		ACTION22(22, PET_ACTION, 0, 0), // Атака петом
		ACTION23(23, PET_ACTION, 0, 0), // Отмена действия у пета
		ACTION52(52, PET_ACTION, 0, 0), // Отзыв саммона
		ACTION53(53, PET_ACTION, 0, 0), // Передвинуть пета к цели
		ACTION54(54, PET_ACTION, 0, 0), // Передвинуть пета к цели

		// Действия петов со скиллами
		ACTION32(32, PET_SKILL, 4230, 0), // Wild Hog Cannon - Mode Change
		ACTION36(36, PET_SKILL, 4259, 0), // Soulless - Toxic Smoke
		ACTION39(39, PET_SKILL, 4138, 0), // Soulless - Parasite Burst
		ACTION41(41, PET_SKILL, 4230, 0), // Wild Hog Cannon - Attack
		ACTION42(42, PET_SKILL, 4378, 0), // Kai the Cat - Self Damage Shield
		ACTION43(43, PET_SKILL, 4137, 0), // Unicorn Merrow - Hydro Screw
		ACTION44(44, PET_SKILL, 4139, 0), // Big Boom - Boom Attack
		ACTION45(45, PET_SKILL, 4025, 0), // Unicorn Boxer - Master Recharge
		ACTION46(46, PET_SKILL, 4261, 0), // Mew the Cat - Mega Storm Strike
		ACTION47(47, PET_SKILL, 4260, 0), // Silhouette - Steal Blood
		ACTION48(48, PET_SKILL, 4068, 0), // Mechanic Golem - Mech. Cannon
		ACTION1000(1000, PET_SKILL, 4079, 0), // Siege Golem - Siege Hammer
		//ACTION1001(1001, PET_SKILL, , 0), // Sin Eater - Ultimate Bombastic Buster
		ACTION1003(1003, PET_SKILL, 4710, 0), // Wind Hatchling/Strider - Wild Stun
		ACTION1004(1004, PET_SKILL, 4711, 0), // Wind Hatchling/Strider - Wild Defense
		ACTION1005(1005, PET_SKILL, 4712, 0), // Star Hatchling/Strider - Bright Burst
		ACTION1006(1006, PET_SKILL, 4713, 0), // Star Hatchling/Strider - Bright Heal
		ACTION1007(1007, PET_SKILL, 4699, 0), // Cat Queen - Blessing of Queen
		ACTION1008(1008, PET_SKILL, 4700, 0), // Cat Queen - Gift of Queen
		ACTION1009(1009, PET_SKILL, 4701, 0), // Cat Queen - Cure of Queen
		ACTION1010(1010, PET_SKILL, 4702, 0), // Unicorn Seraphim - Blessing of Seraphim
		ACTION1011(1011, PET_SKILL, 4703, 0), // Unicorn Seraphim - Gift of Seraphim
		ACTION1012(1012, PET_SKILL, 4704, 0), // Unicorn Seraphim - Cure of Seraphim
		ACTION1013(1013, PET_SKILL, 4705, 0), // Nightshade - Curse of Shade
		ACTION1014(1014, PET_SKILL, 4706, 0), // Nightshade - Mass Curse of Shade
		ACTION1015(1015, PET_SKILL, 4707, 0), // Nightshade - Shade Sacrifice
		ACTION1016(1016, PET_SKILL, 4709, 0), // Cursed Man - Cursed Blow
		ACTION1017(1017, PET_SKILL, 4708, 0), // Cursed Man - Cursed Strike/Stun
		ACTION1031(1031, PET_SKILL, 5135, 0), // Feline King - Slash
		ACTION1032(1032, PET_SKILL, 5136, 0), // Feline King - Spin Slash
		ACTION1033(1033, PET_SKILL, 5137, 0), // Feline King - Hold of King
		ACTION1034(1034, PET_SKILL, 5138, 0), // Magnus the Unicorn - Whiplash
		ACTION1035(1035, PET_SKILL, 5139, 0), // Magnus the Unicorn - Tridal Wave
		ACTION1036(1036, PET_SKILL, 5142, 0), // Spectral Lord - Corpse Kaboom
		ACTION1037(1037, PET_SKILL, 5141, 0), // Spectral Lord - Dicing Death
		ACTION1038(1038, PET_SKILL, 5140, 0), // Spectral Lord - Force Curse
		ACTION1039(1039, PET_SKILL, 5110, 0), // Swoop Cannon - Cannon Fodder
		ACTION1040(1040, PET_SKILL, 5111, 0), // Swoop Cannon - Big Bang
		ACTION1041(1041, PET_SKILL, 5442, 0), // Great Wolf - 5442 - Bite Attack
		ACTION1042(1042, PET_SKILL, 5444, 0), // Great Wolf - 5444 - Moul
		ACTION1043(1043, PET_SKILL, 5443, 0), // Great Wolf - 5443 - Cry of the Wolf
		ACTION1044(1044, PET_SKILL, 5445, 0), // Great Wolf - 5445 - Awakening 70
		ACTION1045(1045, PET_SKILL, 5584, 0), // Wolf Howl
		ACTION1046(1046, PET_SKILL, 5585, 0), // Strider - Roar // TODO скилл не отображается даже на 85 уровне, вероятно нужно корректировать поле type в PetInfo для страйдеров
		ACTION1047(1047, PET_SKILL, 5580, 0), // Divine Beast - Bite
		ACTION1048(1048, PET_SKILL, 5581, 0), // Divine Beast - Stun Attack
		ACTION1049(1049, PET_SKILL, 5582, 0), // Divine Beast - Fire Breath
		ACTION1050(1050, PET_SKILL, 5583, 0), // Divine Beast - Roar
		ACTION1051(1051, PET_SKILL, 5638, 0), // Feline Queen - Bless The Body
		ACTION1052(1052, PET_SKILL, 5639, 0), // Feline Queen - Bless The Soul
		ACTION1053(1053, PET_SKILL, 5640, 0), // Feline Queen - Haste
		ACTION1054(1054, PET_SKILL, 5643, 0), // Unicorn Seraphim - Acumen
		ACTION1055(1055, PET_SKILL, 5647, 0), // Unicorn Seraphim - Clarity
		ACTION1056(1056, PET_SKILL, 5648, 0), // Unicorn Seraphim - Empower
		ACTION1057(1057, PET_SKILL, 5646, 0), // Unicorn Seraphim - Wild Magic
		ACTION1058(1058, PET_SKILL, 5652, 0), // Nightshade - Death Whisper
		ACTION1059(1059, PET_SKILL, 5653, 0), // Nightshade - Focus
		ACTION1060(1060, PET_SKILL, 5654, 0), // Nightshade - Guidance
		ACTION1061(1061, PET_SKILL, 5745, 0), // (Wild Beast Fighter, White Weasel) Death Blow - Awakens a hidden ability to inflict a powerful attack on the enemy. Requires application of the Awakening skill.
		ACTION1062(1062, PET_SKILL, 5746, 0), // (Wild Beast Fighter) Double Attack - Rapidly attacks the enemy twice.
		ACTION1063(1063, PET_SKILL, 5747, 0), // (Wild Beast Fighter) Spin Attack - Inflicts shock and damage to the enemy at the same time with a powerful spin attack.
		ACTION1064(1064, PET_SKILL, 5748, 0), // (Wild Beast Fighter) Meteor Shower - Attacks nearby enemies with a doll heap attack.
		ACTION1065(1065, PET_SKILL, 5753, 0), // (Fox Shaman, Wild Beast Fighter, White Weasel, Fairy Princess) Awakening - Awakens a hidden ability.
		ACTION1066(1066, PET_SKILL, 5749, 0), // (Fox Shaman, Spirit Shaman) Thunder Bolt - Attacks the enemy with the power of thunder.
		ACTION1067(1067, PET_SKILL, 5750, 0), // (Fox Shaman, Spirit Shaman) Flash - Inflicts a swift magic attack upon contacted enemies nearby.
		ACTION1068(1068, PET_SKILL, 5751, 0), // (Fox Shaman, Spirit Shaman) Lightning Wave - Attacks nearby enemies with the power of lightning.
		ACTION1069(1069, PET_SKILL, 5752, 0), // (Fox Shaman, Fairy Princess) Flare - Awakens a hidden ability to inflict a powerful attack on the enemy. Requires application of the Awakening skill.
		ACTION1070(1070, PET_SKILL, 5771, 0), // (White Weasel, Fairy Princess, Improved Baby Buffalo, Improved Baby Kookaburra, Improved Baby Cougar) Buff Control - Controls to prevent a buff upon the master. Lasts for 5 minutes.
		ACTION1071(1071, PET_SKILL, 5761, 0), // (Tigress) Power Striker - Powerfully attacks the target.
		ACTION1072(1072, PET_SKILL, 6046, 0), // (Toy Knight) Piercing attack
		ACTION1073(1073, PET_SKILL, 6047, 0), // (Toy Knight) Whirlwind
		ACTION1074(1074, PET_SKILL, 6048, 0), // (Toy Knight) Lance Smash
		ACTION1075(1075, PET_SKILL, 6049, 0), // (Toy Knight) Battle Cry
		ACTION1076(1076, PET_SKILL, 6050, 0), // (Turtle Ascetic) Power Smash
		ACTION1077(1077, PET_SKILL, 6051, 0), // (Turtle Ascetic) Energy Burst
		ACTION1078(1078, PET_SKILL, 6052, 0), // (Turtle Ascetic) Shockwave
		ACTION1079(1079, PET_SKILL, 6053, 0), // (Turtle Ascetic) Howl
		ACTION1080(1080, PET_SKILL, 6041, 0), // Phoenix Rush
		ACTION1081(1081, PET_SKILL, 6042, 0), // Phoenix Cleanse
		ACTION1082(1082, PET_SKILL, 6043, 0), // Phoenix Flame Feather
		ACTION1083(1083, PET_SKILL, 6044, 0), // Phoenix Flame Beak
		ACTION1084(1084, PET_SKILL, 6054, 0), // (Spirit Shaman, Toy Knight, Turtle Ascetic) Switch State - Toggles you between Attack and Support modes.
		ACTION1086(1086, PET_SKILL, 6094, 0), // Panther Cancel
		ACTION1087(1087, PET_SKILL, 6095, 0), // Panther Dark Claw
		ACTION1088(1088, PET_SKILL, 6096, 0), // Panther Fatal Claw
		ACTION1089(1089, PET_SKILL, 6199, 0), // (Deinonychus) Tail Strike
		ACTION1090(1090, PET_SKILL, 6205, 0), // (Guardian's Strider) Strider Bite
		ACTION1091(1091, PET_SKILL, 6206, 0), // (Guardian's Strider) Strider Fear
		ACTION1092(1092, PET_SKILL, 6207, 0), // (Guardian's Strider) Strider Dash
		ACTION1093(1093, PET_SKILL, 6618, 0),
		ACTION1094(1094, PET_SKILL, 6681, 0),
		ACTION1095(1095, PET_SKILL, 6619, 0),
		ACTION1096(1096, PET_SKILL, 6682, 0),
		ACTION1097(1097, PET_SKILL, 6683, 0),
		ACTION1098(1098, PET_SKILL, 6684, 0),
		ACTION1099(1099, PET_SKILL, 0, 0),
		ACTION5000(5000, PET_SKILL, 23155, 0), // Baby Rudolph - Reindeer Scratch
		ACTION5001(5001, PET_SKILL, 23167, 0),
		ACTION5002(5002, PET_SKILL, 23168, 0),
		ACTION5003(5003, PET_SKILL, 5749, 0),
		ACTION5004(5004, PET_SKILL, 5750, 0),
		ACTION5005(5005, PET_SKILL, 5751, 0),
		ACTION5006(5006, PET_SKILL, 5771, 0),
		ACTION5007(5007, PET_SKILL, 6046, 0),
		ACTION5008(5008, PET_SKILL, 6047, 0),
		ACTION5009(5009, PET_SKILL, 6048, 0),
		ACTION5010(5010, PET_SKILL, 6049, 0),
		ACTION5011(5011, PET_SKILL, 6050, 0),
		ACTION5012(5012, PET_SKILL, 6051, 0),
		ACTION5013(5013, PET_SKILL, 6052, 0),
		ACTION5014(5014, PET_SKILL, 6053, 0),
		ACTION5015(5015, PET_SKILL, 6054, 0),
		ACTION5016(5016, PET_SKILL, 0, 0),

		// Социальные действия
		ACTION12(12, SOCIAL_ACTION, SocialAction.GREETING, 2),
		ACTION13(13, SOCIAL_ACTION, SocialAction.VICTORY, 2),
		ACTION14(14, SOCIAL_ACTION, SocialAction.ADVANCE, 2),
		ACTION24(24, SOCIAL_ACTION, SocialAction.YES, 2),
		ACTION25(25, SOCIAL_ACTION, SocialAction.NO, 2),
		ACTION26(26, SOCIAL_ACTION, SocialAction.BOW, 2),
		ACTION29(29, SOCIAL_ACTION, SocialAction.UNAWARE, 2),
		ACTION30(30, SOCIAL_ACTION, SocialAction.WAITING, 2),
		ACTION31(31, SOCIAL_ACTION, SocialAction.LAUGH, 2),
		ACTION33(33, SOCIAL_ACTION, SocialAction.APPLAUD, 2),
		ACTION34(34, SOCIAL_ACTION, SocialAction.DANCE, 2),
		ACTION35(35, SOCIAL_ACTION, SocialAction.SORROW, 2),
		ACTION62(62, SOCIAL_ACTION, SocialAction.CHARM, 2),
		ACTION66(66, SOCIAL_ACTION, SocialAction.SHYNESS, 2),

		// Парные социальные действия
		ACTION71(71, COUPLE_ACTION, SocialAction.COUPLE_BOW, 2),
		ACTION72(72, COUPLE_ACTION, SocialAction.COUPLE_HIGH_FIVE, 2),
		ACTION73(73, COUPLE_ACTION, SocialAction.COUPLE_DANCE, 2);

		public int id;
		public int type;
		public int value;
		public int transform;

		private Action(int id, int type, int value, int transform)
		{
			this.id = id;
			this.type = type;
			this.value = value;
			this.transform = transform;
		}

		public static Action find(int id)
		{
			for(Action action : Action.values())
				if(action.id == id)
					return action;
			return null;
		}
	}

	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = readD() == 1;
		_shiftPressed = readC() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		// Check if has any bot punishment
		if (activeChar.isBeingPunished() && activeChar.getBotPunishType() == AutoHuntingPunish.Punish.ACTIONBAN)
		{
			// Remove punishment if finished
			if (activeChar.getPlayerPunish().canPerformAction() && activeChar.getBotPunishType() == AutoHuntingPunish.Punish.ACTIONBAN)
			{
				activeChar.endPunishment();
			}
			// Else apply it
			else if (activeChar.getBotPunishType() == AutoHuntingPunish.Punish.ACTIONBAN)
			{
				SystemMsg msgId = null;
				switch (activeChar.getPlayerPunish().getDuration())
				{
					case 1800:
					case 3600:
					case 7200:
						msgId = SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_ACTIONS_WILL_BE_RESTRICTED_FOR_120_MINUTES;
						break;
					case 10800:
						msgId = SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_ACTIONS_WILL_BE_RESTRICTED_FOR_180_MINUTES;
						break;
					default:
						msgId = SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_ACTIONS_WILL_BE_RESTRICTED_FOR_120_MINUTES;
				}
				activeChar.sendPacket(new SystemMessage2(msgId));
				return;
			}
		}

		Action action = Action.find(_actionId);
		if(action == null)
		{
			_log.warn("unhandled action type " + _actionId + " by player " + activeChar.getName());
			activeChar.sendActionFailed();
			return;
		}

		if (Config.SECURITY_ENABLED && Config.SECURITY_CANT_PVP_ENABLED && activeChar.getSecurity())
		{
			activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			return;
		}
		
		boolean usePet = action.type == PET_ACTION || action.type == PET_SKILL;

		// dont do anything if player is dead or confused
		if(!usePet && (activeChar.isOutOfControl() || activeChar.isActionsDisabled()) && !(activeChar.isFakeDeath() && _actionId == 0))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getTransformation() != 0 && action.transform > 0) // TODO разрешить для некоторых трансформ
		{
			activeChar.sendActionFailed();
			return;
		}

		// Социальные действия
		if(action.type == SOCIAL_ACTION)
		{
			if (activeChar.isGM() && _shiftPressed) // Shift-pressed social action from GM will result in target doing it.
			{
				if (activeChar.getTarget() != null && activeChar.getTarget().isPlayer())
				{
					activeChar.sendMessage("Target caused to use social action.");
					activeChar = (Player) activeChar.getTarget();
				}
			}
			
			if(activeChar.isOutOfControl() || activeChar.getTransformation() != 0 || activeChar.isActionsDisabled() || activeChar.isSitting() || activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_NONE || activeChar.isProcessingRequest())
			{
				activeChar.sendActionFailed();
				return;
			}
			if(activeChar.isFishing())
			{
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
				return;
			}
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), action.value));
			if(Config.ALT_SOCIAL_ACTION_REUSE)
			{
				ThreadPoolManager.getInstance().schedule(new SocialTask(activeChar), 2600);
				activeChar.startParalyzed();
			}
			return;
		}

		final GameObject target = activeChar.getTarget();
		// Парные социальные действия
		if(action.type == COUPLE_ACTION)
		{
			if (activeChar.isGM() && _shiftPressed) // Shift-pressed social action from GM will result in target doing it.
			{
				if (activeChar.getTarget() != null && activeChar.getTarget().isPlayer())
				{
					activeChar.sendMessage("Target caused to use social action.");
					activeChar = (Player) activeChar.getTarget();
				}
			}
			
			if(activeChar.isOutOfControl() || activeChar.isActionsDisabled() || activeChar.isSitting())
			{
				activeChar.sendActionFailed();
				return;
			}
			if(target == null || !target.isPlayer())
			{
				activeChar.sendActionFailed();
				return;
			}
			final Player pcTarget = target.getPlayer();
			if(pcTarget.isProcessingRequest() && pcTarget.getRequest().isTypeOf(L2RequestType.COUPLE_ACTION))
			{
				activeChar.sendPacket(new SystemMessage2(SystemMsg.COUPLE_ACTION_CANNOT_C1_TARGET_IN_ANOTHER_COUPLE_ACTION).addName(pcTarget));
				return;
			}
			if(pcTarget.isProcessingRequest())
			{
				activeChar.sendPacket(new SystemMessage2(SystemMsg.C1_IS_ON_ANOTHER_TASK).addName(pcTarget));
				return;
			}
			if(!activeChar.isInRange(pcTarget, 300) || activeChar.isInRange(pcTarget, 25) || activeChar.getTargetId() == activeChar.getObjectId() || !GeoEngine.canSeeTarget(activeChar, pcTarget, false))
			{
				activeChar.sendPacket(SystemMsg.THE_REQUEST_CANNOT_BE_COMPLETED_BECAUSE_THE_TARGET_DOES_NOT_MEET_LOCATION_REQUIREMENTS);
				return;
			}
			if(!activeChar.checkCoupleAction(pcTarget))
				return;

			new Request(L2RequestType.COUPLE_ACTION, activeChar, pcTarget).setTimeout(10000L);
			activeChar.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_REQUESTED_A_COUPLE_ACTION_WITH_C1).addName(pcTarget));
			pcTarget.sendPacket(new ExAskCoupleAction(activeChar.getObjectId(), action.value));

			if(Config.ALT_SOCIAL_ACTION_REUSE)
			{
				ThreadPoolManager.getInstance().schedule(new SocialTask(activeChar), 2600);
				activeChar.startParalyzed();
			}
			return;
		}

		final Summon pet = activeChar.getPet();
		if(usePet)
		{
			if(pet == null || pet.isOutOfControl() || activeChar.isDead())
			{
				activeChar.sendActionFailed();
				return;
			}
			if(pet.isDepressed())
			{
				activeChar.sendPacket(SystemMsg.YOUR_PETSERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
				return;
			}
		}

		// Скиллы петов
		if(action.type == PET_SKILL)
		{
			// TODO перенести эти условия в скиллы
			if(action.id == 1000 && target != null && !target.isDoor()) // Siege Golem - Siege Hammer
			{
				activeChar.sendActionFailed();
				return;
			}
			if (target != null)
			{
				if (action.id == 1039 || action.id == 1040 && target.isDoor() || target instanceof SiegeFlagInstance)
				{
					activeChar.sendActionFailed();
					return;
				}
			}
			UseSkill(action.value);
			return;
		}

		switch(action.id)
		{
			// Действия с игроками:

			case 0: // Сесть/встать
				// На страйдере нельзя садиться
				if(activeChar.isMounted())
				{
					activeChar.sendActionFailed();
					break;
				}

				if(activeChar.isFakeDeath())
				{
					activeChar.breakFakeDeath();
					activeChar.updateEffectIcons();
					break;
				}

				if (activeChar.isGM() && _shiftPressed) // Shift-pressed sit/stand from GM will result in target doing it.
				{
					if (activeChar.getTarget() != null && activeChar.getTarget().isPlayer())
					{
						activeChar.sendMessage("Target caused to sit/stand.");
						activeChar = (Player) activeChar.getTarget();
					}
				}
				
				if(!activeChar.isSitting())
				{
					if(target != null && target instanceof StaticObjectInstance && ((StaticObjectInstance) target).getType() == 1 && activeChar.getDistance3D(target) <= Creature.INTERACTION_DISTANCE)
						activeChar.sitDown((StaticObjectInstance)target);
					else
						activeChar.sitDown(null);
				}
				else
					activeChar.standUp();

				break;
			case 1: // Изменить тип передвижения, шаг/бег
				if (activeChar.isGM() && _shiftPressed) // Shift-pressed run/walk from GM will result in target doing it.
				{
					if (activeChar.getTarget() != null && activeChar.getTarget().isCreature())
					{
						activeChar.sendMessage("Target caused to run/walk.");
						if(((Creature) activeChar.getTarget()).isRunning())
							((Creature) activeChar.getTarget()).setWalking();
						else
							((Creature) activeChar.getTarget()).setRunning();
					}
				}
				else
				{
					if(activeChar.isRunning())
						activeChar.setWalking();
					else
						activeChar.setRunning();
				}
				break;
			case 7: // Next Target
				Creature nearest_target = null;
				for(Creature cha : World.getAroundCharacters(activeChar, 400, 200))
					if(cha != null && !cha.isFakeDeath() && !cha.isDead())
						if((nearest_target == null || activeChar.getDistance3D(cha) < activeChar.getDistance3D(nearest_target)) && cha.isAutoAttackable(activeChar))
							nearest_target = cha;
				if(nearest_target != null && activeChar.getTarget() != nearest_target)
				{
					activeChar.setTarget(nearest_target);
					if(activeChar.getTarget() == nearest_target)
					{
						if(nearest_target.isNpc())
						{
							activeChar.sendPacket(new MyTargetSelected(nearest_target.getObjectId(), activeChar.getLevel() - nearest_target.getLevel()));
							activeChar.sendPacket(nearest_target.makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP));
							activeChar.sendPacket(new ValidateLocation(nearest_target), ActionFail.STATIC);
						}
						else
							activeChar.sendPacket(new MyTargetSelected(nearest_target.getObjectId(), 0));
					}
					return;
				}
				break;
			case 10: // Запрос на создание приватного магазина продажи
			case 61: // Запрос на создание приватного магазина продажи (Package)
			{
				if (activeChar.isGM() && _shiftPressed) // Shift-pressed private store from GM will result in target doing it.
				{
					if (activeChar.getTarget() != null && activeChar.getTarget().isPlayer())
					{
						activeChar.sendMessage("Target caused to use private store.");
						activeChar = (Player) activeChar.getTarget();
					}
				}
				
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(!activeChar.canOpenPrivateStore(_actionId == 61 ? Player.STORE_PRIVATE_SELL_PACKAGE : Player.STORE_PRIVATE_SELL))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new PrivateStoreManageListSell(activeChar, _actionId == 61));
				break;
			}
			case 28: // Запрос на создание приватного магазина покупки
			{
				if (activeChar.isGM() && _shiftPressed) // Shift-pressed private store from GM will result in target doing it.
				{
					if (activeChar.getTarget() != null && activeChar.getTarget().isPlayer())
					{
						activeChar.sendMessage("Target caused to use private store.");
						activeChar = (Player) activeChar.getTarget();
					}
				}
				
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(!activeChar.canOpenPrivateStore(Player.STORE_PRIVATE_BUY))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new PrivateStoreManageListBuy(activeChar));
				break;
			}
			case 37: // Создание магазина Dwarven Craft
			{
				if (activeChar.isGM() && _shiftPressed) // Shift-pressed private manufacture from GM will result in target doing it.
				{
					if (activeChar.getTarget() != null && activeChar.getTarget().isPlayer())
					{
						activeChar.sendMessage("Target caused to use dwarven manufacture.");
						activeChar = (Player) activeChar.getTarget();
					}
				}
				
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(!activeChar.canOpenPrivateStore(Player.STORE_PRIVATE_MANUFACTURE))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
				break;
			}
			case 51: // Создание магазина Common Craft
			{
				if (activeChar.isGM() && _shiftPressed) // Shift-pressed common craft store from GM will result in target doing it.
				{
					if (activeChar.getTarget() != null && activeChar.getTarget().isPlayer())
					{
						activeChar.sendMessage("Target caused to use common craft store.");
						activeChar = (Player) activeChar.getTarget();
					}
				}
				
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(!activeChar.canOpenPrivateStore(Player.STORE_PRIVATE_MANUFACTURE))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new RecipeShopManageList(activeChar, false));
				break;
			}
			case 96: // Quit Party Command Channel?
				_log.info("96 Accessed");
				break;
			case 97: // Request Party Command Channel Info?
				_log.info("97 Accessed");
				break;
				/* Disabled from retail like button, will be handled by voice command.
 			case 65: // Bot Report Button
 				if(Config.ENABLE_AUTO_HUNTING_REPORT)
 				{
 					if(activeChar.getTarget() instanceof Player)
 					{
 						Player reported = (Player) activeChar.getTarget();
 						if(!AutoHuntingManager.getInstance().validateBot(reported, activeChar))
 							return;
 						if(!AutoHuntingManager.getInstance().validateReport(activeChar))
 							return;
 						try
 						{
 							AutoHuntingManager.getInstance().reportBot(reported, activeChar);
 						}
 						catch (Exception e)
 						{
 							e.printStackTrace();
 						}
 					}
 					else
 						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestActionUse.BotReport1", activeChar));
 				}
 				else
 					activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestActionUse.BotReport2", activeChar));
 				break;
 				*/
			// Действия с петами:

			case 15:
			case 21:
				if (pet != null && !pet.isAfraid())
					pet.setFollowMode(!pet.isFollowMode());
				break;
			case 16:
			case 22: // Атака петом
				if(target == null || !target.isCreature() || pet == target || pet.isDead())
				{
					activeChar.sendActionFailed();
					return;
				}

				if(activeChar.isInOlympiadMode() && !activeChar.isOlympiadCompStart())
				{
					activeChar.sendActionFailed();
					return;
				}

				// Sin Eater
				if(pet.getTemplate().getNpcId() == PetDataTable.SIN_EATER_ID)
					return;

				if(!_ctrlPressed && target.isCreature() && !((Creature) target).isAutoAttackable(pet))
				{
					pet.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, Config.FOLLOW_RANGE);
					return;
				}

				if(_ctrlPressed && !target.isAttackable(pet))
				{
					pet.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, Config.FOLLOW_RANGE);
					return;
				}

				/* TODO [4ipolino]: 
				if(!target.isMonster() && (pet.isInZonePeace() || target.isCreature() && ((Creature) target).isInZonePeace()))
				{
					activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
					return;
				}
				*/

				if(activeChar.getLevel() + 20 <= pet.getLevel())
				{
					activeChar.sendPacket(SystemMsg.YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
					return;
				}

				pet.getAI().Attack(target, _ctrlPressed, _shiftPressed);
				break;
			case 17:
			case 23: // Отмена действия у пета
				pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				break;
			case 19: // Отзыв пета
				if(pet.isDead())
				{
					activeChar.sendPacket(SystemMsg.DEAD_PETS_CANNOT_BE_RETURNED_TO_THEIR_SUMMONING_ITEM, ActionFail.STATIC);
					return;
				}

				if(pet.isInCombat() && !activeChar.isGM())
				{
					activeChar.sendPacket(SystemMsg.A_PET_CANNOT_BE_UNSUMMONED_DURING_BATTLE, ActionFail.STATIC);
					break;
				}

				if(!PetDataTable.isVitaminPet(pet.getNpcId()) && pet.isPet() && pet.getCurrentFed() < 0.55 * pet.getMaxFed())
				{
					activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_RESTORE_A_HUNGRY_PET, ActionFail.STATIC);
					break;
				}

				pet.unSummon();
				break;
			case 38: // Mount
				if(activeChar.getTransformation() != 0)
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(pet == null || !pet.isMountable())
				{
					if(activeChar.isMounted())
					{
						if(!activeChar.isGM() && activeChar.isFlying() && !activeChar.checkLandingState()) // Виверна
						{
							activeChar.sendPacket(SystemMsg.YOU_ARE_NOT_ALLOWED_TO_DISMOUNT_IN_THIS_LOCATION, ActionFail.STATIC);
							return;
						}
						activeChar.dismount();
					}
				}
				else if(activeChar.isMounted() || activeChar.isInBoat())
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isDead())
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(pet.isDead())
					activeChar.sendPacket(SystemMsg.A_DEAD_STRIDER_CANNOT_BE_RIDDEN);
				else if(activeChar.isInDuel())
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isInCombat() || pet.isInCombat())
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isFishing())
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isSitting())
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if (activeChar.isCursedWeaponEquipped())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.getActiveWeaponFlagAttachment() != null)
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isCastingNow())
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isParalyzed())
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else
				{
					activeChar.getEffectList().stopEffect(Skill.SKILL_EVENT_TIMER);
					activeChar.setMount(pet.getTemplate().npcId, pet.getObjectId(), pet.getLevel());
					pet.unSummon();
				}
				break;
			case 52: // Отзыв саммона
				if(pet.isInCombat())
				{
					activeChar.sendPacket(SystemMsg.A_PET_CANNOT_BE_UNSUMMONED_DURING_BATTLE);
					activeChar.sendActionFailed();
				}
				else
				{
					pet.saveEffects();
					pet.unSummon();
				}
				break;
			case 53:
			case 54: // Передвинуть пета к цели
				if(target != null && pet != target && !pet.isMovementDisabled())
				{
					pet.setFollowMode(false);
					pet.moveToLocation(target.getLoc(), 100, true);
				}
				break;
			case 1070:
				if(pet instanceof PetBabyInstance)
					((PetBabyInstance) pet).triggerBuff();
				break;
			case 67: // Steer. Allows you to control the Airship.
				if(activeChar.isInBoat() && activeChar.getBoat().isClanAirShip() && !activeChar.getBoat().isMoving())
				{
					ClanAirShip boat = (ClanAirShip) activeChar.getBoat();
					if(boat.getDriver() == null)
						boat.setDriver(activeChar);
					else
						activeChar.sendPacket(SystemMsg.ANOTHER_PLAYER_IS_PROBABLY_CONTROLLING_THE_TARGET);
				}
				break;
			case 68: // Cancel Control. Relinquishes control of the Airship.
				if(activeChar.isClanAirShipDriver())
				{
					ClanAirShip boat = (ClanAirShip) activeChar.getBoat();
					boat.setDriver(null);
					activeChar.broadcastCharInfo();
				}
				break;
			case 69: // Destination Map. Choose from pre-designated locations.
				if(activeChar.isClanAirShipDriver() && activeChar.getBoat().isDocked())
					activeChar.sendPacket(new ExAirShipTeleportList((ClanAirShip) activeChar.getBoat()));
				break;
			case 70: // Exit Airship. Disembarks from the Airship.
				if(activeChar.isInBoat() && activeChar.getBoat().isAirShip() && activeChar.getBoat().isDocked())
					activeChar.getBoat().oustPlayer(activeChar, activeChar.getBoat().getReturnLoc(), true);
				break;
			case 1001:
				break;
			default:
				activeChar.sendMessage("Unhandled action type " + _actionId);
				_log.warn("Unhandled action type " + _actionId + " by player " + activeChar.getName());
		}
		activeChar.sendActionFailed();
	}

	private void UseSkill(int skillId)
	{
		Player activeChar = getClient().getActiveChar();
		Summon pet = activeChar.getPet();
		if(pet == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		int skillLevel = PetSkillsTable.getInstance().getAvailableLevel(pet, skillId);
		if(skillLevel == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
		if(skill == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getLevel() + 20 <= pet.getLevel())
		{
			activeChar.sendPacket(SystemMsg.YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
			return;
		}

		Creature aimingTarget = skill.getAimingTarget(pet, activeChar.getTarget());
		if(skill.checkCondition(pet, aimingTarget, _ctrlPressed, _shiftPressed, true))
			pet.getAI().Cast(skill, aimingTarget, _ctrlPressed, _shiftPressed);
		else
			activeChar.sendActionFailed();
	}

	static class SocialTask extends RunnableImpl
	{
		Player _player;

		SocialTask(Player player)
		{
			_player = player;
		}

		@Override
		public void runImpl() throws Exception
		{
			_player.stopParalyzed();
		}
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return _actionId != 10 && _actionId != 28;
	}
}