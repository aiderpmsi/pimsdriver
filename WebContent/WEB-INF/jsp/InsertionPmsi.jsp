<?xml version="1.0" encoding="ISO-8859-1"?>
<jsp:directive.page contentType="application/xml; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1" 
  import="org.springframework.web.util.UriUtils"
  trimDirectiveWhitespaces="true"/>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<insertion type="PMSI">
  
  <status value="${status}"/>
  
  <listinfos>
    <c:forEach var="item" items="${parserreport}" varStatus="i">
      <info>
        <parser><c:out value="${item.getStackTrace()[0].getClass().getName()}"/></parser>
        <error><c:out value="${item.getMessage()}"/></error>
      </info>
    </c:forEach>
  </listinfos>
  
  <report>
    <c:if test="${report != null}">
      <% out.print((String) request.getAttribute("report")); %>
    </c:if>
  </report>
</body>
</html>