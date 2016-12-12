-- ----------------------------
-- Table structure for `scheme_buffer_profiles`
-- ----------------------------
DROP TABLE IF EXISTS `scheme_buffer_profiles`;
CREATE TABLE `scheme_buffer_profiles` (
  `charId` int(10) unsigned NOT NULL,
  `profile` varchar(45) NOT NULL DEFAULT '',
  `buffs` text,
  PRIMARY KEY (`charId`,`profile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;