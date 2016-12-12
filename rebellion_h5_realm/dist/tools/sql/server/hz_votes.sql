DROP TABLE IF EXISTS `hz_votes`;
CREATE TABLE `hz_votes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(255) NOT NULL,
  `hostname` varchar(255) NOT NULL,
  `ts` bigint(13) NOT NULL DEFAULT '0',
  `rewardsLeft` int(2) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip` (`ip`,`ts`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;