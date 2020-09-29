package microsearch.search;

import java.util.List;

import microsearch.business.entities.Component;
import microsearch.business.entities.Supplier;

public class SearchRequestData implements SearchRequest {
	private ResultContainer resultContainer;
	private int requestedComponentIndex;
	private int supplierIndex;
	
	public SearchRequestData(ResultContainer resultContainer, int reqCompIndex, int supplierIndex) {
		if (resultContainer == null || reqCompIndex < 0 || supplierIndex < 0) {
			throw new IllegalArgumentException("SearchRequest creation with resultContainer: " + resultContainer
					+ ", reqCompIndex: " + reqCompIndex + ", supplierIndex: " + supplierIndex);
		}
		this.resultContainer = resultContainer;
		this.requestedComponentIndex = reqCompIndex;
		this.supplierIndex = supplierIndex;
	}
	
	@Override
	public Supplier getSupplier() {
		return resultContainer.getSupplier(supplierIndex);
	}
	
	@Override
	public String getRequestedComponent() {
		return resultContainer.getRequestedComponent(requestedComponentIndex);
	}
	
	@Override
	public void setFoundComponents(List<Component> components) {
		resultContainer.setFoundComponents(components, requestedComponentIndex, supplierIndex);
	}
	
	@Override
	public void noFoundComponents() {
		resultContainer.noFoundComponents(requestedComponentIndex, supplierIndex);
	}
}
