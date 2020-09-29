package microsearch.search.web;

public class Utils {
	
	public static String wrapInTableRows(String[] elements, int columns) {
		StringBuilder result = new StringBuilder();
		int col = 0;
		for (int i = 0; i < elements.length; i++) {
			if (col == 0) {
				result.append("<tr>");
			}
			result.append("<td>" + elements[i] + "</td>");
			col++;
			if (col == columns) {
				result.append("</tr>");
				col = 0;
			}
		}
		if (col != 0) {
			for (int i = col; i < columns; i++) {
				result.append("<td></td>");
			}
			result.append("</tr>");
		}
		return result.toString();
	}
}
