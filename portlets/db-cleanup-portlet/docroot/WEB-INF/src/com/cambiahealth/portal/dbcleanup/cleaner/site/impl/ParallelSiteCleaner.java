package com.cambiahealth.portal.dbcleanup.cleaner.site.impl;

import com.cambiahealth.portal.dbcleanup.cleaner.site.SiteCleaner;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.Group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.cambiahealth.portal.dbcleanup.DbCleanupConstants.SITE_REMOVAL_TIMEOUT;
import static com.cambiahealth.portal.dbcleanup.DbCleanupConstants.THREAD_POOL_AUTOSIZING_ENABLED;
import static com.cambiahealth.portal.dbcleanup.DbCleanupConstants.THREAD_POOL_SIZE;
class ParallelSiteCleaner extends AbstractSiteCleaner implements SiteCleaner {

	@Override
	public boolean isRunning() {
		return _removeTasks != null && !_removeTasks.isEmpty();
	}

	@Override
	public boolean isTerminated() {
		return _threadExecutor.isTerminated();
	}

	protected List<Group> collectRemoveTasksResults() {
		List<Group> removedSites = new ArrayList<>();

		Iterator<Future<Group>> removeTasksIterator = _removeTasks.iterator();

		while (removeTasksIterator.hasNext()) {
			Future<Group> task = removeTasksIterator.next();

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
			finally {
				removeTasksIterator.remove();
			}

			if (site != null) {
				_sitesToBeRemoved.remove(site);
				removedSites.add(site);
			}
		}

		return removedSites;
	}

	@Override
	protected List<Group> doCall() {
		_log.info(">>> Site removal set to run in parallel");

		_removeTasks = new ArrayList<Future<Group>>();

		CountDownLatch siteRemovalCounter =
			new CountDownLatch(_sitesToBeRemoved.size());

		for (Group site : _sitesToBeRemoved) {
			_removeTasks.add(_threadExecutor.submit(
				newRemoveTaskFor(site, siteRemovalCounter)));
		}

		try {
			_log.info(">>> Site Removal timeout set to: " +
				SITE_REMOVAL_TIMEOUT + " min");

			siteRemovalCounter.await(SITE_REMOVAL_TIMEOUT, TimeUnit.MINUTES);
		}
		catch (InterruptedException ie) {
			_log.error(ie, ie);
		}

		return collectRemoveTasksResults();
	}

	protected Runnable newReindexTaskFor(final Indexer indexer) {
		return new Runnable() {

			@Override
			public void run() {
				final Thread currentThread = Thread.currentThread();
				final String oldThreadName = currentThread.getName();
				currentThread.setName(
					"Reindexing-" + getThreadIndex() + StringPool.DASH +
					indexer.getClass().getSimpleName());

				try {
					ParallelSiteCleaner.super.reindex(indexer);
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
			public Group call() {
				final Thread currentThread = Thread.currentThread();
				final String oldThreadName = currentThread.getName();
				currentThread.setName(
					"SiteCleaner-" + getThreadIndex() + StringPool.DASH +
					site.getGroupId());

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

	@Override
	protected void reindex(Indexer indexer) {
		_threadExecutor.submit(newReindexTaskFor(indexer));
	}

	@Override
	protected void shutdown() {
		super.shutdown();
		_threadExecutor.shutdownNow();
		_log.debug(">>> Shutdown thread executor");
	}

	ParallelSiteCleaner(long companyId, List<String> siteNames) {
		super(companyId, siteNames);

		if (THREAD_POOL_AUTOSIZING_ENABLED) {
			_threadExecutor = Executors.newCachedThreadPool();

			_log.info(">>> Parallel removal set to use a cached thread pool");
		}
		else {
			_threadExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

			_log.info(
				">>> Parallel removal set to use a fixed thread pool with " +
					THREAD_POOL_SIZE + " threads");
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ParallelSiteCleaner.class);

	private List<Future<Group>> _removeTasks;
	private final ExecutorService _threadExecutor;

}