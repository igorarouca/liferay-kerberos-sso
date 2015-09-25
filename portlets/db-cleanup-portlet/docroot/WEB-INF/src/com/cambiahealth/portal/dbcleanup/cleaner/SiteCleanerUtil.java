package com.cambiahealth.portal.dbcleanup.cleaner;

import java.util.List;

import com.cambiahealth.portal.dbcleanup.cleaner.site.SiteCleaner;
import com.cambiahealth.portal.dbcleanup.cleaner.site.SiteCleanerFactory;
import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.impl.CustomFieldCleanerFactoryImpl;
import com.cambiahealth.portal.dbcleanup.cleaner.site.impl.SiteCleanerFactoryImpl;
public class SiteCleanerUtil {

	public static SiteCleaner newSiteCleaner(
		long companyId, List<String> siteNames) {

		String[] customFieldsToMigrate = new String[] { 
			"footer-social-media-article-id", "more-information-article-id", 
			"primary-navigation-article-id", "primary-navigation-top-article-id"
		};

		Runnable customFieldCleaner = newCustomFieldCleaner(
			companyId, customFieldsToMigrate);

		return _siteCleanerFactory.newSiteCleaner(
			companyId, siteNames, customFieldCleaner);
	}

	private static Runnable newCustomFieldCleaner(
		long companyId, String[] customFieldsToMigrate) {

		return _customFieldCleanerFactory.newCustomFieldCleaner(
			companyId, customFieldsToMigrate);
	}

	private SiteCleanerUtil() {
	}

	private static final SiteCleanerFactory _siteCleanerFactory =
		new SiteCleanerFactoryImpl();

	private static final CustomFieldCleanerFactoryImpl
		_customFieldCleanerFactory = new CustomFieldCleanerFactoryImpl();

}