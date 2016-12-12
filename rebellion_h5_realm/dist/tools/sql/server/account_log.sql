DROP TABLE IF EXISTS `account_log`;
CREATE TABLE `account_log` (
  `time` int(11) NOT NULL,
  `login` varchar(32) NOT NULL,
  `ip` varchar(15) NOT NULL,
  `hwid` varchar(255) NOT NULL,
  KEY `login` (`login`),
  KEY `ip` (`ip`),
  KEY `hwid` (`hwid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;