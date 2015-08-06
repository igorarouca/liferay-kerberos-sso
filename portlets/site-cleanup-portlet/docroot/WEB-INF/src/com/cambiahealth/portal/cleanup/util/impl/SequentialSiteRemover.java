package com.cambiahealth.portal.cleanup.util.impl;

import com.liferay.portal.model.Group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
class SequentialSiteRemover extends AbstractSiteRemover {

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
		_currentState = State.TERMINATED;
	}

	SequentialSiteRemover(long companyId, List<String> siteNames) {
		super(companyId, siteNames);

		_currentState = State.CREATED;
	}

	private static enum State { CREATED, RUNNING, TERMINATED };

	private State _currentState;

}