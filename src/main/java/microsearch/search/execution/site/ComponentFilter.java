package microsearch.search.execution.site;

import java.util.List;

import microsearch.business.entities.Component;

public interface ComponentFilter {
	boolean addComponents(List<Component> newComponentList);
	List<Component> getFilteredComponents();
}
