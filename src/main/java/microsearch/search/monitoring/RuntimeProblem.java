package microsearch.search.monitoring;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;

public class RuntimeProblem extends Problem {
	private String stackTrace;
	private Timestamp timestamp;
	
	public RuntimeProblem(String stackTrace) {
		super(Type.RUNTIME_EXCEPTION);
		this.stackTrace = stackTrace;
		this.timestamp = null; //generated value
	}
	
	public RuntimeProblem(Exception e) {
		super(Type.RUNTIME_EXCEPTION);
		try(StringWriter traceWriter = new StringWriter(); 
				PrintWriter tracePrinter = new PrintWriter(traceWriter)) {
			e.printStackTrace(tracePrinter);
			this.stackTrace = traceWriter.toString();
			this.timestamp = null; //generated value
		} catch (IOException x) {}
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}
}
