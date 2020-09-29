package microsearch.search.exceptions;

public class UnexpectedInputException extends Exception {
	private static final long serialVersionUID = 1L;

	public UnexpectedInputException() {
		super();
	}
	
	public UnexpectedInputException(String message) {
		super(message);
	}
}
