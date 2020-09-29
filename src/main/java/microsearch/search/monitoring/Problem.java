package microsearch.search.monitoring;

public abstract class Problem {
	private Type type;
	public enum Type {UNEXPECTED_SUPPLIER_SITE_RESPONSE, NO_SUPPLIER_SITE_CONNECTION, RUNTIME_EXCEPTION};
		
	protected Problem(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
}
