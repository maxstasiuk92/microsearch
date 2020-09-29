package microsearch.business.entities;

public class Component {
	private String name;
	private Availability availability;
	private Pricing pricing;
	
	public Component(String name, Availability availability, Pricing pricing) {
		if (name == null || availability == null || pricing == null) {
			throw new IllegalArgumentException("argument can not be null");
		}
		this.name = name; 
		this.availability = availability;
		this.pricing = pricing;
	}
		
	public String getName() {
		return name;
	}
	
	public Availability getAvailability() {
		return availability;
	}
	
	public Pricing getPricing() {
		return pricing;
	}
	
	
}
