package com.cc.twitttrend.wrapper;

import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SQSWrapper {
	private static SQSWrapper sqsWrapper = null;
	private AmazonSQS sqs = null;
	private static String myQueueUrl = null;

	public static SQSWrapper getSingleton(){
		if(sqsWrapper == null){
			sqsWrapper = new SQSWrapper();
		}
		return sqsWrapper;
	}
	
	private SQSWrapper(){
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("default").getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
					"Please make sure that your credentials file is at the correct " +
					"location (~/.aws/credentials), and is in valid format.",e);
		}

		sqs = new AmazonSQSClient(credentials);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		sqs.setRegion(usWest2);
		if(myQueueUrl == null){
			createQueue();
		}
	}
	
	private void createQueue(){
        CreateQueueRequest createQueueRequest = new CreateQueueRequest("TweetsQueue");
        myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
	}
	
	public void sendMessage(String msg){
		SendMessageRequest request = new SendMessageRequest();
		request.withQueueUrl(myQueueUrl);
		request.withMessageBody(msg);
		sqs.sendMessage(request);
	}
	
	public List<Message> receiveMessages(){
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
		return sqs.receiveMessage(receiveMessageRequest).getMessages();
	}
	
	public void deleteMessage(Message message){
		String messageReceiptHandle = message.getReceiptHandle();
        sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messageReceiptHandle));
	}

}
