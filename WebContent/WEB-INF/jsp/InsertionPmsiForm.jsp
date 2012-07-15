<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix= "c" uri="http://java.sun.com/jstl/core"%>
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
	<h2>Fichier à importer</h2>
 
 	<form:form method="POST" commandName="insertionpmsiform"
		enctype="multipart/form-data">
 
		<form:errors path="*" cssClass="errorblock" element="div" />
 		<input type="text" name="name" />
		Fichier Pmsi à Uploader : <input type="file" name="file" />
		<input type="submit" value="upload" />
		<span><form:errors path="file" cssClass="error" />
		</span>
 
	</form:form> 
</body>
</html>
