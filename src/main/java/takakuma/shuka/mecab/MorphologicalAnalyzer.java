package takakuma.shuka.mecab;

import java.io.IOException;
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

public class MorphologicalAnalyzer implements AutoCloseable {

	private JapaneseAnalyzer analyzer;

	public MorphologicalAnalyzer() {
		UserDictionary userDict = null;
        Mode mode = JapaneseTokenizer.Mode.NORMAL;
        CharArraySet stopSet = JapaneseAnalyzer.getDefaultStopSet();
        Set<String> stopTags = JapaneseAnalyzer.getDefaultStopTags();
    	this.analyzer = new JapaneseAnalyzer(userDict, mode, stopSet, stopTags);
	}

	public List<String> analyze(String text) {
		List<String> results = new ArrayList<String>();

		try (TokenStream tokenStream = this.analyzer.tokenStream("", text)) {
			CharTermAttribute charAttr = tokenStream.addAttribute(CharTermAttribute.class);
        	tokenStream.reset();
        	while (tokenStream.incrementToken()) {
        		String word = charAttr.toString();
        		results.add(word);
        	}
        } catch (IOException e) {
			e.printStackTrace();
		}

		return results;
	}

	public void close() {
		this.analyzer.close();
	}

}
