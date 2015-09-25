package com.cambiahealth.portal.dbcleanup.cleaner;

import com.cambiahealth.portal.dbcleanup.cleaner.site.SiteCleaner;
import com.cambiahealth.portal.dbcleanup.cleaner.site.SiteCleanerFactory;
import com.cambiahealth.portal.dbcleanup.cleaner.site.impl.SiteCleanerFactoryImpl;

import java.util.List;
public class SiteCleanerUtil {

	public static SiteCleaner newSiteCleaner(
		long companyId, List<String> siteNames) {

		return _siteCleanerFactory.newSiteCleaner(companyId, siteNames);
	}

	private SiteCleanerUtil() {
	}

	private static final SiteCleanerFactory _siteCleanerFactory =
		new SiteCleanerFactoryImpl();

}