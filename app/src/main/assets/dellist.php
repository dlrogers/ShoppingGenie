<?php
// @ Copyright 2017 Dennis Rogers
//
//	auth.php: Code to create user authentication info.
//
//		inputs(POST):	command, email, password, listname (Strings)
//
//		command			Function
//
//		"new":	Set's up new user by adding email, password, and listname
//				to users table in the database admin and creating a new
//				database named with the email.
//		"add":	Adds a new list if it doesn't already exist by adding
//				email,password,and listname to users
//				and adding new table, named listname, to the database
//
// Set up php
ini_set("log_errors",1) ;
ini_set("error_log","php_error.log");
ini_set("max_execution_time",5);
//ini_set("ignore_user_abort",1);
error_reporting(E_ALL);

logError("starting dellist");

// Get username and password for mysql and log in
$config = parse_ini_file("../../config/config.ini");
$db = new mysqli('localhost',$config['username'],$config['password']);
//$db = new mysqli('localhost','symdesig_dennis','f0rspark$');
$db->set_charset("UTF-8");

// Open data stream and read in command, uid, pw, and list
$std = fopen("php://input","r");
$reply = sscanf(str_replace(" ","",fgets($std)),"%s");
$email = trim($reply[0],"\n");

$reply = sscanf(fgets($std),"%s");
$passwd = $reply[0];

$list = trim(fgets($std),"\n");

logError($email.",".$passwd.",".$list);
// logError("select * from users where email='".$email."'");
db_query("use symdesig_smartlist");
$rslt=db_query("SELECT * from users where email='".$email."' AND list='".$list."'");
if($rslt==false){
	logError("select error: ".$db->error);
	exit();
}
$row=$rslt->fetch_assoc();
if($row!=null) {
//	$rslt = db_query("SELECT * from users where email='".$email."' and list='".$list."'");
//	$row = $rslt->fetch_assoc();
	$id = $row['id'];
	db_query("UPDATE users SET rem=1 WHERE email='".$email."' AND list='".$list."'");
	db_query("DROP TABLE IF EXISTS T".$id);
	print("ok");
} else 
	print("nok");

register_shutdown_function('shutdown');
function shutdown(){
	exit();
}
function db_query($cmd) {
	global $db;
	logError($cmd);
	return $db->query($cmd);
}
function logError($str){
	error_log($str,0);
}

?>
