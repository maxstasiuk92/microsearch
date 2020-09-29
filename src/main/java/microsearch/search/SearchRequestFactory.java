package microsearch.search;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SearchRequestFactory implements Iterator<SearchRequest>{
	private ResultContainer resultContainer;
	private int reqCompIndex;
	private int supplierIndex;
	private boolean hasNext;
	
	public SearchRequestFactory(ResultContainer container) {
		if (container == null) {
			throw new NullPointerException();
		}
		this.resultContainer = container;
		reqCompIndex = 0;
		supplierIndex = 0;
		if (reqCompIndex < this.resultContainer.getRequestedComponentSize()
				&& supplierIndex < this.resultContainer.getSupplierSize()) {
			hasNext = true;
		} else {
			hasNext = false;
		}
	}
	
	@Override
	public boolean hasNext() {
		return hasNext;
	}
	
	@Override
	public SearchRequest next() throws NoSuchElementException {
		if (!hasNext) {
			throw new NoSuchElementException();
		}
		SearchRequestData request = new SearchRequestData(resultContainer, reqCompIndex, supplierIndex);
		updateIndexes();
		return request;
	}
	
	protected void updateIndexes() {
		supplierIndex++;
		if (supplierIndex >= resultContainer.getSupplierSize()) {
			supplierIndex = 0;
			reqCompIndex++;
			if (reqCompIndex >= resultContainer.getRequestedComponentSize()) {
				hasNext = false;
			}
		}
	}
	
}
