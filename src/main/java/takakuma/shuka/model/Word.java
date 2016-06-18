package takakuma.shuka.model;

import lombok.Setter;
import lombok.Getter;

public class Word {

	@Getter
	private String word;

	@Getter
	private String partOfSpeech;

	public Integer count = 1;

	@Getter @Setter
	private Double probability = 0.0;

	public String getProbabilityString() {
		return String.format("%f%%", this.probability);
	}

	public Word(String word, String partOfSpeech) {
		this.word = word;
		this.partOfSpeech = partOfSpeech;
	}

}
