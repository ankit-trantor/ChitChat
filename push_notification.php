<?php 

	function send_notification ($pushtoken, $message)
	{
		$url = 'https://fcm.googleapis.com/fcm/send';
		$fields = array(
			 'registration_ids' => $pushtoken,
			 'data' => $message
			);

		$headers = array(
			'Authorization:key = AIzaSyAfiSe1JmNV64jbyoVLVuWGhdz_vadRF4Y ',
			'Content-Type: application/json'
			);

	   $ch = curl_init();
       curl_setopt($ch, CURLOPT_URL, $url);
       curl_setopt($ch, CURLOPT_POST, true);
       curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
       curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
       curl_setopt ($ch, CURLOPT_SSL_VERIFYHOST, 0);  
       curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
       curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
       $result = curl_exec($ch);           
       if ($result === FALSE) {
           die('Curl failed: ' . curl_error($ch));
       }
       curl_close($ch);
       return $result;
	}
	
	
	if (isset($_POST["UserId"])) {
		$_uv_Token=$_POST["UserId"];

	$conn = mysqli_connect("localhost","root","kapil","chatdb");

	$sql = " Select Token,Name From users";

	$result = mysqli_query($conn,$sql);
	$tokens = array();
	$names=array();
	$pushtoken=array();
	$index=1000;

	if(mysqli_num_rows($result) > 0 ){

		while ($row = mysqli_fetch_assoc($result)) {
			$tokens[] = $row["Token"];
			$names[]=$row["Name"];
		}
	}
	foreach ($names as $key_name => $key_value) {

	print "Key = " . $key_name . " Value = " . $key_value . "<BR>";
	if($key_value==$_uv_Token)
	{   $index=$key_name;
		
	}
		
 
}
$pushtoken[]=$tokens["$index"];
echo $pushtoken["0"];

	

	mysqli_close($conn);
	
	}
		
	$message = array("message" => "$_uv_Token");
	$message_status = send_notification($pushtoken, $message);
	echo $message_status;
		



 ?>
