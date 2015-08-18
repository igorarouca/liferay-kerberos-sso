package com.cambiahealth.portal.dbcleanup.cleaners.site.impl;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.Group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
class SequentialSiteCleaner extends AbstractSiteCleaner {

	@Override
	public boolean isRunning() {
		return _currentState.equals(State.RUNNING);
	}

	@Override
	public boolean isTerminated() {
		return _currentState.equals(State.TERMINATED);
	}

	@Override
	protected List<Group> doCall() {
		_log.info(">>> Site removal set to run sequentially");

		_currentState = State.RUNNING;
		List<Group> removedSites = new ArrayList<>();
		Iterator<Group> siteIterator = _sitesToBeRemoved.iterator();

		while (siteIterator.hasNext()) {
			Group site = siteIterator.next();

			Group removedSite = remove(site);

			if (removedSite != null) {
				siteIterator.remove();
				removedSites.add(site);
			}
		}

		return removedSites;
	}

	@Override
	protected void shutdown() {
		super.shutdown();
		_currentState = State.TERMINATED;
	}

	SequentialSiteCleaner(long companyId, List<String> siteNames) {
		super(companyId, siteNames);

		_currentState = State.CREATED;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SequentialSiteCleaner.class);

	private static enum State { CREATED, RUNNING, TERMINATED };

	private State _currentState;

}