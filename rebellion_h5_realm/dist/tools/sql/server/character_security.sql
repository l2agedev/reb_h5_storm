DROP TABLE IF EXISTS `character_security`;
CREATE TABLE `character_security` (
  `charId` int(12) NOT NULL,
  `password` varchar(255) CHARACTER SET latin1 DEFAULT NULL,
  `activationDate` bigint(13) DEFAULT NULL,
  `activationHWID` varchar(255) DEFAULT NULL,
  `changeDate` bigint(13) DEFAULT NULL,
  `changeHWID` varchar(255) DEFAULT NULL,
  `remainingTries` tinyint(1) DEFAULT '3',
  PRIMARY KEY (`charId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;