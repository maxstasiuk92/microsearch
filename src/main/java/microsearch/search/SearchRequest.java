package microsearch.search;

import java.util.List;

import microsearch.business.entities.Component;
import microsearch.business.entities.Supplier;

public interface SearchRequest {
	public Supplier getSupplier();
	public String getRequestedComponent();
	public void setFoundComponents(List<Component> components);
	public void noFoundComponents();
}
