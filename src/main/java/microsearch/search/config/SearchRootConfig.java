package microsearch.search.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
		"microsearch.search",
		"microsearch.search.cache",
		"microsearch.search.execution",
		"microsearch.search.execution.cache",
		"microsearch.search.execution.site",
		"microsearch.search.preparation",
		"microsearch.search.monitoring"
		})
public class SearchRootConfig {
	
}
