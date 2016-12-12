DROP TABLE IF EXISTS `online`;
CREATE TABLE `online` (
  `index` int(1) NOT NULL DEFAULT '0',
  `totalOnline` int(6) NOT NULL DEFAULT '0',
  `totalOffline` int(6) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `online` VALUES ('0', '0', '0');