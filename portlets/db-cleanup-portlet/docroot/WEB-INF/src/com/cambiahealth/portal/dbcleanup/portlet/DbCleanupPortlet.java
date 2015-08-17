package com.cambiahealth.portal.dbcleanup.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.ProcessAction;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.cambiahealth.portal.dbcleanup.DbCleanupConstants;
import com.cambiahealth.portal.dbcleanup.cleaners.CorruptedDataCleanerUtil;
import com.cambiahealth.portal.dbcleanup.cleaners.SiteCleanerUtil;
import com.cambiahealth.portal.dbcleanup.cleaners.site.SiteCleaner;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.util.PortalUtil;

/**
 * Portlet implementation class SiteCleanupPortlet
 */
public class DbCleanupPortlet extends GenericPortlet {

	public void doView(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		SiteCleaner siteCleaner =
			(SiteCleaner)renderRequest.getAttribute("siteCleaner");

		if (siteCleaner == null) {
			include(viewTemplate, renderRequest, renderResponse);
			return;
		}

		if (siteCleaner.hasSitesToRemove()) {
			include(resultTemplate, renderRequest, renderResponse);
			return;
		}

		SessionErrors.add(renderRequest, "no-sites-to-remove");
		include(viewTemplate, renderRequest, renderResponse);
	}

	public void init() {
		resultTemplate = getInitParameter("result-template");
		viewTemplate = getInitParameter("view-template");
	}

	@ProcessAction(name="cleanSites")
	public void cleanSites(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {

		String siteList = StringUtil.trim(actionRequest.getParameter("sites"));

		if ((siteList == null) || siteList.isEmpty()) {
			SessionErrors.add(actionRequest, "empty-site-list");
			_log.error(">>> Please provide a list of sites to be removed");
			return;
		}

		long companyId = PortalUtil.getCompanyId(actionRequest);
		List<String> siteNames = new ArrayList<>();

		for (String name : siteList.split(_LINE_SEPARATOR_REGEX)) {
			siteNames.add(StringUtil.trim(name));
		}

		SiteCleaner siteCleaner = null;

		if (DbCleanupConstants.PARALLEL_EXECUTION_ENABLED) {
			siteCleaner = SiteCleanerUtil.getParallelSiteCleaner(
				companyId, siteNames);
		}
		else {
			siteCleaner = SiteCleanerUtil.getSequentialSiteCleaner(
				companyId, siteNames);
		}

		actionRequest.setAttribute("siteCleaner", siteCleaner);
	}

	@ProcessAction(name="removeOrphanRecords")
	public void removeOrphanRecords(
			ActionRequest actionRequest, ActionResponse actionResponse) 
		throws IOException, PortletException {

		CorruptedDataCleanerUtil.clean();
	}

	protected void include(
			String path, RenderRequest renderRequest,
			RenderResponse renderResponse)
		throws IOException, PortletException {

		PortletRequestDispatcher portletRequestDispatcher =
			getPortletContext().getRequestDispatcher(path);

		if (portletRequestDispatcher == null) {
			_log.error(path + " is not a valid include");
		}
		else {
			portletRequestDispatcher.include(renderRequest, renderResponse);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DbCleanupPortlet.class);

	private static final String _LINE_SEPARATOR_REGEX = "[\\r\\n]+";

	private String resultTemplate;
	private String viewTemplate;

}