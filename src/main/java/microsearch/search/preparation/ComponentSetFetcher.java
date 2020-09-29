package microsearch.search.preparation;

import java.util.Set;

import microsearch.search.exceptions.FetcherException;

public interface ComponentSetFetcher {
	Set<String> getRequestedSet(String requestInput) throws FetcherException;
}
