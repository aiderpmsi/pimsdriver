<?xml version="1.0" encoding="ISO-8859-1"?>
<%@page import="aider.org.pmsiadmin.model.xml.XmlReport"%>
<jsp:directive.page contentType="application/xml; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1" 
  import="org.springframework.web.util.UriUtils"
  trimDirectiveWhitespaces="true"/>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set  var="basedir" value="${pageContext.request.contextPath}"/>
<?xml-stylesheet href="${pageContext.request.contextPath}/static/xsl/InsertionPmsi.xsl" type="text/xsl"?>

<insertionpmsi
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:noNamespaceSchemaLocation="${pageContext.request.contextPath}/static/xsd/InsertionPmsi.xsd"
  basedir = "${basedir}">

  <status value="${status}"/>
  
  <listinfos>
    <c:forEach var="item" items="${parserreport}" varStatus="i">
      <info>
        <parser><c:out value="${item.getStackTrace()[0].getClassName()}"/></parser>
        <error><c:out value="${item.getMessage()}"/></error>
      </info>
    </c:forEach>
  </listinfos>
  
  <c:if test="${xmlreport != null}">
    <% out.print(((XmlReport) request.getAttribute("xmlreport")).getReport()); %>
  </c:if>
  
</insertionpmsi>
