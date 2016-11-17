package com.cc.twitttrend.thread;

import java.util.HashMap;
import java.util.Map;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class FetchTweetsThread implements Runnable{
	String myQueueUrl = "TweetsQueue";
	public void run() {
		// TODO Auto-generated method stub
		AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }

        final AmazonSQS sqs = new AmazonSQSClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        sqs.setRegion(usWest2);
        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon SQS");
        System.out.println("===========================================\n");
            
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("oJqYkq4576YtAAZUU8eW0wLMn")
		  .setOAuthConsumerSecret("Hu7HpIosn1B6GfiXHdBjwVmqhe2U7ZUeRODzETBmSqBhcUFQty")
		  .setOAuthAccessToken("789627269539106817-HD7GHP02oc6byjImRljIWYE7wqmM0Da")
		  .setOAuthAccessTokenSecret("OubEPSUfn2zVeovC809hAJU5IgaP4OY60iohDvZ3HDtGp");
				
		StatusListener listener = new StatusListener(){
	        public void onStatus(Status status) {
	        	if(status.getGeoLocation()!=null && status.getLang().equals("en")){
	        		Map<String,MessageAttributeValue> messageAttributes = new HashMap<String,MessageAttributeValue>();
	        		messageAttributes.put("user",new MessageAttributeValue().withDataType("String").withStringValue(status.getUser().toString()));
	        		messageAttributes.put("text",new MessageAttributeValue().withDataType("String").withStringValue(status.getText()));
	        		messageAttributes.put("time",new MessageAttributeValue().withDataType("String").withStringValue(status.getCreatedAt().toString()));
	        		messageAttributes.put("latitude",new MessageAttributeValue().withDataType("Number").withStringValue(status.getGeoLocation().getLatitude()+""));
	        		messageAttributes.put("longitude",new MessageAttributeValue().withDataType("Number").withStringValue(status.getGeoLocation().getLongitude()+""));
	        		
	        		SendMessageRequest request = new SendMessageRequest();
	        		request.withQueueUrl(myQueueUrl);
	        		request.withMessageBody(status.getText());
	        		request.withMessageAttributes(messageAttributes);
	        		sqs.sendMessage(request);
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
