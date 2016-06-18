package takakuma.shuka.model;

import lombok.Getter;

public class Word {

	@Getter
	private String word;

	@Getter
	private String partOfSpeech;

	public Integer count = 1;

	public Word(String word, String partOfSpeech) {
		this.word = word;
		this.partOfSpeech = partOfSpeech;
	}

}
