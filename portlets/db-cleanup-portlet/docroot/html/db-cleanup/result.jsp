<%@include file="/html/init.jsp" %>

<div>Data cleanup completed. Check server log for details.</div> <br />

<ol>
	<c:forEach items="${removedSites}" var="site">
		<li>Removed: <del><c:out value="${site.name}" /></del> </li>
	</c:forEach>
</ol>