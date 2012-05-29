<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>

<title>File Uploading Form</title>

</head>

<body bgcolor="#fffff2"> 
<form action="index.jsp" method="post" enctype="multipart/form-data"> 
<table border="0"> 
<tr><td valign="top"><strong>User name:</strong><br></td>

<td><input type="text" name="userName"> </td></tr>

<tr><td valign="top"><strong>Comment:</strong><br></td>

<td>
<textarea rows='5' cols='20' name="message"> </textarea>

</td></tr>


<tr><td valign="top"><strong>Please choose the file:</strong><br></td>

<td><input type="file" name="fileName"> </td></tr>

<tr><td><input type="submit" value="Submit"></td></tr>

</table>

</form>

</body>

</html>