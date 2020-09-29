package microsearch.business.entities;

public enum Currency {
	HRYVNIA {
		public String symbol() {return "\u20B4";}
	},
	DOLLAR {
		public String symbol() {return "\u0024";}
	},
	EURO {
		public String symbol() {return "\u20AC";}
	},
	YAUN {
		public String symbol() {return "\uFFE5";}
	},
	YEN {
		public String symbol() {return "\u00A5";}
	};
	
	public abstract String symbol();
}
