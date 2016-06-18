package takakuma.shuka.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import takakuma.shuka.model.Word;

public class WordCollector {

	int count = 0;
	private Map<String, Word> words = new HashMap<String, Word>();

	public WordCollector() {

	}

	public void add(String word) {
		++count;

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

	// 追加された全単語数
	public int getAdedCount() {
		return this.count;
	}

	// 単語の種類数
	public int getKindsCount() {
		return this.words.size();
	}

}
