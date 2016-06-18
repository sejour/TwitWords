package takakuma.shuka.mecab;

import lombok.Getter;

public class Morpheme {

	@Getter
	private String word;

	@Getter
	private String partOfSpeech;

	@Getter
	private String reading;

	public Morpheme(String word, String partOfSpeech, String reading) {
		this.word = word;
		this.partOfSpeech = partOfSpeech;
		this.reading = reading;
	}

}
