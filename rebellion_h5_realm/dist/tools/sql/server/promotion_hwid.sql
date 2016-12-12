DROP TABLE IF EXISTS `promotion_hwid`;
CREATE TABLE `promotion_hwid` (
  `name` varchar(255) NOT NULL DEFAULT '',
  `hwid` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`name`, `hwid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;