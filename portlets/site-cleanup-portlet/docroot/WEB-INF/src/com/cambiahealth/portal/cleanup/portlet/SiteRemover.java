package com.cambiahealth.portal.cleanup.portlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.liferay.portal.kernel.dao.orm.ORMException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchEngineUtil;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Layout;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portlet.journal.service.JournalContentSearchLocalServiceUtil;
public class SiteRemover implements Callable<List<String>> {

	@Override
	public List<String> call() {
		final Thread currentThread = Thread.currentThread();
		final String oldThreadName = currentThread.getName();
		currentThread.setName("SiteRemoval-Main");

		List<String> removedSites = Collections.emptyList();

		if (!hasSitesToRemove()) {
			return removedSites;
		}

		long startTime = System.currentTimeMillis();
		boolean indexReadOnly = SearchEngineUtil.isIndexReadOnly();

		// Disable indexing before starting deletions
		SearchEngineUtil.setIndexReadOnly(true);
		_log.info(">>> Disabled search indexing");

		try {
			removedSites = doCall();
		}
		finally {
			currentThread.setName(oldThreadName);

			triggerJournalContentSearchRefresh();

			SearchEngineUtil.setIndexReadOnly(indexReadOnly);
			_log.info(">>> Restored indexReadOnly value");

			triggerBulkReindex();

			_threadExecutor.shutdown();
			_log.info(">>> Shutdown thread executor");
		}

		long endTime = System.currentTimeMillis();
		long duration = TimeUnit.MILLISECONDS.toMinutes(endTime - startTime);

		if (_log.isInfoEnabled()) {
			_log.info(">>> Removed " + removedSites.size() + " sites: " +
				removedSites + " in " + duration + " min");

			if (hasSitesToRemove()) {
				_log.info("There is (are) " + _sitesToBeRemoved.size() +
					" site(s) that could not be removed: " + getListOfSites());
			}
		}

		return removedSites;
	}

	public boolean hasSitesToRemove() {
		return !_sitesToBeRemoved.isEmpty();
	}

	public boolean isRunning() {
		return _removeTasks != null;
	}

	public boolean isTerminated() {
		return _threadExecutor.isTerminated();
	}

	protected String asString(Group group) {
		return "{groupId=" + group.getGroupId() +
			", name=" + group.getName() + "}";
	}

	protected String asString(Layout layout) {
		return "{plid=" + layout.getPlid() + ", friendlyURL=" +
				layout.getFriendlyURL() + "}";
	}

	protected List<String> collectRemoveTasksResults() {
		List<String> removedSites = new ArrayList<>();

		do {
			Future<Group> task = _removeTasks.poll();

			Group site = null;
			try {
				site = task.get();
			}
			catch (ExecutionException ignored) {
				// Already logged during execution
			}
			catch (InterruptedException ie) {
				_log.error(ie, ie);
			}

			if (site != null) {
				_sitesToBeRemoved.remove(site);
				removedSites.add(site.getName());
			}
		}
		while (!_removeTasks.isEmpty());

		return removedSites;
	}

	protected void deleteLayoutsFrom(final Group site) throws SystemException {
		List<Layout> layouts = LayoutLocalServiceUtil.getLayouts(
			site.getGroupId(), false);

		if (layouts != null) {
			for (Layout layout : layouts) {
				try {
					LayoutLocalServiceUtil.deleteLayout(layout);

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
	}

	protected List<String> doCall() {
		_removeTasks = new ConcurrentLinkedQueue<Future<Group>>();

		CountDownLatch siteRemovalCounter =
			new CountDownLatch(_sitesToBeRemoved.size());

		for (Group site : _sitesToBeRemoved) {
			_removeTasks.add(_threadExecutor.submit(
				newRemoveTaskFor(site, siteRemovalCounter)));
		}

		long timeout = GetterUtil.getLong(PropsUtil.get(
			_DB_CLEANUP_SITE_REMOVAL_TIMEOUT), _DEFAULT_CLEANUP_TIMEOUT);

		_log.info(">>> Cleanup timeout set to: " + timeout + " min");

		try {
			siteRemovalCounter.await(timeout, TimeUnit.MINUTES);
		}
		catch (InterruptedException ie) {
			_log.error(ie, ie);
		}

		return collectRemoveTasksResults();
	}

	protected String getListOfSites() {
		StringBuilder siteList = new StringBuilder();

		for (Group site : _sitesToBeRemoved) {
			siteList.append(site.getName());
			siteList.append(StringPool.COMMA).append(StringPool.SPACE);
		}

		int length = siteList.length();
		if (length > 0) {
			siteList.setLength(length - 2);
		}

		return siteList.toString();
	}

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

	protected Runnable newJournalSearchContentRefreshTask() {
		return new Runnable() {
			@Override
			public void run() {
				final Thread currentThread = Thread.currentThread();
				final String oldThreadName = currentThread.getName();
				currentThread.setName("JournalContextSearchRefresh");

				try {
					_log.info(">>> Triggered JournalContentSearch refresh");

					JournalContentSearchLocalServiceUtil.checkContentSearches(
						_companyId);

					_log.info(">>> Finished JournalContentSearch refresh");
				}
				catch (PortalException | SystemException e) {
					_log.error(">>> Error refreshing JournalContentSearch", e);
				}
				finally {
					currentThread.setName(oldThreadName);
				}
			}
		};
	}

	protected Runnable newReindexTaskFor(final Indexer indexer) {
		return new Runnable() {

			@Override
			public void run() {
				final Thread currentThread = Thread.currentThread();
				final String oldThreadName = currentThread.getName();
				currentThread.setName(
					"Reindexing-" + indexer.getClass().getSimpleName());

				try {
					indexer.reindex(
						new String[] { String.valueOf(_companyId) });

					_log.info(">>> Finished reindex for: " + 
						indexer.getClass().getSimpleName());
				}
				catch (SearchException se) {
					_log.error(">>> Error executing bulk re-index", se);
				}
				finally {
					currentThread.setName(oldThreadName);
				}
			}
		};
	}

	protected Callable<Group> newRemoveTaskFor(
		final Group site, final CountDownLatch siteRemovalCounter) {

		return new Callable<Group>() {
			@Override
			public Group call() throws SystemException {
				final Thread currentThread = Thread.currentThread();
				final String oldThreadName = currentThread.getName();
				currentThread.setName("SiteRemoval-" + site.getGroupId());

				try {
					return remove(site);
				}
				finally {
					currentThread.setName(oldThreadName);
					siteRemovalCounter.countDown();
				}
			}
		};
	}

	protected Group remove(final Group site) throws SystemException {
		Group removedSite = null;

		/* Handles data issue with regence_producer_or site by first
		 * deleting all public pages before deleting the site:
		 * https://gist.github.com/anonymous/8fc339193bd5b982406b
		 */
		if (site.getName().equals(_REGENCE_PRODUCER_OR)) {
			deleteLayoutsFrom(site);
		}

		try {
			removedSite = GroupLocalServiceUtil.deleteGroup(site);

			if (_log.isInfoEnabled()) {
				_log.info(">>> Removed site: " + asString(site));
			}

		} catch (PortalException | SystemException e) {
			_log.error(">>> Error removing site: " + asString(site), e);
		}

		return removedSite;
	}

	protected void triggerBulkReindex() {
		for (Indexer indexer : IndexerRegistryUtil.getIndexers()) {
			_threadExecutor.submit(newReindexTaskFor(indexer));
		}
	}

	protected void triggerJournalContentSearchRefresh() {
		_threadExecutor.submit(newJournalSearchContentRefreshTask());
	}

	SiteRemover(long companyId, List<String> siteNames) {
		if ((siteNames == null) || siteNames.isEmpty()) {
			throw new IllegalArgumentException("Site list is empty!");
		}

		_companyId = companyId;
		_sitesToBeRemoved = new ConcurrentLinkedQueue<>();
		_threadExecutor = Executors.newCachedThreadPool();

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

	private static final Log _log = LogFactoryUtil.getLog(SiteRemover.class);
	private static final String _DB_CLEANUP_SITE_REMOVAL_TIMEOUT =
		"db.cleanup.site.removal.timeout";
	private static final int _DEFAULT_CLEANUP_TIMEOUT = 45;
	private static final String _REGENCE_PRODUCER_OR = "regence_producer_or";

	private final long _companyId;
	private Queue<Future<Group>> _removeTasks;
	private final Queue<Group> _sitesToBeRemoved;
	private final ExecutorService _threadExecutor;

}