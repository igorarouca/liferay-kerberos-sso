package com.cambiahealth.portal.cleanup.cleaners;

import com.cambiahealth.portal.cleanup.cleaners.site.SiteCleaner;
import com.cambiahealth.portal.cleanup.cleaners.site.SiteCleanerFactory;
import com.cambiahealth.portal.cleanup.cleaners.site.impl.SiteCleanerFactoryImpl;

import java.util.List;
public class SiteCleanerUtil {

	public static SiteCleaner getParallelSiteCleaner(
		long companyId, List<String> siteNames) {

		return _factory.getParallelSiteCleaner(companyId, siteNames);
	}

	public static SiteCleaner getSequentialSiteCleaner(
		long companyId, java.util.List<String> siteNames) {

		return _factory.getSequentialSiteCleaner(companyId, siteNames);
	};

	private static final SiteCleanerFactory _factory = 
		new SiteCleanerFactoryImpl();

}