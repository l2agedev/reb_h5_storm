DROP TABLE IF EXISTS `character_achievement_levels`;
CREATE TABLE `character_achievement_levels` (
  `char_id` int(11) NOT NULL,
  `achievement_levels` varchar(500) NOT NULL default '',
  PRIMARY KEY  (`char_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;