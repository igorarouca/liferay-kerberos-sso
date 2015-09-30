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
public class ArticleIdResourceUuidConverter {

	public static String convertCommaSeparatedList(
			long companyId, String groupPipeArticleIdList)
		throws PortalException, SystemException {

		StringBuilder groupPipeResourceUuidList = new StringBuilder();

		String[] groupPipeArticleIdStrings = StringUtil.split(
			groupPipeArticleIdList);

		for (String groupPipeArticleId : groupPipeArticleIdStrings) {
			groupPipeResourceUuidList
				.append(convertPipedString(companyId, groupPipeArticleId))
				.append(StringPool.COMMA);
		}

		// Remove extra commna at the end
		groupPipeResourceUuidList.setLength(
			groupPipeResourceUuidList.length() - 1);

		return groupPipeResourceUuidList.toString();
	}

	public static String convertPipedString(
			long companyId, String groupPipeArticleId)
		throws PortalException, SystemException {

		String[] groupArticleIdPair = StringUtil.split(
			groupPipeArticleId, StringPool.PIPE);

		long groupId = getGroupId(companyId, groupArticleIdPair[0]);
		String resourceUUID = getArticleResourceUuid(
			groupId, groupArticleIdPair[1]);

		return groupArticleIdPair[0] + StringPool.PIPE + resourceUUID;
	}

	private static String getArticleResourceUuid(long groupId, String articleId)
		throws PortalException, SystemException {

		try {
			JournalArticle article = JournalArticleLocalServiceUtil.getArticle(
				groupId, articleId);

			return article.getArticleResourceUuid();
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

		try {
			if (groupString.equals(_GLOBAL)) {
				return CompanyLocalServiceUtil.getCompany(
					companyId).getGroup().getGroupId();
			}
			else {
				return GroupLocalServiceUtil.getFriendlyURLGroup(
					companyId, groupString).getGroupId();
			}
		}
		catch (PortalException | SystemException e) {
			_log.error(">>> Error retrieving group ID for " + groupString);
			throw e;
		}
	}

	private ArticleIdResourceUuidConverter() {
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ArticleIdResourceUuidConverter.class);

	private static final String _GLOBAL = "global";

}