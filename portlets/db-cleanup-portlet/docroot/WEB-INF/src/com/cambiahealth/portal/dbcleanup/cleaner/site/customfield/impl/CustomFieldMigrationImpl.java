package com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.impl;

import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldMigration;
import com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.CustomFieldMigrationException;

import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portlet.journal.NoSuchArticleException;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;

import java.text.MessageFormat;

import java.util.regex.Pattern;
class CustomFieldMigrationImpl implements CustomFieldMigration {

	@Override
	public boolean isNeeded() {
		return _customFieldHelper.hasCustomField(_customFieldName);
	}

	@Override
	public void run() throws CustomFieldMigrationException {
		_log.info(MessageFormat.format(
			">>> Started migration of {0} for site [{1}]", _customFieldName,
			getGroupId()));

		String customFieldValue = (String)
			_customFieldHelper.getCustomFieldValue(_customFieldName);

		String newCustomFieldValue = StringPool.BLANK;

		try {
			if ((customFieldValue != null) && !customFieldValue.isEmpty()) {
				StringBuilder newCustomFieldValueBuilder = new StringBuilder();

				String[] groupPipeArticleIdStrings = StringUtil.split(
					customFieldValue);

				for (String groupPipeArticleId : groupPipeArticleIdStrings) {
					newCustomFieldValueBuilder
						.append(migrate(groupPipeArticleId))
						.append(StringPool.COMMA);
				}

				// Remove extra commna at the end
				newCustomFieldValueBuilder.setLength(
					newCustomFieldValueBuilder.length() - 1);

				newCustomFieldValue = newCustomFieldValueBuilder.toString();
			}

			String newCustomFieldName = getNewCustomFieldName();

			_customFieldHelper.setCustomFieldValue(
				newCustomFieldName, newCustomFieldValue);

			removeOldCustomField();

			_log.info(MessageFormat.format(
				_CUSTOM_FIELD_MIGRATION_SUCCESS_MESSAGE, newCustomFieldName,
				newCustomFieldValue, getGroupId()));
		}

		catch(Exception e) {
			_log.error(MessageFormat.format(
				_CUSTOM_FIELD_MIGRATION_ERROR_MESSAGE, _customFieldName,
				customFieldValue, getGroupId()));

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

	String migrate(String groupPipeArticleId)
		throws PortalException, SystemException {

		String[] groupArticleIdPair = StringUtil.split(
			groupPipeArticleId, StringPool.PIPE);

		long groupId = parse(groupArticleIdPair[0]);
		String uuid = getArticleUuid(groupId, groupArticleIdPair[1]);

		return groupArticleIdPair[0] + StringPool.PIPE + uuid;
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

	private String getNewCustomFieldName() {
		if (_newCustomFieldName == null) {
			_newCustomFieldName = _articleIdPattern.matcher(
				_customFieldName).replaceFirst(_UUID);
		}

		return _newCustomFieldName;
	}

	private long parse(String groupString)
		throws PortalException, SystemException {

		if (groupString.equals(_GLOBAL)) {
			return CompanyLocalServiceUtil.getCompany(
				_companyId).getGroup().getGroupId();
		}
		else {
			return GroupLocalServiceUtil.getFriendlyURLGroup(
				_companyId, groupString).getGroupId();
		}
	}

	private void removeOldCustomField() throws NestableException {
		try {
			_customFieldHelper.removeCustomField(_customFieldName);

			_log.info(MessageFormat.format(
				">>> Removed old custom field {0} for site {1}",
					_customFieldName, getGroupId()));
		}
		catch (PortalException | SystemException e) {
			_log.error(MessageFormat.format(
				">>> Error removing old custom field {0} for site {1}",
				_customFieldName, getGroupId()));

			throw e;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CustomFieldMigrationImpl.class);

	private static final Pattern _articleIdPattern = Pattern.compile(
		"article-id");

	private static final String _CUSTOM_FIELD_MIGRATION_ERROR_MESSAGE =
		">>> Error migrating custom field {0} with value [{1}] for site [{2}]";

	private static final String _CUSTOM_FIELD_MIGRATION_SUCCESS_MESSAGE =
		">>> Created new custom field {0} with value [{1}] for site [{2}]";

	private static final String _GLOBAL = "global";

	private static final String _UUID = "uuid";

	private long _companyId;
	private CustomFieldHelper _customFieldHelper;
	private String _customFieldName;
	private String _newCustomFieldName;

}