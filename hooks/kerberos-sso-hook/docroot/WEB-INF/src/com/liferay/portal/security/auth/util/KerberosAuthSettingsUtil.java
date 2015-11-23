package com.liferay.portal.security.auth.util;

public class KerberosAuthSettingsUtil {

	public static boolean isAuthEnabled() {
		return KerberosPropsValues.KERBEROS_AUTH_ENABLED;
	}

	public static boolean isLDAPImportEnabled() {
		return KerberosPropsValues.KERBEROS_IMPORT_FROM_LDAP;
	}

	private KerberosAuthSettingsUtil() {
	}

}
