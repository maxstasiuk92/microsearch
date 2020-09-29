package microsearch.search.execution.site.imrad;

import microsearch.business.entities.Component;
import microsearch.search.SearchRequest;
import microsearch.search.exceptions.UnexpectedInputException;
import microsearch.search.execution.site.ComponentFilter;
import microsearch.search.execution.site.ParserResult;
import microsearch.search.execution.site.RequestorResult;
import microsearch.search.execution.site.SiteCookieRegistry;
import microsearch.search.execution.site.SitePagesRegistry;
import microsearch.search.monitoring.NoSiteConnectionProblem;
import microsearch.search.monitoring.ProblemMonitor;
import microsearch.search.monitoring.RuntimeProblem;
import microsearch.search.monitoring.UnexpectedSiteResponseProblem;

import java.util.List;
import java.util.function.Supplier;

public class ImradSearcher {
	ImradRequestor siteRequestor;
	ImradParser siteParser;
	Supplier<ComponentFilter> componentFilterSupplier;
	ProblemMonitor problemMonitor;
	
	public ImradSearcher(ImradRequestor htmlRequestor, ImradParser htmlParser,
			Supplier<ComponentFilter> componentFilterSupplier, ProblemMonitor problemMonitor) {
		this.siteParser = htmlParser; 
		this.siteRequestor = htmlRequestor;
		this.componentFilterSupplier = componentFilterSupplier;
		this.problemMonitor = problemMonitor;
	}
		
	public void search(SearchRequest searchRequest) {
		String requestedComponent = searchRequest.getRequestedComponent();
		SitePagesRegistry pagesRegistry = new SitePagesRegistry();
		SiteCookieRegistry cookieRegistry = new SiteCookieRegistry();
		
		ComponentFilter componentFilter = componentFilterSupplier.get();
		for (int page = 1; page <= 3; page++) {
			RequestorResult requestorResult = null;
			ParserResult parserResult = null;
			try {
				if (page == 1) {
					requestorResult = siteRequestor.request(requestedComponent, cookieRegistry);
				} else if (pagesRegistry.hasPageAddress(page)) {
					requestorResult = siteRequestor.request(page, pagesRegistry, cookieRegistry);
				} else {
					break;
				}
				
				if (requestorResult.hasResult()) {
					parserResult = siteParser.parse(requestorResult.getBodyStream());
				} else if (requestorResult == RequestorResult.notFound()) {
					break;
				} else if (requestorResult == RequestorResult.noConnection()) {
					problemMonitor.report(new NoSiteConnectionProblem(searchRequest.getSupplier()));
					break;
				}
			} catch (UnexpectedInputException e) {
				problemMonitor.report(new UnexpectedSiteResponseProblem(searchRequest.getSupplier(),
						searchRequest.getRequestedComponent(), e.getMessage()));
				break;
			} catch (Exception e) {
				problemMonitor.report(new RuntimeProblem(e));
				break;
			} finally {
				try {
					if (requestorResult != null) {
						requestorResult.close();
					}
				} catch (Exception e) {} //nothing to do
			}
			
			if (parserResult.hasResults()) {
				List<Component> foundComponents = parserResult.getFoundComponents();
				if (!componentFilter.addComponents(foundComponents)) {
					break;
				}
				pagesRegistry.updatePages(parserResult.getPageLinks());
			}
		}
		List<Component> resultComponents = componentFilter.getFilteredComponents();
		searchRequest.setFoundComponents(resultComponents);
	}
	
}
