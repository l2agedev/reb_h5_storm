DROP TABLE IF EXISTS `character_academy`;
CREATE TABLE `character_academy` (
  `clanId` int(11) NOT NULL DEFAULT '0',
  `charId` int(11) NOT NULL,
  `itemId` int(11) NOT NULL,
  `price` bigint(20) DEFAULT NULL,
  `time` bigint(20) DEFAULT NULL,
  PRIMARY KEY  (`charId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
