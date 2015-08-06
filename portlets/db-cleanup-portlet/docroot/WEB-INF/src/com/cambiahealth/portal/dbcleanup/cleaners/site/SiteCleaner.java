package com.cambiahealth.portal.dbcleanup.cleaners.site;

import com.liferay.portal.model.Group;

import java.util.List;
import java.util.concurrent.Callable;
public interface SiteCleaner extends Callable<List<Group>> {

	boolean hasSitesToRemove();

	boolean isRunning();

	boolean isTerminated();

}