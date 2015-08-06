package com.cambiahealth.portal.cleanup.util;

import com.cambiahealth.portal.cleanup.util.impl.SiteRemoverFactoryImpl;

import java.util.List;
public class SiteRemoverFactoryUtil {

	public static SiteRemover getParallelSiteRemover(
		long companyId, List<String> siteNames) {

		return _singleton.getParallelSiteRemover(companyId, siteNames);
	}

	public static SiteRemover getSequentialSiteRemover(
		long companyId, java.util.List<String> siteNames) {

		return _singleton.getSequentialSiteRemover(companyId, siteNames);
	};

	private static final SiteRemoverFactory _singleton =
		new SiteRemoverFactoryImpl();

}