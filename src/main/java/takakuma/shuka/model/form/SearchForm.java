package takakuma.shuka.model.form;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class SearchForm {

	@NotBlank
	private String keyword;

	public SearchForm() {}

	public SearchForm(String keyword) {
		this.keyword = keyword;
	}

}
