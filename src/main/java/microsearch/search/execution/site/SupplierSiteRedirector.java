package microsearch.search.execution.site;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import microsearch.business.SupplierRegistry;
import microsearch.business.entities.Supplier;
import microsearch.search.RequestExecutor;
import microsearch.search.SearchRequest;

@Component("siteSearcher")
public class SupplierSiteRedirector implements RequestExecutor<SearchRequest> {

	protected ConcurrentHashMap<Supplier, RequestExecutor<SearchRequest>> siteRequestorMap;
	
	@Autowired
	public SupplierSiteRedirector(SupplierRegistry supplierRegistry, ApplicationContext applicationContext) {
		this.siteRequestorMap = new ConcurrentHashMap<>();
		Set<Supplier> supplierSet = supplierRegistry.getSupplierSet();
		
		for (var supplier : supplierSet) {
			var requestExecutorClass = supplierRegistry.getAssociatedResources(supplier).getRequestorClass();
			siteRequestorMap.put(supplier, applicationContext.getBean(requestExecutorClass));
		}
	}
		
	@Override
	public boolean offerRequestToQueue(SearchRequest request) {
		//TODO: is get thread safe?
		RequestExecutor<SearchRequest> executor = siteRequestorMap.get(request.getSupplier());
		return executor.offerRequestToQueue(request);
	}
}
