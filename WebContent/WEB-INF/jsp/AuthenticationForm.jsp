<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix= "c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<style>
.error {
	color: #ff0000;
}
 
.errorblock {
	color: #000;
	background-color: #ffEEEE;
	border: 3px solid #ff0000;
	padding: 8px;
	margin: 16px;
}
</style>
</head>
<body>
	<h2>Authentification</h2>
 
	<form:form method="POST" commandName="authenticationform">
		<form:errors path="*" cssClass="errorblock" element="div" />
		<table>
			<tr>
				<td>User :</td>
				<td><form:input path="user" />
				</td>
				<td><form:errors path="user" cssClass="error" />
				</td>
			</tr>
			<tr>
				<td>Password :</td>
				<td><form:password path="pass" />
				</td>
				<td><form:errors path="pass" cssClass="error" />
				</td>
			</tr>
			<tr>
				<td colspan="3"><input type="submit" /></td>
			</tr>
		</table>
	</form:form>
 
</body>
</html>
