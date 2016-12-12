CREATE TABLE `community_bookmark` (
  `teleportId` int(11) NOT NULL AUTO_INCREMENT,
  `objId` int(11) DEFAULT NULL,
  `teleportName` varchar(250) NOT NULL default '',
  `x` int(9) NOT NULL default '0', 
  `y` int(9) NOT NULL default '0', 
  `z` int(9) NOT NULL default '0', 
  PRIMARY KEY (`teleportId`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;