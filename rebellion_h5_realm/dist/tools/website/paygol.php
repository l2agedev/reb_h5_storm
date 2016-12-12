<?php

// $log = 'paygol.txt';

// check that the request comes from PayGol server
if(!in_array($_SERVER['REMOTE_ADDR'], array('109.70.3.48', '109.70.3.146', '109.70.3.58')))
{
//	fopen($log, 'a');
//	fwrite($log, 'Unauthorized access from: '.$_SERVER['REMOTE_ADDR'].'.\n');
	header("HTTP/1.0 403 Forbidden");
	die("Error: Unknown IP");
//	fclose($log);
}

// get the variables from PayGol system
$message_id	= $_GET['message_id'];
$service_id	= $_GET['service_id'];
$shortcode	= $_GET['shortcode'];
$keyword	= $_GET['keyword'];
$message	= $_GET['message'];
$sender		= $_GET['sender'];
$operator	= $_GET['operator'];
$country	= $_GET['country'];
$custom		= $_GET['custom'];
$points		= $_GET['points'];
$price		= $_GET['price'];
$currency	= $_GET['currency'];

$dbuser = '';
$dbpass = '';
$dbhost = '';
$dbname = '';

$paygol_remote_addr = array("109.70.3.48", "109.70.3.146", "109.70.3.58");

$bonus_item_id = 13693; // Gracian Coin

function l2ws_find_char_id($char_name) {
        $char_name = mysql_real_escape_string($char_name);
        $res = mysql_query("SELECT obj_Id FROM characters WHERE char_name='$char_name'");
        $row = mysql_fetch_object($res);

        return $row->obj_Id;
}

function l2ws_item_order($owner_id, $l2_item_id, $count) {
	settype($owner_id, 'int');
	settype($count, 'int');
	settype($l2_item_id, 'int');
	
 	$charname = $_GET['custom'];
	$time = time();
	$current_time = date('M d Y H:i:s', $time);
	$price = $_GET['price'];
	$itemSender = 'System';
	$points = $_GET['points'];
	$message = $_GET['message'];

	if($l2_item_id) {
		if($owner_id) 
		{
			$res = mysql_query("SELECT MAX(itemNum)+1 AS new_itemNum FROM character_premium_items WHERE charId = '$owner_id'");
			$row = mysql_fetch_object($res);
			$new_itemNum = $row->new_itemNum;
			if($new_itemNum) 
			{
				mysql_query("INSERT INTO character_premium_items (
				charId,
				itemNum,
				itemId,
				itemCount,
				itemSender
				) VALUES (
					'$owner_id',
					'$new_itemNum',
					'$l2_item_id',
					'$count',
					'$itemSender'
					)
				");
				mysql_query("INSERT INTO sms_system (charName, price, points, PIN, date, status) VALUES ('$charname', '$price', '$points', '$message', '$current_time', 'SUCCESS')");
			} 
			else
			{
				mysql_query("INSERT INTO character_premium_items (
				charId,
				itemNum,
				itemId,
				itemCount,
				itemSender
				) VALUES (
					'$owner_id',
					'1',
					'$l2_item_id',
					'$count',
					'$itemSender'
					)
				");
				mysql_query("INSERT INTO sms_system (charName, price, points, PIN, date, status) VALUES ('$charname', '$price', '$points', '$message', '$current_time', 'SUCCESS')");

			}
		}
	}
}


if(in_array($_SERVER['REMOTE_ADDR'], $paygol_remote_addr))
{
	if (mysql_connect($dbhost, $dbuser, $dbpass, $dbname) and mysql_select_db($dbname))
	{
		$time = time();
		$current_time = date('M d Y H:i:s', $time);
		$charname = $_GET['custom'];
		$l2_char_id = l2ws_find_char_id($custom);
		if($l2_char_id) {
			l2ws_item_order($l2_char_id, $bonus_item_id, $points);
		}
		else
		{
			mysql_query("INSERT INTO sms_system (charName, price, points, PIN, date, status) VALUES ('$charname', '$price', '$points', '$message', '$current_time', 'FAILED')");	
		}
	} else {
		die(mysql_error());
	}
}
?>
