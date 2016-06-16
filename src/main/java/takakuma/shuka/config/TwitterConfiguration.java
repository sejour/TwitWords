package takakuma.shuka.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Configuration
public class TwitterConfiguration {

	@Value("${twitter.oauth.consumerKey}")
	private String consumerKey;

	@Value("${twitter.oauth.consumerSecret}")
	private String consumerSecret;

	private static Twitter twitter;

	public Twitter getInstance() throws TwitterException {
		if (twitter == null) {
			ConfigurationBuilder confBuilder = new ConfigurationBuilder();
			confBuilder.setApplicationOnlyAuthEnabled(true);
			twitter = new TwitterFactory(confBuilder.build()).getInstance();
	        twitter.setOAuthConsumer(this.consumerKey, this.consumerSecret);
	        twitter.getOAuth2Token();
		}

        return twitter;
	}

}
