package microsearch.business.entities;

import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Pricing {
	private Currency currency;
	private TreeMap<Integer, Float> prices;
	
	public Pricing(Currency currency) {
		if (currency == null) {
			throw new IllegalArgumentException("currency can not be null");
		}
		this.currency = currency;
		this.prices = new TreeMap<>();
	}
	
	/**
	 * @param price price when ordering >= then fromAmount
	 * @param fromAmount min. order for specified price
	 * @exception ConfigurationException if 
	 * - fromEmount was already specified or
	 * - price is negative
	 */
	public void addPrice(float price, int fromAmount) {
		if (prices.containsKey(fromAmount)) {
			throw new IllegalArgumentException("fromEmount=" + fromAmount + " already exists");
		}
		if (price < 0) {
			throw new IllegalArgumentException("price should not be negative");
		}
		prices.put(fromAmount, price);
	}
	
	public Currency getCurrency() {
		return currency;
	}
	
	public NavigableMap<Integer, Float> getPrices() {
		return Collections.unmodifiableNavigableMap(prices);
	}
}
