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

//$reply = sscanf(fgets($std),"%s");

$list = trim(fgets($std),"\n");


// Search users for email and list
db_query("use symdesig_smartlist");
logError(sprintf("email = %s, passwd = %s, list = %s",$email,$passwd,$list));
$rslt = db_query("select * from users where email='".$email."' AND list='".$list."'");
$row=$rslt->fetch_assoc();
if($row == null) {
	$rslt = db_query("select * from users where email='".$email."'");
	$row=$rslt->fetch_assoc();
	if($row == null) {
		logError("sync: account not found!");
		print("nok");
		exit();		
	} else {
		db_query("INSERT INTO users SET email='".$email."',passwd='".$passwd."',list='".$list."',rem=0");	
		$rslt = db_query("SELECT * from users where email='".$email."' and list='".$list."'");
		$row = $rslt->fetch_assoc();
		$id = $row['id'];
		db_query("CREATE TABLE T".sprintf("%d",$id)."(name VARCHAR(128), flags INT, last_time INT, last_avg INT, ratio REAL)");		
	}
}
if($row["passwd"]===$passwd){					// Is password correct?
	$id = $row['id'];
	$tblname = "T".sprintf("%d",$id);
	if($row['rem']==1){	// Was previously deleted: must recreate table
		db_query("CREATE TABLE T".sprintf("%d",$id)."(name VARCHAR(128), flags INT, last_time INT, last_avg INT, ratio REAL)");		
		db_query("UPDATE users SET rem=0 WHERE ".sprintf("id=%d",$id));
	}
	$names=array();
	$n=0;
	$ok_sent = false;
	while($txt=fgets($std)) {				//Get an item from phone
		$txt=trim($txt,"\n");				//remove returns
		$p_cols = explode(',',$txt);			//separate into columns (name,flags,last_time,last_avg,ratio)
		logError(" \n");
		$rslt=db_query("SELECT * from `".$tblname."` where name=" . //Search server for item
				sprintf("'%s'",$p_cols[0]));
		$names[$n]=$p_cols[0];					//Save names in $names[]
		if($rslt!=false){
			$row = $rslt->fetch_assoc();
			if($row==null) {		//if(does not exist on server add it)
				logError("adding row");
				db_query(sprintf("INSERT INTO ".$tblname." VALUES('%s',%s,%s,%s,%s)",
						$p_cols[0],$p_cols[1],$p_cols[2],$p_cols[3],$p_cols[4]));
			} else {
				$scan = sscanf($p_cols[2],"%d");
				$plt = $scan[0];
				$slt = $row["last_time"];
				$scan = sscanf($p_cols[1],"%d");
				$pflg = $scan[0];
				$sflg = $row['flags'];
				logError(sprintf("From Server: flags = %d,%d, slt = %d, plt = %d, slt-plt = %d",$sflg,$pflg,$slt,$plt,$slt-$plt));
				if($slt>$plt) {		// if phone has not been changed
					if($sflg&2) {
						if(!$ok_sent) print("ok\n");
						$ok_sent = true;
						logError("deleting");
						printf("%s,%s,%s,%s,%s,%s\n",
								"d",$row['name'],$row['flags'],$row['last_time'],$row['last_avg'],$row['ratio']);
					} else {
						logError("changing phone "."flags = ".$row['flags']);
						if(!$ok_sent) print("ok\n");
						$ok_sent = true;
						printf("%s,%s,%s,%s,%s,%s\n",
								"u",$row['name'],$row['flags'],$row['last_time'],$row['last_avg'],$row['ratio']);
					}
				} else if($slt<$plt) {		// If phone copy changed: update server db
					logError("changing server");
					$rslt=db_query(sprintf(
							"UPDATE `".$tblname."` SET last_time=%s,last_avg=%s,ratio=%s,flags=%s WHERE name='%s'",
							$plt,$p_cols[3],$p_cols[4],$p_cols[1],$p_cols[0]));
					if(!$rslt) logError("dB update failed") ;
				}
				else if($sflg!=$pflg) {
					logError("changing flags");
					$rslt=db_query(sprintf(
							"UPDATE `".$tblname."` SET last_time=%s,last_avg=%s,ratio=%s,flags=%s WHERE name='%s'",
							$plt,$p_cols[3],$p_cols[4],$p_cols[1],$p_cols[0]));
					if(!$rslt) logError("dB update failed");
				} else {
					logError("dates same");
				}
				
			}
		}
		$n+=1;
	}
	logError("adding items not on phone");
	if(!$ok_sent) print("ok\n");
	$rslt=db_query("SELECT * FROM `".$tblname."`");
	while(($row=$rslt->fetch_assoc())!=null){
		logError("checking ".$row['name']);
		if(!inPhone($row['name'],$n)) {
			logError("Adding " . $row['name']);
			if(($row["flags"]&2)<1){
				printf("%s,%s,%s,%s,%s,%s\n",
						"i",$row['name'],$row['flags'],$row['last_time'],$row['last_avg'],$row['ratio']);
			}
		}
	}
} else {
	print("nok");
}
/*else {
	/*	logError($email." does not exist");
	 if(!db_query("INSERT INTO users VALUES('".$email."','".$list."','".$passwd."')"))
	 logError("insert error");
	 $rc=db_query("CREATE DATABASE `".$email."`");
	 $rc=db_query("USE `".$email."`");
	 $rc=db_query("CREATE TABLE `".$list."`(name TEXT, flags INT, last_time INT, last_avg INT, ratio REAL, rem INT)");
	 print("created");
}
*/

logError("done");

function inPhone($name,$n){
	global $names;
	for($j=0;$j<$n;$j+=1) {
		//		logError(sprintf("n = %d, name = %s, names[%d] = %s",$n,$name,$j,$names[$j]));
		if(strcmp($name,$names[$j])==0) return true;
	}
	return false;
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