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

package com.liferay.portal.servlet.filters.autologin;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.ProtectedServletRequest;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.InstancePool;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.license.LicenseManager;
import com.liferay.portal.liveusers.LiveUsers;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.AutoLogin;
import com.liferay.portal.security.pwd.PwdEncryptor;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.servlet.filters.BasePortalFilter;
import com.liferay.portal.util.Portal;
import com.liferay.portal.util.PortalInstances;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PropsValues;
import com.liferay.portal.util.WebKeys;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Brian Wing Shun Chan
 * @author Raymond Augé
 */
public class AutoLoginFilter extends BasePortalFilter {

	public static void registerAutoLogin(AutoLogin autoLogin) {
		_autoLogins.add(autoLogin);
	}

	public static void unregisterAutoLogin(AutoLogin autoLogin) {
		_autoLogins.remove(autoLogin);
	}

	public AutoLoginFilter() {
		for (String autoLoginClassName : PropsValues.AUTO_LOGIN_HOOKS) {
			AutoLogin autoLogin = (AutoLogin)InstancePool.get(
				autoLoginClassName);

			_autoLogins.add(autoLogin);
		}
	}

	protected String getLoginRemoteUser(
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, String[] credentials)
		throws Exception {

		if ((credentials == null) || (credentials.length != 3)) {
			return null;
		}

		String jUsername = credentials[0];
		String jPassword = credentials[1];
		boolean encPassword = GetterUtil.getBoolean(credentials[2]);

		if (Validator.isNull(jUsername) || Validator.isNull(jPassword)) {
			return null;
		}

		try {
			long userId = GetterUtil.getLong(jUsername);

			if (userId > 0) {
				User user = UserLocalServiceUtil.getUserById(userId);

				if (user.isLockout()) {
					return null;
				}
			}
			else {
				return null;
			}
		}
		catch (NoSuchUserException nsue) {
			return null;
		}

		session.setAttribute(_J_USERNAME, jUsername);

		// Not having access to the unencrypted password will not allow you to
		// connect to external resources that require it (mail server)

		if (encPassword) {
			session.setAttribute(_J_PASSWORD, jPassword);
		}
		else {
			session.setAttribute(_J_PASSWORD, PwdEncryptor.encrypt(jPassword));

			if (PropsValues.SESSION_STORE_PASSWORD) {
				session.setAttribute(WebKeys.USER_PASSWORD, jPassword);
			}
		}

		session.setAttribute(_J_REMOTEUSER, jUsername);

		if (PropsValues.PORTAL_JAAS_ENABLE) {
			response.sendRedirect(
				PortalUtil.getPathMain() + "/portal/touch_protected");
		}

		return jUsername;
	}

	@SuppressWarnings("unchecked")
	protected boolean hasReachedConcurrentUserLimit() {
		Map<String, String> licenseProperties =
			LicenseManager.getLicenseProperties(_PRODUCT_ID_PORTAL);

		int maxConcurrentUsersCount = GetterUtil.getInteger(
			licenseProperties.get(_MAX_CONCURRENT_USERS));

		return (maxConcurrentUsersCount > 0) &&
			!PropsValues.LIVE_USERS_ENABLED &&
				(LiveUsers.getUserIdsCount() >= maxConcurrentUsersCount);
	}

	@Override
	protected void processFilter(
			HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain)
		throws Exception {

		HttpSession session = request.getSession();

		String host = PortalUtil.getHost(request);

		if (PortalInstances.isAutoLoginIgnoreHost(host)) {
			if (_log.isDebugEnabled()) {
				_log.debug("Ignore host " + host);
			}

			processFilter(
				AutoLoginFilter.class, request, response, filterChain);

			return;
		}

		String contextPath = PortalUtil.getPathContext();

		String path = request.getRequestURI().toLowerCase();

		if (!contextPath.equals(StringPool.SLASH) &&
			(path.indexOf(contextPath) != -1)) {

			path = path.substring(contextPath.length());
		}

		if (PortalInstances.isAutoLoginIgnorePath(path)) {
			if (_log.isDebugEnabled()) {
				_log.debug("Ignore path " + path);
			}

			processFilter(
				AutoLoginFilter.class, request, response, filterChain);

			return;
		}

		/* --- Customization ---
		 *
		 * AutoLogin classes should be executed in either of these conditions:
		 *
		 * 1) REMOTE_USER is null (default behavior)
		 * 2) Kerberos authentication is being used (customization)
		 *
		 * When using Kerberos, the REMOTE_USER will not be null, as the user
		 * was already authenticated before the request is forwarded to the
		 * portal, but we still want to run the KerberosAutoLogin.
		 *
		 */

		String jUserName = (String)session.getAttribute(_J_USERNAME);

		if ((!PropsValues.AUTH_LOGIN_DISABLED ||
			hasReachedConcurrentUserLimit()) && (jUserName == null)) {

			String remoteUser = request.getRemoteUser();

			for (AutoLogin autoLogin : _autoLogins) {
				/* --- Customization starts here --- */
				boolean kerberosAutoLogin =
					autoLogin.toString().equals(_KERBEROS_AUTO_LOGIN);

				if ((remoteUser != null) && !kerberosAutoLogin) {
					continue;
				}
				/* --- Customization finishes here --- */

				try {
					String[] credentials = autoLogin.login(request, response);

					String redirect = (String)request.getAttribute(
						AutoLogin.AUTO_LOGIN_REDIRECT);

					if (Validator.isNotNull(redirect)) {
						response.sendRedirect(redirect);

						return;
					}

					String loginRemoteUser = getLoginRemoteUser(
						request, response, session, credentials);

					if (loginRemoteUser != null) {
						request = new ProtectedServletRequest(
							request, loginRemoteUser);

						if (PropsValues.PORTAL_JAAS_ENABLE) {
							return;
						}

						if (!PropsValues.AUTH_FORWARD_BY_LAST_PATH) {
							redirect = Portal.PATH_MAIN;
						}
						else {
							redirect = (String)request.getAttribute(
								AutoLogin.AUTO_LOGIN_REDIRECT_AND_CONTINUE);
						}

						if (Validator.isNotNull(redirect)) {
							response.sendRedirect(redirect);

							return;
						}
					}
				}
				catch (Exception e) {
					if (_log.isWarnEnabled()) {
						_log.warn(e, e);
					}

					String currentURL = PortalUtil.getCurrentURL(request);

					if (currentURL.endsWith(_PATH_CHAT_LATEST)) {
						if (_log.isWarnEnabled()) {
							_log.warn(
								"Current URL " + currentURL +
									" generates exception: " + e.getMessage());
						}
					}
					else {
						_log.error(
							"Current URL " + currentURL +
								" generates exception: " + e.getMessage());
					}
				}
			}
		}

		processFilter(AutoLoginFilter.class, request, response, filterChain);
	}

	private static final String _J_PASSWORD = "j_password";

	private static final String _J_REMOTEUSER = "j_remoteuser";

	private static final String _J_USERNAME = "j_username";

	private static final String _KERBEROS_AUTO_LOGIN = "KerberosAutoLogin";

	private static final String _MAX_CONCURRENT_USERS = "maxConcurrentUsers";

	private static final String _PATH_CHAT_LATEST = "/-/chat/latest";

	private static final String _PRODUCT_ID_PORTAL = "Portal";

	private static Log _log = LogFactoryUtil.getLog(AutoLoginFilter.class);

	private static List<AutoLogin> _autoLogins =
		new CopyOnWriteArrayList<AutoLogin>();

}