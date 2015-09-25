package com.cambiahealth.portal.dbcleanup.cleaner.p13n.portletpreferences;

import com.cambiahealth.portal.dbcleanup.util.ArticleIdUuidConverter;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.PortletPreferences;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
class PortletPreferencesMigrationImpl implements Runnable {

	@Override
	public void run() {
		try {
			javax.portlet.PortletPreferences javaxPortletPreferences =
				convertToJavaxPortletPreferences(_portletPreferences);

			String[] groupPipeArticleIdStrings =
				javaxPortletPreferences.getValues(_ARTICLE_ENTRIES_KEY, null);

			if (groupPipeArticleIdStrings == null) {
				return;
			}

			String[] groupPipeUuidStrings = migrate(groupPipeArticleIdStrings);

			if (groupPipeUuidStrings.length > 0) {
				javaxPortletPreferences.setValues(
					_ARTICLE_ENTRIES_KEY, groupPipeUuidStrings);

				javaxPortletPreferences.store();
			}
		}
		catch (Exception e) {
			_log.error(">>> Error migrating P13N portlet preferences with ID " +
				_portletPreferences.getPortletPreferencesId(), e);
		}
	}

	PortletPreferencesMigrationImpl(
		long companyId, PortletPreferences portletPreferences) {

		_companyId = companyId;
		_portletPreferences = portletPreferences;
	}

	String[] migrate(String[] groupPipeArticleIdStrings)
		throws PortalException, SystemException {

		String[] groupPipeUuidStrings =
			new String[groupPipeArticleIdStrings.length];

		for (int i = 0; i < groupPipeArticleIdStrings.length; ++i) {
			groupPipeUuidStrings[i] = ArticleIdUuidConverter.convertPipedString(
				_companyId, groupPipeArticleIdStrings[i]);
		}

		return groupPipeUuidStrings;
	}

	private javax.portlet.PortletPreferences convertToJavaxPortletPreferences(
		PortletPreferences preferences) throws SystemException {

		try {
			return PortletPreferencesLocalServiceUtil.getPreferences(
				_companyId, preferences.getOwnerId(),
				preferences.getOwnerType(), preferences.getPlid(),
				preferences.getPortletId());
		}
	catch (SystemException se) {
			_log.error(
				">>> Error converting Liferay PortletPreferences with ID " +
					preferences.getPortletPreferencesId() +
						" to javax.portlet.PortletPreferences");

			throw se;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		PortletPreferencesMigrationImpl.class);

	private static final String _ARTICLE_ENTRIES_KEY = "articleEntries";

	private long _companyId;
	private PortletPreferences _portletPreferences;

}