package microsearch.search.execution.cache;

import java.util.function.Consumer;

import microsearch.search.SearchRequest;

public class CacheSearcher implements Consumer<SearchRequest> {
	
	//TODO: will need connection
	public CacheSearcher() {
		
	}
		
	@Override
	public void accept(SearchRequest request) {
		//TODO: implement search
		request.noFoundComponents();
	}
}
