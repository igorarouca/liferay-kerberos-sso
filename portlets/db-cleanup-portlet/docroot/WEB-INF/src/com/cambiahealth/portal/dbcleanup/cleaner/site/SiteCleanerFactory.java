package com.cambiahealth.portal.dbcleanup.cleaner.site;

import java.util.List;
public interface SiteCleanerFactory {

	public SiteCleaner getParallelSiteCleaner(
		long companyId, List<String> siteNames);

	public SiteCleaner getSequentialSiteCleaner(
		long companyId, List<String> siteNames);

}