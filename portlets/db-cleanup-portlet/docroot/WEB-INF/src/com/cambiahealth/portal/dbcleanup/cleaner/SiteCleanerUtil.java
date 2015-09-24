package com.cambiahealth.portal.dbcleanup.cleaner;

import com.cambiahealth.portal.dbcleanup.cleaner.site.SiteCleaner;
import com.cambiahealth.portal.dbcleanup.cleaner.site.SiteCleanerFactory;
import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.impl.CustomFieldCleanerFactoryImpl;
import com.cambiahealth.portal.dbcleanup.cleaner.site.impl.SiteCleanerFactoryImpl;

import java.util.List;
public class SiteCleanerUtil {

	public static SiteCleaner newSiteCleaner(
		long companyId, List<String> siteNames) {

		String[][] customFieldMigrations = new String[][] {
		{ "footer-social-media-article-id", "footer-social-media-uuid" },
		{ "more-information-article-id", "more-information-uuid" },
		{ "primary-navigation-article-id", "primary-navigation-uuid" },
		{ "primary-navigation-top-article-id", "primary-navigation-top-uuid" }};

		Runnable customFieldCleaner = newCustomFieldCleaner(
			companyId, customFieldMigrations);

		return _siteCleanerFactory.newSiteCleaner(
			companyId, siteNames, customFieldCleaner);
	}

	private static Runnable newCustomFieldCleaner(
		long companyId, String[][] customFieldMigrations) {

		return _customFieldCleanerFactory.newCustomFieldCleaner(
			companyId, customFieldMigrations);
	}

	private SiteCleanerUtil() {
	}

	private static final SiteCleanerFactory _siteCleanerFactory =
		new SiteCleanerFactoryImpl();

	private static final CustomFieldCleanerFactoryImpl
		_customFieldCleanerFactory = new CustomFieldCleanerFactoryImpl();

}