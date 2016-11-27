package com.cc.twitttrend.thread;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.Message;
import com.cc.twitttrend.model.Tweet;
import com.cc.twitttrend.wrapper.SNSWrapper;
import com.cc.twitttrend.wrapper.SQSWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentSentiment;

public class SentimentAnalysisThread implements Runnable{
	public void run() {
		// TODO Auto-generated method stub
		SQSWrapper sqs = SQSWrapper.getSingleton();
		SNSWrapper sns = SNSWrapper.getSingleton();
        sns.subscribeSNS();
        
        try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		while(true){
			try{
				List<Message> messages = sqs.receiveMessages();
				AlchemyLanguage service = new AlchemyLanguage();
				service.setApiKey("bed54ca30f3471577dd73120c73cd71f8edec493");
				//service.setApiKey("3adfa81273783e9fb8ff16ae0ba84fa3e65df15a");
				Map<String,Object> params = new HashMap<String, Object>();
				
				for (Message message : messages) {
					String str = new Gson().toJson(message);
					Gson gson = new Gson();
					JsonObject jsonObject = gson.fromJson(str, JsonObject.class);
					String body = jsonObject.get("body").getAsString();
					Tweet tweet = gson.fromJson(body, Tweet.class);
					
					try{
						params.put(AlchemyLanguage.TEXT,tweet.getText());
						DocumentSentiment sentiment = service.getSentiment(params).execute();
						tweet.setSentiment(sentiment.getSentiment().getType().toString());
						
						Gson snsGson = new Gson();
		            	String snsString = snsGson.toJson(tweet,Tweet.class);
		            	
		            	sns.addSNS(snsString);        
		            	sqs.deleteMessage(message);
					}catch(Exception e){
						
					}
				}
			}catch (AmazonServiceException ase) {
				System.out.println("Caught an AmazonServiceException, which means your request made it " +
						"to Amazon SQS, but was rejected with an error response for some reason.");
				System.out.println("Error Message:    " + ase.getMessage());
				System.out.println("HTTP Status Code: " + ase.getStatusCode());
				System.out.println("AWS Error Code:   " + ase.getErrorCode());
				System.out.println("Error Type:       " + ase.getErrorType());
				System.out.println("Request ID:       " + ase.getRequestId());
			} catch (AmazonClientException ace) {
				System.out.println("Caught an AmazonClientException, which means the client encountered " +
						"a serious internal problem while trying to communicate with SQS, such as not " +
						"being able to access the network.");
				System.out.println("Error Message: " + ace.getMessage());
			}
		}
	}
}
