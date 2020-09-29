package microsearch;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import microsearch.search.config.SearchRootConfig;
import microsearch.search.config.SearchServletConfig;

public class ServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[] {RootConfig.class, SearchRootConfig.class};
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[] {ServletConfig.class, SearchServletConfig.class};
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] {"/"};
	}

}
