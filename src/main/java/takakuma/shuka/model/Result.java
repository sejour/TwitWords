package takakuma.shuka.model;

import java.util.List;

import lombok.Getter;

public class Result {

	@Getter
	private String keyword;

	@Getter
	private int countOfTweets;

	@Getter
	private int countOfWords;

	@Getter
	private int countOfWordKinds;

	@Getter
	private List<Word> words;

	@Getter
	private int rateLimit;

	@Getter
	private int rateLimitRemaining;

	public String getRateLimitStatus() {
		return String.format("%d/%d", this.rateLimitRemaining, this.rateLimit);
	}

	public Result(String keyword, int countOfTweets, int countOfWords, int countOfWordKinds, List<Word> words, int rateLimit, int rateLimitRemaining) {
		this.keyword = keyword;
		this.countOfTweets = countOfTweets;
		this.countOfWords = countOfWords;
		this.countOfWordKinds = countOfWordKinds;
		this.words = words;
		this.rateLimit = rateLimit;
		this.rateLimitRemaining = rateLimitRemaining;
	}

}
