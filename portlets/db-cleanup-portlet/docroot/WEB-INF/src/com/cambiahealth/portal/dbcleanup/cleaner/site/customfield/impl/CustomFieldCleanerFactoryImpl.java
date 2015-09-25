package com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.impl;

import java.util.HashSet;
import java.util.Set;

import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldCleanerFactory;
import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldHelper;
import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldMigration;
public class CustomFieldCleanerFactoryImpl
	implements CustomFieldCleanerFactory {

	public CustomFieldCleanerFactoryImpl() {
	}

	@Override
	public Runnable newCustomFieldCleaner(
		long companyId, String[] customFieldsToMigrate) {

		_customFieldHelper = new CustomFieldHelperImpl(companyId);

		Set<CustomFieldMigration> migrations =
			new HashSet<CustomFieldMigration>(customFieldsToMigrate.length);

		for (String customFieldToMigrate : customFieldsToMigrate) {
			migrations.add(
				newCustomFieldMigration(companyId, customFieldToMigrate));
		}

		return new CustomFieldCleanerImpl(migrations);
	}

	protected CustomFieldMigrationImpl newCustomFieldMigration(
		long companyId, String customFieldToMigrate) {

		return new CustomFieldMigrationImpl(
			companyId, customFieldToMigrate, _customFieldHelper);
	}

	private CustomFieldHelper _customFieldHelper;

}