package microsearch.search.execution.site;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import microsearch.business.entities.Availability;
import microsearch.business.entities.Component;

public class ThreeAvailableComponentFilter implements ComponentFilter {
	ArrayList<Component> components;
	
	public ThreeAvailableComponentFilter() {
		components = new ArrayList<>(3);
	}

	@Override
	public boolean addComponents(List<Component> newComponentList) {
		Iterator<Component> ci = newComponentList.iterator();
		boolean willAccept = components.size() < 3;
		while (willAccept && ci.hasNext()) {
			Component c = ci.next();
			if (c.getAvailability() == Availability.AVAILABLE) {
				components.add(c);
				willAccept = components.size() < 3;
			}
		}
		return willAccept;
	}

	@Override
	public List<Component> getFilteredComponents() {
		return components;
	}

}
