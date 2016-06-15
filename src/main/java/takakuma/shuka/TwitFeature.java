package takakuma.shuka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class TwitFeature {

	public static void main(String[] args) {
		SpringApplication.run(TwitFeature.class, args);
	}

}
