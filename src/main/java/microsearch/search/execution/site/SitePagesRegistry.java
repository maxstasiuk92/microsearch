package microsearch.search.execution.site;

import java.util.Set;
import java.util.SortedMap;

public class SitePagesRegistry {
	private SortedMap<Integer, String> contentPages;
	private int totalPagesEstimate;
	
	public SitePagesRegistry() {
		contentPages = null;
		totalPagesEstimate = 0;
	}
	
	public void updatePages(SortedMap<Integer, String> contentPages) {
		if (contentPages != null && contentPages.size() > 0) {
			int maxPageNumber = contentPages.lastKey();
			Integer.max(totalPagesEstimate, maxPageNumber);
			this.contentPages = contentPages;
		}
	}
	
	public int getTotalPagesEstimate() {
		return totalPagesEstimate;
	}
	
	public Integer getFirstAvailablePage() {
		if (contentPages != null) {
			return contentPages.firstKey();
		} else {
			return null;
		}
	}
	
	public Integer getLastAvailablePage() {
		if (contentPages != null) {
			return contentPages.lastKey();
		} else {
			return null;
		}
	}
	
	public Set<Integer> getAvailablePages() {
		if (contentPages != null) {
			return contentPages.keySet();
		} else {
			return null;
		}
	}
	
	public boolean hasPageAddress(int page) {
		if (page < 0) {
			throw new IllegalArgumentException("arg page is " + page + ", but should be >= 0");
		}
		if (contentPages == null) {
			return false;
		} else {
			return contentPages.containsKey(page);
		}
	}
	
	public String getPageAddress(int page) {
		if (page < 0) {
			throw new IllegalArgumentException("arg page is " + page + ", but should be >= 0");
		}
		if (contentPages == null) {
			throw new IllegalStateException("registry is not initialized");
		} 
		String address = contentPages.get(page);
		if (address == null) {
			throw new IllegalArgumentException("requested page " + page + "which is not in registry");
		}
		return address;
	}
}
