<?php
session_start();
if(!($_SESSION['userID'] && $_SESSION['userPW'])) {
    exit;
}

$query = $_REQUEST['search'];

    include_once $_SERVER['DOCUMENT_ROOT'].'/db.info.php';
    require_once $_SERVER['DOCUMENT_ROOT'].'/phpclass/dbClass.php';
    require_once $_SERVER['DOCUMENT_ROOT'].'/phpclass/xmlClass.php';

    $conn = new MySQLiDbClass();
    $DB_CONNECT = $conn->isConnectDb($DB);


    $sql = "select uid,name,mobile from Person ";
    if(!empty($query)) {
        $sql .= "where name LIKE '%".$query."%' or mobile LIKE '%".$query."%'";
    }
    $result = mysqli_query($DB_CONNECT,$sql);


    $c = new jsonClass();
    echo $c->JSONEncode($result,'result');

    ?>