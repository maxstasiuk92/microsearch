package microsearch;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
		"microsearch.business"
		})
public class RootConfig {

}
