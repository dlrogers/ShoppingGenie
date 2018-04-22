<?php
/* @ Copyright 2017 Dennis Rogers

	sync.php: Code to sync a list on the phone with counterpart on the server

	inputs(via POST):	email, password, listname (new line delimited Strings)

*/
// Setup error reporting
//header("charset=utf-8");
ini_set("log_errors",1) ;
ini_set("error_log","php_error.log");
error_reporting(E_ALL);
logError("starting sync");

// Log into database with credentials in config.ini
$config = parse_ini_file("../../config/config.ini");
$db = new mysqli('localhost',$config['username'],$config['password']);
$db->set_charset("UTF-8");

// Read credentials
$std = fopen("php://input","r");
$reply = sscanf(str_replace(" ","",fgets($std)),"%s");
$email = trim($reply[0],"\n");

$reply = sscanf(fgets($std),"%s") ;
$passwd = $reply[0];


$list = trim(fgets($std),"\n");

function db_query($cmd) {
	global $db;
	logError($cmd);
	return $db->query($cmd);
}
function logError($str){
	error_log($str,0);
}
?>