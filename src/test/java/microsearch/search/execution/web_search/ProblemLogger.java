package microsearch.search.execution.web_search;

import microsearch.search.monitoring.*;


public class ProblemLogger implements ProblemMonitor {

	@Override
	synchronized public void report(Problem problem) {
		System.out.println(describeProblem(problem));
	}
	
	protected String describeProblem(Problem problem) {
		String result = null;
		switch (problem.getType()) {
		case UNEXPECTED_SUPPLIER_SITE_RESPONSE:
			{
				UnexpectedSiteResponseProblem p = (UnexpectedSiteResponseProblem)problem;
				result = p.getType().name() + "; " + p.getSupplier().getName() + "; " 
						+ p.getRequestedComponent() + "; " + p.getMessage();
			}
			break;
		case NO_SUPPLIER_SITE_CONNECTION:
			{
				NoSiteConnectionProblem p = (NoSiteConnectionProblem)problem;
				result = p.getType().name() + "; " + p.getSupplier().getName();
			}
			break;
		case RUNTIME_EXCEPTION:
			{
				RuntimeProblem p = (RuntimeProblem)problem;
				result = p.getType().name() + "; " + p.getStackTrace();
			}
			break;
		}
		return result;
	}

}
