package com.cambiahealth.portal.cleanup.util.impl;

import com.cambiahealth.portal.cleanup.util.SiteRemover;
import com.cambiahealth.portal.cleanup.util.StagingAdvicesThreadLocalUtil;

import com.liferay.portal.kernel.dao.orm.ORMException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchEngineUtil;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Layout;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.LayoutLocalServiceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.cambiahealth.portal.cleanup.DbCleanupConstants.BULK_REINDEX_ENABLED;
public abstract class AbstractSiteRemover implements SiteRemover {

	@Override
	public List<Group> call() {
		final Thread currentThread = Thread.currentThread();
		final String oldThreadName = currentThread.getName();
		currentThread.setName("SiteRemoval-Call");

		List<Group> removedSites = Collections.emptyList();

		if (!hasSitesToRemove()) {
			return removedSites;
		}

		long startTime = System.currentTimeMillis();

		boolean indexReadOnly = SearchEngineUtil.isIndexReadOnly();
		_log.info(">>> Search index read only: " + indexReadOnly);

		if (BULK_REINDEX_ENABLED) {
			SearchEngineUtil.setIndexReadOnly(true);
			_log.info(">>> Disabled search indexing");
		}

		try {
			removedSites = doCall();
		}
		finally {
			currentThread.setName(oldThreadName);

			if (BULK_REINDEX_ENABLED) {
				SearchEngineUtil.setIndexReadOnly(indexReadOnly);
				_log.info(">>> Restored indexReadOnly value");

				triggerBulkReindex();
			}

			shutdown();
		}

		long endTime = System.currentTimeMillis();
		long duration = TimeUnit.MILLISECONDS.toMinutes(endTime - startTime);

		if (_log.isInfoEnabled()) {
			_log.info(">>> Removed " + removedSites.size() + " sites: " +
				asString(removedSites) + " in " + duration + " min");

			if (hasSitesToRemove()) {
				_log.info("There is (are) " + _sitesToBeRemoved.size() +
					" site(s) that could not be removed: " +
						asString(_sitesToBeRemoved));
			}
		}

		return removedSites;
	}

	@Override
	public boolean hasSitesToRemove() {
		return !_sitesToBeRemoved.isEmpty();
	}

	protected AbstractSiteRemover(long companyId, List<String> siteNames) {
		if ((siteNames == null) || siteNames.isEmpty()) {
			throw new IllegalArgumentException("Site list is empty!");
		}

		_companyId = companyId;
		_sitesToBeRemoved = new ArrayList<>();

		Collections.sort(siteNames);
		for (String name : siteNames) {
			Group site = null;
			try {
				site = getSiteBy(_companyId, name);
			}
			catch (ORMException orme) {
				_log.warn(
					">>> Failed to retrieve site: " + name + ". Retrying...",
					orme);

				site = getSiteBy(_companyId, name);
			}

			if (site == null) {
				_log.warn(">>> Site not found: " + name);
			}
			else {
				_log.info(">>> Loaded site: " + name);
				_sitesToBeRemoved.add(site);
			}
		}

		if (hasSitesToRemove()) {
			_log.info(">>> Loaded " + _sitesToBeRemoved.size() +
				" site(s) to be removed.");
		}
	}

	protected String asString(Group group) {
		return "{groupId=" + group.getGroupId() +
			", name=" + group.getName() + "}";
	}

	protected String asString(Layout layout) {
		return "{plid=" + layout.getPlid() + ", friendlyURL=" +
				layout.getFriendlyURL() + "}";
	}

	protected String asString(List<Group> sites) {
		StringBuilder siteList = new StringBuilder();

		for (Group site : sites) {
			siteList.append(site.getName());
			siteList.append(StringPool.COMMA).append(StringPool.SPACE);
		}

		int length = siteList.length();
		if (length > 0) {
			siteList.setLength(length - 2);
		}

		return siteList.toString();
	}

	protected void deleteLayoutsFrom(final Group site) {
		List<Layout> layouts = null;
		try {
			layouts = LayoutLocalServiceUtil.getLayouts(
				site.getGroupId(), false);
		}
		catch (SystemException se) {
			_log.error(
				">>> Error fetching public layouts for site: " + site.getName(),
				se);
		}

		if (layouts != null) {
			for (Layout layout : layouts) {
				try {
					LayoutLocalServiceUtil.deleteLayout(layout, true, null);

					if (_log.isInfoEnabled()) {
						_log.info(">>> Deleted layout: " + asString(layout) +
							" from " + site.getName());
					}
				}

				catch(Exception e) {
					_log.error(">>> Error deleting layout: " +
						asString(layout) + " from " + site.getName(), e);
				}
			}
		}
	}

	protected abstract List<Group> doCall();

	protected Group getSiteBy(long companyId, String name) {

		Group site = null;
		try {
			site = GroupLocalServiceUtil.fetchGroup(companyId, name);
		}
		catch (SystemException se) {
			_log.error(">>> Error looking up site: " + name, se);
		}

		return site;
	}

	protected void reindex(Indexer indexer) {
		try {
			indexer.reindex(new String[] { String.valueOf(_companyId) });

			_log.info(">>> Finished reindex for: " +
				indexer.getClass().getSimpleName());
		}
		catch (SearchException se) {
			_log.error(">>> Error executing bulk re-index", se);
		}
	}

	protected Group remove(final Group site) {
		Group removedSite = null;

		boolean stagingAdvicesEnabled =
			StagingAdvicesThreadLocalUtil.disableStagingAdvices();

		try {
			removedSite = GroupLocalServiceUtil.deleteGroup(site);

			if (_log.isInfoEnabled()) {
				_log.info(">>> Removed site: " + asString(site));
			}

		}
		catch (PortalException | SystemException e) {
			_log.error(">>> Error removing site: " + asString(site), e);
		}
		finally {
			StagingAdvicesThreadLocalUtil.resetStagingAdvices(
				stagingAdvicesEnabled);
		}

		return removedSite;
	}

	protected abstract void shutdown();

	protected void triggerBulkReindex() {
		for (Indexer indexer : IndexerRegistryUtil.getIndexers()) {
			reindex(indexer);
		}
	}

	protected final long _companyId;

	protected final List<Group> _sitesToBeRemoved;

	private static Log _log = LogFactoryUtil.getLog(AbstractSiteRemover.class);

}