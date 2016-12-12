CREATE TABLE IF NOT EXISTS `clanhall` (
  `id` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `name` varchar(40) NOT NULL DEFAULT '',
  `last_siege_date` bigint(20) NOT NULL,
  `own_date` bigint(20) NOT NULL,
  `siege_date` bigint(20) NOT NULL,
  `auction_min_bid` bigint(20) NOT NULL,
  `auction_length` int(11) NOT NULL,
  `auction_desc` text,
  `cycle` int(11) NOT NULL,
  `paid_cycle` int(11) NOT NULL,
  `location` varchar(40) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`,`name`)
);

-- ----------------------------
-- Records of clanhall
-- ----------------------------
INSERT INTO `clanhall` VALUES ('21', 'Fortress of Resistance', '0', '0', '0', '0', '0', null, '0', '0', 'Dion');
INSERT INTO `clanhall` VALUES ('22', 'Moonstone Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Gludio');
INSERT INTO `clanhall` VALUES ('23', 'Onyx Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Gludio');
INSERT INTO `clanhall` VALUES ('24', 'Topaz Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Gludio');
INSERT INTO `clanhall` VALUES ('25', 'Ruby Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Gludio');
INSERT INTO `clanhall` VALUES ('26', 'Crystal Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Gludin');
INSERT INTO `clanhall` VALUES ('27', 'Onyx Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Gludin');
INSERT INTO `clanhall` VALUES ('28', 'Sapphire Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Gludin');
INSERT INTO `clanhall` VALUES ('29', 'Moonstone Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Gludin');
INSERT INTO `clanhall` VALUES ('30', 'Emerald Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Gludin');
INSERT INTO `clanhall` VALUES ('31', 'The Atramental Barracks', '0', '0', '0', '0', '0', null, '0', '0', 'Dion');
INSERT INTO `clanhall` VALUES ('32', 'The Scarlet Barracks', '0', '0', '0', '0', '0', null, '0', '0', 'Dion');
INSERT INTO `clanhall` VALUES ('33', 'The Viridian Barracks', '0', '0', '0', '0', '0', null, '0', '0', 'Dion');
INSERT INTO `clanhall` VALUES ('34', 'Devastated Castle', '0', '0', '0', '0', '0', null, '0', '0', 'Aden');
INSERT INTO `clanhall` VALUES ('35', 'Bandit Stronghold', '0', '0', '0', '0', '0', null, '0', '0', 'Oren');
INSERT INTO `clanhall` VALUES ('36', 'The Golden Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Aden');
INSERT INTO `clanhall` VALUES ('37', 'The Silver Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Aden');
INSERT INTO `clanhall` VALUES ('38', 'The Mithril Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Aden');
INSERT INTO `clanhall` VALUES ('39', 'Silver Manor', '0', '0', '0', '0', '0', null, '0', '0', 'Aden');
INSERT INTO `clanhall` VALUES ('40', 'Gold Manor', '0', '0', '0', '0', '0', null, '0', '0', 'Aden');
INSERT INTO `clanhall` VALUES ('41', 'The Bronze Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Aden');
INSERT INTO `clanhall` VALUES ('42', 'The Golden Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Giran');
INSERT INTO `clanhall` VALUES ('43', 'The Silver Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Giran');
INSERT INTO `clanhall` VALUES ('44', 'The Mithril Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Giran');
INSERT INTO `clanhall` VALUES ('45', 'The Bronze Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Giran');
INSERT INTO `clanhall` VALUES ('46', 'Silver Manor', '0', '0', '0', '0', '0', null, '0', '0', 'Giran');
INSERT INTO `clanhall` VALUES ('47', 'Moonstone Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Goddard');
INSERT INTO `clanhall` VALUES ('48', 'Onyx Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Goddard');
INSERT INTO `clanhall` VALUES ('49', 'Emerald Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Goddard');
INSERT INTO `clanhall` VALUES ('50', 'Sapphire Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Goddard');
INSERT INTO `clanhall` VALUES ('51', 'Mont Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Rune');
INSERT INTO `clanhall` VALUES ('52', 'Astaire Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Rune');
INSERT INTO `clanhall` VALUES ('53', 'Aria Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Rune');
INSERT INTO `clanhall` VALUES ('54', 'Yiana Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Rune');
INSERT INTO `clanhall` VALUES ('55', 'Roien Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Rune');
INSERT INTO `clanhall` VALUES ('56', 'Luna Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Rune');
INSERT INTO `clanhall` VALUES ('57', 'Traban Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Rune');
INSERT INTO `clanhall` VALUES ('58', 'Eisen Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Schuttgart');
INSERT INTO `clanhall` VALUES ('59', 'Heavy Metal Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Schuttgart');
INSERT INTO `clanhall` VALUES ('60', 'Molten Ore Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Schuttgart');
INSERT INTO `clanhall` VALUES ('61', 'Titan Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Schuttgart');
INSERT INTO `clanhall` VALUES ('62', 'Rainbow Springs', '0', '0', '0', '0', '0', null, '0', '0', 'Goddard');
INSERT INTO `clanhall` VALUES ('63', 'Wild Beast Reserve', '0', '0', '0', '0', '0', null, '0', '0', 'Rune');
INSERT INTO `clanhall` VALUES ('64', 'Fortress of the Dead', '0', '0', '0', '0', '0', null, '0', '0', 'Rune');
-- Custom Clanhalls
INSERT INTO `clanhall` VALUES ('122', 'Titanum Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Oren');
INSERT INTO `clanhall` VALUES ('123', 'Knights Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Oren');
INSERT INTO `clanhall` VALUES ('124', 'Phoenix Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Oren');
INSERT INTO `clanhall` VALUES ('125', 'Waterfall Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Heine');
INSERT INTO `clanhall` VALUES ('126', 'Giants Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Heine');
INSERT INTO `clanhall` VALUES ('127', 'Earth Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Heine');
INSERT INTO `clanhall` VALUES ('128', 'Wenus Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Heine');
INSERT INTO `clanhall` VALUES ('129', 'Saturn Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Heine');
INSERT INTO `clanhall` VALUES ('130', 'Hunters Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Hunters');
INSERT INTO `clanhall` VALUES ('131', 'Forbidden Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Hunters');
INSERT INTO `clanhall` VALUES ('132', 'Enchanted Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Hunters');
INSERT INTO `clanhall` VALUES ('133', 'Lion Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Floran');
INSERT INTO `clanhall` VALUES ('134', 'Puma Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Floran');
INSERT INTO `clanhall` VALUES ('135', 'Broken Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Gludin');
INSERT INTO `clanhall` VALUES ('136', 'Talking Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Talking');
INSERT INTO `clanhall` VALUES ('137', 'Silent Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Talking');
INSERT INTO `clanhall` VALUES ('138', 'Dusk Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'DElven');
INSERT INTO `clanhall` VALUES ('139', 'Night Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'DElven');
INSERT INTO `clanhall` VALUES ('140', 'Moonlight Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Elven');
INSERT INTO `clanhall` VALUES ('141', 'Dawn Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Elven');
INSERT INTO `clanhall` VALUES ('142', 'Deviastated Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Despair');
INSERT INTO `clanhall` VALUES ('143', 'Deviastated Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'Despair');
INSERT INTO `clanhall` VALUES ('144', 'Burned Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Despair');
INSERT INTO `clanhall` VALUES ('145', 'Hall of Agony', '0', '0', '0', '0', '0', null, '0', '0', 'Agony');
INSERT INTO `clanhall` VALUES ('146', 'Ruined Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Agony');
INSERT INTO `clanhall` VALUES ('147', 'Wrecked Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Agony');
INSERT INTO `clanhall` VALUES ('148', 'Demolished Hall', '0', '0', '0', '0', '0', null, '0', '0', 'Agony');
INSERT INTO `clanhall` VALUES ('149', 'Hell Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'HBound');
INSERT INTO `clanhall` VALUES ('150', 'Desert Chamber', '0', '0', '0', '0', '0', null, '0', '0', 'HBound');
INSERT INTO `clanhall` VALUES ('151', 'Hall of Illusion', '0', '0', '0', '0', '0', null, '0', '0', 'Forest');
