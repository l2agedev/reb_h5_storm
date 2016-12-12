DROP TABLE IF EXISTS `alternative_voting_reward`;
CREATE TABLE `alternative_voting_reward` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `data` varchar(255) NOT NULL,
  `scope` varchar(255) NOT NULL,
  `time` bigint(20) unsigned NOT NULL,
  `top` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;