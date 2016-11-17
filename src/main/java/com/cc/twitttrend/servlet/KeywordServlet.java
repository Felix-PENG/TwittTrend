package com.cc.twitttrend.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.cc.twitttrend.model.Tweet;
import com.cc.twitttrend.thread.FetchTweetsThread;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.params.Parameters;
import net.sf.json.JSONArray;

/**
 * Servlet implementation class KeywordServlet
 */
@WebServlet(urlPatterns="/Keyword",loadOnStartup=1)
public class KeywordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	public void init(){
		Thread t = new Thread(new FetchTweetsThread());
		t.start();
	}
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KeywordServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		String keyword = request.getParameter("keyword");
		
		
//		TransportClient client = TransportClient.builder().build()
//		        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig
		                        .Builder("http://search-twittmap-7jxppunqbynyqq7m5aed3liifq.us-east-1.es.amazonaws.com")
		                        .multiThreaded(true)
		                        .build());
		JestClient client = factory.getObject();
//		SearchResponse res;
//		if(keyword.equals("All")){
//			res = client.prepareSearch("twitter")
//					.setTypes("tweet")
//			        .setQuery(QueryBuilders.matchAllQuery())
//			        .setSize(10000)
//			        .execute()
//			        .actionGet();
//		}else{
//			res = client.prepareSearch("twitter")
//					.setTypes("tweet")
//			        .setQuery(QueryBuilders.queryStringQuery(keyword))
//			        .setSize(10000)
//			        .execute()
//			        .actionGet();
//		}
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		if(keyword.equals("All")){
			searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		}else{
			searchSourceBuilder.query(QueryBuilders.queryStringQuery(keyword));
		}

		Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex("twitter")
                .addType("tweet")
                .setParameter(Parameters.SIZE, 10000)
                .build();

		JestResult result = client.execute(search);
		List<Tweet> tweetList = result.getSourceAsObjectList(Tweet.class);
		//SearchHit[] hits = res.getHits().getHits();
//		System.out.println(hits.size());
//		for(int i = 0;i < hits.size();i++){
//			Tweet t = hits.get(i).source;
//			tweetList.add(t);
//		}
	
		JSONArray jsonList = JSONArray.fromObject(tweetList);
		out.println(jsonList);
	}
}
