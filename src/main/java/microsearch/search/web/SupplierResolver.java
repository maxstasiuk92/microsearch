package microsearch.search.web;

import java.util.Set;

import microsearch.business.entities.Supplier;
import microsearch.search.exceptions.FetcherException;


public interface SupplierResolver {
	public String[] getHtmlPresentations();
	public Set<Supplier> getRequestedSet(String[] requestedSuppliers) throws FetcherException;
}
