CREATE TABLE `vote` (
  `id` int(10) NOT NULL DEFAULT '0',
  `HWID` varchar(255) NOT NULL DEFAULT '',
  `vote` int(10) NOT NULL DEFAULT '0',
  `comment` text NOT NULL,
  PRIMARY KEY (`id`,`HWID`,`vote`),
  KEY `Index 2` (`id`,`vote`),
  KEY `Index 3` (`id`),
  KEY `Index 4` (`HWID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;