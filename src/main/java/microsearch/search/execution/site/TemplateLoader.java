package microsearch.search.execution.site;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import htmlparser.Template;
import htmlparser.templatebuilders.XmlTemplateBuilder;

@Component
@Scope("prototype")
public class TemplateLoader extends XmlTemplateBuilder implements ServletContextAware {
	private ServletContext servletContext;
	private String baseDir;
	
	public Map<String, Template> getTemplates(String[] templateNames) {
		HashMap<String, Template> templates = new HashMap<>(templateNames.length);
		for (var name : templateNames) {
			Template template = this.getTemplate(name);
			templates.put(name, template);
		}
		return templates;
	}
	
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	@Override
	protected InputStream getInputStream(String templateName) throws IOException {
		return servletContext.getResourceAsStream("/WEB-INF/parsing-templates/" + baseDir + "/" + templateName);
	}
	
}
