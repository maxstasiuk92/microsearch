package microsearch.search.execution.site;

import java.util.List;
import java.util.SortedMap;

import microsearch.business.entities.Component;

public class ParserResult {
	private List<Component> foundComponents;
	private SortedMap<Integer, String> pageLinks;
	private boolean notFound;
	private static ParserResult notFoundResult;
	
	public static ParserResult notFound() {
		if (notFoundResult == null) {
			notFoundResult = new ParserResult();
			notFoundResult.notFound = true;
		}
		return notFoundResult;
	}
	
	private ParserResult() {}
	
	/**
	 * @param foundComponents not null
	 * @param pageLinks not null, put empty map if no section with link to pages
	 */
	public ParserResult(List<Component> foundComponents, SortedMap<Integer, String> pageLinks) {
		if (foundComponents == null || pageLinks == null) {
			throw new IllegalArgumentException("arguments can not be null");
		}
		this.foundComponents = foundComponents;
		this.pageLinks = pageLinks;
		notFound = false;
	}
	
	public boolean hasResults() {
		return !notFound;
	}
	
	public List<Component> getFoundComponents() {
		if (notFound) {
			throw new UnsupportedOperationException("getFoundComponents() invoked when notFound");
		}
		return foundComponents;
	}
	
	public SortedMap<Integer, String> getPageLinks() {
		if (notFound) {
			throw new UnsupportedOperationException("getPageLinks() invoked when notFound");
		}
		return pageLinks;
	}
}
