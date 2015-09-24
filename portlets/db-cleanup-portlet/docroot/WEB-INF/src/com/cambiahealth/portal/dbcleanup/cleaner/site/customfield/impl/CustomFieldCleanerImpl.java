package com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.impl;

import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldMigration;
import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldMigrationException;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.GroupConstants;

import java.util.Iterator;
import java.util.Set;

import static com.cambiahealth.portal.dbcleanup.DbCleanupConstants.SITE_CUSTOM_FIELD_MIGRATION_ENABLED;
import static com.cambiahealth.portal.dbcleanup.DbCleanupConstants.SITE_CUSTOM_FIELD_MIGRATION_GROUP_IDS;
class CustomFieldCleanerImpl implements Runnable {

	@Override
	public void run() {
		if (!SITE_CUSTOM_FIELD_MIGRATION_ENABLED) {
			return;
		}

		if (hasMigrationsToRun()) {
			return;
		}

		try {
			for (CustomFieldMigration migration : _migrations) {
				run(migration);
			}
		}
		catch (CustomFieldMigrationException cfme) {
			_log.error(cfme);
		}
		finally {
			_migrations.clear();
		}
	}

	CustomFieldCleanerImpl(Set<CustomFieldMigration> migrations) {
		_migrations = migrations;
	}

	private long[] getSiteGroupIdsToMigrate() {
		return StringUtil.split(
			PropsUtil.get(SITE_CUSTOM_FIELD_MIGRATION_GROUP_IDS),
			GroupConstants.DEFAULT_LIVE_GROUP_ID);
	}

	private boolean hasMigrationsToRun() {
		boolean hasMigrationsToRun = false;

		Iterator<CustomFieldMigration> migrationIterator =
			_migrations.iterator();

		while (migrationIterator.hasNext()) {
			CustomFieldMigration migration = migrationIterator.next();

			if (migration.isNeeded()) {
				hasMigrationsToRun = true;
			}
			else {
				migrationIterator.remove();
			}
		}

		return hasMigrationsToRun;
	}

	private void run(CustomFieldMigration migration) {
		for (long groupId : getSiteGroupIdsToMigrate()) {
			migration.setGroupId(groupId);
			migration.run();
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CustomFieldCleanerImpl.class);

	private Set<CustomFieldMigration> _migrations;

}