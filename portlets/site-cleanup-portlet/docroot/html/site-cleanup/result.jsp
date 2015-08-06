<%@include file="/html/init.jsp" %>

<c:if test="${!siteRemover.isRunning() && !siteRemover.isTerminated()}">
	<div>Started data cleanup...</div> <br />

	<!-- The call() method blocks until the cleanup is over -->
	<c:set value="${siteRemover.call()}" var="removedSites" />
</c:if>

<div>Data cleanup completed. Check server log for details.</div> <br />

<ol>
	<c:forEach items="${removedSites}" var="site">
		<li>Removed: <del><c:out value="${site.name}" /></del> </li>
	</c:forEach>
</ol>