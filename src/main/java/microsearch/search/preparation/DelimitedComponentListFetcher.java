package microsearch.search.preparation;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * expected format: <component name without white spaces><',' or ';'><component name without spaces>...
 * throw FetcherException if format is different
 */

@Component(value = "componentListFetcher")
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DelimitedComponentListFetcher implements ComponentSetFetcher {

	@Override
	public Set<String> getRequestedSet(String requestInput) {
		final char[] spliters = new char[] {',', ';'};
		requestInput = requestInput + spliters[0];
		HashSet<String> resultSet = new HashSet<>();
		HashSet<String> contentSet = new HashSet<>();
		
		StringBuilder componentBuilder = new StringBuilder();
		for (int i = 0; i < requestInput.length(); i++) {
			char symbol;
			if (equalsAny(symbol = requestInput.charAt(i), spliters)) {
				if (componentBuilder.length() > 0) {
					String component = componentBuilder.toString();
					if (contentSet.add(component.toLowerCase())) {
						resultSet.add(component);
					}
				}
				componentBuilder = new StringBuilder();
			} else {
				componentBuilder.append(symbol);
			}
		}
		
		return resultSet;		
	}
	
	protected boolean equalsAny(char symbol, char[] variants) {
		for (var v : variants) {
			if (symbol == v) {
				return true;
			}
		}
		return false;
	}
	
}
