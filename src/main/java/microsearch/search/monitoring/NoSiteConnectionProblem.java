package microsearch.search.monitoring;

import java.sql.Timestamp;

import microsearch.business.entities.Supplier;

public class NoSiteConnectionProblem extends Problem{
	private Supplier supplier;
	private Timestamp timestamp;
	
	public NoSiteConnectionProblem(Supplier supplier) {
		super(Type.NO_SUPPLIER_SITE_CONNECTION);
		this.supplier = supplier;
		this.timestamp = null; //generated value
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}
	
}
