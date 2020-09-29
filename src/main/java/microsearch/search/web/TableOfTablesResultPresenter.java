package microsearch.search.web;

import java.util.Iterator;

import microsearch.business.entities.Component;
import microsearch.business.entities.Pricing;
import microsearch.search.ResultContainer;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class TableOfTablesResultPresenter implements ResultPresenter {
	protected static final String FOUND_COMPONENT_STYLE = "border:1px dotted black; width: 100%;";
	protected static final String RESULT_CELL_STYLE = "border:1px solid black;";
	protected static final String REQUESTED_COMPONENT_STYLE = "border:1px solid black; text-align: center;";
	
	@Override
	public String getHtmlPresentation(ResultContainer resultContainer) {
		int reqCompSize = resultContainer.getRequestedComponentSize();
		StringBuilder result = new StringBuilder();
		result.append("<table style=\"border:3px solid black;\">");
		result.append("<thead>" + presentHeaderRow(resultContainer) + "</thead>");
		result.append("<tbody>");
		for (int c = 0; c < reqCompSize; c++) {
			result.append(presentDataRow(resultContainer, c));
		}
		result.append("</tbody>");
		result.append("</table>");
		return result.toString();
	}
	
	protected String presentHeaderRow(ResultContainer resultContainer) {
		StringBuilder result = new StringBuilder(100);
		result.append("<tr>");
		result.append("<th>Req.</th>");
		int supplierSize = resultContainer.getSupplierSize();
		for (int s = 0; s < supplierSize; s++) {
			result.append("<th>" + resultContainer.getSupplier(s).getName() + "</th>");
		}
		result.append("</tr>");
		return result.toString();
	}
	
	protected String presentDataRow(ResultContainer resultContainer, int requestedCompIndex) {
		int supplierSize = resultContainer.getSupplierSize();
		StringBuilder result = new StringBuilder(100);
		result.append("<tr>");
		result.append("<td style=\"" + REQUESTED_COMPONENT_STYLE + "\">" + resultContainer.getRequestedComponent(requestedCompIndex) + "</td>");
		for (int s = 0; s < supplierSize; s++) {
			result.append("<td style=\"" + RESULT_CELL_STYLE + "\">");
			Iterator<Component> foundComponents = resultContainer.getFoundComponents(requestedCompIndex, s).iterator();
			if (foundComponents.hasNext()) {
				var c = foundComponents.next();
				result.append(presentComponentItem(c));
			}
			while (foundComponents.hasNext()) {
				var c = foundComponents.next();
				result.append("<div style=\"line-height: 20%;\"> <br/> </div>");
				result.append(presentComponentItem(c));
			}
			
			result.append("</td>");
		}
		result.append("</tr>");
		return result.toString();
	}
	
	protected String presentComponentItem(Component component) {
		String availability = null;
		switch (component.getAvailability()) {
		case NOT_AVAILABLE: availability = "<div style=\"color:red\">not available</div>";
			break;
		case AVAILABLE: availability = "<div style=\"color:green\">available</div>";
			break;
		case PRE_ORDER: availability = "<div style=\"color:orange\">pre-order</div>";
			break;
		}
		
		Pricing pricing = component.getPricing();
		StringBuilder pricingBuilder = new StringBuilder(20);
		String currency = pricing.getCurrency().symbol();
			
		for (var p : pricing.getPrices().entrySet()) {
			pricingBuilder.append("<tr><td>from " + p.getKey() + ": " + p.getValue() + " " + currency + "</td></tr>");
		}
		
		return "<table style=\""+ FOUND_COMPONENT_STYLE +"\">" 
				+ "<thead><tr><th>" + component.getName() + "</th></tr></thead>" 
				+ "<tbody>" 
					+ "<tr><td>" + availability + "</td></tr>"
					+ pricingBuilder.toString()
				+ "</tbody>"
				+ "</table>";
	}
		
}
