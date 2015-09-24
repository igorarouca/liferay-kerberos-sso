package com.cambiahealth.portal.dbcleanup.cleaner.site.customfield;

public interface CustomFieldMigration extends Runnable {

	boolean isNeeded();

	void setGroupId(long groupId);

}