package com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.impl;

import static com.cambiahealth.portal.dbcleanup.DbCleanupConstants.SITE_CUSTOM_FIELD_MIGRATION_ENABLED;
import static com.cambiahealth.portal.dbcleanup.DbCleanupConstants.SITE_CUSTOM_FIELD_MIGRATION_GROUP_IDS;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldMigration;
import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldMigrationException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.GroupConstants;
public class CustomFieldMigrationUtil {

	public static void migrate(long companyId, String[] customFields) {
		if (!SITE_CUSTOM_FIELD_MIGRATION_ENABLED) {
			return;
		}

		Set<CustomFieldMigration> migrations =
			new HashSet<CustomFieldMigration>(customFields.length);

		CustomFieldHelper customFieldHelper = new CustomFieldHelper(companyId);

		for (String customField : customFields) {
			migrations.add(newCustomFieldMigration(
				companyId, customField, customFieldHelper));
		}

		// In case of re-execution after failure
		filterMigrations(migrations);

		if (migrations.isEmpty()) {
			return;
		}

		try {
			for (CustomFieldMigration migration : migrations) {
				run(migration);
			}
		}
		catch (CustomFieldMigrationException cfme) {
			_log.error(cfme);
		}
		finally {
			migrations.clear();
		}
	}

	private static void filterMigrations(Set<CustomFieldMigration> migrations) {
		Iterator<CustomFieldMigration> migrationIterator =
			migrations.iterator();

		while (migrationIterator.hasNext()) {
			CustomFieldMigration migration = migrationIterator.next();

			if (!migration.isNeeded()) {
				migrationIterator.remove();
			}
		}
	}

	private static long[] getSiteGroupIdsToMigrate() {
		return StringUtil.split(
			SITE_CUSTOM_FIELD_MIGRATION_GROUP_IDS,
			GroupConstants.DEFAULT_LIVE_GROUP_ID);
	}

	private static CustomFieldMigrationImpl newCustomFieldMigration(
		long companyId, String customField, 
		CustomFieldHelper customFieldHelper) {

		return new CustomFieldMigrationImpl(
			companyId, customField, customFieldHelper);
	}

	private static void run(CustomFieldMigration migration) {
		for (long groupId : getSiteGroupIdsToMigrate()) {
			migration.setGroupId(groupId);
			migration.run();
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
			CustomFieldMigrationUtil.class);

}