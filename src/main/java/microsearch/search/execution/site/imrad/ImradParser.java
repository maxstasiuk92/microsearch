package microsearch.search.execution.site.imrad;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import htmlparser.Template;
import htmlparser.data.Composition;
import htmlparser.data.Parameter;
import htmlparser.data.collectors.CompositionCollector;
import htmlparser.data.collectors.ParameterCollector;
import htmlparser.sequencer.HtmlParsingSequencer;
import microsearch.business.entities.Availability;
import microsearch.business.entities.Component;
import microsearch.business.entities.Currency;
import microsearch.business.entities.Pricing;
import microsearch.search.exceptions.UnexpectedInputException;
import microsearch.search.execution.site.ParserResult;


public class ImradParser {
	protected static final String NAME_TEMPLATE = "name.xml";
	protected static final String AVAILABLE_TEMPLATE = "available.xml";
	protected static final String PRE_ORDER_TEMPLATE = "pre-order.xml";
	protected static final String NOT_AVAILABLE_TEMPLATE = "not-available.xml";
	protected static final String PRICING_TEMPLATE = "pricing.xml";
	public static final String[] templateNames = {NAME_TEMPLATE, AVAILABLE_TEMPLATE,
			PRE_ORDER_TEMPLATE, NOT_AVAILABLE_TEMPLATE, PRICING_TEMPLATE};
	
	Matcher quantityMatcher;
	HtmlParsingSequencer parsingSequencer;
	Map<String, Template> templateRegister;
	
	public ImradParser(Map<String, Template> templateRegister, HtmlParsingSequencer parsingSequencer) {
		final String spaces = "\\s*?";
		final String quantityRegex = spaces + "דנם" + spaces + "/" + spaces + "רע"
								+ spaces + "-" + spaces + "מע" + spaces + "(\\d+)" + spaces;
		final Pattern quantityPattern = Pattern.compile(quantityRegex);
		this.quantityMatcher = quantityPattern.matcher("");
		this.templateRegister = templateRegister;
		this.parsingSequencer = parsingSequencer;
	}
	
	public ParserResult parse(InputStream httpBody) throws UnexpectedInputException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode bodyTree = null;
		try {
			bodyTree = objectMapper.readTree(new InputStreamReader(httpBody, StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		JsonNode componentFields = bodyTree.get("rows");
		Iterator<JsonNode> compFieldIterator = componentFields.elements();
		ArrayList<Component> foundComponents = new ArrayList<>(24);
		while (compFieldIterator.hasNext()) {
			Component c = parseComponent(compFieldIterator.next());
			foundComponents.add(c);
		}
		TreeMap<Integer, String> pageLinks = new TreeMap<>();
		return new ParserResult(foundComponents, pageLinks);
	}
	
	protected Component parseComponent(JsonNode componentJson) throws UnexpectedInputException {
		final String noBaseUri = "";
		
		String nameHtml = componentJson.get("name").textValue();
		if (nameHtml == null) {
			throw new UnexpectedInputException("field 'name' was not found in JSON");
		}
		Node nameHtmlTree = Jsoup.parse(nameHtml, noBaseUri, Parser.xmlParser());
		if (nameHtmlTree == null) {
			throw new UnexpectedInputException("name tree was not parsed by Jsoup");
		}
		String componentName = parseName(nameHtmlTree);
		
		String priceHtml = componentJson.get("price").textValue();
		if (priceHtml == null) {
			throw new UnexpectedInputException("field 'price' was not found in JSON");
		}
		Node priceHtmlTree = Jsoup.parse(priceHtml, noBaseUri, Parser.xmlParser());
		if (priceHtmlTree == null) {
			throw new UnexpectedInputException("pricing tree was not parsed by Jsoup");
		}
		Availability componentAvailability = parseAvailability(priceHtmlTree);
		
		Pricing componentPricing;
		if (componentAvailability != Availability.NOT_AVAILABLE) {
			componentPricing = parsePricing(priceHtmlTree);
		} else {
			componentPricing = new Pricing(Currency.HRYVNIA);
		}
				
		return new Component(componentName, componentAvailability, componentPricing);
	}
	
	protected String parseName(Node nameHtmlTree) throws UnexpectedInputException {
		Template nameTemplate = templateRegister.get(NAME_TEMPLATE);
		
		parsingSequencer.parse(nameTemplate, nameHtmlTree);
		if (!parsingSequencer.isParsed()) {
			throw new UnexpectedInputException("name was not parsed by htmlparser");
		}
		ParameterCollector nameCollector = (ParameterCollector)parsingSequencer.getDataCollector();
		List<Parameter> nameParameterList = nameCollector.getParameterList();
		if (nameParameterList.size() != 1) {
			throw new UnexpectedInputException("parsed not single parameter with name; number of parameters="
									+ nameParameterList.size());
		}
		Parameter nameParameter = nameParameterList.get(0); 
		if (!"name".equals(nameParameter.getName())) {
			throw new UnexpectedInputException("unexpected parameter name: '" + nameParameter.getName() 
									+ "'; expected 'name'");
		}
		return nameParameter.getValue();
	}
	
	protected Availability parseAvailability(Node priceHtmlTree) throws UnexpectedInputException {
		Template availableTemplate = templateRegister.get(AVAILABLE_TEMPLATE);
		Template preOrderTemplate = templateRegister.get(PRE_ORDER_TEMPLATE);
		Template notAvailableTemplate = templateRegister.get(NOT_AVAILABLE_TEMPLATE);
		
		Availability availability = null;
		if (parsingSequencer.parse(availableTemplate, priceHtmlTree)) {
			availability = Availability.AVAILABLE;
		} else if (parsingSequencer.parse(preOrderTemplate, priceHtmlTree)) {
			availability = Availability.PRE_ORDER;
		} else if (parsingSequencer.parse(notAvailableTemplate, priceHtmlTree)) {
			availability = Availability.NOT_AVAILABLE;
		} else {
			throw new UnexpectedInputException("availability was not parsed by htmlparser");
		}
		return availability;
	}
	
	protected Pricing parsePricing(Node priceHtmlTree) throws UnexpectedInputException {
		Pricing pricing = new Pricing(Currency.HRYVNIA);
		Template pricingTemplate = templateRegister.get(PRICING_TEMPLATE);
		
		parsingSequencer.parse(pricingTemplate, priceHtmlTree);
		if (!parsingSequencer.isParsed()) {
			throw new UnexpectedInputException("prising was not parsed by htmlparser");
		}
		CompositionCollector pricingCollector = (CompositionCollector)parsingSequencer.getDataCollector();
		List<Composition> priceCompositionList = pricingCollector.getCompositionList();
		HashSet<Integer> pricingSet = new HashSet<>();
		
		for (var priceComposition : priceCompositionList) {
			Map.Entry<Integer, Float> qp = getQuantityPrice(priceComposition);
			if (pricingSet.add(qp.getKey())) { //price - quantity pairs are duplicated in the input
				pricing.addPrice(qp.getValue(), qp.getKey());
			}
		}
		return pricing;
	}
	
	protected Map.Entry<Integer, Float> getQuantityPrice(Composition priceComposition) throws UnexpectedInputException {
		int parameterSize = priceComposition.size();
		String retailPriceText = null, wholesalePriceText = null;
		String quantityText = null;
		for (int i = 0; i < parameterSize; i++) {
			Parameter parameter = priceComposition.getParameter(i);
			switch (parameter.getName()) {
			case "price.retail":
				if (retailPriceText != null) {
					throw new UnexpectedInputException("parsed not single retail price");
				}
				retailPriceText = parameter.getValue();
				break;
			case "price.wholesale":
				if (wholesalePriceText != null) {
					throw new UnexpectedInputException("parsed not single wholesale price");
				}
				wholesalePriceText = parameter.getValue();
				break;
			case "quantity":
				if (quantityText != null) {
					throw new UnexpectedInputException("parsed not single quantity");
				}
				quantityText = parameter.getValue();
				break;
			default:
				throw new UnexpectedInputException("parsed unknown parameter: '" + parameter.getName() + "'");
			}
		}
		
		if (retailPriceText != null && wholesalePriceText != null) {
			throw new UnexpectedInputException("parsed both retail and wholesail prices");
		} else if (retailPriceText == null && wholesalePriceText == null) {
			throw new UnexpectedInputException("parsed neither retail nor wholesail prices");
		}
		if (wholesalePriceText != null && quantityText == null) {
			throw new UnexpectedInputException("quantity was not parsed");
		}
		
		int quantity;
		float price;
		if (retailPriceText != null) {
			try {
				price = Float.parseFloat(retailPriceText.replaceAll("\\s", "").replace(',', '.'));
			} catch (NumberFormatException e) {
				throw new UnexpectedInputException("retail price has incorrect number format: '"
								+ retailPriceText + "'");
			}
			quantity = 1;
		} else {
			try {
				price = Float.parseFloat(wholesalePriceText.replaceAll("\\s", "").replace(',', '.'));
			} catch (NumberFormatException e) {
				throw new UnexpectedInputException("wholesale price has incorrect number format: '"
								+ wholesalePriceText + "'");
			}
			quantityMatcher.reset(quantityText);
			if (!quantityMatcher.matches()) {
				throw new UnexpectedInputException("quantity pattern do not match: '" + quantityText +"'");
			}
			quantityText = quantityMatcher.group(1);
			try {
				quantity = Integer.parseInt(quantityText);
			} catch(NumberFormatException e) {
				throw new UnexpectedInputException("quantity has incorrect number format: '" + quantityText + "'");
			}
		}
		return new AbstractMap.SimpleEntry<>(quantity, price);
	}

}
