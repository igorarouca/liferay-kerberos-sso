<%@include file="/html/init.jsp" %>

<portlet:actionURL name="cleanSites" var="cleanSites" />

<liferay-ui:error key="empty-site-list" message="Please provide a list of sites to be removed" />
<liferay-ui:error key="no-sites-to-remove" message="There are no sites to remove" />

<aui:form action="${cleanSites}" cssClass="site-cleanup" method="post" name="<portlet:namespace />clean-sites-fm">
	<aui:input cols="3" cssClass="sites" label="Sites to be removed:" name="sites" placeholder="Enter one site name per line" rows="40" style="width: 200px; height: 300px;" type="textarea">
		<aui:validator errorMessage="Please provide a list of sites to be removed" name="required" />
	</aui:input>

	<aui:button-row>
		<aui:button name="clean" type="submit" value="Clean" />
	</aui:button-row>
</aui:form>