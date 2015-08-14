package com.cambiahealth.portal.dbcleanup.cleaners.site.impl;

import static com.cambiahealth.portal.dbcleanup.DbCleanupConstants.BULK_REINDEX_ENABLED;
import static com.cambiahealth.portal.dbcleanup.DbCleanupConstants.PATCH_CAMBIA_129_INSTALLED;
import static com.cambiahealth.portal.dbcleanup.DbCleanupConstants.REGENCE_PRODUCER_OR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.cambiahealth.portal.dbcleanup.DbCleanupConstants;
import com.cambiahealth.portal.dbcleanup.cleaners.CorruptedDataCleanerUtil;
import com.cambiahealth.portal.dbcleanup.cleaners.site.SiteCleaner;
import com.cambiahealth.portal.dbcleanup.service.CorruptedLayoutLocalServiceUtil;
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
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Layout;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.LayoutLocalServiceUtil;
public abstract class AbstractSiteCleaner implements SiteCleaner {

	@Override
	public List<Group> call() {
		final Thread currentThread = Thread.currentThread();
		final String oldThreadName = currentThread.getName();
		currentThread.setName("SiteCleaner-Call");

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
			_log.info(">>> Removed " + removedSites.size() + " site(s): " +
				asString(removedSites) + " in " + duration + " min");

			if (hasSitesToRemove()) {
				_log.info("There is (are) " + _sitesToBeRemoved.size() +
					" site(s) that could not be removed: " +
						asString(_sitesToBeRemoved));
			}
		}

		cleanOrphanData(removedSites);

		return removedSites;
	}

	@Override
	public boolean hasSitesToRemove() {
		return !_sitesToBeRemoved.isEmpty();
	}

	protected AbstractSiteCleaner(long companyId, List<String> siteNames) {
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

	protected void cleanOrphanData(List<Group> removedSites) {
		String groupIdCommaSeparatedList =
			DbCleanupConstants.DANGLING_GROUP_IDS;

		if (groupIdCommaSeparatedList.isEmpty()) {
			_log.warn(">>> Property 'db.cleanup.dangling.group.ids' is empty");

			if (removedSites.isEmpty()) {
				return;
			}
		}

		long[] danglingGroupIds = StringUtil.split(
			groupIdCommaSeparatedList, 0l);

		_log.info(
			">>> 'db.cleanup.dangling.group.ids' = " +
				Arrays.toString(danglingGroupIds));

		long[] groupIds;
		if (PATCH_CAMBIA_129_INSTALLED) {
			groupIds = danglingGroupIds;
		}
		else {
			// Combine dangling groupIds with groupIds from removed sites
			int middleIndex = danglingGroupIds.length;
			groupIds = new long[middleIndex + removedSites.size()];

			for (int i = 0; i < middleIndex; ++i) {
				groupIds[i] = danglingGroupIds[i];
			}

			int j = middleIndex;
			for (Group removedSite : removedSites) {
				groupIds[j] = removedSite.getGroupId();
				++j;
			}
		}

		CorruptedDataCleanerUtil.clean(groupIds);
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

		/* Handles data issue with regence_producer_or site by first
		 * deleting all public pages before deleting the site:
		 *
		 * https://gist.github.com/anonymous/8fc339193bd5b982406b
		 */
		if (site.getName().equals(REGENCE_PRODUCER_OR)) {
			removePublicLayoutsFrom(site);
		}

		try {
			removedSite = GroupLocalServiceUtil.deleteGroup(site);

			if (_log.isInfoEnabled()) {
				_log.info(">>> Removed site: " + asString(site));
			}

		}
		catch (PortalException | SystemException e) {
			_log.error(">>> Error removing site: " + asString(site), e);
		}

		return removedSite;
	}

	protected void removePublicLayoutsFrom(final Group site) {
		List<Layout> layouts = null;
		try {
			layouts = LayoutLocalServiceUtil.getLayouts(
				site.getGroupId(), false);
		}
		catch (SystemException se) {
			_log.error(
				">>> Error retrieving public layouts for: " +
					site.getName(), se);
		}

		if (layouts == null | layouts.isEmpty()) {
			return;
		}

		for (Layout layout : layouts) {
			try {
				CorruptedLayoutLocalServiceUtil.deleteCorruptedLayout(layout);

				if (_log.isInfoEnabled()) {
					_log.info(">>> Deleted layout: " + asString(layout) +
						" from regence_producer_or");
				}
			}

			catch(Exception e) {
				_log.error(">>> Error deleting layout: " +
					asString(layout) + " from regence_producer_or", e);
			}
		}
	}

	protected abstract void shutdown();

	protected void triggerBulkReindex() {
		for (Indexer indexer : IndexerRegistryUtil.getIndexers()) {
			reindex(indexer);
		}
	}

	protected final long _companyId;

	protected final List<Group> _sitesToBeRemoved;

	private static Log _log = LogFactoryUtil.getLog(AbstractSiteCleaner.class);

}