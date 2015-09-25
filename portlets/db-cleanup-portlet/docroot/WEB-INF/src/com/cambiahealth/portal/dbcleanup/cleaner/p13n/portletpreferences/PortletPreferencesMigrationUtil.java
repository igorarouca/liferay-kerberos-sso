package com.cambiahealth.portal.dbcleanup.cleaner.p13n.portletpreferences;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.PortletPreferences;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;

import java.util.List;
public class PortletPreferencesMigrationUtil {

	@SuppressWarnings("unchecked")
	public static void migratePortletPreferences(long companyId) {
		DynamicQuery query = PortletPreferencesLocalServiceUtil.dynamicQuery();

		query.add(_PORTLET_ID_QUERY_PROPERTY.like(_P13N_PORTLET_ID));
		query.add(
			_PREFERENCES_QUERY_PROPERTY.like(_ARTICLE_ENTRIES_PREFERENCES_KEY));

		List<PortletPreferences> portletPreferencesList = null;
		try {
			portletPreferencesList =
				PortletPreferencesLocalServiceUtil.dynamicQuery(query);

		}
		catch (SystemException se) {
			_log.error(">>> Error retrieving for P13N portlet preferences", se);
		}

		if ((portletPreferencesList == null)
			|| portletPreferencesList.isEmpty()) {

			_log.info(">>> No portlet preferences to migrate");

			return;
		}

		_log.info(">>> Started portlet-preferences migrations");

		for (PortletPreferences portletPreferences : portletPreferencesList) {
			migrate(companyId, portletPreferences);
		}

		_log.info(">>> Finished portlet-preferences migrations");
	}

	protected static void migrate(
		long companyId, PortletPreferences portletPreferences) {

		PortletPreferencesMigrationImpl migration =
			new PortletPreferencesMigrationImpl(companyId, portletPreferences);

		try {
			migration.run();
		}
		catch (Exception e) {
			_log.error(e);
		}
	}

	private PortletPreferencesMigrationUtil() {
	}

	private static final Log _log = LogFactoryUtil.getLog(
		PortletPreferencesMigrationUtil.class);

	private static final String _ARTICLE_ENTRIES_PREFERENCES_KEY =
		"%articleEntries%";

	private static final String _P13N_PORTLET_ID =
		"contentwebdisplay_WAR_contentwebdisplayportlet_%";

	private static final Property _PORTLET_ID_QUERY_PROPERTY =
		PropertyFactoryUtil.forName("portletId");

	private static final Property _PREFERENCES_QUERY_PROPERTY =
		PropertyFactoryUtil.forName("preferences");

}