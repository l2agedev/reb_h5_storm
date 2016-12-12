DROP TABLE IF EXISTS `referral_system`;
CREATE TABLE `referral_system` (
  `senderAccount` varchar(45) DEFAULT NULL,
  `senderHWID` varchar(100) NOT NULL,
  `senderCharacter` varchar(45) DEFAULT NULL,
  `senderReward` int(7) NOT NULL DEFAULT '1000',
  `referralName` varchar(45) NOT NULL DEFAULT '-',
  `date` date NOT NULL DEFAULT '0000-00-00',
  `rewarded` tinyint(2) NOT NULL DEFAULT '0',
  PRIMARY KEY (`senderHWID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;