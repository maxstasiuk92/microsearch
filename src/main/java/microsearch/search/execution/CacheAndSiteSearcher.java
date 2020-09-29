package microsearch.search.execution;

import microsearch.search.cache.CacheReplenishRequest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import microsearch.business.entities.Component;
import microsearch.business.entities.Supplier;
import microsearch.search.*;

@org.springframework.stereotype.Component
@Primary
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CacheAndSiteSearcher implements SearchResultProvider {
	private RequestExecutor<SearchRequest> cacheSearcher;
	private RequestExecutor<SearchRequest> siteSearcher;
	private RequestExecutor<CacheReplenishRequest> cacheReplenisher;
	
	public CacheAndSiteSearcher(
			@Autowired @Qualifier(value = "cacheSearcher") RequestExecutor<SearchRequest> cacheSearcher, 
			@Autowired @Qualifier(value = "siteSearcher") RequestExecutor<SearchRequest> siteSearcher,
			@Autowired @Qualifier(value = "cacheReplenisher") RequestExecutor<CacheReplenishRequest> cacheReplenisher) {
		this.cacheSearcher = cacheSearcher;
		this.siteSearcher = siteSearcher;
		this.cacheReplenisher = cacheReplenisher;
	}
	
	@Override
	public void fillResultContainer(ResultContainer container) {
		SearchRequestFactory requestFactory = new SearchRequestFactory(container);
		while (requestFactory.hasNext()) {
			MultiLevelRequest request = new MultiLevelRequest(requestFactory.next());
			cacheSearcher.addRequestToQueue(request);
		}
		
		while (true) {
			if (container.isFilled()) {
				break;
			} else {
				Thread.yield();
			}
		}
	}
	
	protected class MultiLevelRequest implements SearchRequest {
		private SearchRequest searchRequest;
		private int requestorIndex;
		private final int CACHE_REQUESTOR = 0, WEB_REQUESTOR = 1;

		public MultiLevelRequest(SearchRequest searchRequest) {
			this.searchRequest = searchRequest;
			requestorIndex = 0;
		}

		@Override
		public Supplier getSupplier() {
			return searchRequest.getSupplier();
		}

		@Override
		public String getRequestedComponent() {
			return searchRequest.getRequestedComponent();
		}

		@Override
		public void setFoundComponents(List<Component> components) {
			searchRequest.setFoundComponents(components);
			if (requestorIndex == WEB_REQUESTOR) {
				Supplier supplier = searchRequest.getSupplier();
				String requestedComponent = searchRequest.getRequestedComponent();
				for (var foundComponent : components) {
					CacheReplenishRequest replenishRequest = new CacheReplenishRequest(supplier, requestedComponent, foundComponent);
					cacheReplenisher.addRequestToQueue(replenishRequest);
				}
			}
		}

		@Override
		public void noFoundComponents() {
			if (requestorIndex == CACHE_REQUESTOR) {
				requestorIndex = WEB_REQUESTOR;
				siteSearcher.addRequestToQueue(this);
			} else {
				searchRequest.noFoundComponents();
			}
		}
	}
}
