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

package com.liferay.portal.security.auth;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.util.KerberosPropsValues;
import com.liferay.portal.security.ldap.PortalLDAPImporterUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
public class KerberosAutoLogin implements AutoLogin {

	@Override
	public String[] login(
			HttpServletRequest request, HttpServletResponse response)
		throws AutoLoginException {

		try {
			if (!KerberosPropsValues.KERBEROS_AUTH_ENABLED) {
				_log.debug(">>> Kerberos authentication is disabled");

				return null;
			}

			String userPrincipalName = request.getRemoteUser();

			if (Validator.isNull(userPrincipalName)) {
				_log.debug(">>> Kerberos UPN is null");

				return null;
			}

			int slashIndex = userPrincipalName.lastIndexOf(
				StringPool.BACK_SLASH);
			String userName = userPrincipalName.substring(++slashIndex);

			Company company = PortalUtil.getCompany(request);
			long companyId = company.getCompanyId();
			User user = null;

			if (KerberosPropsValues.KERBEROS_IMPORT_FROM_LDAP) {
				_log.debug(">>> User import from LDAP is enabled");

				try {
					user = PortalLDAPImporterUtil.importLDAPUser(
						companyId, StringPool.BLANK, userName);
				}
				catch (Exception se) {
					_log.error(">>> Error importing user from LDAP", se);
				}
			}
			else {
				_log.debug(">>> User import from LDAP is disabled");
			}

			if (user == null) {
				_log.debug(">>> Fetching user by screenName from database");

				user = UserLocalServiceUtil.fetchUserByScreenName(
					companyId, userPrincipalName);
			}

			if (user == null) {
				_log.warn(
					">>> User '" + userName + "' not found in the Liferay" +
						"database. Please verify why user was not imported " +
							"from Active Directory");

				return null;
			}

			String[] credentials = new String[3];

			credentials[0] = String.valueOf(user.getUserId());
			credentials[1] = user.getPassword();
			credentials[2] = StringPool.TRUE;

			return credentials;
		}
		catch (Exception e) {
			_log.error(">>> Error executing Kerberos auto login", e);

			return null;
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	private static Log _log = LogFactoryUtil.getLog(KerberosAutoLogin.class);

}