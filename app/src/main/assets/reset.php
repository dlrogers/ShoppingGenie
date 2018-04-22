<?php
/* @ Copyright 2017 Dennis Rogers
	Symbiotic Designs Confidential
	
	lostpw.php: Code to send lost password email
*/

ini_set("log_errors",1) ;
ini_set("error_log","php_error.log");
ini_set("max_execution_time",5);
// ini_set("ignore_user_abort",1);
error_reporting(E_ALL);
logError("starting reset.php");
// Open data stream and read in email
$std = fopen("php://input","r");
$reply = sscanf(str_replace(" ","",fgets($std)),"%s");
$email = trim($reply[0],"\n");
// Send email
$hdr[] = 'MIME-Version: 1.0';
$hdr[] = 'Content-type: text/html; charset=iso-8859-1';
$link = "http://symdesign.us/php/reset_action.html";
$title="reset link";
$code="reset code";
$msg = "In order to reset you password, Click on the following link: 
	<a href='".$link."'>" .$title. "</a> and provide the following 
	reset code: ".$code; 
mail("dlrog@yahoo.com","Password Reset",$msg,implode("\r\n",$hdr));
print("ok");
function db_query($cmd) {
	global $db;
	logError($cmd);
	return $db->query($cmd);
}
function logError($str){
	error_log($str,0);
}
?>