package microsearch.search.execution.site;

import java.io.InputStream;

public class RequestorResult implements AutoCloseable {
	private InputStream bodyStream;
	private static RequestorResult notFoundResult;
	private static RequestorResult noConnectionResult;
	private int type;
	private static final int NORMAL = 0;
	private static final int NOT_FOUND = 1;
	private static final int NO_CONNECTION = 2;
	
	public static RequestorResult notFound() {
		if (notFoundResult == null) {
			notFoundResult = new RequestorResult();
			notFoundResult.type = NOT_FOUND;
		}
		return notFoundResult;
	}
	
	public static RequestorResult noConnection() {
		if (noConnectionResult == null) {
			noConnectionResult = new RequestorResult();
			noConnectionResult.type = NO_CONNECTION;
		}
		return noConnectionResult;
	}
	
	private RequestorResult() {}
	
	public RequestorResult(InputStream bodyStream) {
		if (bodyStream == null) {
			throw new IllegalArgumentException("bodyStream can not be null");
		}
		this.bodyStream = bodyStream;
		this.type = NORMAL;
	}
	
	public boolean hasResult() {
		return type == NORMAL;
	}
	
	public InputStream getBodyStream() {
		if (type != NORMAL) {
			throw new UnsupportedOperationException("getBodyStream() was invoked when notFound");
		}
		return bodyStream;
	}
	
	@Override
	public void close() throws Exception {
		if (bodyStream != null) {
			bodyStream.close();
		}	
	}
	
}
