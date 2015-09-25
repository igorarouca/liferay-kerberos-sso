package com.cambiahealth.portal.dbcleanup.cleaner.p13n.portletpreferences.impl;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
class PortletPreferencesMigrationImpl implements Runnable {

	@Override
	public void run() {
		DynamicQuery query = PortletPreferencesLocalServiceUtil.dynamicQuery();
		query.add(null);
	}

	PortletPreferencesMigrationImpl() {
	}

}