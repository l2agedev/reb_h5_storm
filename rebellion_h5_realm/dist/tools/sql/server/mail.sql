CREATE TABLE IF NOT EXISTS `mail` (
  `message_id` int(11) NOT NULL AUTO_INCREMENT,
  `sender_id` int(11) NOT NULL,
  `sender_name` varchar(32) CHARACTER SET utf8 NOT NULL,
  `receiver_id` int(10) NOT NULL,
  `receiver_name` varchar(32) CHARACTER SET utf8 NOT NULL,
  `expire_time` int(11) NOT NULL,
  `topic` tinytext CHARACTER SET utf8 NOT NULL,
  `body` text CHARACTER SET utf8 NOT NULL,
  `price` bigint(20) NOT NULL,
  `type` tinyint(4) NOT NULL DEFAULT '0',
  `unread` tinyint(4) NOT NULL DEFAULT '1',
  `returned` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`message_id`),
  KEY `sender_id` (`sender_id`),
  KEY `receiver_id` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;