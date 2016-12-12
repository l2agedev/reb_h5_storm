DROP TABLE IF EXISTS `clan_notice`;
CREATE TABLE `clan_notice` (
`clan_id` INT UNSIGNED NOT NULL,
`type` SMALLINT NOT NULL DEFAULT '0',
`notice` text NOT NULL,
`lastUpdated` bigint(20) NOT NULL DEFAULT '0',
PRIMARY KEY(`clan_id`,`type`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
