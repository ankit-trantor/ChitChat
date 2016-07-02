<?php 

	if (isset($_POST["Token"])) {
		
		   $_uv_Token=$_POST["Token"];
		   $_uv_Name=$_POST["Name"];

		   $conn = mysqli_connect("localhost","root","kapil","chatdb") or die("Error connecting");

		   $q="INSERT INTO users (Token, Name) VALUES ( '$_uv_Token', '$_uv_Name') "
              ." ON DUPLICATE KEY UPDATE Token = '$_uv_Token';";
              
      mysqli_query($conn,$q) or die(mysqli_error($conn));

      mysqli_close($conn);

	}


 ?>
