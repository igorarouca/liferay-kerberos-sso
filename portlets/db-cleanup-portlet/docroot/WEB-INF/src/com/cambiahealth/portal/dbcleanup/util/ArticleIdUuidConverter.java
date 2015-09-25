package com.cambiahealth.portal.dbcleanup.util;

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
public class ArticleIdUuidConverter {

	public static String convertCommaSeparatedList(
			long companyId, String groupPipeArticleIdList)
		throws PortalException, SystemException {

		String newCustomFieldValue;
		StringBuilder newCustomFieldValueBuilder = new StringBuilder();

		String[] groupPipeArticleIdStrings = StringUtil.split(
			groupPipeArticleIdList);

		for (String groupPipeArticleId : groupPipeArticleIdStrings) {
			newCustomFieldValueBuilder
				.append(convertPipedString(companyId, groupPipeArticleId))
				.append(StringPool.COMMA);
		}

		// Remove extra commna at the end
		newCustomFieldValueBuilder.setLength(
			newCustomFieldValueBuilder.length() - 1);

		newCustomFieldValue = newCustomFieldValueBuilder.toString();
		return newCustomFieldValue;
	}

	public static String convertPipedString(
			long companyId, String groupPipeArticleId)
		throws PortalException, SystemException {

		String[] groupArticleIdPair = StringUtil.split(
			groupPipeArticleId, StringPool.PIPE);

		String groupString = groupArticleIdPair[0];
		String articleId = groupArticleIdPair[1];

		long groupId = getGroupId(companyId, groupString);
		String uuid = getArticleUuid(groupId, articleId);

		return groupString + StringPool.PIPE + uuid;
	}

	private static String getArticleUuid(long groupId, String articleId)
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

	private static long getGroupId(long companyId, String groupString)
		throws PortalException, SystemException {

		if (groupString.equals(_GLOBAL)) {
			return CompanyLocalServiceUtil.getCompany(
				companyId).getGroup().getGroupId();
		}
		else {
			return GroupLocalServiceUtil.getFriendlyURLGroup(
				companyId, groupString).getGroupId();
		}
	}

	private ArticleIdUuidConverter() {
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ArticleIdUuidConverter.class);

	private static final String _GLOBAL = "global";

}