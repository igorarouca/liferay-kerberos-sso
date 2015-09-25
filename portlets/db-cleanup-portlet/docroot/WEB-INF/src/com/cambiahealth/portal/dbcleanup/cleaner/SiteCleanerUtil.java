package com.cambiahealth.portal.dbcleanup.cleaner;

import java.util.List;

import com.cambiahealth.portal.dbcleanup.cleaner.site.SiteCleaner;
import com.cambiahealth.portal.dbcleanup.cleaner.site.SiteCleanerFactory;
import com.cambiahealth.portal.dbcleanup.cleaner.site.impl.SiteCleanerFactoryImpl;
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