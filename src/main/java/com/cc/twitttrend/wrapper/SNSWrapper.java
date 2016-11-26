package com.cc.twitttrend.wrapper;

import java.net.InetAddress;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest;
import com.amazonaws.services.sns.model.ConfirmSubscriptionResult;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;

public class SNSWrapper {
	private static SNSWrapper snsWrapper = null;
	private AmazonSNS sns = null;
	private String topicArn = null;
	
	public static SNSWrapper getSingleton(){
		if(snsWrapper == null){
			snsWrapper = new SNSWrapper();
		}
		return snsWrapper;
	}
	
	private SNSWrapper(){
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("default").getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
					"Please make sure that your credentials file is at the correct " +
					"location (~/.aws/credentials), and is in valid format.",e);
		}

		sns = new AmazonSNSClient(credentials);	
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		sns.setRegion(usWest2);
		createTopic();
	}
	
	private void createTopic(){
		if(topicArn == null){
			//create a new SNS topic
			CreateTopicRequest createTopicRequest = new CreateTopicRequest("Tweets");
			CreateTopicResult createTopicResult = sns.createTopic(createTopicRequest);
			topicArn = createTopicResult.getTopicArn();
		}
	}
	public void addSNS(String message){
		//publish to an SNS topic
		PublishRequest publishRequest = new PublishRequest(topicArn, message);
		sns.publish(publishRequest);
	}
	
	public void subscribeSNS(){
		//subscribe to an SNS topic
		try {
			String localIP = InetAddress.getLocalHost().getHostAddress();
			SubscribeRequest subRequest = new SubscribeRequest(topicArn, "http", "http://" + localIP + ":8080/twitttrend/snssubscriber");
			sns.subscribe(subRequest);
			System.out.println(localIP);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//get request id for SubscribeRequest from SNS metadata
		//System.out.println("SubscribeRequest - " + sns.getCachedResponseMetadata(subRequest));
		//System.out.println("Check your email and confirm subscription.");
	}
	
	public void confirmSubscribe(String topicArn,String token){
		try{
		   ConfirmSubscriptionRequest confirmSubscriptionRequest = new ConfirmSubscriptionRequest()
			.withTopicArn(topicArn)
			.withToken(token);
	       ConfirmSubscriptionResult resutlt = sns.confirmSubscription(confirmSubscriptionRequest);
	       System.out.println("subscribed to " + resutlt.getSubscriptionArn());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
