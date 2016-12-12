DROP TABLE IF EXISTS `hz_votes_hwids`;
CREATE TABLE `hz_votes_hwids` (
  `hwid` varchar(255) NOT NULL DEFAULT '',
  `lastRewardTime` decimal(20,0) NOT NULL DEFAULT '0',
  PRIMARY KEY (`hwid`),
  UNIQUE KEY `hwid` (`hwid`,`lastRewardTime`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;