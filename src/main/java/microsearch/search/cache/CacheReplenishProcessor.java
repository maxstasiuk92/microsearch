package microsearch.search.cache;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;

import fastexecution.ProcessorCallback;
import fastexecution.ProcessorException;
import fastexecution.ProcessorThread;
import fastexecution.processors.AbstractProcessor;
import fastexecution.threads.QueueHandlerThread;
import microsearch.search.RequestExecutor;

@Component("cacheReplenisher")
public class CacheReplenishProcessor extends AbstractProcessor 
		implements RequestExecutor<CacheReplenishRequest> {
	private ConcurrentLinkedQueue<CacheReplenishRequest> requestQueue;
	
	//TODO: will need connection pool
	public CacheReplenishProcessor() throws ProcessorException {
		requestQueue = new ConcurrentLinkedQueue<>();
	}
	
	@Override
	public boolean offerRequestToQueue(CacheReplenishRequest request) {
		//TODO: will add to queue
		return true;
	}
	
	@Override
	protected ProcessorThread getProcessorThread(ProcessorCallback callback) {
		//get connection; if no connection -> return null
		CacheReplenisher cacheReplenisher = new CacheReplenisher();
		return new QueueHandlerThread<CacheReplenishRequest>(callback, requestQueue, cacheReplenisher);
	}
	
}
