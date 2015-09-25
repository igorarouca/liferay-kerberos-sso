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
	public SiteCleaner newSiteCleaner(long companyId, List<String> siteNames) {

		if (DbCleanupConstants.PARALLEL_EXECUTION_ENABLED) {
			_log.debug(">>> Creating parallel site cleaner");

			return new ParallelSiteCleaner(companyId, siteNames);
		}
		else {
			_log.debug(">>> Creating sequential site cleaner");

			return new SequentialSiteCleaner(companyId, siteNames);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SiteCleanerFactoryImpl.class);

}