package microsearch.search;

/**
 * If component for SearchRequest was not found - EmptyComponent should be added to the list of found components 
 */
public interface RequestExecutor<T> {
	/**
	 * waits till request is added 
	 */
	default void addRequestToQueue(T request) {
		while (!offerRequestToQueue(request)) {
			Thread.yield();
		}
	}
	
	/**
	 * try to add request to queue
	 * @return true if request was added to queue 
	 */
	boolean offerRequestToQueue(T request);
	
}
