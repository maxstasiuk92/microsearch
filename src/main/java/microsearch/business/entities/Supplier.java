package microsearch.business.entities;

public class Supplier {
	private int id;
	private String name;
	private String url;
	//TODO: add address, phone etc.
	
	protected static SupplierCreator supplierCreator;
	
	public static void setSupplierCreator(SupplierCreator creator) {
		Supplier.supplierCreator = creator;
	}
	
	private Supplier(int id, String name, String url) {
		this.id = Integer.valueOf(id);
		this.name = name;
		this.url = url;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUrl() {
		return url;
	}
	
	public static abstract class SupplierCreator {
		protected Supplier createSupplier(int id, String name, String url) {
			return new Supplier(id, name, url);
		}
	}
	
}