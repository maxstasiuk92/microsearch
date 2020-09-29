package microsearch.search.preparation;

import java.util.Set;

import microsearch.business.entities.Supplier;

/**
 * requestedComponents and suppliers are set in order defined by Iterator 
 */
public class UndefinedOrderArranger implements ResultContainerArranger {
	protected Set<String> requestedComponents;
	protected Set<Supplier> suppliers;
	
	public UndefinedOrderArranger(Set<String> requestedComponents, Set<Supplier> suppliers) throws IllegalArgumentException {
		if (requestedComponents == null || suppliers == null) {
			throw new IllegalArgumentException();
		}
		this.requestedComponents = requestedComponents;
		this.suppliers = suppliers;
	}
	
	@Override
	public String[] getRequestedComponents() {
		int i = 0;
		String[] reqCompList = new String[requestedComponents.size()];
		for (String c : requestedComponents) {
			reqCompList[i++] = c;
		}
		return reqCompList;
	}
	
	@Override
	public Supplier[] getSuppliers() {
		int i = 0;
		Supplier[] supplierList = new Supplier[suppliers.size()];
		for (Supplier s : suppliers) {
			supplierList[i++] = s;
		}
		return supplierList;
	}
	
}
