<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Broker Info</title>

<script src="http://code.jquery.com/jquery-2.1.4.min.js"></script>

<script><!--

var infoResourceURL = "api/info";

function update() {
	
	$.getJSON(infoResourceURL, function(data) {
		
	  	$( "#brokerID" ).text(data.brokerID);
  		$( "#brokerName" ).text(data.brokerName);
  		$( "#brokerVersion" ).text(data.brokerVersion);
  		
  		$( "#memory" ).text((data.runtime.memoryLimit / 1024 / 1024) + "MB (" + data.runtime.memoryPercentUsage + "% used)");
  		$( "#store" ).text((data.runtime.storeLimit / 1024 / 1024) + "MB (" + data.runtime.storePercentUsage + "% used)");
  		$( "#temp" ).text((data.runtime.tempLimit / 1024 / 1024) + "MB (" + data.runtime.tempPercentUsage + "% used)");
  		
  		$( "#uptime" ).text((data.runtime.uptimeMillis / 1000) + "s");
  		
	});
}

$(document).ready(update);

//--></script>

</head>
<body>

<h1>Broker Info</h1>

<div><table>
  <tr><th colspan="2">Broker</th></tr>
  <tr><th>ID</th><td><span id="brokerID" /></td></tr>
  <tr><th>Name</th><td><span id="brokerName" /></td></tr>
  <tr><th>Version</th><td><span id="brokerVersion" /></td></tr>
</table></div>

<div><table>
  <tr><th colspan="2">Runtime</th></tr>
  <tr><th>Memory</th><td><span id="memory" /></td></tr>
  <tr><th>Store</th><td><span id="store" /></td></tr>
  <tr><th>Temp</th><td><span id="temp" /></td></tr>
  <tr><th>Uptime</th><td><span id="uptime" /></td></tr>
</table></div>

</body>
</html>