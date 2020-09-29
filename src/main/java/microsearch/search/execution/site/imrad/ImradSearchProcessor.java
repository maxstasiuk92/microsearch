package microsearch.search.execution.site.imrad;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fastexecution.ProcessorCallback;
import fastexecution.ProcessorException;
import fastexecution.ProcessorThread;
import fastexecution.processors.AbstractProcessor;
import fastexecution.threads.QueueHandlerThread;
import htmlparser.Template;
import htmlparser.sequencer.HtmlParsingSequencer;
import microsearch.search.RequestExecutor;
import microsearch.search.SearchRequest;
import microsearch.search.execution.site.RateLimitedHttpProvider;
import microsearch.search.execution.site.TemplateLoader;
import microsearch.search.execution.site.ThreeAvailableComponentFilter;
import microsearch.search.monitoring.ProblemMonitor;

@Component
public class ImradSearchProcessor extends AbstractProcessor implements RequestExecutor<SearchRequest> {
	private ConcurrentLinkedQueue<SearchRequest> requestQueue;
	private ProblemMonitor problemMonitor;
	private Map<String, Template> parserTemplates;
	private RateLimitedHttpProvider httpProvider;	
	
	@Autowired
	public ImradSearchProcessor(ProblemMonitor problemMonitor, TemplateLoader templateLoader)
			throws ProcessorException {
		requestQueue = new ConcurrentLinkedQueue<>();
		this.problemMonitor = problemMonitor;
		this.httpProvider = new RateLimitedHttpProvider(200, 10_000, 10_000, this.problemMonitor);
		templateLoader.setBaseDir("imrad");
		this.parserTemplates = templateLoader.getTemplates(ImradParser.templateNames);
	}

	@Override
	protected ProcessorThread getProcessorThread(ProcessorCallback callback) {
		ImradRequestor requestor = new ImradRequestor(httpProvider);
		ImradParser parser = new ImradParser(parserTemplates, new HtmlParsingSequencer());
		ImradSearcher searcher = new ImradSearcher(requestor, parser, ()->new ThreeAvailableComponentFilter(), problemMonitor);
		return new QueueHandlerThread<SearchRequest>(callback, requestQueue, searcher::search);
	}

	@Override
	public boolean offerRequestToQueue(SearchRequest request) {
		return requestQueue.add(request);
	}
}
