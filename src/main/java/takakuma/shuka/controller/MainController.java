package takakuma.shuka.controller;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import takakuma.shuka.config.TwitterConfiguration;
import takakuma.shuka.mecab.Morpheme;
import takakuma.shuka.mecab.MorphologicalAnalyzer;
import takakuma.shuka.model.Result;
import takakuma.shuka.model.Word;
import takakuma.shuka.model.form.SearchForm;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@Controller
public class MainController {

	@Autowired
	private TwitterConfiguration twConfig;

	@RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("searchForm", new SearchForm());
        return "index";
    }

	private static int ACQUISITION_COUNT_MAX = 300;

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public String getSerarch(@RequestParam("keyword") String keyword, Model model) throws TwitterException, IOException {
		Result result = this.searchAndCollect(keyword);
        model.addAttribute("result", result);
        model.addAttribute("searchForm", new SearchForm());
        return "result";
	}

	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public String postSerarch(@ModelAttribute SearchForm searchForm, Model model) throws TwitterException, IOException {
		Result result = this.searchAndCollect(searchForm.getKeyword());
        model.addAttribute("result", result);
        model.addAttribute("searchForm", new SearchForm());
        return "result";
	}

	private Result searchAndCollect(String keyword) throws TwitterException {
		Query query = new Query(keyword);
		query.setLang("ja");
		query.count(100);

        Twitter twitter = this.twConfig.getInstance();

        // ツイート検索
        List<Status> tweets = new ArrayList<Status>();
        while (tweets.size() < ACQUISITION_COUNT_MAX) {
        	QueryResult result = twitter.search(query);
        	tweets.addAll(result.getTweets());
        	if ((query = result.nextQuery()) == null) break;
        }

        WordCollector collector = new WordCollector();

        try (MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer()) {
        	// ツイートを形態素解析
        	for (Status tweet : tweets) {
        		String text = textNormalize(tweet.getText().replaceAll("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?", ""));
        		for (Morpheme morpheme : analyzer.analyze(text)) {
        			collector.add(morpheme);
        		}
        	}
        }

        List<Word> words = collector.getSortedCollection();

        // 確率計算
        double allCount = collector.getAdedCount();
        for (Word word : words) {
        	word.setProbability((double)word.count / allCount);
        }

        RateLimitStatus rls = twitter.getRateLimitStatus().get("/search/tweets");

        return new Result(keyword, tweets.size(), collector.getAdedCount(), collector.getKindsCount(), words, rls.getLimit(), rls.getRemaining());
	}

	private static String textNormalize(String text) {
		return Normalizer.normalize(text, Normalizer.Form.NFKC)
				.replaceAll("[˗֊‐‑‒–⁃⁻₋−]+", "-")
				.replaceAll("[﹣－ｰ—―─━ー]+", "ー")
				.replaceAll("[~∼∾〜〰～]", "")
				.replaceAll("[ 　]+",  " ")
				.trim();
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

}
