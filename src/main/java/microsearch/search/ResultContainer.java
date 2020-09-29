package microsearch.search;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import microsearch.business.entities.Component;
import microsearch.business.entities.Supplier;
import microsearch.search.preparation.ResultContainerArranger;

public class ResultContainer {
	private String[] requestedComponents; //rows
	private Supplier[] suppliers; //columns
	private LinkedList<Component>[][] foundComponents; //table with results
	private AtomicLong emptyPositionCounter;
	private AtomicBoolean[][] submittedFlags; //table which indicates, what was already filled. To reduce fragility
	
	@SuppressWarnings("unchecked")
	public ResultContainer(ResultContainerArranger arranger) {
		requestedComponents = arranger.getRequestedComponents();
		suppliers = arranger.getSuppliers();
		int reqCompSize = requestedComponents.length;
		int suppliersSize = suppliers.length;
		foundComponents = new LinkedList[reqCompSize][suppliersSize];
		submittedFlags = new AtomicBoolean[reqCompSize][suppliersSize];
		for (int i = 0; i < reqCompSize; i++) {
			for (int j = 0; j < suppliersSize; j++) {
				foundComponents[i][j] = new LinkedList<Component>();
				submittedFlags[i][j] = new AtomicBoolean(false);
			}
		}
		emptyPositionCounter = new AtomicLong((long)reqCompSize*(long)suppliersSize);
	}
	
	public int getRequestedComponentSize() {
		return requestedComponents.length;
	}
	
	public String getRequestedComponent(int reqCompIndex) {
		return requestedComponents[reqCompIndex];
	}
	
	public int getSupplierSize() {
		return suppliers.length;
	}
	
	public Supplier getSupplier(int supplierIndex) {
		return suppliers[supplierIndex];
	}
	
	public void setFoundComponents(List<Component> foundComponents,
			int reqCompIndex, int supplierIndex) {
		if (foundComponents == null) {
			throw new IllegalArgumentException("foundComponents can not be null");
		}
		AtomicBoolean submittedFlag = this.submittedFlags[reqCompIndex][supplierIndex];
		synchronized(submittedFlag) {
			if (submittedFlag.get()) {
				throw new IllegalStateException("components for reqCompIndex=" + reqCompIndex
						+ ", supplierIndex=" + supplierIndex + "were already set");
			}
			this.foundComponents[reqCompIndex][supplierIndex].addAll(foundComponents);
			emptyPositionCounter.decrementAndGet();
			submittedFlag.set(true);
		}
	}
	
	public void noFoundComponents(int reqCompIndex, int supplierIndex) {
		AtomicBoolean submittedFlag = this.submittedFlags[reqCompIndex][supplierIndex];
		synchronized(submittedFlag) {
			if (submittedFlag.get()) {
				throw new IllegalStateException("components for reqCompIndex=" + reqCompIndex
						+ ", supplierIndex=" + supplierIndex + "were already set");
			}
			emptyPositionCounter.decrementAndGet();
			submittedFlag.set(true);
		}
	}
	
	public List<Component> getFoundComponents(int reqCompIndex, int supplierIndex) {
		AtomicBoolean submittedFlag = this.submittedFlags[reqCompIndex][supplierIndex];
		List<Component> result;
		synchronized (submittedFlag) {
			if (!submittedFlag.get()) {
				throw new IllegalStateException("components for reqCompIndex=" + reqCompIndex
						+ ", supplierIndex=" + supplierIndex + "were NOT set");
			}
			result = Collections.unmodifiableList(foundComponents[reqCompIndex][supplierIndex]);
		}
		
		return result;
	}
	
	public boolean isFilled() {
		return emptyPositionCounter.get() == 0;
	}
}
