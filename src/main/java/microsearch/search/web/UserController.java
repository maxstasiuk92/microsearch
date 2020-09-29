package microsearch.search.web;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import microsearch.search.ResultContainer;
import microsearch.search.execution.SearchResultProvider;
import microsearch.search.preparation.ComponentSetFetcher;
import microsearch.search.preparation.UndefinedOrderArranger;
import microsearch.business.entities.*;


@Controller
public class UserController {
	static final int REQUEST_FORM_COLUMNS = 3;
	
	private ComponentSetFetcher componentSetFetcher;
	private SupplierResolver supplierResolver;
	private SearchResultProvider resultProvider;
	private ResultPresenter resultPresenter;
	
	@Autowired
	@Qualifier(value = "componentListFetcher")
	public void setComponentSetFetcher(@NonNull ComponentSetFetcher componentSetFetcher) {
		this.componentSetFetcher = componentSetFetcher;
	}
	
	@Autowired
	public void setSupplierResolver(@NonNull SupplierResolver supplierResolver) {
		this.supplierResolver = supplierResolver;
	}
	
	@Autowired
	public void setResultPresenter(ResultPresenter resultPresenter) {
		this.resultPresenter = resultPresenter;
	}
	
	@Autowired
	public void setResultProvider(@NonNull SearchResultProvider resultProvider) {
		this.resultProvider = resultProvider;
	}
	
	@RequestMapping(value = "/")
	public String emptyMainView(Model model) {
		initRequestForm(model, supplierResolver.getHtmlPresentations(), "", "");
		return "firstRequest";
	}
	
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public String getResults(HttpServletRequest httpRequest, Model model) {
		String request = httpRequest.getParameter("request");
		String[] supplierIds = httpRequest.getParameterValues("suppliers");
		
		if (supplierIds == null) {
			initRequestForm(model, supplierResolver.getHtmlPresentations(), request, "choose at least one supplier");
			return "firstRequest";
		}

		Set<String> requestedComponents = null;
		Set<Supplier> requestedSuppliers = null;
		
		requestedSuppliers = supplierResolver.getRequestedSet(supplierIds);
		requestedComponents = componentSetFetcher.getRequestedSet(request);
		if (requestedComponents.size() == 0) {
			initRequestForm(model, supplierResolver.getHtmlPresentations(), request, "type at least one component");
			return "firstRequest";
		}
		ResultContainer resultContainer = new ResultContainer(new UndefinedOrderArranger(requestedComponents, requestedSuppliers));
		resultProvider.fillResultContainer(resultContainer);
		
		model.addAttribute("result", resultPresenter.getHtmlPresentation(resultContainer));
		return "resultList";
	}
	
	public static void initRequestForm(Model model, String[] supplierViews, String defaultRequest, String message) {
		String supplierTable = Utils.wrapInTableRows(supplierViews, REQUEST_FORM_COLUMNS);
		model.addAttribute("columns", Integer.toString(REQUEST_FORM_COLUMNS));
		model.addAttribute("suppliers", supplierTable);
		model.addAttribute("default_request", defaultRequest);
		model.addAttribute("message", message);
	}
}