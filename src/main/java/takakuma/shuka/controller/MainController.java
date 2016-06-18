package takakuma.shuka.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletResponse;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseAnalyzer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer.Mode;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.dict.UserDictionary;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.ReadingAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import takakuma.shuka.config.TwitterConfiguration;
import takakuma.shuka.mecab.MorphologicalAnalyzer;
import takakuma.shuka.model.Word;
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
	public String twitterHello(@RequestParam("keyword") String keyword) throws TwitterException, IOException {
		Twitter twitter = this.twConfig.getInstance();
        StringBuilder builder = new StringBuilder();

        UserDictionary userDict = null;
        Mode mode = JapaneseTokenizer.Mode.NORMAL;
        CharArraySet stopSet = JapaneseAnalyzer.getDefaultStopSet();
        Set<String> stopTags = JapaneseAnalyzer.getDefaultStopTags();
    	JapaneseAnalyzer analyzer = new JapaneseAnalyzer(userDict, mode, stopSet, stopTags);

        Query query = new Query(keyword);
        query.setLang("ja");
        query.count(100);
        QueryResult result = twitter.search(query);
        List<Status> tweets = result.getTweets();

        for (Status tweet : tweets) {
        	String text = textNormalize(tweet.getText().replaceAll("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?", ""));
        	builder.append("<p>@" + tweet.getUser().getScreenName() + " - " + text + "</p>\n");
        	builder.append("<table>\n");

            try (TokenStream tokenStream = analyzer.tokenStream("", text)) {
            	CharTermAttribute charAttr = tokenStream.addAttribute(CharTermAttribute.class);
            	PartOfSpeechAttribute posAttr = tokenStream.addAttribute(PartOfSpeechAttribute.class);
            	ReadingAttribute readAttr = tokenStream.addAttribute(ReadingAttribute.class);

            	tokenStream.reset();
            	while (tokenStream.incrementToken()) {
            		String word = charAttr.toString();
            		String reading = readAttr.getReading();
            		String partOfSpeech = posAttr.getPartOfSpeech();
            		builder.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td></tr>", word, reading, partOfSpeech));
            	}

            	builder.append("</table>\n");
            }

            builder.append("<hr />");
        }

        builder.append("<p>LATELIMIT: " + twitter.getRateLimitStatus().get("/search/tweets").getRemaining() + "/" + twitter.getRateLimitStatus().get("/search/tweets").getLimit() + "</p>\n");

        analyzer.close();

        return builder.toString();
	}

	@RequestMapping(value = "/collect", method = RequestMethod.GET)
	public void serarchAndCollect(ServletResponse response, @RequestParam("keyword") String keyword) throws TwitterException, IOException {
        // search tweet
		Query query = new Query(keyword);
		query.setLang("ja");
		query.count(100);
        Twitter twitter = this.twConfig.getInstance();
        QueryResult result = twitter.search(query);
        List<Status> tweets = result.getTweets();

        WordCollector collector = new WordCollector();

        try (MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer()) {
        	// ツイートを形態素解析
        	for (Status tweet : tweets) {
        		String text = textNormalize(tweet.getText().replaceAll("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?", ""));
        		for (String word: analyzer.analyze(text)) {
        			collector.add(word);
        		}
        	}
        }

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 出現頻度順で出力
        out.println("<table border=\"1\" align=\"center\">");
        out.println("<tr><td>単語</td><td>出現回数</td></tr>");
        for (Word word : collector.getSortedCollection()) {
        	out.printf("<tr><td>%s</td><td>%d</td></tr>\n", word.getWord(), word.count);
        }
        out.println("</table>");

        out.println("<p>LATELIMIT: " + twitter.getRateLimitStatus().get("/search/tweets").getRemaining() + "/" + twitter.getRateLimitStatus().get("/search/tweets").getLimit() + "</p>");
	}

	private static String textNormalize(String text) {
		return Normalizer.normalize(text, Normalizer.Form.NFKC)
				.replaceAll("[˗֊‐‑‒–⁃⁻₋−]+", "-")
				.replaceAll("[﹣－ｰ—―─━ー]+", "ー")
				.replaceAll("[~∼∾〜〰～]", "")
				.replaceAll("[ 　]+",  " ")
				.trim();
	}

}
