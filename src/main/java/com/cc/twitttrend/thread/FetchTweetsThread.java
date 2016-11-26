package com.cc.twitttrend.thread;

import com.cc.twitttrend.model.Tweet;
import com.cc.twitttrend.wrapper.SQSWrapper;
import com.google.gson.Gson;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class FetchTweetsThread implements Runnable{
    String keywords[] = {"Columbia","NYC","Trump","Google","Facebook","Weather","Work","Love","Food","Sports"};
	public void run() {
		// TODO Auto-generated method stub
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("oJqYkq4576YtAAZUU8eW0wLMn")
		  .setOAuthConsumerSecret("Hu7HpIosn1B6GfiXHdBjwVmqhe2U7ZUeRODzETBmSqBhcUFQty")
		  .setOAuthAccessToken("789627269539106817-HD7GHP02oc6byjImRljIWYE7wqmM0Da")
		  .setOAuthAccessTokenSecret("OubEPSUfn2zVeovC809hAJU5IgaP4OY60iohDvZ3HDtGp");
				
		StatusListener listener = new StatusListener(){
	        public void onStatus(Status status) {
	        	if(status.getGeoLocation()!=null && status.getLang().equals("en")){
	        		Tweet tweet = new Tweet();
	        		tweet.setUser(status.getUser().toString());
	        		tweet.setText(status.getText());
	        		tweet.setTime(status.getCreatedAt().toString());
	        		tweet.setLatitude(status.getGeoLocation().getLatitude());
	        		tweet.setLongitude(status.getGeoLocation().getLongitude());
	        		for(int i = 0;i < keywords.length;i++){
	        			if(status.getText().contains(keywords[i])){
	        				tweet.setKeyword(keywords[i]);
	        				break;
	        			}
	        		}
	        		
	        		SQSWrapper sqs = SQSWrapper.getSingleton();
	        		Gson gson = new Gson();
	        		String tweetString = gson.toJson(tweet,Tweet.class);
	        		sqs.sendMessage(tweetString);
	        	}
	        }
	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
	        public void onException(Exception ex) {

	        }
			public void onScrubGeo(long arg0, long arg1) {
				// TODO Auto-generated method stub
				
			}
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
			}
	    };
	    TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
	    FilterQuery fq = new FilterQuery();
        fq.track(keywords);
	    twitterStream.addListener(listener);
        twitterStream.filter(fq);
	}     
}
