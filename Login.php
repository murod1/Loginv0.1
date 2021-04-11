<?php
session_start();
if(isset($_POST['loginID']) && !empty($_POST['loginID']) && isset($_POST['loginPW']) && !empty($_POST['loginPW'])) {
    $loginID = trim($_POST['loginID']);
    $loginPW = trim($_POST['loginPW']);
    $deviceID = trim($_POST['deviceID']);

    $deviceID = $deviceID ? $deviceID : '';  

    if(empty($deviceID)){
        require_once $_SERVER['DOCUMENT_ROOT'].'/dbconnect.php';
        require_once $_SERVER['DOCUMENT_ROOT'].'/phpclass/loginClass.php';
        $c=new LoginClass();

        $row = $c->WebUserAuthCheck($loginID,$loginPW);
        if(is_array($row)) {
            if($row['code'] > 0) {
                $_SESSION['userID'] = $row['id'];
                $_SESSION['userPW'] = md5($loginPW);
                $_SESSION['code'] = $row['code'];
                $_SESSION['ip'] = $_SERVER['REMOTE_ADDR'];
                $_SESSION['ua'] = $_SERVER['HTTP_USER_AGENT'];

                echo("<meta http-equiv='Refresh' content='0; URL=mobile/list.php'>");
            }
        }

    } else {
        require_once $_SERVER['DOCUMENT_ROOT'].'/db.info.php';
        require_once $_SERVER['DOCUMENT_ROOT'].'/phpclass/dbClass.php';
        $conn=new MySQLiDbClass();
        $DB_CONNECT = $conn->isConnectDb($DB);

        require_once $_SERVER['DOCUMENT_ROOT'].'/phpclass/loginClass.php';
        $c=new LoginClass();

        $result = $c->MobileUserAuthCheck($loginID,$loginPW,$deviceID);
        if($result > 0 ) {
            session_save_path('./_tmp/session');

            $_SESSION['userID'] = $loginID;
            $_SESSION['userPW'] = md5($loginPW);
            $_SESSION['ip'] = $_SERVER['REMOTE_ADDR'];
            $_SESSION['ua'] = $_SERVER['HTTP_USER_AGENT'];
            echo 'Login Success';
        } else if($result == '0') {
            echo 'Login Fail';
        } else {
            echo 'Phone Dismatch';
        }
    }

} else {
    echo("<meta http-equiv='Refresh' content='0; URL=loginForm.php'>");
}
?>

