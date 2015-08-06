package com.cambiahealth.portal.cleanup.portlet;

import com.cambiahealth.portal.cleanup.DbCleanupConstants;
import com.cambiahealth.portal.cleanup.util.SiteRemover;
import com.cambiahealth.portal.cleanup.util.SiteRemoverFactoryUtil;
import com.cambiahealth.portal.cleanup.util.impl.AbstractSiteRemover;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.util.PortalUtil;

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

/**
 * Portlet implementation class SiteCleanupPortlet
 */
public class SiteCleanupPortlet extends GenericPortlet {

	public void doView(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		AbstractSiteRemover siteRemover =
			(AbstractSiteRemover)renderRequest.getAttribute("siteRemover");

		if (siteRemover == null) {
			include(viewTemplate, renderRequest, renderResponse);
			return;
		}

		if (siteRemover.hasSitesToRemove()) {
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
	public void processAction(
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

		SiteRemover siteRemover = null;

		if (DbCleanupConstants.PARALLEL_EXECUTION_ENABLED) {
			siteRemover = SiteRemoverFactoryUtil.getParallelSiteRemover(
				companyId, siteNames);

			_log.info(">>> Site removal set to run in parallel");
		}
		else {
			siteRemover = SiteRemoverFactoryUtil.getSequentialSiteRemover(
				companyId, siteNames);

			_log.info(">>> Site removal set to run sequentially");
		}

		actionRequest.setAttribute("siteRemover", siteRemover);
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
		SiteCleanupPortlet.class);

	private static final String _LINE_SEPARATOR_REGEX = "[\\r\\n]+";

	private String resultTemplate;
	private String viewTemplate;

}