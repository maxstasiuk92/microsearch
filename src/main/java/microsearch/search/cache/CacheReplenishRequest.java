package microsearch.search.cache;

import microsearch.business.entities.Component;
import microsearch.business.entities.Supplier;

public class CacheReplenishRequest {
	private Supplier supplier;
	private String requestedComponent;
	private Component foundComponent;
	
	public CacheReplenishRequest(Supplier supplier, String requestedComponent, Component foundComponent) {
		this.supplier = supplier;
		this.requestedComponent = requestedComponent;
		this.foundComponent = foundComponent;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public String getRequestedComponent() {
		return requestedComponent;
	}

	public Component getFoundComponent() {
		return foundComponent;
	}

}
