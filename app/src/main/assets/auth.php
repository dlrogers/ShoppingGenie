<?php
// @ Copyright 2017 Dennis Rogers
//
//	auth.php: Code to create user authentication info.
//
//	inputs(via POST):	email, password, listname (new line delimited Strings)
//
//	if(no email)	Set's up new user by adding email, password, and listname 
//					to users table in the database admin and creating a new 
//					database named with the email containing the new list.
//	if(email but no list)
//					Adds a new list by adding 
//					email,password,and listname to users table
//					and adding new table, named listname, to the database
//	else 
//					returns "exists"
// Set up php
ini_set("log_errors",1) ;
ini_set("error_log","php_error.log");
ini_set("max_execution_time",5);
//ini_set("ignore_user_abort",1);
error_reporting(E_ALL);

logError("starting auth");

// Get username and password for mysql and log in
$config = parse_ini_file("../../config/config.ini");
$db = new mysqli('localhost','symdesig_dennis','f0rspark$');
$db->set_charset("UTF-8");

// Open data stream and read in command, uid, pw, and list
$std = fopen("php://input","r");
$txt = trim(fgets($std),"\n");
$reply = sscanf(str_replace(" ","",$txt),"%s");
$email = $reply[0];

$reply = sscanf(fgets($std),"%s");
$passwd = $reply[0];

//$reply = sscanf(fgets($std),"%s");
//$list = $reply[0];
$list = trim(fgets($std),"\n");

logError($email.",".$passwd.",".$list);

// logError("select * from users where email='".$email."'");
$db->query("use symdesig_smartlist");
$rslt = $db->query("SELECT * from users where email='".$email."'");
if($rslt ==  false) logError("rslt is false");
if( ($rslt->fetch_assoc()) == null) {
	logError("adding email and list");
	db_query("INSERT INTO users SET email='".$email."',passwd='".$passwd."',list='".$list."',rem=0");	
	$rslt = db_query("SELECT * from users where email='".$email."' and list='".$list."'");
	$row = $rslt->fetch_assoc();
	$id = $row['id'];
	logError(sprintf("id = %d",$id));
	db_query("CREATE TABLE T".sprintf("%d",$id)."(name VARCHAR(128), flags INT, last_time INT, last_avg INT, ratio REAL)");
	print("ok");
} else {
	logError("adding list");
	$rslt = db_query("SELECT * from users where email='".$email."' and list='".$list."'");
	$row = $rslt->fetch_assoc();
	if($row == null) {
		db_query("INSERT INTO users SET email='".$email."',passwd='".$passwd."',list='".$list."',rem=0");
		$rslt = db_query("SELECT * from users where email='".$email."' and list='".$list."'");		
		$row = $rslt->fetch_assoc();
		$id = $row['id'];
		db_query("CREATE TABLE T".sprintf("%d",$id)."(name VARCHAR(128), flags INT, last_time INT, last_avg INT, ratio REAL)");			
		print("ok");
	} else
		print("exists");
}

register_shutdown_function('shutdown');

function read_data(){
	
	}
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