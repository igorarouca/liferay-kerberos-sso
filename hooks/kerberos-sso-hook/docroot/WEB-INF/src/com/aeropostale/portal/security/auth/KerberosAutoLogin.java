package com.aeropostale.portal.security.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.security.auth.AutoLogin;
import com.liferay.portal.security.auth.AutoLoginException;

public class KerberosAutoLogin implements AutoLogin {

	@Override
	public String[] login(
		HttpServletRequest request, HttpServletResponse response) throws AutoLoginException {

		return null;
	}

}
