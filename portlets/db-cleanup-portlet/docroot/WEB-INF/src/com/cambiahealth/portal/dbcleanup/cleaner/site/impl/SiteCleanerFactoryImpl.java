package com.cambiahealth.portal.dbcleanup.cleaner.site.impl;

import com.cambiahealth.portal.dbcleanup.DbCleanupConstants;
import com.cambiahealth.portal.dbcleanup.cleaner.site.SiteCleaner;
import com.cambiahealth.portal.dbcleanup.cleaner.site.SiteCleanerFactory;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.util.List;
public class SiteCleanerFactoryImpl implements SiteCleanerFactory {

	public SiteCleanerFactoryImpl() {
	}

	@Override
	public SiteCleaner newSiteCleaner(
		long companyId, List<String> siteNames, Runnable customFieldCleaner) {

		if (DbCleanupConstants.PARALLEL_EXECUTION_ENABLED) {
			_log.debug(">>> Creating parallel site cleaner");

			return newParallelSiteCleaner(
				companyId, siteNames, customFieldCleaner);
		}
		else {
			_log.debug(">>> Creating sequential site cleaner");

			return newSequentialSiteCleaner(
				companyId, siteNames, customFieldCleaner);
		}
	}

	private SiteCleaner newParallelSiteCleaner(
		long companyId, List<String> siteNames, Runnable customFieldCleaner) {

		return new ParallelSiteCleaner(
			companyId, siteNames, customFieldCleaner);
	}

	private SiteCleaner newSequentialSiteCleaner(
		long companyId, List<String> siteNames, Runnable customFieldCleaner) {

		return new SequentialSiteCleaner(
				companyId, siteNames, customFieldCleaner);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SiteCleanerFactoryImpl.class);

}