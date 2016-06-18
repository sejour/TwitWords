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
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.ReadingAttribute;

public class MorphologicalAnalyzer implements AutoCloseable {

	private JapaneseAnalyzer analyzer;

	public MorphologicalAnalyzer() {
		UserDictionary userDict = null;
        Mode mode = JapaneseTokenizer.Mode.NORMAL;
        CharArraySet stopSet = JapaneseAnalyzer.getDefaultStopSet();
        Set<String> stopTags = JapaneseAnalyzer.getDefaultStopTags();
    	this.analyzer = new JapaneseAnalyzer(userDict, mode, stopSet, stopTags);
	}

	public List<Morpheme> analyze(String text) {
		List<Morpheme> results = new ArrayList<Morpheme>();

		try (TokenStream tokenStream = this.analyzer.tokenStream("", text)) {
			CharTermAttribute charAttr = tokenStream.addAttribute(CharTermAttribute.class);
			PartOfSpeechAttribute posAttr = tokenStream.addAttribute(PartOfSpeechAttribute.class);
            ReadingAttribute readAttr = tokenStream.addAttribute(ReadingAttribute.class);
        	tokenStream.reset();
        	while (tokenStream.incrementToken()) {
        		results.add(new Morpheme(charAttr.toString(), posAttr.getPartOfSpeech(), readAttr.getReading()));
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
