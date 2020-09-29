package microsearch.search.web;

import microsearch.search.ResultContainer;

public interface ResultPresenter {
	String getHtmlPresentation(ResultContainer resultContainer);
}
