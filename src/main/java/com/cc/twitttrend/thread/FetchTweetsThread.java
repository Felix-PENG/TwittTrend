package com.cc.twitttrend.thread;

import java.io.IOException;
import org.elasticsearch.client.transport.TransportClient;

import com.cc.twitttrend.model.Tweet;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class FetchTweetsThread implements Runnable{
	TransportClient client;
	
	public void run() {
		// TODO Auto-generated method stub
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("oJqYkq4576YtAAZUU8eW0wLMn")
		  .setOAuthConsumerSecret("Hu7HpIosn1B6GfiXHdBjwVmqhe2U7ZUeRODzETBmSqBhcUFQty")
		  .setOAuthAccessToken("789627269539106817-HD7GHP02oc6byjImRljIWYE7wqmM0Da")
		  .setOAuthAccessTokenSecret("OubEPSUfn2zVeovC809hAJU5IgaP4OY60iohDvZ3HDtGp");
		
//		try {
//			client = TransportClient.builder().build()
//			        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"),9300));
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig
		                        .Builder("http://search-twittmap-7jxppunqbynyqq7m5aed3liifq.us-east-1.es.amazonaws.com")
		                        .multiThreaded(true)
		                        .build());
		final JestClient client = factory.getObject();
				
		StatusListener listener = new StatusListener(){
	        public void onStatus(Status status) {
	        	if(status.getGeoLocation()!=null){
//	        		Map<String, Object> json = new HashMap<String, Object>();
//	        		json.put("user",status.getUser());
//	        		json.put("text", status.getText());
//	        		json.put("time", status.getCreatedAt());
//	        		json.put("latitude", status.getGeoLocation().getLatitude());
//	        		json.put("longitude",status.getGeoLocation().getLongitude());
	        		Tweet tweet = new Tweet();
	        		tweet.setUser(status.getUser().toString());
	        		tweet.setText(status.getText());
	        		tweet.setTime(status.getCreatedAt().toString());
	        		tweet.setLatitude(status.getGeoLocation().getLatitude());
	        		tweet.setLongitude(status.getGeoLocation().getLongitude());
	        		Index index = new Index.Builder(tweet).index("twitter").type("tweet").build();
	        		try {
						client.execute(index);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		//IndexResponse response = client.prepareIndex("twitter", "tweet", null).setSource(json).get();
	        		System.out.println(index);
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
        String keywords[] = {"President","Hillary","Clinton","Trump","Google","Facebook","Columbia","NYC","Weather","Job","Love","Work"};
        fq.track(keywords);
	    twitterStream.addListener(listener);
        twitterStream.filter(fq);
	}

}
