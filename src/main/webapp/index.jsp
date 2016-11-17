<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
	<title>TwittMap</title>
	<link rel="stylesheet" type="text/css" href="css/bootstrap.min.css" />
	<style>
	html,body {width: 100%;height:100%;}
	</style>
</head>
<body style="margin: 0%;font-family: 'Comic Sans MS', 'Comic Sans', cursive;">
	<div>
		<div style="background-color: #0666c6;height:50px;line-height: 50px;box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2);">
			<span style="margin-left: 1%;text-shadow: 2px 2px 10px black;font-size: 5vh;color:white">TwittMap</span>
			<span style="display: inline-block;float: right;margin-right: 1%;margin-top:8px">
				<select id="filterSelect" class="form-control" onchange="reloadMarkers()">
					<option>All</option>
					<option>President</option>
					<option>Hillary</option>
					<option>Clinton</option>
					<option>Trump</option>
					<option>Google</option>
					<option>Facebook</option>
					<option>Columbia</option>
					<option>NYC</option>
					<option>Job</option>
					<option>Love</option>
					<option>Work</option>
				</select>
			</span>
			<span style="float: right;margin-right: 1%;color:white;">Filter</span>
		</div>
		<div id="map" style="width:100%;height: calc(100% - 50px);"></div>
	</div>
	
	<script type="text/javascript" src="js/jquery-3.1.1.min.js"></script>
	<script  type="text/javascript">
		var myMap;
        var markerCluster;
        var lastKeyword = "All";
        var dict = []; 
		
		window.onload=function(){
			reloadMarkers();
			dict['All'] = null;
			dict['President'] = null;
			dict['Hillary'] = null;
			dict['Clinton'] = null;
			dict['Trump'] = null;
			dict['Google'] = null;
			dict['Facebook'] = null;
			dict['Columbia'] = null;
			dict['NYC'] = null;
			dict['Job'] = null;
			dict['Love'] = null;
			dict['Work'] = null;
		}
		
		function reloadMarkers(){
			var keyword = $("#filterSelect").val()
			if(dict[keyword] != null){
				//render map with cache data
				addMarker(dict[keyword]);
				//fetch real time data in backend
				$.post("Keyword",
				{
					keyword: keyword
				},
				function(data){
					var loc = eval(data);
					var locations = new Array(loc.length);
					for(var i = 0;i < loc.length;i++){
						var tweet = loc[i];
						locations[i] = new google.maps.LatLng(tweet.latitude,tweet.longitude);
					}
					dict[keyword] = locations;	
				});
			}else{
				$.post("Keyword",
				{
					keyword: keyword
				},
				function(data){
					var loc = eval(data);
					var locations = new Array(loc.length);
					for(var i = 0;i < loc.length;i++){
					var tweet = loc[i];
						locations[i] = new google.maps.LatLng(tweet.latitude,tweet.longitude);
					}
					dict[keyword] = locations;
					if(keyword == $("#filterSelect").val()){
						addMarker(locations);
					}
				});
			}
		}
		
		function initMap() {
	        myMap = new google.maps.Map(document.getElementById('map'), {
	          zoom: 3,
	          center: {lat: 40, lng: -100},
	          mapTypeId: google.maps.MapTypeId.TERREN
	        });	       
	     }
		
		function addMarker(locations){
			if(markerCluster != null){
				markerCluster.clearMarkers();
			}
			
			var markers = locations.map(function(location, i) {
		          return new google.maps.Marker({
		            position: location,
		            label: '1',
		            animation: google.maps.Animation.DROP
		          });
		     });

		      // Add a marker clusterer to manage the markers.
		      markerCluster = new MarkerClusterer(myMap, markers,
		          {imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m'}
		      );
		}
		
		setInterval(function(){
			var keyword = $("#filterSelect").val()
			if(keyword == lastKeyword){
				reloadMarkers();
			}
			lastKeyword = keyword;
		 },8000);
	</script>
	<script src="https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/markerclusterer.js"></script>
	<script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCjxHGCl5uN2DPCMVaRttY0BeIaMFV-xM4&callback=initMap"></script>
</body>
</html>