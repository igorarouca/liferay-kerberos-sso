package com.cambiahealth.portal.cleanup.util.impl;

import com.cambiahealth.portal.cleanup.util.SiteRemover;
import com.cambiahealth.portal.cleanup.util.SiteRemoverFactory;

import java.util.List;
public class SiteRemoverFactoryImpl implements SiteRemoverFactory {

	public SiteRemoverFactoryImpl() {
	}

	@Override
	public SiteRemover getParallelSiteRemover(
		long companyId, List<String> siteNames) {

		return new ParallelSiteRemover(companyId, siteNames);
	}

	@Override
	public SiteRemover getSequentialSiteRemover(
		long companyId, List<String> siteNames) {

		return new SequentialSiteRemover(companyId, siteNames);
	}

}