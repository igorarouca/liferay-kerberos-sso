package com.cambiahealth.portal.cleanup.util;

import com.liferay.portal.model.Group;

import java.util.List;
import java.util.concurrent.Callable;
public interface SiteRemover extends Callable<List<Group>> {

	boolean hasSitesToRemove();

	boolean isRunning();

	boolean isTerminated();

}