package microsearch.business;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;

import microsearch.business.entities.Supplier;
import microsearch.search.RequestExecutor;
import microsearch.search.SearchRequest;

public abstract class SupplierRegistry extends Supplier.SupplierCreator {
	
	private HashMap<Supplier, AssociatedResources> supplierRegistry;
	
	protected SupplierRegistry() {
		supplierRegistry = new HashMap<>();
		loadSupplierRegistry();
	}
	
	public Set<Supplier> getSupplierSet() {
		return supplierRegistry.keySet();
	}
	
	/**
	 * @exception NoSuchElementException if no supplier with such id
	 */
	public Supplier getSupplier(int id) {
		Supplier result = null;
		var si = supplierRegistry.keySet().iterator();
		while (si.hasNext()) {
			var s = si.next();
			if (s.getId() == id) {
				result = s;
				break;
			}
		}
		if (result == null) {
			throw new NoSuchElementException("supplier with id=" + id + " does not exist");
		}
		return result;
	}
	
	
	/**
	 * @exception NoSuchElementException if no AssociatedResources for supplier
	 */
	public AssociatedResources getAssociatedResources(Supplier supplier) {
		AssociatedResources result = supplierRegistry.get(supplier);
		if (result == null) {
			throw new NoSuchElementException("resources for supplier with id=" + supplier.getId() + " do not exist");
		}
		return result;
	}
	
	protected void addToSupplierRegistry(Supplier supplier, AssociatedResources resources) {
		if (supplierRegistry.containsKey(supplier)) {
			throw new IllegalArgumentException("already contains such supplier");
		}
		if (resources == null) {
			throw new IllegalArgumentException("resources can not be null");
		}
		supplierRegistry.put(supplier, resources);
	}
	
	/**
	 * should fill information for supplier using 
	 * {@link #addToSupplierRegistry(Supplier, AssociatedResources) addToSupplierRegistry}
	 */
	protected abstract void loadSupplierRegistry();
	
	
	public static class AssociatedResources {
		protected Class<? extends RequestExecutor<SearchRequest>> requestorClass;
		
		public AssociatedResources(Class<? extends RequestExecutor<SearchRequest>> requestorClass) {
			this.requestorClass = requestorClass;
		}
		
		public Class<? extends RequestExecutor<SearchRequest>> getRequestorClass() {
			return requestorClass;
		}
	}
}
