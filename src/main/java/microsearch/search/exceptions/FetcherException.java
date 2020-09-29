package microsearch.search.exceptions;

public class FetcherException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public FetcherException() {
		super();
	}
	
	public FetcherException(String message) {
		super(message);
	}

}
