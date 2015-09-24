package com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.impl;

import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldCleanerFactory;
import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldHelper;
import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldMigration;

import java.util.HashSet;
import java.util.Set;
public class CustomFieldCleanerFactoryImpl
	implements CustomFieldCleanerFactory {

	public CustomFieldCleanerFactoryImpl() {
	}

	@Override
	public Runnable newCustomFieldCleaner(
		long companyId, String[][] oldFieldNewFieldNamePairs) {

		_customFieldHelper = new CustomFieldHelperImpl(companyId);

		Set<CustomFieldMigration> migrations =
			new HashSet<CustomFieldMigration>(oldFieldNewFieldNamePairs.length);

		for (String[] oldFieldNewFieldPair : oldFieldNewFieldNamePairs) {
			migrations.add(
				newCustomFieldMigration(companyId, oldFieldNewFieldPair));
		}

		return new CustomFieldCleanerImpl(migrations);
	}

	protected CustomFieldMigrationImpl newCustomFieldMigration(
		long companyId, String[] oldFieldNewFieldPair) {

		return new CustomFieldMigrationImpl(
			companyId, oldFieldNewFieldPair[0], oldFieldNewFieldPair[1],
			_customFieldHelper);
	}

	private CustomFieldHelper _customFieldHelper;

}