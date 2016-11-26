<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<html>
<head>
<title>TwittMap</title>
<link rel="stylesheet" type="text/css" href="css/bootstrap.min.css" />
<style>
html, body {
	width: 100%;
	height: 100%;
}
</style>
</head>
<body
	style="margin: 0%; font-family: 'Comic Sans MS', 'Comic Sans', cursive;">
	<div>
		<div
			style="background-color: #0666c6; height: 50px; line-height: 50px; box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2);">
			<span
				style="margin-left: 1%; text-shadow: 2px 2px 10px black; font-size: 5vh; color: white">TwittMap</span>
			<span
				style="display: inline-block; float: right; margin-right: 1%; margin-top: 8px">
				<select id="filterSelect" class="form-control"
				onchange="filter()">
					<option>Columbia</option>
					<option>NYC</option>
					<option>Trump</option>
					<option>Google</option>
					<option>Facebook</option>
					<option>Weather</option>
					<option>Work</option>
					<option>Love</option>
					<option>Food</option>
					<option>Sports</option>
			</select>
			</span> <span style="float: right; margin-right: 1%; color: white;">Filter</span>
		</div>
		<div id="map" style="width: 100%; height: calc(100% - 50px);"></div>
	</div>

	<script type="text/javascript" src="js/jquery-3.1.1.min.js"></script>
	<script type="text/javascript" src="https://www.google.com/jsapi"></script>
	<script>
		var keyword = ["Columbia","NYC","Trump","Google","Facebook","Weather","Work","Love","Food","Sports"];
		var pointArray = new Array(keyword.length);
		var myMap;
		var markers = [];
		
		var localIP = document.domain + ":" + location.port;
		var wsUri = "ws://" + localIP + "/twitttrend/server";
		alert(wsUri);
		var websocket;
		
		function initMap() {
			websocket = new WebSocket(wsUri);
			websocket.onopen = function(evt) {
		        websocket.send(keyword[0]);
			}
			websocket.onmessage = function(evt) {
				var dataList = eval(evt.data);
				if(dataList.length > 0){
					var index = 0;
	
					for(var i = 0;i < keyword.length;i++){
						if(dataList[0].keyword == keyword[i]){
							index = i;
							break;
						}
					}
					
					if(dataList.length == 1){   //new data from backend
						var data = dataList[0];
						addMarker(data);
					}else{ //Search keywords
						pointArray[index] = [];
						for(var i = 0;i < dataList.length;i++){
							var data = dataList[i];
							pointArray[index].push(data);
						}
						replaceMarkers(pointArray[index]);
					}
				}else{
					alert("No data sent back!");
				}
			}
			websocket.onerror = function(evt) {
				alert('ERROR: ' + evt.data);
			}
			
	        myMap = new google.maps.Map(document.getElementById('map'), {
	          zoom: 3,
	          center: {lat: 40, lng: -100},
	          mapTypeId: google.maps.MapTypeId.TERREN
	        });	 
	      
	     }
		
		function replaceMarkers(tweets){	
			for(var i = 0;i < markers.length;i++){
				markers[i].setMap(null);
			}
			markers = [];
			
			markers = tweets.map(function(tweet, i) {
				var image;
				if(tweet.sentiment == 'POSITIVE'){
					image = 'image/smiley_happy.png';
				}else if(tweet.sentiment == 'NEGATIVE'){
					image = 'image/smiley_sad.png';
				}else{
					image = 'image/smiley_neutral.png';
				}
		        return new google.maps.Marker({
		           position: new google.maps.LatLng(tweet.latitude,
							tweet.longitude),
		           icon:image,
		           map:myMap,
		         });
		     });
		}
		
		function addMarker(tweet){
			var image;
			if(tweet.sentiment == 'POSITIVE'){
				image = 'image/smiley_happy.png';
			}else if(tweet.sentiment == 'NEGATIVE'){
				image = 'image/smiley_sad.png';
			}else{
				image = 'image/smiley_neutral.png';
			}
			if(tweet.keyword == $("#filterSelect").val()){
				alert("New tweet at (" + tweet.latitude + "," + tweet.longitude + ")");
				var marker = new google.maps.Marker({
			    	position: new google.maps.LatLng(tweet.latitude,tweet.longitude),
			        icon:image,
			        map:myMap,
			    });
				markers.push(marker);
			}
		}
		
		function filter() {
			var val = $("#filterSelect").val();
			websocket.send(val);
		}
	</script>
	<script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCjxHGCl5uN2DPCMVaRttY0BeIaMFV-xM4&callback=initMap"></script>
</body>
</html>