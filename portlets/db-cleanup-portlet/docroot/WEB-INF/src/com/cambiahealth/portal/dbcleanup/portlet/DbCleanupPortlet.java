package com.cambiahealth.portal.dbcleanup.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.ProcessAction;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.cambiahealth.portal.dbcleanup.DbCleanupConstants;
import com.cambiahealth.portal.dbcleanup.cleaner.CorruptedDataCleanerUtil;
import com.cambiahealth.portal.dbcleanup.cleaner.SiteCleanerUtil;
import com.cambiahealth.portal.dbcleanup.cleaner.site.SiteCleaner;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.util.PortalUtil;

/**
 * Portlet implementation class SiteCleanupPortlet
 */
public class DbCleanupPortlet extends GenericPortlet {

	@ProcessAction(name="cleanSites")
	public void cleanSites(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {

		_log.debug(">>> Clean sites action was triggered");

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
			_log.debug(">>> Creating parallel site cleaner");

			siteCleaner = SiteCleanerUtil.getParallelSiteCleaner(
				companyId, siteNames);
		}
		else {
			_log.debug(">>> Creating sequential site cleaner");

			siteCleaner = SiteCleanerUtil.getSequentialSiteCleaner(
				companyId, siteNames);
		}

		PortletPreferences preferences = actionRequest.getPreferences();

		boolean cleaningSites = GetterUtil.getBoolean(preferences.getValue(
			_CLEANING_SITES_FLAG, null), false);

		if (cleaningSites) {
			return;
		}

		try {
			preferences.setValue(_CLEANING_SITES_FLAG, StringPool.TRUE);
			preferences.store();

			List<Group> removedSites = siteCleaner.call();

			actionRequest.setAttribute("removedSites", removedSites);
		}
		catch (Exception e) {
			_log.error(">>> Error cleaning sites", e);
			SessionErrors.add(actionRequest, "error-cleaning-sites");
		}
		finally {
			preferences.reset(_CLEANING_SITES_FLAG);;
			preferences.store();
		}
	}

	@SuppressWarnings("unchecked")
	public void doView(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		List<Group> removedSites =
			(List<Group>)renderRequest.getAttribute("removedSites");

		if (removedSites == null) {
			include(viewTemplate, renderRequest, renderResponse);
			return;
		}

		if (removedSites.isEmpty()) {
			SessionErrors.add(renderRequest, "no-sites-to-remove");
			include(viewTemplate, renderRequest, renderResponse);
			return;
		}

		include(resultTemplate, renderRequest, renderResponse);
	}

	public void init() {
		resultTemplate = getInitParameter("result-template");
		viewTemplate = getInitParameter("view-template");
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

	private static final String _CLEANING_SITES_FLAG = "cleaningSites";

	private static final String _LINE_SEPARATOR_REGEX = "[\\r\\n]+";

	private String resultTemplate;
	private String viewTemplate;

}