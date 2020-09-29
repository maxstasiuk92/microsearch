package microsearch.search.monitoring;

import java.sql.Timestamp;

import microsearch.business.entities.Supplier;

public class UnexpectedSiteResponseProblem extends Problem {
	private Supplier supplier;
	private String requestedComponent;
	private String message;
	private Timestamp timestamp;
	
	public UnexpectedSiteResponseProblem( Supplier supplier, String requestedComponent, String message) {
		super(Type.UNEXPECTED_SUPPLIER_SITE_RESPONSE);
		this.supplier = supplier;
		this.requestedComponent = requestedComponent;
		this.message = message;
		this.timestamp = null; //generated value
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public String getRequestedComponent() {
		return requestedComponent;
	}

	public String getMessage() {
		return message;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}
}
