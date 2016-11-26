package com.cc.twitttrend.model;

public class Tweet {
	String user;
	String text;
	double longitude;
	double latitude;
	String time;
	String sentiment;
	String keyword;
	
	public String getUser(){
		return this.user;
	}
	public void setUser(String user){
		this.user = user; 
	}
	
	public String getText(){
		return this.text;
	}
	public void setText(String text){
		this.text = text;
	}
	
	public double getLongitude(){
		return this.longitude;
	}
	public void setLongitude(double longitude){
		this.longitude = longitude;
	}
	
	public double getLatitude(){
		return this.latitude;
	}
	public void setLatitude(double latitude){
		this.latitude = latitude;
	}
	
	public String getTime(){
		return this.time;
	}
	public void setTime(String time){
		this.time = time;
	}
	
	public String getSentiment(){
		return this.sentiment;
	}
	public void setSentiment(String sentiment){
		this.sentiment = sentiment;
	}
	
	public String getKeyword(){
		return this.keyword;
	}
	public void setKeyword(String keyword){
		this.keyword = keyword;
	}
}
