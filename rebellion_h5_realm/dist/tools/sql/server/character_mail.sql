CREATE TABLE IF NOT EXISTS `character_mail` (
  `char_id` int(11) NOT NULL,
  `message_id` int(11) NOT NULL,
  `is_sender` tinyint(1) NOT NULL,
  PRIMARY KEY (`char_id`,`message_id`),
  KEY `message_id` (`message_id`),
  FOREIGN KEY (`message_id`) REFERENCES `mail` (`message_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;