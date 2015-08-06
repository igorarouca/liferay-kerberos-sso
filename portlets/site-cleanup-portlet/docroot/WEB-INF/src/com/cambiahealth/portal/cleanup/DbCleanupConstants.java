package com.cambiahealth.portal.cleanup;

import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.Layout;
import com.liferay.portal.util.PortalUtil;
public final class DbCleanupConstants {

	public static final boolean BULK_REINDEX_ENABLED =
		GetterUtil.getBoolean(
			PropsUtil.get(Keys._DB_CLEANUP_BULK_REINDEX_ENABLED),
			Defaults._DB_CLEANUP_BULK_REINDEX_ENABLED);

	public static final Property CLASS_NAME_ID_QUERY_PROPERTY =
		PropertyFactoryUtil.forName("classNameId");

	public static final String DANGLING_GROUP_IDS =
		GetterUtil.getString(
			PropsUtil.get(Keys._DB_CLEANUP_DANGLING_GROUP_IDS),
			Defaults._DB_CLEANUP_DANGLING_GROUP_IDS);

	public static final Property GROUP_ID_QUERY_PROPERTY =
		PropertyFactoryUtil.forName("groupId");

	public static final long LAYOUT_CLASS_NAME_ID = PortalUtil.getClassNameId(
		Layout.class);

	public static final boolean PARALLEL_EXECUTION_ENABLED =
		GetterUtil.getBoolean(
			PropsUtil.get(Keys._DB_CLEANUP_PARALLEL_EXECUTION_ENABLED),
			Defaults._DB_CLEANUP_PARALLEL_EXECUTION_ENABLED);

	public static final String REGENCE_PRODUCER_OR = "regence_producer_or";

	public static final long SITE_REMOVAL_TIMEOUT =
		GetterUtil.getLong(
			PropsUtil.get(Keys._DB_CLEANUP_SITE_REMOVAL_TIMEOUT),
			Defaults._DB_CLEANUP_SITE_REMOVAL_TIMEOUT);

	public static final boolean THREAD_POOL_AUTOSIZING_ENABLED =
		GetterUtil.getBoolean(
			PropsUtil.get(Keys._DB_CLEANUP_THREAD_POOL_AUTOSIZING_ENABLED),
			Defaults._DB_CLEANUP_THREAD_POOL_AUTOSIZING_ENABLED);

	public static final int THREAD_POOL_SIZE =
		GetterUtil.getInteger(
			PropsUtil.get(Keys._DB_CLEANUP_THREAD_POOL_SIZE),
			Defaults._DB_CLEANUP_THREAD_POOL_SIZE);

	private final class Defaults {
		private static final boolean _DB_CLEANUP_BULK_REINDEX_ENABLED = false;

		private static final String _DB_CLEANUP_DANGLING_GROUP_IDS =
			StringPool.BLANK;

		private static final boolean _DB_CLEANUP_PARALLEL_EXECUTION_ENABLED =
			false;

		// Time unit is 'minutes'
		private static final long _DB_CLEANUP_SITE_REMOVAL_TIMEOUT = 45;

		private static final boolean _DB_CLEANUP_THREAD_POOL_AUTOSIZING_ENABLED
			= false;

		private static final int _DB_CLEANUP_THREAD_POOL_SIZE = 16;

		private Defaults() {}
	}

	private final class Keys {
		private static final String _DB_CLEANUP_BULK_REINDEX_ENABLED =
			"db.cleanup.bulk.reindex.enabled";

		private static final String _DB_CLEANUP_DANGLING_GROUP_IDS =
			"db.cleanup.dangling.group.ids";

		private static final String _DB_CLEANUP_PARALLEL_EXECUTION_ENABLED =
			"db.cleanup.parallel.execution.enabled";

		private static final String _DB_CLEANUP_SITE_REMOVAL_TIMEOUT =
			"db.cleanup.site.removal.timeout";

		private static final String _DB_CLEANUP_THREAD_POOL_AUTOSIZING_ENABLED =
			"db.cleanup.thread.pool.autosizing.enabled";

		private static final String _DB_CLEANUP_THREAD_POOL_SIZE =
			"db.cleanup.site.removal.thread.pool.size";

		private Keys() {}
	}

}