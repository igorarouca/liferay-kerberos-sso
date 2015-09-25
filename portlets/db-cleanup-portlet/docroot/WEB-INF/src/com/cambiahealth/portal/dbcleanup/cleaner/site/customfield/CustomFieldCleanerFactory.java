package com.cambiahealth.portal.dbcleanup.cleaner.site.customfield;

public interface CustomFieldCleanerFactory {

	public abstract Runnable newCustomFieldCleaner(
		long companyId, String[] customFieldsToMigrate);

}