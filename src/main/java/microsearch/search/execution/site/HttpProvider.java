package microsearch.search.execution.site;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface HttpProvider {
	/**
	 *	Blocks till response is not received 
	 */
	public HttpResponse<InputStream> getResponse(HttpRequest request);
}
