DROP TABLE IF EXISTS `character_counters_monthly`;
CREATE TABLE `character_counters_monthly` (
  `season` smallint(5) NOT NULL,
  `type` varchar(50) NOT NULL,
  `data` varchar(255) NOT NULL,
  PRIMARY KEY (`season`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
