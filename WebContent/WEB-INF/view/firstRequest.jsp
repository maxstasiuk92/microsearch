<%@ page language="java" contentType="text/html; charset=UTF-16"
    pageEncoding="UTF-16"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-16">
<title>mainView</title>
</head>
<body>
	<h2 align="center">microsearch</h2>
	<form action="search" target="_self" method="post">
		<table>
			<thead>
				<tr>
					<th colspan="${columns}">
						<label for="request-input">Semicolon separated list of components</label>
						<input id="request-input" name="request" type="text" size="80" maxlength=256 value="${default_request}">
						<input type="submit" value="Search">
					</th>
				</tr>
			</thead>
			<tbody>
				${suppliers}
			</tbody>
		</table>
	</form>
	<div style="text-align: center;">${message}</div>
</body>
</html>