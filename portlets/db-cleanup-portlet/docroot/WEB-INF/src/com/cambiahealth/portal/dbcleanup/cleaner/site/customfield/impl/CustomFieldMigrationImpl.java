package com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.impl;

import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldMigration;
import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldMigrationException;
import com.cambiahealth.portal.dbcleanup.util.ArticleIdResourceUuidConverter;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;

import java.util.regex.Pattern;
class CustomFieldMigrationImpl implements CustomFieldMigration {

	@Override
	public boolean isNeeded() {
		return _customFieldHelper.hasCustomField(_customFieldName);
	}

	@Override
	public void removeOldCustomField() {
		try {
			_customFieldHelper.removeCustomField(_customFieldName);

			if (_log.isInfoEnabled()) {
				_log.info(">>> Removed old custom field " + _customFieldName);
			}
		}
		catch (PortalException | SystemException e) {
			_log.error(
				">>> Error removing old custom field " + _customFieldName, e);
		}
	}

	@Override
	public void run() throws CustomFieldMigrationException {
		String customFieldValue = (String)
			_customFieldHelper.getCustomFieldValue(_customFieldName);

		if (_log.isInfoEnabled()) {
			_log.info(
				">>> Started migration of " + _customFieldName + " with value ["
					+ customFieldValue + "] for site " + getGroupId());
		}

		String newCustomFieldValue = StringPool.BLANK;

		try {
			if ((customFieldValue != null) && !customFieldValue.isEmpty()) {
				newCustomFieldValue =
					ArticleIdResourceUuidConverter.convertCommaSeparatedList(
						_companyId, customFieldValue);
			}

			String newCustomFieldName = getNewCustomFieldName();

			_customFieldHelper.setCustomFieldValue(
				newCustomFieldName, newCustomFieldValue);
		}

		catch(Exception e) {
			_log.error(
				">>> Error migrating custom field " + _customFieldName
					+ " with value [" + customFieldValue + "] for site "
						+ getGroupId());

			throw new CustomFieldMigrationException(e);
		}
	}

	@Override
	public void setGroupId(long groupId) {
		_customFieldHelper.setGroupId(groupId);
	}

	CustomFieldMigrationImpl(
		long companyId, String customFieldToMigrate,
		CustomFieldHelper customFieldHelper) {

		_companyId = companyId;
		_customFieldName = customFieldToMigrate;
		_customFieldHelper = customFieldHelper;
	}

	private long getGroupId() {
		return _customFieldHelper.getGroupId();
	}

	private String getNewCustomFieldName() {
		if (_newCustomFieldName == null) {
			_newCustomFieldName = _articleIdPattern.matcher(
				_customFieldName).replaceFirst(_UUID);
		}

		return _newCustomFieldName;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CustomFieldMigrationImpl.class);

	private static final Pattern _articleIdPattern = Pattern.compile(
		"article-id");

	private static final String _UUID = "uuid";

	private long _companyId;
	private CustomFieldHelper _customFieldHelper;
	private String _customFieldName;
	private String _newCustomFieldName;

}