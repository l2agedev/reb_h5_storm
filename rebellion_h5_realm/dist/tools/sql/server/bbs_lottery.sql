DROP TABLE IF EXISTS `bbs_lottery`;
CREATE TABLE `bbs_lottery` (
	`count` INT(11) NOT NULL DEFAULT '0',
	`type` VARCHAR(86) NOT NULL DEFAULT '0',
	`name` VARCHAR(86) CHARACTER SET UTF8 NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO bbs_lottery VALUES ('0', 'total_games', '0');
INSERT INTO bbs_lottery VALUES ('0', 'jackpot', '0');
