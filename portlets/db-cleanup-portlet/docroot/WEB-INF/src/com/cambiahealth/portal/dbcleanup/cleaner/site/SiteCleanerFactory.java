package com.cambiahealth.portal.dbcleanup.cleaner.site;

import java.util.List;
public interface SiteCleanerFactory {

	SiteCleaner newSiteCleaner(long companyId, List<String> siteNames);

}