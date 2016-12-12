DROP TABLE IF EXISTS `premium_accounts`;
CREATE TABLE `premium_accounts` (
  `accountName` varchar(45) NOT NULL DEFAULT '',
  `templateId` smallint(4) NOT NULL DEFAULT '0',
  `endTime` bigint(13) NOT NULL DEFAULT '0',
  PRIMARY KEY (`accountName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;