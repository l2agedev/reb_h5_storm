/* L2J TO REBELLION */
/* 
 * GO TO OLD DATABASE, AND EXECUTE THE SCRIPT FROM THERE:
 * mysql> use l2agefortests;
 * mysql> source /home/freya/L2Age_rebellion1/L2J_TO_REBELLION_SQLS.sql
 * And, the script will start executing each query
*/
/* A BACKUP OF THE REBELLION CLEAN DATABASE IS PREPARED ACCORDING TO THIS FILE (empty, no need, bla bla, 6 Feb 2013) */
USE l2agefortests;

/* !!!!!! l2j - sms_system - we have not added this in rebellion  !!!!!! */

/* ACCOUNTS -> EMPTY */
INSERT INTO `l2age_rebellion`.`accounts` (`login`, `password`, `access_level`, `last_ip`)
SELECT `accounts`.`login`, `accounts`.`password`, `accounts`.`accessLevel`, `accounts`.`lastIP`
FROM `accounts`;
/* set last_server = 1 for all, else problems */
UPDATE `l2age_rebellion`.`accounts` SET `last_server` = '1';
/* Fix GM access levels :) */
UPDATE `l2age_rebellion`.`accounts` SET `access_level` = '127' WHERE `login` = 'corrupted1';
UPDATE `l2age_rebellion`.`accounts` SET `access_level` = '100' WHERE `login` = 'tomalie22';
UPDATE `l2age_rebellion`.`accounts` SET `access_level` = '127' WHERE `login` = 'nik';
UPDATE `l2age_rebellion`.`accounts` SET `access_level` = '100' WHERE `login` = 'a4o';
UPDATE `l2age_rebellion`.`accounts` SET `access_level` = '127' WHERE `login` = 'kinsi';
UPDATE `l2age_rebellion`.`accounts` SET `access_level` = '127' WHERE `login` = 'mag3t0';
UPDATE `l2age_rebellion`.`accounts` SET `access_level` = '100' WHERE `login` = 'mihail51722';
UPDATE `l2age_rebellion`.`accounts` SET `access_level` = '127' WHERE `login` = 'onepamopa';
/* END OF ACCOUNTS */

/* ACCOUNT_DATA -> EMPTY */
INSERT INTO `l2age_rebellion`.`account_data` (`account_name`, `var`, `value`)
SELECT `account_data`.`account_name`, `account_data`.`var`, `account_data`.`value`
FROM `account_data`;
/* END OF ACCOUNT_DATA */

/* ADD_SPAWNLIST - here are the GM spawns, DO NOT EMPTY THIS TABLE !!! */

/* ACCOUNT_LOG -> EMPTY */

/* ALLY_DATA -> EMPTY */
/* expelled_member - omited, crest - omited */
/* we use IGNORE, cause we get the data from clan_data and there are duplicates, we are not concerned about this cause we only get certain values */
/* ALLY ID = ID OF THE CLAN THAT CREATED THE ALLIANCE = CLAN_ID */
/*
Query OK, 242 rows affected (0.00 sec)
Records: 242  Duplicates: 0  Warnings: 0
*/
INSERT IGNORE INTO `l2age_rebellion`.`ally_data` (`ally_id`, `ally_name`, `leader_id`)
SELECT `clan_data`.`ally_id`, `clan_data`.`ally_name`, `clan_data`.`clan_id`
FROM `clan_data` WHERE `clan_data`.`ally_id` != '0' AND `clan_data`.`ally_id` = `clan_data`.`clan_id`;
/* END OF ALLY_DATA */

/* ARMOR */
/* TABLE NOT USED, ALL IS IN XML, TABLE IS DISABLED. */
/* END OF ARMOR */

/* BANS */
/* Nothing to do here. -> EMPTY */
/* END OF BANS */

/* BBS_BUFFS */
/* Table filled by default, do not empty. */
/* END OF BBS_BUFFS */

/* BBS_CLANNOTICE -> EMPTY */
/* SELECT FROM clan_notices.sql insert here */
INSERT INTO `l2age_rebellion`.`bbs_clannotice` (`clan_id`, `notice`)
SELECT `clan_notices`.`clan_id`, `clan_notices`.`notice`
FROM `clan_notices`;
/* type: 0 - disabled; 1 - enabled | 0 by default so.. */
/* Update type and set it accordingly */
UPDATE `l2age_rebellion`.`bbs_clannotice` AS a JOIN `l2agefortests`.`clan_notices` AS b ON (b.`clan_id` = a.`clan_id`) AND (b.enabled = true) SET a.`type` = '1';
/* END OF BBS_CLANNOTICE */

/* BBS_FAVORITES */
/* This table is empty by default. Nothing here. */
/* END OF BBS_FAVORITES */

/* BBS_MAIL -> EMPTY */
/* Some messages come into this table, I don't know which. Nothing to do here (for now?) */
/* END OF BBS_MAIL */

/* BBS_MEMO -> Empty by default */
/* nothing here  */
/* END OF BBS_MEMO */

/* BBS_POINTSAVE -> Empty by default */
/* nothing here */
/* END OF BBS_POINTSAVE */

/* BBS_SKILLSAVE -> Empty by default */
/* nothing here */
/* END OF BBS_SKILLSAVE */

/* BOT_REPORT -> Empty by default */
/* nothing here */
/* END OF BOT_REPORT */

/* BOT_REPORTED_PUNISH -> Empty by default */
/* nothing here */
/* END OF BOT_REPORTED_PUNISH */

/* CASTLE */
/* from castle.sql */
UPDATE `l2age_rebellion`.`castle` AS a JOIN `l2agefortests`.`castle` AS b ON (b.`id` = a.`id`) 
SET a.`tax_percent` = b.`taxPercent`, a.`treasury` = b.`treasury`, a.`siege_date` = b.`siegeDate`, a.`reward_count` = b.`bloodAlliance`, a.`last_siege_date` = '0', a.`own_date` = '0';
/* END OF CASTLE */

/* CASTLE_DAMAGE_ZONES -> Empty by default */
/* buy_trap, should be filled automaticly. */
/* END OF CASTLE_DAMAGE_ZONES */

/* CASTLE_DOOR_UPGRADE -> EMPTY */
/* NO pDef and mDef in rebellion */
/* Also, l2j table castle_doorupgrade is empty */
INSERT INTO `l2age_rebellion`.`castle_door_upgrade` (`door_id`, `hp`)
SELECT `castle_doorupgrade`.`doorId`, `castle_doorupgrade`.`hp`
FROM `castle_doorupgrade`;
/* END OF CASTLE_DOOR_UPGRADE */

/* CASTLE_HIRED_GUARDS -> Empty by default */
/* should be empty. */
/* END OF CASTLE_HIRED_GUARDS */

/* CASTLE_MANOR_PROCURE -> EMPTY */
INSERT INTO `l2age_rebellion`.`castle_manor_procure` (`castle_id`, `crop_id`, `can_buy`, `start_buy`, `price`, `reward_type`, `period`)
SELECT `castle_manor_procure`.`castle_id`, `castle_manor_procure`.`crop_id`, `castle_manor_procure`.`can_buy`, `castle_manor_procure`.`start_buy`, `castle_manor_procure`.`price`, `castle_manor_procure`.`reward_type`, `castle_manor_procure`.`period`
FROM `castle_manor_procure`;
/* END OF CASTLE_MANOR_PROCURE */

/* CASTLE_MANOR_PRODUCTION -> EMPTY */
INSERT INTO `l2age_rebellion`.`castle_manor_production` (`castle_id`, `seed_id`, `can_produce`, `start_produce`, `seed_price`, `period`)
SELECT `castle_manor_production`.`castle_id`, `castle_manor_production`.`seed_id`, `castle_manor_production`.`can_produce`, `castle_manor_production`.`start_produce`, `castle_manor_production`.`seed_price`, `castle_manor_production`.`period`
FROM `castle_manor_production`;
/* END OF CASTLE_MANOR_PRODUCTION */

/* CHARACTER_ACHIEVEMENT_LEVELS -> EMPTY */
/* for achievements, nothing to do here */
/* END OF CHARACTER_ACHIEVEMENT_LEVELS */

/* CHARACTER_BLOCKLIST -> EMPTY */
/* from character_friends | blocked = 1 */ /* Query 10 */
INSERT INTO `l2age_rebellion`.`character_blocklist` (`obj_Id`, `target_Id`)
SELECT `character_friends`.`charId`, `character_friends`.`friendId`
FROM `character_friends` WHERE `character_friends`.`relation` = '1';
/* END OF CHARACTER_BLOCKLIST */

/* CHARACTER_BOOKMARKS -> EMPTY */
/* FROM character_tpbookmark.sql */
INSERT INTO `l2age_rebellion`.`character_bookmarks` (`char_Id`, `idx`, `name`, `acronym`, `icon`, `x`, `y`, `z`)
SELECT `character_tpbookmark`.`charId`, `character_tpbookmark`.`Id`, `character_tpbookmark`.`name`, `character_tpbookmark`.`tag`, `character_tpbookmark`.`icon`, `character_tpbookmark`.`x`, `character_tpbookmark`.`y`, `character_tpbookmark`.`z`
FROM `character_tpbookmark`;
/* END OF CHARACTER_BOOKMARKS */

/* CHARACTER_COUNTERS -> EMPTY */
/* for achievements, nothing to do here */
/* END OF CHARACTER_COUNTERS */

/* CHARACTERS_EFFECTS_SAVE -> EMPTY */
/* leave empty */ 
/* END OF CHARACTERS_EFFECTS_SAVE */

/* CHARACTER_FRIENDS -> EMPTY */
INSERT INTO `l2age_rebellion`.`character_friends` (`char_id`, `friend_id`)
SELECT `character_friends`.`charId`, `character_friends`.`friendId`
FROM `character_friends`;
/* END OF CHARACTER_FRIENDS */

/* CHARACTER_GROUP_REUSE -> EMPTY */
/* leave empty */
/* END OF CHARACTER_GROUP_REUSE */

/* CHARACTER_INSTANCES -> EMPTY */
/* from character_instance_time.sql */
INSERT INTO `l2age_rebellion`.`character_instances` (`obj_id`, `id`, `reuse`)
SELECT `character_instance_time`.`charId`, `character_instance_time`.`instanceId`, `character_instance_time`.`time`
FROM `character_instance_time`;
/* END OF CHARACTER_INSTANCES */

/* CHARACTER_L2TOP_VOTES -> Empty by default */
/* nothing here */
/* END OF CHARACTER_L2TOP_VOTES */

/* CHARACTER_MACROSES -> EMPTY */
INSERT INTO `l2age_rebellion`.`character_macroses` (`char_obj_id`, `id`, `icon`, `name`, `descr`, `acronym`, `commands`)
SELECT `character_macroses`.`charId`, `character_macroses`.`id`, `character_macroses`.`icon`, `character_macroses`.`name`, `character_macroses`.`descr`, `character_macroses`.`acronym`, `character_macroses`.`commands`
FROM `character_macroses`;
/* END OF CHARACTER_MACROSES */

/* CHARACTER_MINIGAME_SCORE */
/* nothing here */
/* END OF CHARACTER_MINIGAME_SCORE */

/* CHARACTER_MMOTOP_VOTES */
/* nothing here */
/* END OF CHARACTER_MMOTOP_VOTES */

/* CHARACTER_POST_FRIENDS -> EMPTY */
/* leave empty*/ 
/* END OF CHARACTER_POST_FRIENDS */

/* CHARACTER_PREMIUM_ITEMS -> empty by default */
/* from character_premium_items.sql it is the same */
INSERT INTO `l2age_rebellion`.`character_premium_items` (`charId`, `itemNum`, `itemId`, `itemCount`, `itemSender`)
SELECT `character_premium_items`.`charId`, `character_premium_items`.`itemNum`, `character_premium_items`.`itemId`, `character_premium_items`.`itemCount`, `character_premium_items`.`itemSender`
FROM `character_premium_items`;
/* END OF CHARACTER_PREMIUM_ITEMS */

/* ======================================================= CHARACTER_QUESTS -> EMPTY ======================================================= */
/* TODO: MY IDEA IS TO INSERT ALL Q's THAT ARE COMPLETED, FIX THE NAMES */
/* My idea: Implementation: */
/* First, we insert ALL quests that are COMPLETED */
INSERT IGNORE INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `var` = '<state>' AND `value` = 'Completed';

/* Now, we delete * for quests that are unknown, or doesn't match with rebellion, will check them later.. */
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'GiftOfVitality';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '9002_SubClassCertification';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'OracleTeleport';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'RiftQuest';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'CharacterBirthday';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'PriestOfBlessing';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'HallowedYou';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'TeleportToFantasy';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'DelusionTeleport';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'North';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'South';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'TeleportToRaceTrack';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'Tower';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'Square';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'East';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'West';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'KetraOrcSupport';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '262_BringMeMushrooms1';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '289_NoMoreSoupForYou';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '662_AGameOfCards';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '170_DangerousAllure';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '4_LongLiveLordOfFlame';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'GrandBossTeleporters';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'VarkaSilenosSupport';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '169_NightmareChildren';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '3_ReleaseDarkelfElder1';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '353_PowerOfDarkness';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '292_CrushBrigands';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '370_AWisemanSowsSeeds';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'IOPRace';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '274_AgainstWolfMen';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '295_DreamsOfFlight';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '62_PathoftheTrooper';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '263_KillAllSylphs1';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '374_WhisperOfDreams1';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '151_SaveMySister1';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '261_DreamOfMoneylender1';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '165_WildHunt';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '102_FungusFever';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '65_CertifiedSoulBreaker';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '998_FallenAngelSelect';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '375_WhisperOfDreams2';

/* Also, delete quests that we will do later (make them complete, or they are identical, or etc etc, queries are already done for them */
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '115_TheOtherSideOfTruth';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '648_AnIceMerchantsDream';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '10283_RequestOfIceMerchant';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'Q10284_AcquisitionOfDivineSword';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'Q10285_MeetingSirra';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'Q10286_ReunionWithSirra';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'Q10287_StoryOfThoseLeft';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '119_LastImperialPrince';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '654_JourneytoaSettlement';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'Q00241_PossessorOfAPreciousSoul1';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'Q00242_PossessorOfAPreciousSoul2';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'Q00246_PossessorOfAPreciousSoul3';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'Q00247_PossessorOfAPreciousSoul4';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '131_BirdInACage';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '618_IntoTheFlame';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'Q254_LegendaryTales';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'Q456_DontKnowDontCare';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = 'Q130_PathToHellbound';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '422_RepentYourSins';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '10282_ToTheSeedOfAnnihilation';

/* Delete daily quests */
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_452_FindingtheLostSoldiers';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_453_NotStrongEnough';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_455_WingsofSand';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_456_DontKnowDontCare';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_463_IMustBeaGenius';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_551_OlympiadStarter';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_552_OlympiadVeteran';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_553_OlympiadUndefeated';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_902_ReclaimOurEra';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_903_TheCallofAntharas';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_905_RefinedDragonBlood';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_906_TheCallofValakas';
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_254_LegendaryTales';

/* Now, we update the Q-names for all remaining quests :P */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_385_YokeOfThePast' WHERE `name` = '385_YokeofthePast';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_634_InSearchofDimensionalFragments' WHERE `name` = '634_InSearchofDimensionalFragments';
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10282_ToTheSeedOfAnnihilation' WHERE `name` = '10282_ToTheSeedOfAnnihilation'; */
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10283_RequestOfIceMerchant' WHERE `name` = '10283_RequestOfIceMerchant'; */
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_115_TheOtherSideOfTruth' WHERE `name` = '115_TheOtherSideOfTruth'; */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_124_MeetingTheElroki' WHERE `name` = '124_MeetingTheElroki';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_125_InTheNameOfEvilPart1' WHERE `name` = '125_TheNameOfEvil1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_126_IntheNameofEvilPart2' WHERE `name` = '126_TheNameOfEvil2';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_344_1000YearsEndofLamentation' WHERE `name` = '344_1000YearsEndofLamentation';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_348_ArrogantSearch' WHERE `name` = '348_ArrogantSearch';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_350_EnhanceYourWeapon' WHERE `name` = '350_EnhanceYourWeapon';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_376_GiantsExploration1' WHERE `name` = '376_ExplorationOfTheGiantsCavePart1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_377_GiantsExploration2' WHERE `name` = '377_ExplorationOfTheGiantsCavePart2';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_453_NotStrongEnough' WHERE `name` = '453_NotStrongEnoughAlone';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_620_FourGoblets' WHERE `name` = '620_FourGoblets';
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_648_AnIceMerchantsDream' WHERE `name` = '648_AnIceMerchantsDream'; */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_734_PierceThroughAShield' WHERE `name` = '734_Piercethroughashield';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_735_MakeSpearsDull' WHERE `name` = '735_Makespearsdull';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_736_WeakenTheMagic' WHERE `name` = '736_Weakenmagic';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_737_DenyBlessings' WHERE `name` = '737_DenyBlessings';
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_241_PossessorOfaPreciousSoul1' WHERE `name` = 'Q00241_PossessorOfAPreciousSoul1'; */
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_242_PossessorOfaPreciousSoul2' WHERE `name` = 'Q00242_PossessorOfAPreciousSoul2'; */
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_246_PossessorOfaPreciousSoul3' WHERE `name` = 'Q00246_PossessorOfAPreciousSoul3'; */
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_247_PossessorOfaPreciousSoul4' WHERE `name` = 'Q00247_PossessorOfAPreciousSoul4'; */
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10284_AcquisionOfDivineSword' WHERE `name` = 'Q10284_AcquisitionOfDivineSword'; */
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10285_MeetingSirra' WHERE `name` = 'Q10285_MeetingSirra'; */
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10286_ReunionWithSirra' WHERE `name` = 'Q10286_ReunionWithSirra'; */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10292_SevenSignsGirlOfDoubt' WHERE `name` = 'Q10292_SevenSignsGirlofDoubt';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10293_SevenSignsForbiddenBook' WHERE `name` = 'Q10293_SevenSignsForbiddenBook';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10294_SevenSignsMonasteryofSilence' WHERE `name` = 'Q10294_SevenSignToTheMonastery';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10295_SevenSignsSolinasTomb' WHERE `name` = 'Q10295_SevenSignSolinasTomb';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10296_SevenSignsPoweroftheSeal' WHERE `name` = 'Q10296_SevenSignOneWhoSeeksPower';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10501_CapeEmbroideredSoulOne' WHERE `name` = 'Q10501_ZakenEmbroideredSoulCloak';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10502_CapeEmbroideredSoulTwo' WHERE `name` = 'Q10502_FreyaEmbroideredSoulCloak';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10503_CapeEmbroideredSoulThree' WHERE `name` = 'Q10503_FrintezzaEmbroideredSoulCloak';
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_130_PathToHellbound' WHERE `name` = 'Q130_PathToHellbound'; */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_192_SevenSignSeriesOfDoubt' WHERE `name` = 'Q192_SevenSignSeriesOfDoubt';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_193_SevenSignDyingMessage' WHERE `name` = 'Q193_SevenSignDyingMessage';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_194_SevenSignsMammonsContract' WHERE `name` = 'Q194_SevenSignContractOfMammon';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_195_SevenSignsSecretRitualofthePriests' WHERE `name` = 'Q195_SevenSingSecretRitualOfThePriests';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_196_SevenSignsSealoftheEmperor' WHERE `name` = 'Q196_SevenSignSealOfTheEmperor';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_197_SevenSignsTheSacredBookofSeal' WHERE `name` = 'Q197_SevenSignTheSacredBookOfSeal';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_198_SevenSignsEmbryo' WHERE `name` = 'Q198_SevenSignEmbryo';
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_254_LegendaryTales' WHERE `name` = 'Q254_LegendaryTales'; */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_455_WingsofSand' WHERE `name` = 'Q455_WingsOfSand';
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_456_DontKnowDontCare' WHERE `name` = 'Q456_DontKnowDontCare'; */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_694_BreakThroughTheHallOfSuffering' WHERE `name` = 'Q694_BreakThroughTheHallOfSuffering';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_905_RefinedDragonBlood' WHERE `name` = 'Q905_RefinedDragonBlood';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_617_GatherTheFlames' WHERE `name` = '617_GatherTheFlames';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_643_RiseAndFallOfTheElrokiTribe' WHERE `name` = '643_RiseandFalloftheElrokiTribe';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_642_APowerfulPrimevalCreature' WHERE `name` = 'Q642_APowerfulPrimevalCreature';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_109_InSearchOfTheNest' WHERE `name` = '109_InSearchOfTheNest';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_129_PailakaDevilsLegacy' WHERE `name` = '129_PailakaDevilsLegacy';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_363_SorrowfulSoundofFlute' WHERE `name` = '363_SorrowfulSoundofFlute';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_628_HuntGoldenRam' WHERE `name` = '628_HuntGoldenRam';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_629_CleanUpTheSwampOfScreams' WHERE `name` = '629_CleanUpTheSwampOfScreams';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_311_ExpulsionOfEvilSpirits' WHERE `name` = '311_ExpulsionOfEvilSpirits';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_235_MimirsElixir' WHERE `name` = '235_MimirsElixir';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_738_DestroyKeyTargets' WHERE `name` = '738_DestroyKeyTargets';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_038_DragonFangs' WHERE `name` = '38_DragonFangs';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_310_OnlyWhatRemains' WHERE `name` = '310_OnlyWhatRemains';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_619_RelicsOfTheOldEmpire' WHERE `name` = '619_RelicsOfTheOldEmpire';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_240_ImTheOnlyOneYouCanTrust' WHERE `name` = 'Q00240_ImTheOnlyOneYouCanTrust';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_147_PathToBecomingAnEliteMercenary' WHERE `name` = '147_PathtoBecominganEliteMercenary';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_026_TiredOfWaiting' WHERE `name` = '26_TiredOfWaiting';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_312_TakeAdvantageOfTheCrisis' WHERE `name` = '312_TakeAdvantageOfTheCrisis';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_240_ImTheOnlyOneYouCanTrust' WHERE `name` = '240_ImTheOnlyOneYouCanTrust';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_288_HandleWithCare' WHERE `name` = '288_HandleWithCare';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_309_ForAGoodCause' WHERE `name` = '309_ForAGoodCause';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_419_GetaPet' WHERE `name` = '419_GetAPet';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_128_PailakaSongofIceandFire' WHERE `name` = '128_PailakaSongOfIceAndFire';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_663_SeductiveWhispers' WHERE `name` = '663_SeductiveWhispers';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_503_PursuitClanAmbition' WHERE `name` = '503_PursuitClanAmbition';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_136_MoreThanMeetsTheEye' WHERE `name` = '136_MoreThanMeetsTheEye';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_144_PailakaInjuredDragon' WHERE `name` = '144_PailakaInjuredDragon';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_251_NoSecrets' WHERE `name` = '251_NoSecrets';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_290_ThreatRemoval' WHERE `name` = '290_ThreatRemoval';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_688_DefeatTheElrokianRaiders' WHERE `name` = '688_DefeatTheElrokianRaiders';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_132_MatrasCuriosity' WHERE `name` = '132_MatrasCuriosity';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_032_AnObviousLie' WHERE `name` = '32_AnObviousLie';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_690_JudesRequest' WHERE `name` = '690_JudesRequest';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_691_MatrasSuspiciousRequest' WHERE `name` = '691_MatrasSuspiciousRequest';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_279_TargetOfOpportunity' WHERE `name` = '279_TargetOfOpportunity';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_293_HiddenVein' WHERE `name` = '293_HiddenVein';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_183_RelicExploration' WHERE `name` = '183_Relic_Exploration';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_451_LuciensAltar' WHERE `name` = '451_LuciensAltar';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_551_OlympiadStarter' WHERE `name` = '551_OlympiadStarter';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_552_OlympiadVeteran' WHERE `name` = '552_OlympiadVeteran';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_553_OlympiadUndefeated' WHERE `name` = '553_OlympiadUndefeated';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10273_GoodDayToFly' WHERE `name` = '10273_GoodDayToFly';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_307_ControlDeviceoftheGiants' WHERE `name` = '307_ControlDeviceOfTheGiants';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_337_AudienceWithLandDragon' WHERE `name` = '337_AudienceWithTheLandDragon';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_426_QuestforFishingShot' WHERE `name` = '426_FishingShot';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_501_ProofOfClanAlliance' WHERE `name` = '501_ProofOfClanAlliance';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_020_BringUpWithLove' WHERE `name` = '20_BringUpWithLove';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_278_HomeSecurity' WHERE `name` = '278_HomeSecurity';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_631_DeliciousTopChoiceMeat' WHERE `name` = 'Q631_DeliciousTopChoiceMeat';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_359_ForSleeplessDeadmen' WHERE `name` = '359_ForSleeplessDeadmen';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_217_TestimonyOfTrust' WHERE `name` = '217_TestimonyOfTrust';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_111_ElrokianHuntersProof' WHERE `name` = '111_Elrokian_Hunters_Proof';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_308_ReedFieldMaintenance' WHERE `name` = '308_ReedFieldMaintenance';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_721_ForTheSakeOfTheTerritoryAden' WHERE `name` = '721_FortheSakeoftheTerritoryAden';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_627_HeartInSearchOfPower' WHERE `name` = '627_HeartInSearchOfPower';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_638_SeekersOfTheHolyGrail' WHERE `name` = '638_SeekersOfTheHolyGrail';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_252_GoodSmell' WHERE `name` = '252_ItSmellsDelicious';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_373_SupplierOfReagents' WHERE `name` = '373_SupplierOfReagents';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_176_StepsForHonor' WHERE `name` = '176_StepsForHonor';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_902_ReclaimOurEra' WHERE `name` = 'Q902_ReclaimOurEra';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_237_WindsOfChange' WHERE `name` = '237_WindsOfChange';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_238_SuccessFailureOfBusiness' WHERE `name` = '238_SuccesFailureOfBusiness';
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_618_IntoTheFlame' WHERE `name` = '618_IntoTheFlame'; */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10269_ToTheSeedOfDestruction' WHERE `name` = '10269_ToTheSeedOfDestruction';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_511_AwlUnderFoot' WHERE `name` = '511_AwlUnderFoot';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_512_AwlUnderFoot' WHERE `name` = 'Q512_BladeUnderFoot';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10288_SecretMission' WHERE `name` = '10288_SecretMission';
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_119_LastImperialPrince' WHERE `name` = '119_LastImperialPrince'; */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_722_ForTheSakeOfTheTerritoryInnadril' WHERE `name` = '722_FortheSakeoftheTerritoryInnadril';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_723_ForTheSakeOfTheTerritoryGoddard' WHERE `name` = '723_FortheSakeoftheTerritoryGoddard';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_724_ForTheSakeOfTheTerritoryRune' WHERE `name` = '724_FortheSakeoftheTerritoryRune';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_725_ForTheSakeOfTheTerritoryShuttdart' WHERE `name` = '725_FortheSakeoftheTerritorySchuttgart';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_360_PlunderTheirSupplies' WHERE `name` = '360_PlunderTheirSupplies';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_358_IllegitimateChildOfAGoddess' WHERE `name` = '358_IllegitimateChildOfAGoddess';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_719_ForTheSakeOfTheTerritoryGiran' WHERE `name` = '719_FortheSakeoftheTerritoryGiran';
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_131_BirdInACage' WHERE `name` = '131_BirdInACage'; */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_328_SenseForBusiness' WHERE `name` = '328_SenseForBusiness';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_452_FindingtheLostSoldiers' WHERE `name` = '452_FindingtheLostSoldiers';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_692_HowtoOpposeEvil' WHERE `name` = '692_HowtoOpposeEvil';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_112_WalkOfFate' WHERE `name` = '112_WalkOfFate';
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_422_RepentYourSins' WHERE `name` = '422_RepentYourSins'; */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_647_InfluxOfMachines' WHERE `name` = '647_InfluxOfMachines';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_164_BloodFiend' WHERE `name` = '164_BloodFiend';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_250_WatchWhatYouEat' WHERE `name` = '250_WatchWhatYouEat';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_287_FiguringItOut' WHERE `name` = '287_FiguringItOut';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_122_OminousNews' WHERE `name` = 'Q00122_OminousNews';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_624_TheFinestIngredientsPart1' WHERE `name` = '624_TheFinestIngredientsPart1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_639_GuardiansOfTheHolyGrail' WHERE `name` = '639_GuardiansOfTheHolyGrail';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_167_DwarvenKinship' WHERE `name` = '167_DwarvenKinship';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_420_LittleWings' WHERE `name` = '420_LittleWings';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_212_TrialOfDuty' WHERE `name` = '212_TrialOfDuty';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_040_ASpecialOrder' WHERE `name` = '40_ASpecialOrder';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_213_TrialOfSeeker' WHERE `name` = '213_TrialOfSeeker';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_022_TragedyInVonHellmannForest' WHERE `name` = '22_TragedyInVonHellmannForest';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_645_GhostsOfBatur' WHERE `name` = '645_GhostsOfBatur';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10279_MutatedKaneusOren' WHERE `name` = '10279_MutatedKaneusOren';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_249_PoisonedPlainsOfTheLizardmen' WHERE `name` = '249_PoisonedPlainsOfTheLizardmen';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_351_BlackSwan' WHERE `name` = '351_BlackSwan';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10268_ToTheSeedOfInfinity' WHERE `name` = '10268_ToTheSeedOfInfinity';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10270_BirthOfTheSeed' WHERE `name` = '10270_BirthOfTheSeed';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10274_CollectingInTheAir' WHERE `name` = '10274_CollectingInTheAir';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10281_MutatedKaneusRune' WHERE `name` = '10281_MutatedKaneusRune';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10291_FireDragonDestroyer' WHERE `name` = '10291_FireDragonDestroyer';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_699_GuardianoftheSkies' WHERE `name` = '699_GuardianOfTheSkies';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_605_AllianceWithKetraOrcs' WHERE `name` = '605_AllianceWithKetraOrcs';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_219_TestimonyOfFate' WHERE `name` = '219_TestimonyOfFate';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_044_HelpTheSon' WHERE `name` = 'Q00044_HelpTheSon';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10289_FadeToBlack' WHERE `name` = '10289_FadeToBlack';
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_654_JourneytoaSettlement' WHERE `name` = '654_JourneytoaSettlement'; */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_450_GraveRobberMemberRescue' WHERE `name` = '450_GraveRobberMemberRescue';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_644_GraveRobberAnnihilation' WHERE `name` = '644_GraveRobberAnnihilation';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_230_TestOfSummoner' WHERE `name` = '230_TestOfSummoner';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_329_CuriosityOfDwarf' WHERE `name` = '329_CuriosityOfDwarf';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_336_CoinOfMagic' WHERE `name` = '336_CoinOfMagic';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_366_SilverHairedShaman' WHERE `name` = '366_SilverHairedShaman';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_234_FatesWhisper' WHERE `name` = '234_FatesWhisper';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_623_TheFinestFood' WHERE `name` = '623_TheFinestFood';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_146_TheZeroHour' WHERE `name` = '146_TheZeroHour';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_031_SecretBuriedInTheSwamp' WHERE `name` = '31_SecretBuriedInTheSwamp';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10275_ContainingTheAttributePower' WHERE `name` = '10275_ContainingTheAttributePower';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_411_PathToAssassin' WHERE `name` = '411_PathToAssassin';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_037_PleaseMakeMeFormalWear' WHERE `name` = '37_PleaseMakeMeFormalWear';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_113_StatusOfTheBeaconTower' WHERE `name` = '113_StatusOfTheBeaconTower';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_064_CertifiedBerserker' WHERE `name` = '64_CertifiedBerserker';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_362_BardsMandolin' WHERE `name` = '362_BardsMandolin';
/* UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10287_StoryOfThoseLeft' WHERE `name` = 'Q10287_StoryOfThoseLeft'; */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_185_NikolasCooperationConsideration' WHERE `name` = '185_Nikolas_Cooperation_Consideration';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_187_NikolasHeart' WHERE `name` = '187_Nikolas_Heart';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_190_LostDream' WHERE `name` = '190_Lost_Dream';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10267_JourneyToGracia' WHERE `name` = '10267_JourneyToGracia';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_611_AllianceWithVarkaSilenos' WHERE `name` = '611_AllianceWithVarkaSilenos';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_615_MagicalPowerofFire1' WHERE `name` = '615_MagicalPowerOfFirePart1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_720_ForTheSakeOfTheTerritoryOren' WHERE `name` = '720_FortheSakeoftheTerritoryOren';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10271_TheEnvelopingDarkness' WHERE `name` = '10271_TheEnvelopingDarkness';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_700_CursedLife' WHERE `name` = '700_CursedLife';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_702_ATrapForRevenge' WHERE `name` = '702_ATrapForRevenge';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_384_WarehouseKeepersPastime' WHERE `name` = '384_WarehouseKeepersPastime';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_281_HeadForTheHills' WHERE `name` = '281_HeadForTheHills';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_174_SupplyCheck' WHERE `name` = '174_SupplyCheck';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_033_MakeAPairOfDressShoes' WHERE `name` = '33_MakeAPairOfDressShoes';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_034_InSearchOfClothes' WHERE `name` = '34_InSearchOfClothes';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_035_FindGlitteringJewelry' WHERE `name` = '35_FindGlitteringJewelry';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_114_ResurrectionOfAnOldManager' WHERE `name` = '114_ResurrectionOfAnOldManager';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_121_PavelTheGiants' WHERE `name` = 'Q00121_PavelTheGiant';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_320_BonesTellFuture' WHERE `name` = '320_BonesTellFuture';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_386_StolenDignity' WHERE `name` = '386_StolenDignity';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_423_TakeYourBestShot' WHERE `name` = '423_TakeYourBestShot';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_045_ToTalkingIsland' WHERE `name` = '45_ToTalkingIsland';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_036_MakeASewingKit' WHERE `name` = '36_MakeASewingKit';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_184_NikolasCooperationContract' WHERE `name` = '184_Nikolas_Cooperation_Contract';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_186_ContractExecution' WHERE `name` = '186_Contract_Execution';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_116_BeyondtheHillsofWinter' WHERE `name` = '116_BeyondTheHillsOfWinter';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_297_GateKeepersFavor' WHERE `name` = '297_GatekeepersFavor';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10290_LandDragonConqueror' WHERE `name` = '10290_LandDragonConqueror';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_015_SweetWhispers' WHERE `name` = '15_SweetWhispers';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_019_GoToThePastureland' WHERE `name` = '19_GoToThePastureland';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_661_TheHarvestGroundsSafe' WHERE `name` = '661_TheHarvestGroundsSafe';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10278_MutatedKaneusHeine' WHERE `name` = '10278_MutatedKaneusHeine';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_189_ContractCompletion' WHERE `name` = '189_Contract_Completion';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_906_TheCallofValakas' WHERE `name` = '906_TheCallOfValakas';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_042_HelpTheUncle' WHERE `name` = 'Q00042_HelpTheUncle';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_014_WhereaboutsoftheArchaeologist' WHERE `name` = '14_WhereaboutsOfTheArchaeologist';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_087_SagaOfEvasSaint' WHERE `name` = '87_SagaOfEvasSaint';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_106_ForgottenTruth' WHERE `name` = '106_ForgottenTruth';
/* We leave the above one, no need to explain why */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '247_PossessorOfAPreciousSoul_3' WHERE `name` = '_246_PossessorOfaPreciousSoul3';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_175_TheWayOfTheWarrior' WHERE `name` = '175_TheWayOfTheWarrior';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_178_IconicTrinity' WHERE `name` = '178_IconicTrinity';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_236_SeedsOfChaos' WHERE `name` = '236_SeedsOfChaos';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_331_ArrowForVengeance' WHERE `name` = '331_ArrowForVengeance';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_701_ProofofExistence' WHERE `name` = '701_Proof_Of_Existence';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_606_WarwithVarkaSilenos' WHERE `name` = '606_WarWithVarkaSilenos';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_652_AnAgedExAdventurer' WHERE `name` = '652_AnAgedExAdventurer';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_372_LegacyOfInsolence' WHERE `name` = '372_LegacyOfInsolence';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_621_EggDelivery' WHERE `name` = '621_EggDelivery';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_622_DeliveryofSpecialLiquor' WHERE `name` = '622_DeliveryOfSpecialLiquor';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_021_HiddenTruth' WHERE `name` = '21_HiddenTruth';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_023_LidiasHeart' WHERE `name` = '23_LidiasHeart';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_601_WatchingEyes' WHERE `name` = '601_WatchingEyes';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_409_PathToOracle' WHERE `name` = '409_PathToOracle';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10276_MutatedKaneusGludio' WHERE `name` = '10276_MutatedKaneusGludio';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_110_ToThePrimevalIsle' WHERE `name` = '110_ToThePrimevalIsle';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_013_ParcelDelivery' WHERE `name` = '13_ParcelDelivery';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_018_MeetingwiththeGoldenRam' WHERE `name` = '18_MeetingWithTheGoldenRam';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_604_DaimontheWhiteEyedPart2' WHERE `name` = '604_DaimontheWhiteEyedPart2';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_625_TheFinestIngredientsPart2' WHERE `name` = '625_TheFinestIngredientsPart2';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_603_DaimontheWhiteEyedPart1' WHERE `name` = '603_DaimontheWhiteEyedPart1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_161_FruitsOfMothertree' WHERE `name` = '161_FruitsOfMothertree';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_007_ATripBegins' WHERE `name` = '7_ATripBegins';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_717_ForTheSakeOfTheTerritoryGludio' WHERE `name` = '717_FortheSakeoftheTerritoryGludio';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_508_TheClansReputation' WHERE `name` = '508_AClansReputation';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_308_ReedFieldMaintenance' WHERE `name` = 'Q308_ReedFieldMaintenance';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_636_TruthBeyond' WHERE `name` = '636_TruthBeyond';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_354_ConquestofAlligatorIsland' WHERE `name` = '354_ConquestOfAlligatorIsland';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_232_TestOfLord' WHERE `name` = '232_TestOfLord';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_051_OFullesSpecialBait' WHERE `name` = '51_OFullesSpecialBait';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_610_MagicalPowerofWater2' WHERE `name` = '610_MagicalPowerOfWaterPart2';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_225_TestOfTheSearcher' WHERE `name` = '225_TestOfSearcher';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_073_SagaOfTheDuelist' WHERE `name` = '73_SagaOfTheDuelist';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_265_ChainsOfSlavery' WHERE `name` = '265_ChainsOfSlavery';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_272_WrathOfAncestors' WHERE `name` = '272_WrathOfAncestors';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_463_IMustBeaGenius' WHERE `name` = '463_IMustBeaGenius';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_163_LegacyOfPoet' WHERE `name` = '163_LegacyOfPoet';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_298_LizardmensConspiracy' WHERE `name` = '298_LizardmensConspiracy';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_404_PathToWizard' WHERE `name` = '404_PathToWizard';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_403_Rogue' WHERE `name` = '403_PathToRogue';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_294_CovertBusiness' WHERE `name` = '294_CovertBusiness';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_277_GatekeepersOffering' WHERE `name` = '277_GatekeepersOffering';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_347_GoGetTheCalculator' WHERE `name` = '347_GoGetTheCalculator';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_215_TrialOfPilgrim' WHERE `name` = '215_TrialOfPilgrim';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_260_HuntTheOrcs' WHERE `name` = '260_HuntForOrcs1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_365_DevilsLegacy' WHERE `name` = '365_DevilsLegacy';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_718_ForTheSakeOfTheTerritoryDion' WHERE `name` = '718_FortheSakeoftheTerritoryDion';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_006_StepIntoTheFuture' WHERE `name` = '6_StepIntoTheFuture';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_118_ToLeadAndBeLed' WHERE `name` = '118_ToLeadAndBeLed';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_166_DarkMass' WHERE `name` = '166_DarkMass';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_326_VanquishRemnants' WHERE `name` = '326_VanquishRemnants';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_602_ShadowofLight' WHERE `name` = '602_ShadowOfLight';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_159_ProtectHeadsprings' WHERE `name` = '159_ProtectHeadsprings';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_317_CatchTheWind' WHERE `name` = '317_CatchTheWind';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_352_HelpRoodRaiseANewPet' WHERE `name` = '352_HelpRoodRaiseANewPet';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_369_CollectorOfJewels' WHERE `name` = '369_CollectorOfJewels';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_223_TestOfChampion' WHERE `name` = '223_TestOfChampion';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_133_ThatsBloodyHot' WHERE `name` = '133_ThatsBloodyHot';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_412_PathToDarkwizard' WHERE `name` = '412_PathToDarkwizard';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_633_InTheForgottenVillage' WHERE `name` = '633_InTheForgottenVillage';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_222_TestOfDuelist' WHERE `name` = '222_TestOfDuelist';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_316_DestroyPlaguebringers' WHERE `name` = '316_DestroyPlaguebringers';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_105_SkirmishWithOrcs' WHERE `name` = '105_SkirmishWithOrcs';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_239_WontYouJoinUs' WHERE `name` = '239_WontYouJoinUs';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_357_WarehouseKeepersAmbition' WHERE `name` = '357_WarehouseKeepersAmbition';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_903_TheCallofAntharas' WHERE `name` = 'Q903_TheCallOfAntharas';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_637_ThroughOnceMore' WHERE `name` = '637_ThroughOnceMore';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_405_PathToCleric' WHERE `name` = '405_PathToCleric';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_257_GuardIsBusy' WHERE `name` = '257_GuardIsBusy1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_267_WrathOfVerdure' WHERE `name` = '267_WrathOfVerdure';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_001_LettersOfLove' WHERE `name` = '1_LettersOfLove1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_172_NewHorizons' WHERE `name` = '172_NewHorizons';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_291_RevengeOfTheRedbonnet' WHERE `name` = '291_RedBonnetsRevenge';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_343_UndertheShadowoftheIvoryTower' WHERE `name` = '343_UnderTheShadowOfTheIvoryTower';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_334_TheWishingPotion' WHERE `name` = '334_TheWishingPotion';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_345_MethodToRaiseTheDead' WHERE `name` = '345_MethodToRaiseTheDead';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_221_TestimonyOfProsperity' WHERE `name` = '221_TestimonyOfProsperity';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_220_TestimonyOfGlory' WHERE `name` = '220_TestimonyOfGlory';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_046_OnceMoreInTheArmsOfTheMotherTree' WHERE `name` = '46_OnceMoreInTheArmsOfTheMotherTree';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_048_ToTheImmortalPlateau' WHERE `name` = '48_ToTheImmortalPlateau';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_421_LittleWingAdventures' WHERE `name` = '421_LittleWingAdventures';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_431_WeddingMarch' WHERE `name` = '431_WeddingMarch';
/* We leave the above Q, no need to ask why. */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_241_PossessorOfaPreciousSoul1' WHERE `name` = 'Q241_PossessorOfAPreciousSoul1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_090_SagaOfTheStormScreamer' WHERE `name` = '90_SagaOfTheStormScreamer';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_296_SilkOfTarantula' WHERE `name` = '296_SilkOfTarantula';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_508_TheClansReputation' WHERE `name` = '508_TheClansReputation';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_510_AClansReputation' WHERE `name` = '510_AClansReputation';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_509_TheClansPrestige' WHERE `name` = '510_AClansPrestige';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10277_MutatedKaneusDion' WHERE `name` = '10277_MutatedKaneusDion';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_134_TempleMissionary' WHERE `name` = '134_TempleMissionary';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_135_TempleExecutor' WHERE `name` = '135_TempleExecutor';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_011_SecretMeetingWithKetraOrcs' WHERE `name` = '11_SecretMeetingWithKetraOrcs';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_012_SecretMeetingWithVarkaSilenos' WHERE `name` = '12_SecretMeetingWithVarkaSilenos';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_613_ProveYourCourage' WHERE `name` = '613_ProveYourCourage_Varka';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_008_AnAdventureBegins' WHERE `name` = '8_AnAdventureBegins';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_364_JovialAccordion' WHERE `name` = '364_JovialAccordion';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_047_IntoTheDarkForest' WHERE `name` = '47_IntoTheDarkForest';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_356_DigUpTheSeaOfSpores' WHERE `name` = '356_DigUpTheSeaOfSpores';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_367_ElectrifyingRecharge' WHERE `name` = '367_ElectrifyingRecharge';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_402_PathToKnight' WHERE `name` = '402_PathToKnight';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_330_AdeptOfTaste' WHERE `name` = '330_AdeptOfTaste';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_641_AttackSailren' WHERE `name` = '641_AttackSailren';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_612_WarwithKetraOrcs' WHERE `name` = '612_WarWithKetraOrcs';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_154_SacrificeToSea' WHERE `name` = '154_SacrificeToSea';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_117_OceanOfDistantStar' WHERE `name` = '117_OceanOfDistantStar';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_609_MagicalPowerofWater1' WHERE `name` = '609_MagicalPowerOfWaterPart1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_632_NecromancersRequest' WHERE `name` = '632_NecromancersRequest';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_659_IdRatherBeCollectingFairyBreath' WHERE `name` = '659_IdRatherBeCollectingFairyBreath';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_005_MinersFavor' WHERE `name` = '5_MinersFavor';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_155_FindSirWindawood' WHERE `name` = '155_FindSirWindawood';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_266_PleaOfPixies' WHERE `name` = '266_PleaOfPixies';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_152_ShardsOfGolem' WHERE `name` = '152_ShardsOfGolem';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_158_SeedOfEvil' WHERE `name` = '158_SeedOfEvil';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_306_CrystalOfFireice' WHERE `name` = '306_CrystalOfFireice';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_418_PathToArtisan' WHERE `name` = '418_PathToArtisan';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10280_MutatedKaneusSchuttgart' WHERE `name` = '10280_MutatedKaneusSchuttgart';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_335_TheSongOfTheHunter' WHERE `name` = '335_TheSongOfTheHunter';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_407_PathToElvenScout' WHERE `name` = '407_PathToElvenScout';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_413_PathToShillienOracle' WHERE `name` = '413_PathToShillienOracle';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_043_HelpTheSister' WHERE `name` = 'Q00043_HelpTheSister';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_010_IntoTheWorld' WHERE `name` = '10_IntoTheWorld';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_410_PathToPalusKnight' WHERE `name` = '410_PathToPalusKnight';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_103_SpiritOfCraftsman' WHERE `name` = '103_SpiritOfCraftsman';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_338_AlligatorHunter' WHERE `name` = '338_AlligatorHunter';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_303_CollectArrowheads' WHERE `name` = '303_CollectArrowheads';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_168_DeliverSupplies' WHERE `name` = '168_DeliverSupplies';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_284_MuertosFeather' WHERE `name` = '284_MuertosFeather';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_094_SagaOfTheSoultaker' WHERE `name` = '94_SagaOfTheSoultaker';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_229_TestOfWitchcraft' WHERE `name` = '229_TestOfWitchcraft';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_379_FantasyWine' WHERE `name` = '379_FantasyWine';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_108_JumbleTumbleDiamondFuss' WHERE `name` = '108_JumbleTumbleDiamondFuss';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_319_ScentOfDeath' WHERE `name` = '319_ScentOfDeath';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_009_IntoTheCityOfHumans' WHERE `name` = '9_IntoTheCityOfHumans';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_280_TheFoodChain' WHERE `name` = '280_TheFoodChain';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_107_MercilessPunishment' WHERE `name` = '107_MercilessPunishment';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_417_PathToScavenger' WHERE `name` = '417_PathToScavenger';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_039_RedEyedInvaders' WHERE `name` = '39_RedEyedInvaders';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_049_TheRoadHome' WHERE `name` = '49_TheRoadHome';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_050_LanoscosSpecialBait' WHERE `name` = '50_LanoscosSpecialBait';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_138_TempleChampionPart2' WHERE `name` = '138_TempleChampionPart2';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_378_MagnificentFeast' WHERE `name` = '378_MagnificentFeast';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_067_SagaOfTheDoombringer' WHERE `name` = '67_SagaOfTheDoombringer';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_162_CurseOfUndergroundFortress' WHERE `name` = '162_CurseOfFortress';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_415_PathToOrcMonk' WHERE `name` = '415_PathToOrcMonk';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_355_FamilyHonor' WHERE `name` = '355_FamilyHonor';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_264_KeenClaws' WHERE `name` = '264_KeenClaws';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_401_PathToWarrior' WHERE `name` = '401_PathToWarrior';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_179_IntoTheLargeCavern' WHERE `name` = '179_IntoTheLargeCavern';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_275_BlackWingedSpies' WHERE `name` = '275_BlackWingedSpies';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_626_ADarkTwilight' WHERE `name` = '626_ADarkTwilight';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_028_ChestCaughtWithABaitOfIcyAir' WHERE `name` = '28_ChestCaughtWithABaitOfIcyAir';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_002_WhatWomenWant' WHERE `name` = '2_WhatWomenWant1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_313_CollectSpores' WHERE `name` = '313_CollectSpores';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_408_PathToElvenwizard' WHERE `name` = '408_PathToElvenwizard';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_060_GoodWorksReward' WHERE `name` = '60_GoodWorkReward';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_075_SagaOfTheTitan' WHERE `name` = '75_SagaOfTheTitan';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_093_SagaOfTheSpectralMaster' WHERE `name` = '93_SagaOfTheSpectralMaster';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_156_MillenniumLove' WHERE `name` = '156_MillenniumLove';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_016_TheComingDarkness' WHERE `name` = '16_TheComingDarkness';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_082_SagaOfTheSagittarius' WHERE `name` = '82_SagaOfTheSagittarius';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_414_PathToOrcRaider' WHERE `name` = '414_PathToOrcRaider';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_088_SagaOfTheArchmage' WHERE `name` = '88_SagaOfTheArchmage';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_269_InventionAmbition' WHERE `name` = '269_InventionAmbition';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_053_LinnaeusSpecialBait' WHERE `name` = '53_LinnaeusSpecialBait';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_099_SagaOfTheFortuneSeeker' WHERE `name` = '99_SagaOfTheFortuneSeeker';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_660_AidingtheFloranVillage' WHERE `name` = '660_AidingtheFloranVillage';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_300_HuntingLetoLizardman' WHERE `name` = '300_HuntingLetoLizardman';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_063_PathToWarder' WHERE `name` = '63_PathoftheWarder';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_120_PavelsResearch' WHERE `name` = '120_PavelsResearch';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_017_LightAndDarkness' WHERE `name` = '17_LightAndDarkness';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_271_ProofOfValor' WHERE `name` = '271_ProofOfValor';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10272_LightFragment' WHERE `name` = '10272_LightFragment';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_153_DeliverGoods' WHERE `name` = '153_DeliverGoods';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_259_RanchersPlea' WHERE `name` = '259_RanchersPlea';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_406_PathToElvenKnight' WHERE `name` = '406_PathToElvenKnight';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_079_SagaOfTheAdventurer' WHERE `name` = '79_SagaOfTheAdventurer';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_258_BringWolfPelts' WHERE `name` = '258_BringWolfPelt1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_653_WildMaiden' WHERE `name` = '653_WildMaiden';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_224_TestOfSagittarius' WHERE `name` = '224_TestOfSagittarius';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_276_HestuiTotem' WHERE `name` = '276_HestuiTotem';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_137_TempleChampionPart1' WHERE `name` = '137_TempleChampionPart1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_139_ShadowFoxPart1' WHERE `name` = '139_ShadowFoxPart1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_140_ShadowFoxPart2' WHERE `name` = '140_ShadowFoxPart2';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_141_ShadowFoxPart3' WHERE `name` = '141_ShadowFoxPart3';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_142_FallenAngelRequestOfDawn' WHERE `name` = '142_FallenAngelRequestOfDawn';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_607_ProveYourCourage' WHERE `name` = '607_ProveYourCourage_Ketra';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_097_SagaOfTheShillienTemplar' WHERE `name` = '97_SagaOfTheShillienTemplar';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_123_TheLeaderAndTheFollower' WHERE `name` = '123_TheLeaderAndTheFollower';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_211_TrialOfChallenger' WHERE `name` = '211_TrialOfChallenger';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_324_SweetestVenom' WHERE `name` = '324_SweetestVenom';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_651_RunawayYouth' WHERE `name` = '651_RunawayYouth';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_029_ChestCaughtWithABaitOfEarth' WHERE `name` = '29_ChestCaughtWithABaitOfEarth';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_052_WilliesSpecialBait' WHERE `name` = '52_WilliesSpecialBait';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_216_TrialoftheGuildsman' WHERE `name` = '216_TrialOfGuildsman';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_231_TestOfTheMaestro' WHERE `name` = '231_TestOfMaestro';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_649_ALooterandaRailroadMan' WHERE `name` = '649_ALooterAndARailroadMan';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_148_PathToBecomingAnExaltedMercenary' WHERE `name` = '148_PathtoBecominganExaltedMercenary';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_368_TrespassingIntoTheSacredArea' WHERE `name` = '368_TrespassingIntoTheSacredArea';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_157_RecoverSmuggled' WHERE `name` = '157_RecoverSmuggled';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_432_BirthdayPartySong' WHERE `name` = '432_BirthdayPartySong';
/* We leave the above one, no need to explain why */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_242_PossessorOfaPreciousSoul2' WHERE `name` = 'Q242_PossessorOfAPreciousSoul2';
/* We leave the above one, no need to explain why */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_246_PossessorOfaPreciousSoul3' WHERE `name` = 'Q246_PossessorOfAPreciousSoul3';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_214_TrialOfScholar' WHERE `name` = '214_TrialOfScholar';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_228_TestOfMagus' WHERE `name` = '228_TestOfMagus';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_299_GatherIngredientsforPie' WHERE `name` = '299_GatherIngredientsForPie';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_325_GrimCollector' WHERE `name` = '325_GrimCollector';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_080_SagaOfTheWindRider' WHERE `name` = '80_SagaOfTheWindRider';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_171_ActsOfEvil' WHERE `name` = '171_ActsOfEvil';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_340_SubjugationofLizardmen' WHERE `name` = '340_SubjugationOfLizardmen';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_083_SagaOfTheMoonlightSentinel' WHERE `name` = '83_SagaOfTheMoonlightSentinel';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_092_SagaOfTheElementalMaster' WHERE `name` = '92_SagaOfTheElementalMaster';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_614_SlayTheEnemyCommander' WHERE `name` = '614_SlayTheEnemyCommander_Varka';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_616_MagicalPowerofFire2' WHERE `name` = '616_MagicalPowerOfFirePart2';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_069_SagaOfTheTrickster' WHERE `name` = '69_SagaOfTheTrickster';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_286_FabulousFeathers' WHERE `name` = '286_FabulousFeathers';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_273_InvadersOfHolyland' WHERE `name` = '273_InvadersOfHolyland';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_024_InhabitantsOfTheForestOfTheDead' WHERE `name` = '24_InhabitantsOfTheForrestOfTheDead';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_068_SagaOfTheSoulHound' WHERE `name` = '68_SagaOfTheSoulHound';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_380_BringOutTheFlavorOfIngredients' WHERE `name` = '380_BringOutTheFlavorOfIngredients';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_081_SagaOfTheGhostHunter' WHERE `name` = '81_SagaOfTheGhostHunter';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_371_ShriekOfGhosts' WHERE `name` = '371_ShriekOfGhosts';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_076_SagaOfTheGrandKhavatari' WHERE `name` = '76_SagaOfTheGrandKhavatari';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_416_PathToOrcShaman' WHERE `name` = '416_PathToOrcShaman';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_091_SagaOfTheArcanaLord' WHERE `name` = '91_SagaOfTheArcanaLord';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_066_CertifiedArbalester' WHERE `name` = '66_CertifiedArbalester';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_226_TestOfHealer' WHERE `name` = '226_TestOfHealer';


/* 115_TheOtherSideOfTruth IDENTICAL																				= DONE */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = '115_TheOtherSideOfTruth';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_115_TheOtherSideOfTruth' WHERE `name` = '115_TheOtherSideOfTruth';

/* 648_AnIceMerchantsDream IDENTICAL																				= DONE */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = '648_AnIceMerchantsDream';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_648_AnIceMerchantsDream' WHERE `name` = '648_AnIceMerchantsDream';

/* Q10283_RequestOfIceMerchant IDENTICAL																			= DONE */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = '10283_RequestOfIceMerchant';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10283_RequestOfIceMerchant' WHERE `name` = '10283_RequestOfIceMerchant';

/* Q10284_AcquisitionOfDivineSword 																					= DONE */
/* First, insert all data for this quest, even if it is not completed etc */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = 'Q10284_AcquisitionOfDivineSword';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10284_AcquisionOfDivineSword' WHERE `name` = 'Q10284_AcquisitionOfDivineSword';
/* Now, update all fields for this Q where var <state> set value = Completed */
UPDATE `l2age_rebellion`.`character_quests` SET `value` = 'Completed' WHERE `name` = '_10284_AcquisionOfDivineSword' AND `var` = '<state>';
/* And now, delete all records for this Q, where var is NOT <state> and value is NOT Completed */
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_10284_AcquisionOfDivineSword' AND `value` != 'Completed';

/* Q10285_MeetingSirra 																								= DONE */
/* First, insert all data for this quest, even if it is not completed etc */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = 'Q10285_MeetingSirra';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10285_MeetingSirra' WHERE `name` = 'Q10285_MeetingSirra';
/* Now, update all fields for this Q where var <state> set value = Completed */
UPDATE `l2age_rebellion`.`character_quests` SET `value` = 'Completed' WHERE `name` = '_10285_MeetingSirra' AND `var` = '<state>';
/* And now, delete all records for this Q, where var is NOT <state> and value is NOT Completed */
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_10285_MeetingSirra' AND `value` != 'Completed';

/* Q10286_ReunionWithSirra 																							= DONE */
/* First, insert all data for this quest, even if it is not completed etc */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = 'Q10286_ReunionWithSirra';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10286_ReunionWithSirra' WHERE `name` = 'Q10286_ReunionWithSirra';
/* Now, update all fields for this Q where var <state> set value = Completed */
UPDATE `l2age_rebellion`.`character_quests` SET `value` = 'Completed' WHERE `name` = '_10286_ReunionWithSirra' AND `var` = '<state>';
/* And now, delete all records for this Q, where var is NOT <state> and value is NOT Completed */
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_10286_ReunionWithSirra' AND `value` != 'Completed';

/* Q10287_StoryOfThoseLeft																							= DONE */
/* First, insert all data for this quest, even if it is not completed etc */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = 'Q10287_StoryOfThoseLeft';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10287_StoryOfThoseLeft' WHERE `name` = 'Q10287_StoryOfThoseLeft';
/* Now, update all fields for this Q where var <state> set value = Completed */
UPDATE `l2age_rebellion`.`character_quests` SET `value` = 'Completed' WHERE `name` = '_10287_StoryOfThoseLeft' AND `var` = '<state>';
/* And now, delete all records for this Q, where var is NOT <state> and value is NOT Completed */
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_10287_StoryOfThoseLeft' AND `value` != 'Completed';

/*
Antharas quest
Audience with land Dragon -- dont transfer it. -> Ok, I won't :)
*/

/* 
Frintezza quest	
119_LastImperialPrince IDENTICAL																						= DONE 
*/
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = '119_LastImperialPrince';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_119_LastImperialPrince' WHERE `name` = '119_LastImperialPrince';

/* 654_JourneytoaSettlement IDENTICAL 																					= DONE */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = '654_JourneytoaSettlement';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_654_JourneytoaSettlement' WHERE `name` = '654_JourneytoaSettlement';

/*
Noble quest
Q00241_PossessorOfAPreciousSoul1 IDENTICAL																				= DONE 
*/
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = 'Q00241_PossessorOfAPreciousSoul1';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_241_PossessorOfaPreciousSoul1' WHERE `name` = 'Q00241_PossessorOfAPreciousSoul1';

/* Q00242_PossessorOfAPreciousSoul2 																					= DONE */
/* First, insert all data for this quest, even if it is not completed etc */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = 'Q00242_PossessorOfAPreciousSoul2';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_242_PossessorOfaPreciousSoul2' WHERE `name` = 'Q00242_PossessorOfAPreciousSoul2';
/* Now, delete all records for this Q, where value is not Completed or Started */
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_242_PossessorOfaPreciousSoul2' AND `value` != 'Completed' AND `value` != 'Started';
/* Insert char_id's for this Q, where value = Started - this inserts: charid, empty, empty, empty */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`)
SELECT `character_quests`.`charId`
FROM `character_quests` WHERE `character_quests`.`name` = 'Q00242_PossessorOfAPreciousSoul2' AND `character_quests`.`value` = 'Started';
/* Now, insert: name = qname, var = cond, value = 11 for all char_id where `var` = '' */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_242_PossessorOfaPreciousSoul2', `var` = 'cond', `value` = '11' WHERE `var` = '';


/* Q00246_PossessorOfAPreciousSoul3 																					= DONE */
/* First, insert all data for this quest, even if it is not completed etc */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = 'Q00246_PossessorOfAPreciousSoul3';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_246_PossessorOfaPreciousSoul3' WHERE `name` = 'Q00246_PossessorOfAPreciousSoul3';
/* Now, delete all records for this Q, where value is not Completed or Started */
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_246_PossessorOfaPreciousSoul3' AND `value` != 'Completed' AND `value` != 'Started';
/* Insert char_id's for this Q, where value = Started - this inserts: charid, empty, empty, empty */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`)
SELECT `character_quests`.`charId`
FROM `character_quests` WHERE `character_quests`.`name` = 'Q00246_PossessorOfAPreciousSoul3' AND `character_quests`.`value` = 'Started';
/* Now, insert: name = qname, var = cond, value = 1 for all char_id where `var` = '' */
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_246_PossessorOfaPreciousSoul3', `var` = 'cond', `value` = '1' WHERE `var` = '';

/* Q00247_PossessorOfAPreciousSoul4	IDENTICAL																			= DONE */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = 'Q00247_PossessorOfAPreciousSoul4';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_247_PossessorOfaPreciousSoul4' WHERE `name` = 'Q00247_PossessorOfAPreciousSoul4';


/*
Quest: Bylord
131_BirdInACage	IDENTICAL																								= DONE 
*/
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = '131_BirdInACage';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_131_BirdInACage' WHERE `name` = '131_BirdInACage';

/* 618_IntoTheFlame	IDENTICAL	 																						= DONE */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = '618_IntoTheFlame';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_618_IntoTheFlame' WHERE `name` = '618_IntoTheFlame';

/* Q254_LegendaryTales 																									= DONE */
/* First, insert all data for this quest, even if it is not completed etc */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = 'Q254_LegendaryTales';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_254_LegendaryTales' WHERE `name` = 'Q254_LegendaryTales';
/* Now, update all fields for this Q where var <state> set value = Completed */
UPDATE `l2age_rebellion`.`character_quests` SET `value` = 'Completed' WHERE `name` = '_254_LegendaryTales' AND `var` = '<state>';
/* And now, delete all records for this Q, where var is NOT <state> and value is NOT Completed */
DELETE FROM `l2age_rebellion`.`character_quests` WHERE `name` = '_254_LegendaryTales' AND `value` != 'Completed';

/* Q456_DontKnowDontCare IDENTICAL										 												= DONE */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = 'Q456_DontKnowDontCare';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_456_DontKnowDontCare' WHERE `name` = 'Q456_DontKnowDontCare';

/* Q130_PathToHellbound IDENTICAL 																						= DONE */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = 'Q130_PathToHellbound';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_130_PathToHellbound' WHERE `name` = 'Q130_PathToHellbound';

/* 422_RepentYourSins LOOKS IDENTICAL																					= DONE */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = '422_RepentYourSins';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_422_RepentYourSins' WHERE `name` = '422_RepentYourSins';

/* 10282_ToTheSeedOfAnnihilation (this Q is inserted fully, it has 99% Completed, 1% <started> with cond = 1 */
INSERT INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `character_quests`.`charId`, `character_quests`.`name`, `character_quests`.`var`, `character_quests`.`value`
FROM `character_quests` WHERE `character_quests`.`name` = '10282_ToTheSeedOfAnnihilation';
UPDATE IGNORE `l2age_rebellion`.`character_quests` SET `name` = '_10282_ToTheSeedOfAnnihilation' WHERE `name` = '10282_ToTheSeedOfAnnihilation';

/* Now, we set var = cond & value = -1 for all Completed quests */
/* we use IGNORE cause there are some quests that we made (check up :) */
INSERT IGNORE INTO `l2age_rebellion`.`character_quests` (`char_id`, `name`, `var`, `value`)
SELECT `char_id`, `name`, 'cond', '-1' FROM `l2age_rebellion`.`character_quests`;

/* ======================================================= END OF CHARACTER_QUESTS ======================================================= */

/* CHARACTER_RECIPEBOOK -> EMPTY */
/* IGNORE - due to duplicates */
INSERT IGNORE INTO `l2age_rebellion`.`character_recipebook` (`char_id`, `id`)
SELECT `character_recipebook`.`charId`, `character_recipebook`.`id`
FROM `character_recipebook`;
/* END OF CHARACTER_RECIPEBOOK */

/* CHARACTER_SECONDARY_PASSWORD -> Empty by default */
/* THIS IS REBELLION ACCOUNT SECONDARY PASSWORD IMPLEMENTATION - We don't use it. */
/* END OF CHARACTER_SECONDARY_PASSWORD */

/* CHARACTER_SECURITY -> EMPTY */
/* from character_security.sql - our new character secondary password implementation has more fields */
INSERT INTO `l2age_rebellion`.`character_security` (`charId`, `password`)
SELECT `character_security`.`charId`, `character_security`.`password`
FROM `character_security`;
/* END OF CHARACTER_SECURITY */

/* CHARACTER_SKILLS_SAVE -> EMPTY */
/* WE HAVE TO DROP L2J's character_skills_save key `skill_level` before we select from this table */
ALTER IGNORE TABLE `character_skills_save` DROP PRIMARY KEY, ADD PRIMARY KEY(`charId`,`skill_id`,`class_index`);
/* insert data */
INSERT INTO `l2age_rebellion`.`character_skills_save` (`char_obj_id`, `skill_id`, `skill_level`, `class_index`, `end_time`, `reuse_delay_org`)
SELECT `character_skills_save`.`charId`, `character_skills_save`.`skill_id`, `character_skills_save`.`skill_level`, `character_skills_save`.`class_index`, `character_skills_save`.`systime`, `character_skills_save`.`reuse_delay`
FROM `character_skills_save`;
/* Delete all certificate skills for players - za da gi nauchat nanovo */
DELETE FROM `l2age_rebellion`.`character_skills_save` WHERE `skill_id` IN (631, 632, 633, 634, 637, 638, 639, 640, 641, 642, 643, 644, 645, 
646, 647, 648, 649, 650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 661, 662, 1489, 1490, 1491);
/* END OF CHARACTER_SKILLS_SAVE */

/* CHARACTER_SMS_DONATE -> Empty by default */
/* use it for more fields trough sms php script */ /* ---- TODO: What ? */ 
/* END OF CHARACTER_SMS_DONATE */

/* CHARACTER_SUBCLASSES -> EMPTY */
/* If player has 3 subclasses, they are active = 0 i isBase = 0, the base class is active = 1 i isBase = 1 */
/* ---- TODO: check if hp,mp,cp.. etc = 1 will be ok when player login - they are 1, but can be filled, the data is saved correctly. */
/* 1 Warning on this query, possibly exp or sp (longer values) */
/* INSERT THE BASE CLASS FOR ALL CHARACTERS FIRST (FROM CHARACTERS) */
/* First, we need to set classid = base_class in l2j_characters (to activate the BASE CLASS for all players) */
UPDATE `characters` SET `classid` = `base_class`;
/* Now, we insert the base classes from l2j_characters */
INSERT INTO `l2age_rebellion`.`character_subclasses` (`char_obj_id`, `class_id`, `level`, `exp`, `sp`, `curHp`, `curMp`, `curCp`, `maxHp`, `maxMp`, `maxCp`)
SELECT `characters`.`charId`, `characters`.`classid`, `characters`.`level`, `characters`.`exp`, `characters`.`sp`, 
`characters`.`curHp`, `characters`.`curMp`, `characters`.`curCp`, `characters`.`maxHp`, `characters`.`maxMp`, `characters`.`maxCp`
FROM `characters`;
/* Activate the base class for all */
UPDATE `l2age_rebellion`.`character_subclasses` SET `isBase` = '1', `active` = '1';
/* Now, Insert subclasses for all players from l2j:character_subclasses */
INSERT IGNORE INTO `l2age_rebellion`.`character_subclasses` (`char_obj_id`, `class_id`, `level`, `exp`, `sp`)
SELECT `character_subclasses`.`charId`, `character_subclasses`.`class_id`, `character_subclasses`.`level`, `character_subclasses`.`exp`, `character_subclasses`.`sp`
FROM `character_subclasses`;
/* active, isBase & death_penalty = 0 by default, no update to change subclasses required. */
/* set curHp, curMp, curCp, maxHp, maxMp, maxCp = 1  ONLY ON SUBCLASSES (isBase != 1) */
UPDATE `l2age_rebellion`.`character_subclasses` AS a JOIN `l2agefortests`.`character_subclasses` AS b ON (b.`charId` = a.`char_obj_id`)
SET a.`curHp` = '1', a.`curMp` = '1', a.`curCp` = '1', a.`maxHp` = '1', a.`maxMp` = '1', a.`maxCp` = '1' WHERE a.`isBase` != '1';
/* 
THIS IS NOT NEEDED:
Now, we will set the base-class active for all players, where the sub-classes will not be activated
UPDATE `l2age_rebellion`.`character_subclasses` AS a JOIN `l2agefortests`.`character_subclasses` AS b ON (b.`charId` = a.`char_obj_id`)
SET a.`active` = '1', a.`isBase` = '1' WHERE b.`class_index` = '1';
*/
/* END OF CHARACTER_SUBCLASSES */

/* CHARACTER_HENNAS: THIS TABLE SHOULD BE EXECUTED AFTER CHARACTER_SUBCLASSES IS UPDATED, SO, WE PUT IT AFTER IT :) !!!!!! -> EMPTY */
INSERT INTO `l2age_rebellion`.`character_hennas` (`char_obj_id`, `symbol_id`, `slot`, `class_index`)
SELECT `character_hennas`.`charId`, `character_hennas`.`symbol_id`, `character_hennas`.`slot`, `character_hennas`.`class_index`
FROM `character_hennas`;
/* Update class_index, Cuz we need to get it from character_subclasses */
/* This will set class_index = class_id (get from l2j.character_subclasses) for all subclasses (1/2/3) */
UPDATE `l2age_rebellion`.`character_hennas` AS a JOIN `character_subclasses` AS b ON (b.`charId` = a.`char_obj_id`)
SET a.`class_index` = b.`class_id` WHERE b.`class_index` = a.`class_index`;
/* This will set class_index = class_id (get from Reb.character_subclasses) for all base-classes (0) */
UPDATE `l2age_rebellion`.`character_hennas` AS a JOIN `l2age_rebellion`.`character_subclasses` AS b ON (b.`char_obj_id` = a.`char_obj_id`)
SET a.`class_index` = b.`class_id` WHERE a.`class_index` = '0' AND b.`isBase` = '1' AND b.`active` = '1';
/* END OF CHARACTER_HENNAS */

/* CHARACTER_SKILLS -> THIS TABLE SHOULD BE EXECUTED AFTER CHARACTER_SUBCLASSES IS UPDATED, SO, WE PUT IT AFTER IT :) !!!!!! -> EMPTY */
/* from character_skills.sql - should be same */
INSERT INTO `l2age_rebellion`.`character_skills` (`char_obj_id`, `skill_id`, `skill_level`, `class_index`)
SELECT `character_skills`.`charId`, `character_skills`.`skill_id`, `character_skills`.`skill_level`, `character_skills`.`class_index`
FROM `character_skills`;
/* Delete all certificate skills for players - za da gi nauchat nanovo */
DELETE FROM `l2age_rebellion`.`character_skills` WHERE `skill_id` IN (631, 632, 633, 634, 637, 638, 639, 640, 641, 642, 643, 644, 645, 
646, 647, 648, 649, 650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 661, 662, 1489, 1490, 1491);
/* This will set class_index = class_id (get from l2j.character_subclasses) for all subclasses (1/2/3) */
UPDATE IGNORE `l2age_rebellion`.`character_skills` AS a JOIN `character_subclasses` AS b ON (b.`charId` = a.`char_obj_id`)
SET a.`class_index` = b.`class_id` WHERE b.`class_index` = a.`class_index`;
/* for base class */
UPDATE IGNORE `l2age_rebellion`.`character_skills` AS a JOIN `l2age_rebellion`.`character_subclasses` AS b ON (b.`char_obj_id` = a.`char_obj_id`)
SET a.`class_index` = b.`class_id` WHERE a.`class_index` = '0' AND b.`isBase` = '1' AND b.`active` = '1';

/* END OF CHARACTER_SKILLS */


/* CHARACTER_SHORTCUTS -> EMPTY */
/* Rebellion has character_type (0,1,2) - from where to get it ? */
INSERT INTO `l2age_rebellion`.`character_shortcuts` (`object_id`, `slot`, `page`, `type`, `shortcut_id`, `level`, `class_index`)
SELECT `character_shortcuts`.`charId`, `character_shortcuts`.`slot`, `character_shortcuts`.`page`, `character_shortcuts`.`type`, `character_shortcuts`.`shortcut_id`, `character_shortcuts`.`level`, `character_shortcuts`.`class_index`
FROM `character_shortcuts`;
/* we gotta set class_index (l2j:0/1/2/3) to character class */
UPDATE IGNORE `l2age_rebellion`.`character_shortcuts` AS a JOIN `l2age_rebellion`.`character_subclasses` AS b ON (b.`char_obj_id` = a.`object_id`)
SET a.`class_index` = b.`class_id` WHERE a.`class_index` = '0' AND b.`isBase` = '1';
/* END OF CHARACTER_SHORTCUTS */

/* CHARACTER_VARIABLES -> EMPTY */
/* --- TODO: ASK ICO */
/* expire_time = -1 -> permanent */
/* Here we may insert for all players (as defaults):
lang@ en -1
AutoLoot false -1
AutoLootHerbs false -1
AutoLootOnlyAdena false -1
DroplistIcons 1 -1
noColor 1 -1
noShift 1 -1

We may also insert offline traders:
selllist - item;quantity;price:item;quantity;price:item;quantity;price: (END IS WITH : not with ;)
sellstorename - imeto na magazina (title)
/* END OF CHARACTER_VARIABLES */

/* CHARACTERS -> EMPTY */
/* 
Query OK, 106751 rows affected, 1 warning (2.55 sec)
Records: 106751  Duplicates: 0  Warnings: 1
Need to check what is going on here.
*/
INSERT INTO `l2age_rebellion`.`characters` (`account_name`, `obj_Id`, `char_name`, `face`, `hairStyle`, `hairColor`, `sex`, `heading`, `x`, `y`, `z`, `karma`, `pvpkills`, `pkkills`, `clanid`, `title`, `onlinetime`, `online`, `accesslevel`, `pcBangPoints`, `vitality`, `bookmarks`, `lvl_joined_academy`, `apprentice`, `fame`, `pledge_rank`, `pledge_type`)
SELECT `characters`.`account_name`, `characters`.`charId`, `characters`.`char_name`, `characters`.`face`, `characters`.`hairStyle`, `characters`.`hairColor`, `characters`.`sex`, `characters`.`heading`, `characters`.`x`, `characters`.`y`, `characters`.`z`, `characters`.`karma`, `characters`.`pvpkills`, `characters`.`pkkills`, `characters`.`clanid`, `characters`.`title`, `characters`.`onlinetime`, `characters`.`online`, `characters`.`accesslevel`, `characters`.`pccafe_points`, `characters`.`vitality_points`, `characters`.`bookmarkslot`, `characters`.`lvl_joined_academy`, `characters`.`apprentice`, `characters`.`fame`, `characters`.`power_grade`, `characters`.`subpledge`
FROM `characters`;
/*
added in the first query -> pledge_rank (rebellion) = power_grade (l2j)
added in the first query ->case1: if l2j_subpledge = -1 then reb_pledge_type = -1
only upgrade this: case2: if l2j_subpledge = 0 AND l2j_clanId = 0 then reb_pledge_type = -128
*/
UPDATE `l2age_rebellion`.`characters` AS a JOIN `l2agefortests`.`characters` AS b ON (b.`charId` = a.`obj_Id`) SET a.`pledge_type` = '-128' 
WHERE b.`subpledge` = '0' AND b.`clanId` = '0';

/* `lastAccess` l2j:millis -3 digits */
UPDATE `l2age_rebellion`.`characters` AS a JOIN `l2agefortests`.`characters` AS b ON (b.`charId` = a.`obj_Id`) SET a.`lastAccess` = substring(b.`lastAccess`, 1, length(b.`lastAccess`) - 3);
/* `deletetime` l2j:millis -3 digits */
UPDATE `l2age_rebellion`.`characters` AS a JOIN `l2agefortests`.`characters` AS b ON (b.`charId` = a.`obj_Id`) SET a.`lastAccess` = substring(b.`deletetime`, 1, length(b.`deletetime`) - 3);
/* `createtime` */
UPDATE `l2age_rebellion`.`characters` AS a JOIN `l2agefortests`.`characters` AS b ON (b.`charId` = a.`obj_Id`) SET a.`createtime` = unix_timestamp(b.createDate);
/* hunt_points + hunt_time */
UPDATE `l2age_rebellion`.`characters` AS a JOIN `l2agefortests`.`character_hunting_bonus` AS b ON (b.`charId` = a.`obj_Id`) SET a.`hunt_time` = b.`advent_time`;
UPDATE `l2age_rebellion`.`characters` AS a JOIN `l2agefortests`.`character_hunting_bonus` AS b ON (b.`charId` = a.`obj_Id`) SET a.`hunt_points` = b.`advent_points`;

/* Fix GM access levels :) */
UPDATE `l2age_rebellion`.`characters` SET `accesslevel` = '127' WHERE `account_name` = 'corrupted1';
UPDATE `l2age_rebellion`.`characters` SET `accesslevel` = '100' WHERE `account_name` = 'tomalie22';
UPDATE `l2age_rebellion`.`characters` SET `accesslevel` = '127' WHERE `account_name` = 'nik';
UPDATE `l2age_rebellion`.`characters` SET `accesslevel` = '100' WHERE `account_name` = 'a4o';
UPDATE `l2age_rebellion`.`characters` SET `accesslevel` = '127' WHERE `account_name` = 'kinsi';
UPDATE `l2age_rebellion`.`characters` SET `accesslevel` = '127' WHERE `account_name` = 'mag3t0';
UPDATE `l2age_rebellion`.`characters` SET `accesslevel` = '100' WHERE `account_name` = 'mihail51722';
UPDATE `l2age_rebellion`.`characters` SET `accesslevel` = '127' WHERE `account_name` = 'onepamopa';
/* END OF CHARACTERS */

/* CLAN_DATA -> EMPTY */
/* from clan_data.sql (more or less) rebellion has lots of fields, I will put only these that exist, others -> ask ico. */
/* with auction_bid_at and expelled_member */
/*
INSERT INTO `l2age_rebellion`.`clan_data` (`clan_id`, `clan_level`, `hasCastle`, `ally_id`, `reputation_score`, `auction_bid_at`, `expelled_member`)
SELECT `clan_data`.`clan_id`, `clan_data`.`clan_level`, `clan_data`.`hasCastle`, `clan_data`.`ally_id`, `clan_data`.`reputation_score`, `clan_data`.`auction_bid_at`, `clan_data`.`char_penalty_expiry_time`
FROM `clan_data`;
*/
/* without auction_bid_at and expelled_member */
INSERT INTO `l2age_rebellion`.`clan_data` (`clan_id`, `clan_level`, `hasCastle`, `ally_id`, `reputation_score`)
SELECT `clan_data`.`clan_id`, `clan_data`.`clan_level`, `clan_data`.`hasCastle`, `clan_data`.`ally_id`, `clan_data`.`reputation_score`
FROM `clan_data`;
/* clan halls */
/*
ot l2j clanhall vzima6 ownerid i po nego slaga6 id-to na ch-to vav clan_data kadeto ownera e ednakav sas clanId-to
id-to na ch-to itva vav hashideout 
exclude id po-golqmo ot 61
i
w clanhall -> auction_length = 0
*/
UPDATE `l2age_rebellion`.`clan_data` AS a JOIN `clanhall` AS b ON (b.`ownerId` = a.`clan_id`)
SET a.`hasHideout` = b.`id` WHERE b.`id` < '62';

UPDATE `l2age_rebellion`.`clanhall` SET `auction_length` = '0', `paid_cycle` = '60', `cycle` = '1';
/* END OF CLAN_DATA */

/* CLAN_PRIVS -> EMPTY */
/* from clan_privs.sql l2j has additional column - party */
INSERT INTO `l2age_rebellion`.`clan_privs` (`clan_id`, `rank`, `privileges`)
SELECT `clan_privs`.`clan_id`, `clan_privs`.`rank`, `clan_privs`.`privs`
FROM `clan_privs`;
/* END OF CLAN_PRIVS */

/* CLAN_SKILLS -> EMPTY */
/* FROM clan_skills.sql l2j has additional: skill_name and sub_pledge_id */
/* WE ONLY INSERT CLAN SKILLS HERE, SUBPLEDGES (sub_pledge_id != -2) ARE IN DIFFERENT TABLE) */
INSERT INTO `l2age_rebellion`.`clan_skills` (`clan_id`, `skill_id`, `skill_level`)
SELECT `clan_skills`.`clan_id`, `clan_skills`.`skill_id`, `clan_skills`.`skill_level`
FROM `clan_skills` WHERE `clan_skills`.`sub_pledge_id` = '-2';
/* END OF CLAN_SKILLS */

/* CLAN_SUBPLEDGES -> EMPTY */
/* FROM clan_subpledges.sql in l2j */ /* ---- TODO: WHAT ?! */
INSERT INTO `l2age_rebellion`.`clan_subpledges` (`clan_id`, `type`, `name`, `leader_id`)
SELECT `clan_subpledges`.`clan_id`, `clan_subpledges`.`sub_pledge_id`, `clan_subpledges`.`name`, `clan_subpledges`.`leader_id`
FROM `clan_subpledges`;
/* Insert main clan (reb type = 0 (it's 0 by default in the table) */
INSERT INTO `l2age_rebellion`.`clan_subpledges` (`clan_id`, `name`, `leader_id`)
SELECT `clan_data`.`clan_id`, `clan_data`.`clan_name`, `clan_data`.`leader_id`
FROM `clan_data`;
/* END OF CLAN_SUBPLEDGES */

/* CLAN_SUBPLEDGES_SKILLS -> EMPTY */
/* get clan id , type = sub_pledge_id ,skillid , skill level from clan skills of l2j table, use sub_pledge_id for squad skills  */
/* `sub_pledge_id` -2 - clan skills, everything else - squad, and we only insert squad here :) */
INSERT INTO `l2age_rebellion`.`clan_subpledges_skills` (`clan_id`, `type`, `skill_id`, `skill_level`)
SELECT `clan_skills`.`clan_id`, `clan_skills`.`sub_pledge_id`, `clan_skills`.`skill_id`, `clan_skills`.`skill_level` 
FROM `clan_skills` WHERE `clan_skills`.`sub_pledge_id` != '-2';
/* END OF CLAN_SUBPLEDGES_SKILLS */

/* CLAN_WARS -> EMPTY */
/* FROM clan_wars.sql l2j has additional wantspeace1 and wantspeace2 */
INSERT INTO `l2age_rebellion`.`clan_wars` (`clan1`, `clan2`)
SELECT `clan_wars`.`clan1`, `clan_wars`.`clan2`
FROM `clan_wars`;
/* END OF CLAN_WARS */

/* CLANHALL -> EMPTY */
/* Frist, delete all records from the table, we will insert them manually */
TRUNCATE TABLE `l2age_rebellion`.`clanhall`;
/* insert data */
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('21', 'Fortress of Resistance', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('22', 'Moonstone Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('23', 'Onyx Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('24', 'Topaz Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('25', 'Ruby Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('26', 'Crystal Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('27', 'Onyx Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('28', 'Sapphire Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('29', 'Moonstone Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('30', 'Emerald Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('31', 'The Atramental Barracks', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('32', 'The Scarlet Barracks', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('33', 'The Viridian Barracks', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('34', 'Devastated Castle', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('35', 'Bandit Stronghold', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('36', 'The Golden Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('37', 'The Silver Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('38', 'The Mithril Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('39', 'Silver Manor', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('40', 'Gold Manor', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('41', 'The Bronze Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('42', 'The Golden Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('43', 'The Silver Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('44', 'The Mithril Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('45', 'The Bronze Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('46', 'Silver Manor', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('47', 'Moonstone Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('48', 'Onyx Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('49', 'Emerald Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('50', 'Sapphire Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('51', 'Mont Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('52', 'Astaire Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('53', 'Aria Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('54', 'Yiana Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('55', 'Roien Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('56', 'Luna Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('57', 'Traban Chamber', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('58', 'Eisen Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('59', 'Heavy Metal Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('60', 'Molten Ore Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('61', 'Titan Hall', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('62', 'Rainbow Springs', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('63', 'Wild Beast Reserve', '0', '1358773200000', '0', '0', '0', null, '0', '0');
INSERT INTO `l2age_rebellion`.`clanhall` VALUES ('64', 'Fortress of the Dead', '0', '1358773200000', '0', '0', '0', null, '0', '0');
/* insert shits into clanhall: will be put in clan_data */
/* UPDATE `l2age_rebellion`.`clanhall` AS a JOIN */
/* END OF CLANHALL */

/* CLASS_LIST -> EMPTY */
/* NOTHING HERE, table is filled by default */
/* END OF CLASS_LIST */

/* COMMUNITY_NEWS -> EMPTY BY DEFAULT */
/* UNKNOWN, leave empty */
/* END OF COMMUNITY_NEWS */

/* COMMUNITYBUFF_ALLOWED_BUFFS -> DO NOT EMPTY THIS TABLE */

/* COMMUNITYBUFF_GRP -> EMPTY (FIND OUT IF WE CAN INSERT SOMETHING HERE, AND FROM WHERE), perhaps related to character_variables ? */

/* COMMUNITYBUFF_GRP_ALLOWED_BUFFS -> DO NOT EMPTY THIS TABLE */

/* COMMUNITYBUFF_GRP_ALLOWED_BUFFS_4e4o -> DO NOT EMPTY THIS TABLE */

/* COMMUNITYBUFF_GRP_BUFFS -> EMPTY (FIND OUT IF WE CAN INSERT SOMETHING HERE, AND FROM WHERE), perhaps related to character_variables ? */

/* COMTELEPORT -> Empty by default */
/* ignore */
/* END OF COMTELEPORT */

/* COUPLES -> EMPTY */
/* from mods_wedding.sql */
INSERT INTO `l2age_rebellion`.`couples` (`id`, `player1Id`, `player2Id`, `maried`, `affiancedDate`, `weddingDate`)
SELECT `mods_wedding`.`id`, `mods_wedding`.`player1Id`, `mods_wedding`.`player2Id`, `mods_wedding`.`married`, `mods_wedding`.`affianceDate`, `mods_wedding`.`weddingDate`
FROM `mods_wedding`;
/* END OF COUPLES */

/* CURSED_WEAPONS -> EMPTY */
/* FROM cursed_weapons.sql rebellion has additional x y z */
/*
Query OK, 1 row affected, 1 warning (0.00 sec)
Records: 1  Duplicates: 0  Warnings: 1
1 Warning.. need to check what it is.
*/
INSERT INTO `l2age_rebellion`.`cursed_weapons` (`item_id`, `player_id`, `player_karma`, `player_pkkills`, `nb_kills`, `end_time`)
SELECT `cursed_weapons`.`itemId`, `cursed_weapons`.`charId`, `cursed_weapons`.`playerKarma`, `cursed_weapons`.`playerPkKills`, `cursed_weapons`.`nbKills`, `cursed_weapons`.`endTime`
FROM `cursed_weapons`;
/* END OF CURSED_WEAPONS */

/* CUSTOM_AUCTION -> EMPTY */
/* FROM custom_auction.sql it is same */
INSERT INTO `l2age_rebellion`.`custom_auction` (`auction_id`, `seller_id`, `item_obj`, `max_bid`, `last_bider`, `auction_end`, `all_bids`, `isgolden`)
SELECT `custom_auction`.`auction_id`, `custom_auction`.`seller_id`, `custom_auction`.`item_obj`, `custom_auction`.`max_bid`, `custom_auction`.`last_bider`, `custom_auction`.`auction_end`, `custom_auction`.`all_bids`, `custom_auction`.`isgolden`
FROM `custom_auction`;
/* END OF CUSTOM_AUCTION */

/* DOMINION -> First, EMPTY */
TRUNCATE TABLE `l2age_rebellion`.`dominion`;
/* Now, we insert the default values: */
INSERT INTO `l2age_rebellion`.`dominion` VALUES ('81', 'Gludio Territory', '0', '81;', '0'); /* clan_data.hasCastle = 1 */
INSERT INTO `l2age_rebellion`.`dominion` VALUES ('82', 'Dion Territory', '0', '82;', '0'); /* clan_data.hasCastle = 2 */
INSERT INTO `l2age_rebellion`.`dominion` VALUES ('83', 'Giran Territory', '0', '83;', '0'); /* clan_data.hasCastle = 3 */
INSERT INTO `l2age_rebellion`.`dominion` VALUES ('84', 'Oren Territory', '0', '84;', '0'); /* clan_data.hasCastle = 4 */
INSERT INTO `l2age_rebellion`.`dominion` VALUES ('85', 'Aden Territory', '0', '85;', '0'); /* clan_data.hasCastle = 5 */
INSERT INTO `l2age_rebellion`.`dominion` VALUES ('86', 'Innadril Territory', '0', '86;', '0'); /* clan_data.hasCastle = 6 */
INSERT INTO `l2age_rebellion`.`dominion` VALUES ('87', 'Goddard Territory', '0', '87;', '0'); /* clan_data.hasCastle = 7 */
INSERT INTO `l2age_rebellion`.`dominion` VALUES ('88', 'Rune Territory', '0', '88;', '0'); /* clan_data.hasCastle = 8 */
INSERT INTO `l2age_rebellion`.`dominion` VALUES ('89', 'Schuttgart Territory', '0', '89;', '0'); /* clan_data.hasCastle = 9 */
/* Now, update wards (l2j: ownedWardIds) */
UPDATE `l2age_rebellion`.`dominion` AS a JOIN `territories` AS b ON (a.`id` = b.`territoryId`) SET a.`wards` = b.`ownedWardIds`;
/* Set the siege date */
UPDATE `l2age_rebellion`.`dominion` SET `siege_date` = '1363366800000';
/* Now, set lord_object_id */
UPDATE `l2age_rebellion`.`dominion` AS a JOIN `clan_data` AS b SET a.`lord_object_id` = b.`leader_id` WHERE a.`id` = '81' AND b.`hasCastle` = '1';
UPDATE `l2age_rebellion`.`dominion` AS a JOIN `clan_data` AS b SET a.`lord_object_id` = b.`leader_id` WHERE a.`id` = '82' AND b.`hasCastle` = '2';
UPDATE `l2age_rebellion`.`dominion` AS a JOIN `clan_data` AS b SET a.`lord_object_id` = b.`leader_id` WHERE a.`id` = '83' AND b.`hasCastle` = '3';
UPDATE `l2age_rebellion`.`dominion` AS a JOIN `clan_data` AS b SET a.`lord_object_id` = b.`leader_id` WHERE a.`id` = '84' AND b.`hasCastle` = '4';
UPDATE `l2age_rebellion`.`dominion` AS a JOIN `clan_data` AS b SET a.`lord_object_id` = b.`leader_id` WHERE a.`id` = '85' AND b.`hasCastle` = '5';
UPDATE `l2age_rebellion`.`dominion` AS a JOIN `clan_data` AS b SET a.`lord_object_id` = b.`leader_id` WHERE a.`id` = '86' AND b.`hasCastle` = '6';
UPDATE `l2age_rebellion`.`dominion` AS a JOIN `clan_data` AS b SET a.`lord_object_id` = b.`leader_id` WHERE a.`id` = '87' AND b.`hasCastle` = '7';
UPDATE `l2age_rebellion`.`dominion` AS a JOIN `clan_data` AS b SET a.`lord_object_id` = b.`leader_id` WHERE a.`id` = '88' AND b.`hasCastle` = '8';
UPDATE `l2age_rebellion`.`dominion` AS a JOIN `clan_data` AS b SET a.`lord_object_id` = b.`leader_id` WHERE a.`id` = '89' AND b.`hasCastle` = '9';
/* END OF DOMINION */

/* DOMINION_REWARDS -> EMPTY */
/* END OF DOMINION_REWARDS */

/* EPIC_BOSS_SPAWN - NO NEED TO EMPTY */
/* --- TODO: possibly get something from l2j grandboss_data ? */
/* respawnDate is not in milliseconds: 1361450127, l2j is in milliseconds: 1363320491997 -> 3 digits less: 1363320491 */
/* state: 0,1,2,3 */
/* Table is filled by default but we have to reset it first: */
REPLACE INTO `l2age_rebellion`.`epic_boss_spawn` (`bossId`, `respawnDate`, `state`) VALUES
(29068,'0',0), /* Antharas */
(29020,'0',0), /* Baium */
(29028,'0',0), /* Valakas */
(29062,'0',0), /* Andreas Van Halter */ /* not in grandboss_data or raidboss_spawnlist; boss is with same npcid in l2j */
(29065,'0',0), /* Sailren */ /* not in grandboss_data or raidboss_spawnlist; boss is with same npcid in l2j */
(29099,'0',0); /* Baylor */ /* not in grandboss_data or raidboss_spawnlist; boss has 2 npcids in l2j: 29099 and 29103 */
/*
I will only update bosses that exist in grandboss_data
Rows matched: 3  Changed: 3  Warnings: 0
 */
UPDATE `l2age_rebellion`.`epic_boss_spawn` AS a JOIN `grandboss_data` AS b ON (a.`bossId` = b.`boss_id`)
SET a.`respawnDate` = substring(b.`respawn_time`, 1, length(b.`respawn_time`) - 3), a.`state` = b.`status`;
/* END OF EPIC_BOSS_SPAWN */

/* EVENT_DATA - Empty by default */
/* NOTHING HERE */
/* END OF EVENT_DATA */

/* FISH */
/* Table is filled by default, l2j has this in xml */
/* END OF FISH */

/* FISHING_CHAMPIONSHIP -> EMPTY */
/* handled by event*/
/* END OF FISHING_CHAMPIONSHIP */

/* FISHREWARD - NO NEED TO EMPTY */
/* Table is filled by default */
/* END OF FISHREWARD */

/* FORTRESS -> EMPTY AND PUT DEFAULT DATA */
/* We fill the table with default values */
TRUNCATE TABLE `l2age_rebellion`.`fortress`;
/* Insert default data, If we need to update fields, we can do it after these queries. */
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('101', 'Shanty Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('102', 'Southern Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('103', 'Hive Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('104', 'Valley Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('105', 'Ivory Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('106', 'Narsell Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('107', 'Bayou Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('108', 'White Sands Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('109', 'Borderland Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('110', 'Swamp Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('111', 'Archaic Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('112', 'Floran Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('113', 'Cloud Mountain Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('114', 'Tanor Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('115', 'Dragonspine Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('116', 'Antharas Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('117', 'Western Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('118', 'Hunters Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('119', 'Aaru Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('120', 'Demon Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
INSERT INTO `l2age_rebellion`.`fortress` VALUES ('121', 'Monastic Fortress', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0');
/* Now, we get data from l2j: fort - rebellion has MORE COLUMNS... ICO NEED TO CHECK IT !!! */
UPDATE `l2age_rebellion`.`fortress` AS a JOIN `fort` AS b ON (a.`id` = b.`id`)
SET a.`state` = b.`state`, a.`siege_date` = b.`siegeDate`, a.`blood_oath_count` = b.`blood`, a.`own_date` = b.`lastOwnedTime`;
/* END OF FORTRESS */

/* FOUR_SEPULCHERS_SPAWNLIST */
/* Table is filled by default */
/* END OF FOUR_SEPULCHERS_SPAWNLIST */

/* GAME_LOG -> EMPTY BY DEFAULT */
/* twa e nqkakaw debug log naj-weroqtno, nz. nqma kwo da se prawi w taq tablica */
/* END OF GAME_LOG */

/* GAMES -> EMPTY */
/* from games.sql - it is same */
INSERT INTO `l2age_rebellion`.`games` (`id`, `idnr`, `number1`, `number2`, `prize`, `newprize`, `prize1`, `prize2`, `prize3`, `enddate`, `finished`)
SELECT `games`.`id`, `games`.`idnr`, `games`.`number1`, `games`.`number2`, `games`.`prize`, `games`.`newprize`, `games`.`prize1`, `games`.`prize2`, `games`.`prize3`, `games`.`enddate`, `games`.`finished`
FROM `games`;
/* END OF GAMES */

/* GLOBAL_TASKS - DO NOT EMPTY */
/* No need to insert anything here, since task names are different, l2j has more tasks than rebellion, however 
If we need any of them - we can simply add them.  
*/ 
/* rebellion task TYPE - SAME AS L2J:
	TYPE_NONE,
	TYPE_TIME,
	TYPE_SHEDULED,
	TYPE_FIXED_SHEDULED,
	TYPE_GLOBAL_TASK,
	TYPE_STARTUP,
	TYPE_SPECIAL
*/
/* --- WON't be used cause tasks are different names etc ?
INSERT INTO `l2age_rebellion`.`global_tasks` (`id`, `task`, `type`, `last_activation`, `param1`, `param2`, `param3`)
SELECT `global_tasks`.`id`, `global_tasks`.`task`, `global_tasks`.`type`, `global_tasks`.`last_activation`, `global_tasks`.`param1`, `global_tasks`.`param2`, `global_tasks`.`param3`
FROM `global_tasks`;
*/
/* END OF GLOBAL_TASKS */

/* HEROES -> EMPTY */
/* from heroes.sql - rebellion has active, l2j has class_id */
INSERT INTO `l2age_rebellion`.`heroes` (`char_id`, `count`, `played`, `message`)
SELECT `heroes`.`charId`, `heroes`.`count`, `heroes`.`played`, `heroes`.`message`
FROM `heroes`;
/* END OF HEROES */

/* HEROES_DIARY -> EMPTY */
/* from heroes_diary.sql - it is the same */
INSERT INTO `l2age_rebellion`.`heroes_diary` (`charId`, `time`, `action`, `param`)
SELECT `heroes_diary`.`charId`, `heroes_diary`.`time`, `heroes_diary`.`action`, `heroes_diary`.`param`
FROM `heroes_diary`;
/* END OF HEROES_DIARY */

/* HWID_BANS -> Empty by default */
/* HWID_INFO -> Empty by default */
/* HWID_LOG -> Empty by default */
/* nothing in these */

/* HZ_VOTES (table in svn - fixed) -> EMPTY */
/*
I'll add it manuallty cuz the empty backup has the old table.
Query OK, 66091 rows affected, 65535 warnings (0.87 sec)
Records: 66091  Duplicates: 0  Warnings: 66092
*/
INSERT INTO `l2age_rebellion`.`hz_votes` (`ip`, `ts`, `rewardsLeft`)
SELECT `hz_votes`.`ip`, `hz_votes`.`ts`, `hz_votes`.`rewardsLeft`
FROM `hz_votes`;
/* END OF HZ_VOTES */

/* HZ_VOTES_HWIDS -> EMPTY */
/* from hz_votes_hwids - it is the same */
INSERT INTO `l2age_rebellion`.`hz_votes_hwids` (`hwid`, `lastRewardTime`)
SELECT `hz_votes_hwids`.`hwid`, `hz_votes_hwids`.`lastRewardTime`
FROM `hz_votes_hwids`;
/* END OF HZ_VOTES_HWIDS */

/* IP_TABLE -> Empty by default */

/* ITEM_ATTRIBUTES -> EMPTY */
/* not used */
/* END OF ITEM_ATTRIBUTES */

/* ITEM_AUCTION -> EMPTY */
/* from item_auction.sql - same (more or less) */
/* table in l2j is empty, this is why Query OK, 0 rows affected (0.00 sec) */
INSERT INTO `l2age_rebellion`.`item_auction` (`auctionId`, `instanceId`, `auctionItemId`, `startingTime`, `endingTime`, `auctionStateId`)
SELECT `item_auction`.`auctionId`, `item_auction`.`instanceId`, `item_auction`.`auctionItemId`, `item_auction`.`startingTime`, `item_auction`.`endingTime`, `item_auction`.`auctionStateId`
FROM `item_auction`;
/* END OF ITEM_AUCTION */

/* ITEM_AUCTION_BID -> EMPTY */
/* from item_auction_bid.sql - it is the same */
/* table in l2j is empty, this is why Query OK, 0 rows affected (0.00 sec) */
INSERT INTO `l2age_rebellion`.`item_auction_bid` (`auctionId`, `playerObjId`, `playerBid`)
SELECT `item_auction_bid`.`auctionId`, `item_auction_bid`.`playerObjId`, `item_auction_bid`.`playerBid`
FROM `item_auction_bid`;
/* END OF ITEM_AUCTION_BID */

/* ITEMS -> EMPTY */
/*
Query OK, 6772244 rows affected, 12 warnings (5 min 18.30 sec)
Records: 6772244  Duplicates: 0  Warnings: 12
*/
INSERT INTO `l2age_rebellion`.`items` (`object_id`, `owner_id`, `item_id`, `count`, `enchant_level`, `loc`, `loc_data`)
SELECT `items`.`object_id`, `items`.`owner_id`, `items`.`item_id`, `items`.`count`, `items`.`enchant_level`, `items`.`loc`, `items`.`loc_data`
FROM `items`;
/* `life_time` (l2j: `items`.`time_of_use`) separately */
/*
Query OK, 0 rows affected, 65535 warnings (1 min 48.87 sec)
Rows matched: 6772244  Changed: 0  Warnings: 6772244
These warnings are cause l2j.items.time_of_use is null
*/
UPDATE `l2age_rebellion`.`items` AS a JOIN `l2agefortests`.`items` AS b ON (b.`object_id` = a.`object_id`) SET a.`life_time` = substring(b.`time_of_use`, 1, length(b.`time_of_use`) - 3);
/* ITEMS -> item_attributes */
UPDATE `l2age_rebellion`.`items` AS a JOIN `l2agefortests`.`item_attributes` AS b ON (b.`itemId` = a.`object_id`) SET a.`augmentation_id` = b.`augAttributes`;
/* ITEMS -> item_elementals */
/* attribute_fire 0 */
UPDATE `l2age_rebellion`.`items` AS a JOIN `l2agefortests`.`item_elementals` AS b ON (b.`itemId` = a.`object_id`) AND (b.elemType = 0) SET a.`attribute_fire` = b.`elemValue`;
/* attribute_water 1 */
UPDATE `l2age_rebellion`.`items` AS a JOIN `l2agefortests`.`item_elementals` AS b ON (b.`itemId` = a.`object_id`) AND (b.elemType = 1) SET a.`attribute_water` = b.`elemValue`;
/* attribute_wind 2 */
UPDATE `l2age_rebellion`.`items` AS a JOIN `l2agefortests`.`item_elementals` AS b ON (b.`itemId` = a.`object_id`) AND (b.elemType = 2) SET a.`attribute_wind` = b.`elemValue`;
/* attribute_earth 3 */
UPDATE `l2age_rebellion`.`items` AS a JOIN `l2agefortests`.`item_elementals` AS b ON (b.`itemId` = a.`object_id`) AND (b.elemType = 3) SET a.`attribute_earth` = b.`elemValue`;
/* attribute_holy 4 */
UPDATE `l2age_rebellion`.`items` AS a JOIN `l2agefortests`.`item_elementals` AS b ON (b.`itemId` = a.`object_id`) AND (b.elemType = 4) SET a.`attribute_holy` = b.`elemValue`;
/* attribute_unholy 5 */
UPDATE `l2age_rebellion`.`items` AS a JOIN `l2agefortests`.`item_elementals` AS b ON (b.`itemId` = a.`object_id`) AND (b.elemType = 5) SET a.`attribute_unholy` = b.`elemValue`;
/* User's equiped items will go to inventory to avoid "invisible" item bug. */
UPDATE `l2age_rebellion`.`items` SET `loc` = 'INVENTORY', `loc_data` = '0' WHERE `loc` = 'PAPERDOLL';
/* Delete all books for certification skills, players will have lern certification skills again */
DELETE FROM `l2age_rebellion`.`items` WHERE `item_id` IN (10612 , 10280, 10281, 10282, 10283, 10284, 10285 , 10286, 10287);
/* END OF ITEMS */

/* ITEMS_DELAYED -> Empty by default */
/* NOTHING HERE */
/* END OF ITEMS_DELAYED */

/* LASTHWID */
/* ALTER TABLE `characters` ADD `LastHWID` VARCHAR( 32 ) default ''; */
/* This is already in characters, so no execute */
/* END OF LASTHWID */

/* LVLUPGAIN -> No need to empty this table */
/* Table is filled by default */
/* END OF LVLUPGAIN */

/* ************************************************************************************************************************ */
/* MAIL -> EMPTY */
/* 
Query OK, 43428 rows affected, 3 warnings (1.04 sec)
Records: 43428  Duplicates: 0  Warnings: 3
3 Warnings, need to check them ...
*/
INSERT INTO `l2age_rebellion`.`mail` (`message_id`, `sender_id`, `receiver_id`, `topic`, `body`, `price`, `type`)
SELECT `messages`.`messageId`, `messages`.`senderId`, `messages`.`receiverId`, `messages`.`subject`, `messages`.`content`, `messages`.`reqAdena`, `messages`.`sendBySystem`
FROM `messages`;

UPDATE `l2age_rebellion`.`mail` AS a JOIN `l2agefortests`.`characters` AS b ON (a.`sender_id` = b.`charId`) SET a.`sender_name` = b.`char_name`;
UPDATE `l2age_rebellion`.`mail` AS a JOIN `l2agefortests`.`characters` AS b ON (a.`receiver_id` = b.`charId`) SET a.`receiver_name` = b.`char_name`;
UPDATE `l2age_rebellion`.`mail` AS a JOIN `l2agefortests`.`messages` AS b ON (a.`sender_id` = b.`senderId`) SET a.`expire_time` = substring(b.`expiration`, 1, length(b.`expiration`) - 3) WHERE b.`expiration` != 0;
/* unread(default 1) isUnread(true,false) */
UPDATE `l2age_rebellion`.`mail` AS a JOIN `l2agefortests`.`messages` AS b ON (a.`sender_id` = b.`senderId`) SET a.`unread` = '0' WHERE b.`isUnread` = 'false';
/* END OF MAIL */

/* MAIL_ATTACHMENTS -> EMPTY */
INSERT INTO `l2age_rebellion`.`mail_attachments` (`message_id`, `item_id`)
SELECT a.`messageId`, b.`object_id` FROM `messages` AS a JOIN `items` AS b ON
(a.`hasAttachments` = 'true') AND (a.`messageId` = b.`loc_data`) AND (b.`loc` = 'MAIL') AND (b.`loc_data` != '-1');
/* END OF MAIL_ATTACHMENTS */

/* CHARACTER_MAIL -> EMPTY */
/* receiver 0 */
/*
We will not use receiver, we will only put senders cause there are duplicates
INSERT IGNORE INTO `l2age_rebellion`.`character_mail` (`char_id`, `message_id`)
SELECT `messages`.`receiverId`, `messages`.`messageId`
FROM `messages`;
UPDATE `l2age_rebellion`.`character_mail` AS a JOIN `l2agefortests`.`messages` AS b ON (b.`receiverId` = a.`char_id`) SET a.`is_sender` = '0' WHERE (b.`messageId` = a.`message_id`);
*/
/* sender 1 (we omit sender -1 */
/*
Query OK, 42351 rows affected, 1 warning (0.66 sec)
Records: 42351  Duplicates: 0  Warnings: 1
1 Warning
*/
INSERT INTO `l2age_rebellion`.`character_mail` (`char_id`, `message_id`) 
SELECT `messages`.`senderId`, `messages`.`messageId`
FROM `messages` WHERE `messages`.`senderId` != '-1';
UPDATE `l2age_rebellion`.`character_mail` AS a JOIN `l2agefortests`.`messages` AS b ON (b.`senderId` = a.`char_id`) SET a.`is_sender` = '1' WHERE (b.`messageId` = a.`message_id`);
/* END OF CHARACTER_MAIL */
/* ************************************************************************************************************************ */

/* OLYMPIAD_HISTORY -> EMPTY */
/*
Query OK, 200 rows affected, 1 warning (0.09 sec)
Records: 200  Duplicates: 0  Warnings: 1
1 Warning, need to check it
*/
INSERT INTO `l2age_rebellion`.`olympiad_history` (`object_id_1`, `object_id_2`, `class_id_1`, `class_id_2`, `game_start_time`, `game_time`, `game_status`, `game_type`)
SELECT `olympiad_fights`.`charOneId`, `olympiad_fights`.`charTwoId`, `olympiad_fights`.`charOneClass`, `olympiad_fights`.`charTwoClass`, `olympiad_fights`.`start`, `olympiad_fights`.`time`, `olympiad_fights`.`winner`, `olympiad_fights`.`classed`
FROM `olympiad_fights` ORDER BY `olympiad_fights`.`start` DESC LIMIT 200;
/* rebellion:
  `game_status` INT(11) NOT NULL, --- DONE
  `game_type` INT(11) NOT NULL, = classed, non classed , team = classed form lj2 --- DONE
  `old` INT(11) NOT NULL = poslednite 200 resulta insertvai po nai visok start ot l2j..
*/
/* l2j:
  `winner` tinyint(1) unsigned NOT NULL default '0', (l2j: 0,1,2)  rebellion game_status: // 1 - выиграл 1, 2 выиграл 2, 0 - "устали" --- DONE
  `classed` tinyint(1) unsigned NOT NULL default '0', (l2j: 0,1) 0 = ? 1 =  ---DONE
*/
/* player names */
UPDATE `l2age_rebellion`.`olympiad_history` AS a JOIN `l2agefortests`.`characters` AS b ON (a.`object_id_1` = b.`charId`) SET a.`name_1` = b.`char_name`;
UPDATE `l2age_rebellion`.`olympiad_history` AS a JOIN `l2agefortests`.`characters` AS b ON (a.`object_id_2` = b.`charId`) SET a.`name_2` = b.`char_name`;
UPDATE `l2age_rebellion`.`olympiad_history` AS a JOIN `l2agefortests`.`olympiad_fights` AS b ON (a.`object_id_1` = b.`charOneId`) SET a.`old` = '1';
/* END OF OLYMPIAD_HISTORY */

/* OLYMPIAD_NOBLES -> EMPTY */
INSERT INTO `l2age_rebellion`.`olympiad_nobles` (`char_id`, `class_id`, `olympiad_points`, `competitions_done`, `competitions_win`, `competitions_loose`, `game_classes_count`, `game_noclasses_count`, `game_team_count`)
SELECT `olympiad_nobles`.`charId`, `olympiad_nobles`.`class_id`, `olympiad_nobles`.`olympiad_points`, `olympiad_nobles`.`competitions_done`, `olympiad_nobles`.`competitions_won`, `olympiad_nobles`.`competitions_lost`, `olympiad_nobles`.`competitions_done_week_classed`, `olympiad_nobles`.`competitions_done_week_non_classed`, `olympiad_nobles`.`competitions_done_week_team`
FROM `olympiad_nobles`;
/* some points */
UPDATE `l2age_rebellion`.`olympiad_nobles` AS a JOIN `l2agefortests`.`olympiad_nobles_eom` AS b ON (a.`char_id` = b.`charId`) SET a.`olympiad_points_past` = b.`olympiad_points`, a.`olympiad_points_past_static` = b.`olympiad_points`;
/* add missing nobless */
/* This will insert all charId's from characters that have nobless = 1 nomatter if they exist in olympiad_history */
INSERT IGNORE INTO `l2age_rebellion`.`olympiad_nobles` (`char_id`)
SELECT `charId` FROM `characters` WHERE `nobless` = '1';
/* Since olympiad_nobless.class_id and olympiad_nobless.olympiad_points are 0 by default, we will make the update by class_id */
UPDATE `l2age_rebellion`.`olympiad_nobles` AS a JOIN `characters` AS b ON (a.`char_id` = b.`charId`)
SET a.`class_id` = b.`base_class`, a.`olympiad_points` = '10' WHERE a.`class_id` = '0';
/* END OF OLYMPIAD_NOBLES */

/* ONLINE -> Empty by default */
/* Server fills this itself I guess */
/* END OF ONLINE */

/* PET_DATA - No need to empty it */
/* Table is filled by default */
/* END OF PET_DATA */

/* PETITIONS - Empty by default*/
/* Nothing to do here. */
/* END OF PETITIONS */

/* PETS -> EMPTY */
/* from pets.sql more or less, rebellion does not have pet restore shits */
/* rebellion has `max_fed` ico has to check what the hell it is. */
/* Ico has added name, so we put it to use :P */
/* Something smells here, I selected item_obj_id and objId in rebellion from characters, neither of them is OWNER (no such char id) ?!?!?!? */

/* Also, our pets table name is SHORTER than l2j, fixing to avoid warn. */
ALTER TABLE `l2age_rebellion`.`pets` MODIFY COLUMN `name` VARCHAR(16) CHARACTER SET UTF8 DEFAULT NULL;
INSERT INTO `l2age_rebellion`.`pets` (`item_obj_id`, `objId`, `name`, `level`, `curHp`, `curMp`, `exp`, `sp`, `fed`)
SELECT `pets`.`item_obj_id`, `pets`.`ownerId`, `pets`.`name`, `pets`.`level`, `pets`.`curHp`, `pets`.`curMp`, `pets`.`exp`, `pets`.`sp`, `pets`.`fed`
FROM `pets`;
/* few fixes */
UPDATE `l2age_rebellion`.`pets` SET `level` = '86' WHERE `level` = '86';
UPDATE `l2age_rebellion`.`pets` SET `exp` = '3905839397' WHERE level = '85';
/* END OF PETS */

/* PET_SKILLS -> No need to empty it */
/* Table is filled by default */
/* END OF PET_SKILLS */

/* PREMIUM_ACCOUNT_TABLE - Leave it. */
/* this is rebellion premium accounts table implementation, a lot of more shits, we may not use it dunno. nothing to do here for now */
/* END OF PREMIUM_ACCOUNT_TABLE */

/* PREMIUM_ACCOUNTS -> EMPTY */
/* from premium_accounts.sql - it is the same */
INSERT INTO `l2age_rebellion`.`premium_accounts` (`accountName`, `templateId`, `endTime`)
SELECT `premium_accounts`.`accountName`, `premium_accounts`.`templateId`, `premium_accounts`.`endTime`
FROM `premium_accounts`;
/* END OF PREMIUM_ACCOUNTS */

/* RAIDBOSS_POINTS -> EMPTY */
/* owner_id boss_id points, filled by server */
/* END OF RAIDBOSS_POINTS */

/* RAIDBOSS_STATUS -> EMPTY */
/* Filled by server, perhaps we can get data from l2j: raidboss_spawnlist and put here? */
/* The server will put the data itself (id, respawn_delay), as is in the OBT TODO: ask ico */
/* END OF RAIDBOSS_STATUS */

/* RANDOM_SPAWN - No need to empty it */
/* Table is filled by default */
/* END OF RANDOM_SPAWN */

/* RANDOM_SPAWN_LOC - No need to empthy it */
/* Table is filled by default */
/* END OF RANDOM_SPAWN_LOC */

/* REFERRAL_SYSTEM -> EMPTY */
/* from referral_system.sql - it is the same */
INSERT INTO `l2age_rebellion`.`referral_system` (`senderAccount`, `senderHWID`, `senderCharacter`, `senderReward`, `referralName`, `date`, `rewarded`)
SELECT `referral_system`.`senderAccount`, `referral_system`.`senderHWID`, `referral_system`.`senderCharacter`, `referral_system`.`senderReward`, `referral_system`.`referralName`, `referral_system`.`date`, `referral_system`.`rewarded`
FROM `referral_system`;
/* END OF REFERRAL_SYSTEM */

/* REFFERAL_SYSTEM - what the fuck is this table ? maybe added by mistake - todo: check and delete */

/* RESIDENCE_FUNCTIONS - EMPTY IT, AND DON'T TRANSFER IT */
/* vzima6 ot l2j castle_functions i clan_hall functions i gi slaga6 vav rebellion residense_functions. i pravi6 inDebt = 0 */
/* key: id, type */
/* UPDATE TABLE COLUMN id to INT(4) first .... */
/*
ALTER TABLE `l2age_rebellion`.`residence_functions` MODIFY COLUMN `id` int(4) unsigned NOT NULL DEFAULT '0';
*/
/* insert data */
/*
INSERT INTO `l2age_rebellion`.`residence_functions` (`id`, `type`, `lvl`, `lease`, `rate`)
SELECT `castle_functions`.`castle_id`, `castle_functions`.`type`, `castle_functions`.`lvl`, `castle_functions`.`lease`, `castle_functions`.`rate`
FROM `castle_functions`;
*/
/* endTime too large, need to insert it separately */
/*
UPDATE `l2age_rebellion`.`residence_functions` AS a JOIN `castle_functions` AS b ON (a.`id` = b.`castle_id`) SET a.`endTime` = substring(b.`endTime`, 1, length(b.`endTime`) - 3);

INSERT INTO `l2age_rebellion`.`residence_functions` (`id`, `type`, `lvl`, `lease`, `rate`)
SELECT `clanhall_functions`.`hall_id`, `clanhall_functions`.`type`, `clanhall_functions`.`lvl`, `clanhall_functions`.`lease`, `clanhall_functions`.`rate`
FROM `clanhall_functions`;
*/
/* endTime too large, need to insert it separately */
/*
UPDATE `l2age_rebellion`.`residence_functions` AS a JOIN `clanhall_functions` AS b ON (a.`id` = b.`hall_id`) SET a.`endTime` = substring(b.`endTime`, 1, length(b.`endTime`) - 3);
*/
/* inDebt = 0 for castles and clanhalls */
/*
UPDATE `l2age_rebellion`.`residence_functions` AS a JOIN `castle_functions` AS b ON (a.`id` = b.`castle_id`) SET a.`inDebt` = '0';
UPDATE `l2age_rebellion`.`residence_functions` AS a JOIN `clanhall_functions` AS b ON (a.`id` = b.`hall_id`) SET a.`inDebt` = '0';
*/
/* END OF RESIDENCE_FUNCTIONS */

/* SERVER_VARIABLES - No need to empty it */
/* we must compare it with l2j about scheduled global events as olympiad etc.. */ /* ---- TODO: ico */ 
/* rebellion sets the server variables with ServerVariables.set */
/* END OF SERVER_VARIABLES */

/* SEVEN_SIGNS -> EMPTY */
/* note: cabal l2j - dusk/dawn/empty */
/* cabal rebellion - dusk/dawn/No Cabal (default) */
/* from seven_signs - more or less same ... */
INSERT INTO `l2age_rebellion`.`seven_signs` (`char_obj_id`, `seal`, `dawn_red_stones`, `dawn_green_stones`, `dawn_blue_stones`, `dawn_ancient_adena_amount`, `dawn_contribution_score`)
SELECT `seven_signs`.`charId`, `seven_signs`.`seal`, `seven_signs`.`red_stones`, `seven_signs`.`green_stones`, `seven_signs`.`blue_stones`, `seven_signs`.`ancient_adena_amount`, `seven_signs`.`contribution_score`
FROM `seven_signs`;
/* `cabal` = 'No Cabal' by default, so we only update where l2j cabal != '' */
UPDATE `l2age_rebellion`.`seven_signs` AS a JOIN `l2agefortests`.`seven_signs` AS b ON (b.`charId` = a.`char_obj_id`) SET a.`cabal` = b.`cabal` WHERE b.`cabal` != '';
/* NOT NEEDED - update empty cabal to 'No Cabal' */
/* UPDATE `l2age_rebellion`.`seven_signs` AS a JOIN `l2agefortests`.`seven_signs` AS b ON (b.`charId` = a.`char_obj_id`) SET a.`cabal` = 'No Cabal' WHERE a.`cabal` = ''; */
/* END OF SEVEN_SIGNS */

/* SEVEN_SIGNS_FESTIVAL - No need to empty it */
/* Table is filled by default */
/* END OF SEVEN_SIGNS_FESTIVAL */

/* SEVEN_SIGNS_STATUS -> EMPTY */
INSERT INTO `l2age_rebellion`.`seven_signs_status` (`current_cycle`, `festival_cycle`, `active_period`, `previous_winner`, `dawn_stone_score`, `dawn_festival_score`, `dusk_stone_score`, `dusk_festival_score`, `avarice_owner`, `gnosis_owner`, `strife_owner`, `avarice_dawn_score`, `gnosis_dawn_score`, `strife_dawn_score`, `avarice_dusk_score`, `gnosis_dusk_score`, `strife_dusk_score`, `accumulated_bonus0`, `accumulated_bonus1`, `accumulated_bonus2`, `accumulated_bonus3`, `accumulated_bonus4`)
SELECT `seven_signs_status`.`current_cycle`, `seven_signs_status`.`festival_cycle`, `seven_signs_status`.`active_period`, `seven_signs_status`.`previous_winner`, `seven_signs_status`.`dawn_stone_score`, `seven_signs_status`.`dawn_festival_score`, `seven_signs_status`.`dusk_stone_score`, `seven_signs_status`.`dusk_festival_score`, `seven_signs_status`.`avarice_owner`, `seven_signs_status`.`gnosis_owner`, `seven_signs_status`.`strife_owner`, `seven_signs_status`.`avarice_dawn_score`, `seven_signs_status`.`gnosis_dawn_score`, `seven_signs_status`.`strife_dawn_score`, `seven_signs_status`.`avarice_dusk_score`, `seven_signs_status`.`gnosis_dusk_score`, `seven_signs_status`.`strife_dusk_score`, `seven_signs_status`.`accumulated_bonus0`, `seven_signs_status`.`accumulated_bonus1`, `seven_signs_status`.`accumulated_bonus2`, `seven_signs_status`.`accumulated_bonus3`, `seven_signs_status`.`accumulated_bonus4`
FROM `seven_signs_status`;
/* date */
UPDATE `l2age_rebellion`.`seven_signs_status` AS a JOIN `seven_signs_status` AS b ON (a.`current_cycle` = b.`current_cycle`)
SET a.`date` = DAYOFWEEK(FROM_UNIXTIME(b.date / 1000));
/* END OF SEVEN_SIGNS_STATUS */

/* SIEGE_CLANS -> EMPTY */
/* leave it empty its for sieges clan registration */ 
/* END OF SIEGE_CLANS */

/* SIEGE_GUARDS (l2j: castle_siege_guards.sql) -> No need to empty it */
/* Nothing to do here - table is filled by default */
/* END OF SIEGE_GUARDS */

/* SIEGE_PLAYERS -> EMPTY */
/* again leave it empty they should reg ...*/ 
/* END OF SIEGE_PLAYERS */

/* VOTE -> EMPTY */
/* leave empty */
/* END OF VOTE */
