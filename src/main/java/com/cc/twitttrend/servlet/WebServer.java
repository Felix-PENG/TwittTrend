package com.cc.twitttrend.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import com.cc.twitttrend.model.Tweet;
import com.cc.twitttrend.thread.FetchTweetsThread;
import com.cc.twitttrend.thread.SentimentAnalysisThread;
import com.google.gson.Gson;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.params.Parameters;

/**
 * Servlet implementation class WebServer
 */
@ServerEndpoint("/server")
public class WebServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String elasticSearchUrl = "http://search-twitttrend-shjvcamfkizll7huthykh5txnm.us-west-2.es.amazonaws.com";
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WebServer() {
		super();
		// TODO Auto-generated constructor stub
	}

	static Session session;
	@OnOpen
	public void open(Session session) {
		WebServer.session = session;
		Thread t1 = new Thread(new FetchTweetsThread());
		Thread t2 = new Thread(new SentimentAnalysisThread());
		t1.start();
		t2.start();
	}

	@OnClose
	public void close(Session session) {
	}

	@OnError
	public void onError(Throwable error) {
	}

	@OnMessage
	public void handleMessage(String message, Session session) {
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig
				.Builder(elasticSearchUrl)
				.multiThreaded(true)
				.build());
		JestClient client = factory.getObject();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.queryStringQuery(message));

		Search search = new Search.Builder(searchSourceBuilder.toString())
				.addIndex("twitter")
				.addType("tweet")
				.setParameter(Parameters.SIZE, 10000)
				.build();

		List<Tweet> tweetList = new ArrayList<Tweet>();
		try {
			JestResult result = client.execute(search);
			tweetList = result.getSourceAsObjectList(Tweet.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("type", "SearchResult");
		map.put("data", tweetList);
		Gson gson = new Gson();
		String msg = gson.toJson(map);
		try {
			synchronized(WebServer.session.getBasicRemote()) {
				WebServer.session.getBasicRemote().sendText(msg);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void send(String msg){
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig
				.Builder(elasticSearchUrl)
				.multiThreaded(true)
				.build());
		JestClient client = factory.getObject();
		Gson gson = new Gson();
		Tweet tweet = gson.fromJson(msg,Tweet.class);
		Index index = new Index.Builder(tweet).index("twitter").type("tweet").build();
		System.out.println("Send to frontend:" + tweet.getKeyword());
		try {
			client.execute(index);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String,Object> map = new HashMap<String,Object>();
		map.put("type", "RealTime");
		map.put("data", tweet);
		String tweetStr = gson.toJson(map);
		try {
			synchronized(WebServer.session.getBasicRemote()) {
				WebServer.session.getBasicRemote().sendText(tweetStr);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
