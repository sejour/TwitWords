package takakuma.shuka.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import takakuma.shuka.model.Word;

public class WordCollector {

	private Map<String, Word> words = new HashMap<String, Word>();

	public WordCollector() {

	}

	public void add(String word) {
		Word w = this.words.get(word);

		if (w == null) {
			this.words.put(word, new Word(word));
			return;
		}

		++w.count;
	}

	// Wordを出現頻度降順でソートして返す
	public List<Word> getSortedCollection() {
		List<Word> collection = new ArrayList<Word>(this.words.values());
		Collections.sort(collection, (word1, word2) -> {
			return word2.count.compareTo(word1.count);
		});
		return collection;
	}

}
