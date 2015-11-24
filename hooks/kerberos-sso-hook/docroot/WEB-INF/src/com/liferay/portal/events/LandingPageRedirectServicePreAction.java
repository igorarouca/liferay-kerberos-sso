/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.portal.events;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
public class LandingPageRedirectServicePreAction extends Action {

	@Override
	public void run(HttpServletRequest request, HttpServletResponse response)
		throws ActionException {

		try {
			ThemeDisplay themeDisplay =
				(ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
			User user = PortalUtil.getUser(request);
			boolean signedIn = false;

			if (user != null) {
				if (!user.isDefaultUser()) {
					signedIn = true;
				}

				/*
				 * --- Landing page customization starts here ---
				 *
				 * Requirement: Ensure that when a user lands on "/",
				 * it redirects to the default landing page
				 *
				 */
				if (signedIn &&
					themeDisplay.getURLCurrent().equals(StringPool.SLASH)) {

					String landingPath = PrefsPropsUtil.getStringArray(
						themeDisplay.getCompanyId(),
						"default.landing.page.path", ",") [0];

					response.sendRedirect(landingPath);
				}

				/* --- Landing page customization ends here--- */
			}
		} catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug(e);
			}
		}
	}

	private static Log _log = LogFactoryUtil.getLog(
		LandingPageRedirectServicePreAction.class);

}