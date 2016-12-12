CREATE TABLE IF NOT EXISTS `mail_attachments` (
  `message_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  UNIQUE KEY `item_id` (`item_id`),
  KEY `messageId` (`message_id`),
  FOREIGN KEY (`message_id`) REFERENCES `mail` (`message_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;