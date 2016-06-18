package takakuma.shuka.model;

import lombok.Getter;

public class Word {

	@Getter
	private String word;

	public Integer count = 1;

	public Word(String word) {
		this.word = word;
	}

}
