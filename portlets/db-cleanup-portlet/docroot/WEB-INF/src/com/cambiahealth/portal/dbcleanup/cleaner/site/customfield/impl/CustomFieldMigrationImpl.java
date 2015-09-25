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

import java.util.regex.Pattern;
class CustomFieldMigrationImpl implements CustomFieldMigration {

	@Override
	public boolean isNeeded() {
		return _customFieldHelper.hasCustomField(_customFieldName);
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
			_log.error(
				">>> Error retrieving article with ID " + articleId
					+ " for site " + groupId);

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

			if (_log.isInfoEnabled()) {
				_log.info(
					">>> Removed old custom field " + _customFieldName
						+ " for site " + getGroupId());
			}
		}
		catch (PortalException | SystemException e) {
			_log.error(
				">>> Error removing old custom field " + _customFieldName
					+ " for site " + getGroupId());

			throw e;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CustomFieldMigrationImpl.class);

	private static final Pattern _articleIdPattern = Pattern.compile(
		"article-id");

	private static final String _GLOBAL = "global";

	private static final String _UUID = "uuid";

	private long _companyId;
	private CustomFieldHelper _customFieldHelper;
	private String _customFieldName;
	private String _newCustomFieldName;

}