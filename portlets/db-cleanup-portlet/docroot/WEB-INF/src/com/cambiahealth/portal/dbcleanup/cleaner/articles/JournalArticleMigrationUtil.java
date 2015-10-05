package com.cambiahealth.portal.dbcleanup.cleaner.articles;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.model.JournalStructure;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portlet.journal.service.JournalStructureLocalServiceUtil;
import com.liferay.portlet.journal.service.JournalTemplateLocalServiceUtil;

import java.util.List;
public class JournalArticleMigrationUtil {

	public static void migrateJournalArticles(long companyId) {
		try {
			List<JournalStructure> structures = getJournalStructuresToMigrate();

			if ((structures == null) || structures.isEmpty()) {
				_log.info(">>> No journal articles to migrate");
				return;
			}

			long groupId = getGroupId(companyId);

			for (JournalStructure structure : structures) {
				List<JournalArticle> articles = getArticlesWith(
					groupId, structure.getStructureId());

				for (JournalArticle article : articles) {
					migrate(article);
				}
			}
		}
		catch (Exception e) {
			throw new JournalArticleMigrationException(e);
		}
	}

	protected static void migrate(JournalArticle article) {
		Runnable migration = new JournalArticleMigrationImpl(article);

		try {
			migration.run();
		}
		catch (JournalArticleMigrationException jame) {
			_log.error(jame);
		}
	}

	private static List<JournalArticle> getArticlesWith(
		long groupId, String structureId) {

		try {
			return JournalArticleLocalServiceUtil.getStructureArticles(
				groupId, structureId);
		}
		catch (SystemException e) {
			_log.error(
				">>> Error retrieving articles for structure: " + structureId);

			throw new JournalArticleMigrationException(e);
		}
	}

	private static long getGroupId(long companyId)
		throws PortalException, SystemException {

		try {
			Company company = CompanyLocalServiceUtil.getCompanyById(companyId);
			return company.getGroup().getGroupId();
		}
		catch (PortalException | SystemException e) {
			_log.error(">>> Error retrieving Global groupId");

			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	private static List<JournalStructure> getJournalStructuresToMigrate()
		throws SystemException {

		DynamicQuery query = JournalTemplateLocalServiceUtil.dynamicQuery();
		query.add(PropertyFactoryUtil.forName(
			"name").like(_GLOBAL_CONTENT_WITH_LOCAL_SIDEBAR));

		try {
			return JournalStructureLocalServiceUtil.dynamicQuery(query);
		}
		catch (SystemException se) {
			_log.error(
				">>> Error retrieving journal structures with name: " +
					_GLOBAL_CONTENT_WITH_LOCAL_SIDEBAR);

			throw se;
		}
	}

	private JournalArticleMigrationUtil() {
	}

	private static final Log _log = LogFactoryUtil.getLog(
		JournalArticleMigrationUtil.class);

	private static final String _GLOBAL_CONTENT_WITH_LOCAL_SIDEBAR =
		"%Global Content with Local Sidebar%";

}