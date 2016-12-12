DROP TABLE IF EXISTS `custom_auction`;
CREATE TABLE `custom_auction` (
  `auction_id` int(10) NOT NULL,
  `seller_id` int(10) NOT NULL,
  `item_obj` int(10) NOT NULL,
  `max_bid` bigint(20) NOT NULL,
  `buyout_price` bigint(20) NOT NULL DEFAULT '0',
  `last_bider` int(10) NOT NULL,
  `auction_end` bigint(20) NOT NULL,
  `all_bids` int(20) NOT NULL,
  `isgolden` tinyint(2) NOT NULL DEFAULT '0',
  PRIMARY KEY (`auction_id`,`item_obj`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;