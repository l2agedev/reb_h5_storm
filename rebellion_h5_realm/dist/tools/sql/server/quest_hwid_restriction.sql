DROP TABLE IF EXISTS `quest_hwid_restriction`;
CREATE TABLE `quest_hwid_restriction` (
  `hwid` varchar(255) NOT NULL,
  `questname` varchar(255) NOT NULL,
  `restricUntil` bigint(20) NOT NULL,
  PRIMARY KEY (`hwid`, `questname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;