package takakuma.shuka.model.form;

import lombok.Data;

@Data
public class SearchForm {

	private String keyword;

	public SearchForm() {}

	public SearchForm(String keyword) {
		this.keyword = keyword;
	}

}
