package microsearch.search.preparation;

import microsearch.business.entities.Supplier;

public interface ResultContainerArranger {
	public String[] getRequestedComponents();
	public Supplier[] getSuppliers();
}
