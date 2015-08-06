package com.cambiahealth.portal.cleanup.util;

import java.util.List;
public interface SiteRemoverFactory {

	public SiteRemover getParallelSiteRemover(
		long companyId, List<String> siteNames);

	public SiteRemover getSequentialSiteRemover(
		long companyId, List<String> siteNames);

}