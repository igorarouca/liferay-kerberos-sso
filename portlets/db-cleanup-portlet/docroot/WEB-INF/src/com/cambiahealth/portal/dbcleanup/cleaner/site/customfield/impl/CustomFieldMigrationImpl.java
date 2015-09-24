package com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.impl;

import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldHelper;
import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldMigration;
import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldMigrationException;

import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Group;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portlet.journal.NoSuchArticleException;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;

import java.text.MessageFormat;
class CustomFieldMigrationImpl implements CustomFieldMigration {

	@Override
	public boolean isNeeded() {
		return !_customFieldHelper.hasCustomField(_oldCustomFieldName);
	}

	@Override
	public void run() throws CustomFieldMigrationException {
		String oldCustomFieldValue = (String)
			_customFieldHelper.getCustomFieldValue(_oldCustomFieldName);

		String newCustomFieldValue = StringPool.BLANK;

		try {
			if ((oldCustomFieldValue != null)
				&& !oldCustomFieldValue.isEmpty()) {

				String[] groupIdArticleIdPair = StringUtil.split(
					oldCustomFieldValue, StringPool.PIPE);

				long groupId = parse(groupIdArticleIdPair[0]);
				String uuid = getArticleUuid(groupId, groupIdArticleIdPair[1]);

				newCustomFieldValue = groupId + StringPool.PIPE + uuid;
			}

			_customFieldHelper.setCustomFieldValue(
				_newCustomFieldName, newCustomFieldValue);

			removeOldCustomField();
		}

		catch(Exception e) {
			_log.error(MessageFormat.format(
				_CUSTOM_FIELD_MIGRATION_ERROR_MESSAGE, _oldCustomFieldName,
				oldCustomFieldValue, getGroupId()));

			throw new CustomFieldMigrationException(e);
		}
	}

	@Override
	public void setGroupId(long groupId) {
		_customFieldHelper.setGroupId(groupId);
	}

	CustomFieldMigrationImpl(
		long companyId, String oldCustomFieldName, String newCustomFieldName,
		CustomFieldHelper customFieldHelper) {

		_companyId = companyId;

		_oldCustomFieldName = oldCustomFieldName;
		_newCustomFieldName = newCustomFieldName;

		_customFieldHelper = customFieldHelper;
	}

	private String getArticleUuid(long groupId, String articleId)
		throws PortalException, SystemException {

		try {
			JournalArticle article = JournalArticleLocalServiceUtil.getArticle(
				groupId, articleId);

			return article.getUuid();
		}
		catch (NoSuchArticleException nsae) {
			_log.error(MessageFormat.format(
				">>> Error retrieving article with ID {0} for site {1}",
				articleId, groupId));

			throw nsae;
		}
	}

	private long getGroupId() {
		return _customFieldHelper.getGroupId();
	}

	private long parse(String groupIdString)
		throws PortalException, SystemException {

		if (groupIdString.equals(_GLOBAL)) {
			Company company;
				company = CompanyLocalServiceUtil.getCompany(_companyId);

			return company.getGroup().getGroupId();
		}
		else {
			Group group = GroupLocalServiceUtil.getFriendlyURLGroup(
				_companyId, groupIdString);

			return group.getGroupId();
		}
	}

	private void removeOldCustomField() throws NestableException {
		try {
			_customFieldHelper.removeCustomField(_oldCustomFieldName);
		}
		catch (PortalException | SystemException e) {
			_log.error(MessageFormat.format(
				">>> Error removing old custom field {0} for site {1}",
				_oldCustomFieldName, getGroupId()));

			throw e;
		}
	}

	private static final String _CUSTOM_FIELD_MIGRATION_ERROR_MESSAGE =
		">>> Error migrating custom field {0} with value [{1}] for site [{2}]";

	private static final String _GLOBAL = "global";

	private static final Log _log = LogFactoryUtil.getLog(
		CustomFieldMigrationImpl.class);

	private long _companyId;
	private CustomFieldHelper _customFieldHelper;
	private String _newCustomFieldName;
	private String _oldCustomFieldName;

}