package com.cambiahealth.portal.cleanup.action;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.SimpleAction;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.LayoutSetBranch;
import com.liferay.portal.service.LayoutSetBranchLocalServiceUtil;
public class DBCleanupAction extends SimpleAction {

	public DBCleanupAction() {
		super();
	}

	public void run(String[] ids) throws ActionException {
		_log.info(
			">>> Started DB cleanup process on portal instance: " + ids[0]);

		String groupIdCommaSeparatedList = GetterUtil.getString(
			PropsUtil.get(_DB_CLEANUP_DANGLING_GROUP_IDS));

		if (groupIdCommaSeparatedList.isEmpty()) {
			_log.warn(">>> Property 'db.cleanup.dangling.group.ids' is empty");
			return;
		}

		_threadExecutor = Executors.newCachedThreadPool();

		long[] danglingGroupIds = StringUtil.split(
			groupIdCommaSeparatedList, 0l);

		_log.info(
				">>> Dangling group IDs: " + Arrays.toString(danglingGroupIds));

		for (long groupId : danglingGroupIds) {
			if (groupId > 0) {
				triggerOrphanRecordsCleanupFor(groupId);
			}
		}

		_threadExecutor.shutdown();

		try {
			_threadExecutor.awaitTermination(
				_DEFAULT_CLEANUP_TIMEOUT, TimeUnit.MINUTES);
		}
		catch (InterruptedException ie) {
			_log.error(ie, ie);
		}
	}

	protected void cleanLayouSetBranches(long groupId) {
		triggerPublicLayouSetBranchesCleanup(groupId);
		triggerPrivateLayouSetBranchesCleanup(groupId);
	}

	protected void triggerLayoutSetBranchCleanup(
		long groupId, boolean privateLayout) {

		try {
			List<LayoutSetBranch> layoutSetBranches =
				LayoutSetBranchLocalServiceUtil.getLayoutSetBranches(
					groupId, privateLayout);

			if ((layoutSetBranches == null) || layoutSetBranches.isEmpty()) {
				_log.info(">>> No layout set branches to delete for group: " +
					groupId);
				return;
			}

			LayoutSetBranchLocalServiceUtil.deleteLayoutSetBranches(
				groupId, privateLayout, true);

			_log.info(">>> Deleted layout set branches for group: " + groupId);
		}
		catch (PortalException | SystemException e) {
			_log.error(">>> Error deleting layout set branch for groupId: " +
				groupId, e);
		}
	}

	protected void triggerOrphanRecordsCleanupFor(final long groupId) {
		_threadExecutor.submit(new Runnable() {
			@Override
			public void run() {
				_log.info(">>> Triggered orphan records cleanup for group: " +
					groupId);

				final Thread currentThread = Thread.currentThread();
				final String oldThreadName = currentThread.getName();
				currentThread.setName("OrphanRecordsCleanup-" + groupId);

				try {
					cleanLayouSetBranches(groupId);
				}
				finally {
					currentThread.setName(oldThreadName);
				}
			}
		});
	}

	protected void triggerPrivateLayouSetBranchesCleanup(long groupId) {
		triggerLayoutSetBranchCleanup(groupId, true);
	}

	protected void triggerPublicLayouSetBranchesCleanup(long groupId) {
		triggerLayoutSetBranchCleanup(groupId, false);
	}

	private static final String _DB_CLEANUP_DANGLING_GROUP_IDS =
		"db.cleanup.dangling.group.ids";

	private static final int _DEFAULT_CLEANUP_TIMEOUT = 30;

	private final static Log _log = LogFactoryUtil.getLog(
		DBCleanupAction.class);

	private ExecutorService _threadExecutor;

}