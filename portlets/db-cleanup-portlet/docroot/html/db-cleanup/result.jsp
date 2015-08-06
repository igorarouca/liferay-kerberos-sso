<%@include file="/html/init.jsp" %>

<c:if test="${!siteCleaner.isRunning() && !siteCleaner.isTerminated()}">
	<div>Started database cleanup...</div> <br />

	<!-- The call() method blocks until the cleanup is over -->
	<c:set value="${siteCleaner.call()}" var="removedSites" />
</c:if>

<div>Data cleanup completed. Check server log for details.</div> <br />

<ol>
	<c:forEach items="${removedSites}" var="site">
		<li>Removed: <del><c:out value="${site.name}" /></del> </li>
	</c:forEach>
</ol>