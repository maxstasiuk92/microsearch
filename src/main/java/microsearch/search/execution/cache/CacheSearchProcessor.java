package microsearch.search.execution.cache;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;

import fastexecution.ProcessorCallback;
import fastexecution.ProcessorThread;
import fastexecution.processors.AbstractProcessor;
import fastexecution.threads.QueueHandlerThread;
import microsearch.search.RequestExecutor;
import microsearch.search.SearchRequest;

@Component("cacheSearcher")
public class CacheSearchProcessor extends AbstractProcessor implements RequestExecutor<SearchRequest> {
	private ConcurrentLinkedQueue<SearchRequest> requestQueue;
	
	//TODO: will need connection pool to create thread
	public CacheSearchProcessor() {
		requestQueue = new ConcurrentLinkedQueue<>();
	}
	
	@Override
	protected ProcessorThread getProcessorThread(ProcessorCallback callback) {
		CacheSearcher cacheSearcher = new CacheSearcher();
		return new QueueHandlerThread<SearchRequest>(callback, requestQueue, cacheSearcher);
	}

	@Override
	public boolean offerRequestToQueue(SearchRequest request) {
		return requestQueue.add(request);
	}

}
