package takakuma.shuka.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import takakuma.shuka.config.TwitterConfiguration;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@RestController
@EnableAutoConfiguration
public class MainController {

	@Autowired
	private TwitterConfiguration twConfig;

	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String hello() {
		return "Hello World!";
	}

	@RequestMapping(value = "/tw", method = RequestMethod.GET)
	public String twitterHello(@RequestParam("query") String queryString) throws TwitterException {
		Twitter twitter = this.twConfig.getInstance();

        StringBuilder builder = new StringBuilder();

        Query query = new Query(queryString);
        QueryResult result = twitter.search(query);
        List<Status> tweets = result.getTweets();
        for (Status tweet : tweets) {
        	builder.append("<p>@" + tweet.getUser().getScreenName() + " - " + tweet.getText() + "</p>");
        }

        builder.append("<p>LATELIMIT: " + twitter.getRateLimitStatus().get("/search/tweets").getRemaining() + "/" + twitter.getRateLimitStatus().get("/search/tweets").getLimit() + "</p>\n");

        return builder.toString();
	}

}
