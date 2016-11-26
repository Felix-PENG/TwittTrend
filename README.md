# TwittTrend
Based on TwittMap, add Amazon SQS service and Amazon SNS service.

## Objectives
  * Use Amazon SQS service to create a processing queue for the Tweets that are delivered by the Twitter Streaming API.
  * Integrate a third party cloud service API into the Tweet processing flow.
  * Use Amazon SNS service to update the status processing on each tweet so the UI can refresh.
  
## Overview

### Streaming
  * Read a stream of tweets using Twitter Streaming API.
  * After fetching a new tweet, check to see if it has geolocation info and is in English.
  * Once the tweet validates these filters, send a message to SQS for asynchronous processing on the text of the tweet.

### Worker
  * Define a worker pool that will pick up messages from the queue to process.
  * Make a call to the AlchemyLanguage service to analyze sentiment of tweet text(positive, negative or neutral).
  * As soon as the tweet is processed, using SNS to send a notification that contains the information about the tweet.

### Backend
  * On receiving the notification, index this tweet in Elasticsearch.
  * Provide the functionality for users to search for tweets that match a particular keyword.
  
### FrontEnd
  * When a new tweet is indexed into ElasticSearch, add a corresponding marker on the map.
  * Users can search keywords via a dropdown.
  * Plot the tweets that match the query on a map. (Using icons).

### Deployment
  * Deploy the application on AWS Elastic Beanstalk.

