package microsearch.search.execution.site.imrad;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import microsearch.search.exceptions.UnexpectedInputException;
import microsearch.search.execution.site.HttpProvider;
import microsearch.search.execution.site.RequestorResult;
import microsearch.search.execution.site.SiteCookieRegistry;
import microsearch.search.execution.site.SitePagesRegistry;

public class ImradRequestor {
	HttpProvider httpProvider;
	
	public ImradRequestor(HttpProvider httpProvider) {
		this.httpProvider = httpProvider;
	}
	
	public RequestorResult request(String requestedComponent, SiteCookieRegistry cookieRegistry)
			throws UnexpectedInputException {
		String link = getPageAddress(requestedComponent);
		return requestAddress(link);
	}
	
	public RequestorResult request(int page, SitePagesRegistry pagesRegistry, SiteCookieRegistry cookieRegistry) 
			throws UnexpectedInputException {
		String link = getPageAddress(page, pagesRegistry);
		return requestAddress(link);
	}
	
	protected String getPageAddress(int page, SitePagesRegistry sitePages) {
		String link = sitePages.getPageAddress(page);
		if (link == null) {
			throw new IllegalArgumentException("page " + page + " was not found in registry");
		}
		return link;
	}
	
	protected String getPageAddress(String requestedComponent) {
		return "https://imrad.com.ua/ru/search?searchkeywords=" + URLEncoder.encode(requestedComponent, StandardCharsets.UTF_8);
	}
	
	protected RequestorResult requestAddress(String link) throws UnexpectedInputException {
		URI uri;
		try {
			uri = new URI(link);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(uri)
				.POST(HttpRequest.BodyPublishers.ofString("size=24", StandardCharsets.UTF_8))
				.setHeader("Accept", "*/*")
				.setHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
				.setHeader("Sec-Fetch-Dest", "empty")
				.setHeader("Sec-Fetch-Mode", "cors")
				.setHeader("Sec-Fetch-Site", "same-origin")
				.setHeader("X-Requested-With", "XMLHttpRequest")
				.build();
		
		HttpResponse<InputStream> httpResponse = httpProvider.getResponse(httpRequest);
		
		if (httpResponse == null) {
			return RequestorResult.noConnection();
		}
		
		int statusCode = httpResponse.statusCode();
		if (statusCode == HttpServletResponse.SC_OK) {
			return new RequestorResult(httpResponse.body());
		} else if (statusCode == HttpServletResponse.SC_MOVED_PERMANENTLY){
			return RequestorResult.notFound();
		} else {
			throw new UnexpectedInputException("received not expected statuc code " + statusCode);
		}
	}
	
	
}
