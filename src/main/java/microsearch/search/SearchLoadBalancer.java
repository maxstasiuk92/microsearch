package microsearch.search;

import java.util.Map;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import fastexecution.processors.AbstractProcessor;

@Component
public class SearchLoadBalancer {
	private Map<String, AbstractProcessor> processors;
	
	@Autowired
	public SearchLoadBalancer(ApplicationContext applicationContext) {
		processors = applicationContext.getBeansOfType(AbstractProcessor.class);
		processors.forEach((n, p)->p.start());
	}
	
	@PreDestroy
	protected void stopAll() {
		processors.forEach((n, p)->p.stop());
	}
}
