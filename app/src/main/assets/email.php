<?php
/* @ Copyright 2017 Dennis Rogers
	Symbiotic Designs Confidential
	
	email.php: Code to send lost password email
*/

ini_set("log_errors",1) ;
ini_set("error_log","php_error.log");
ini_set("max_execution_time",5);
//ini_set("ignore_user_abort",1);
error_reporting(E_ALL);

logError("starting email");
$hdr[] = 'MIME-Version: 1.0';
$hdr[] = 'Content-type: text/html; charset=iso-8859-1';
$link = "http://www.yahoo.com";
$msg = "Click on this link: <a href=".$link."'>" .$title. "</a>;
 
mail("dlrog@yahoo.com","a test",$msg,implode("\r\n",$hdr));

function logError($str){
	error_log($str,0);
}
?>