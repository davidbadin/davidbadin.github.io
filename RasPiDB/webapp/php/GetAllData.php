<?php 

$servername = "http://192.168.0.101/";
$username = "testuser";
$password = "testpasswd";
$dbname = "testDB";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
// Check connection
if ($conn->connect_error) {
	die("Connection failed: " . $conn->connect_error);
}

$sql = "select * from TestTable";
 
$res = mysqli_query($conn,$sql);
 
$result = array();
 
while($row = mysqli_fetch_array($res)){
	array_push($result, 
	array('Id'=>$row[0],'Name'=>$row[1],'Rating'=>$row[2],'Date'=>$row[3]));
}
 
 echo json_encode(array('result'=>$result));
?>