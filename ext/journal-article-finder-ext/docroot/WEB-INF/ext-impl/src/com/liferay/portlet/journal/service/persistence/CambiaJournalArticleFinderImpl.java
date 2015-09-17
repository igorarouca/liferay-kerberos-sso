package com.liferay.portlet.journal.service.persistence;

import com.liferay.portal.kernel.dao.orm.QueryPos;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.SQLQuery;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.dao.orm.Type;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.CalendarUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.security.permission.InlineSQLHelperUtil;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.model.impl.JournalArticleImpl;
import com.liferay.portlet.journal.service.CambiaJournalArticleFinder;
import com.liferay.util.dao.orm.CustomSQLUtil;

import java.sql.Timestamp;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
public class CambiaJournalArticleFinderImpl extends JournalArticleFinderImpl
	implements CambiaJournalArticleFinder {

	public static final String COUNT_BY_C_G_C_U_A_V_T_D_C_T_S_T_D_S_R =
		JournalArticleFinder.class.getName() +
			".countByC_G_C_U_A_V_T_D_C_T_S_T_D_S_R";

	public static final String FIND_BY_C_G_C_U_A_V_T_D_C_T_S_T_D_S_R =
		JournalArticleFinder.class.getName() +
			".findByC_G_C_U_A_V_T_D_C_T_S_T_D_S_R";

	@Override
	public int filterCountByC_G_C_U_A_V_T_D_C_T_S_T_D_S_R(
			long companyId, long groupId, long classNameId, String uuid,
			String articleId, Double version, String title, String description,
			String content, String type, String structureId, String templateId,
			Date displayDateGT, Date displayDateLT, int status, Date reviewDate,
			boolean andOperator)
		throws SystemException {

		String[] articleIds = CustomSQLUtil.keywords(articleId, false);
		String[] contents = CustomSQLUtil.keywords(content, false);
		String[] descriptions = CustomSQLUtil.keywords(description, false);
		String[] structureIds = CustomSQLUtil.keywords(structureId, false);
		String[] templateIds = CustomSQLUtil.keywords(templateId, false);
		String[] titles = CustomSQLUtil.keywords(title);
		String[] uuids = CustomSQLUtil.keywords(uuid, false);

		return doCountByC_G_C_U_A_V_T_D_C_T_S_T_D_S_R(
			companyId, groupId, classNameId, uuids, articleIds, version, titles,
			descriptions, contents, type, structureIds, templateIds,
			displayDateGT, displayDateLT, status, reviewDate, andOperator,
			andOperator);
	}

	@Override
	public List<JournalArticle> findByC_G_C_U_A_V_T_D_C_T_S_T_D_S_R(
			long companyId, long groupId, long classNameId, String uuid,
			String articleId, Double version, String title, String description,
			String content, String type, String structureId, String templateId,
			Date displayDateGT, Date displayDateLT, int status, Date reviewDate,
			boolean andOperator, int start, int end,
			OrderByComparator orderByComparator)
		throws SystemException {

		String[] uuids = CustomSQLUtil.keywords(uuid, false);
		String[] articleIds = CustomSQLUtil.keywords(articleId, false);
		String[] titles = CustomSQLUtil.keywords(title);
		String[] descriptions = CustomSQLUtil.keywords(description, false);
		String[] contents = CustomSQLUtil.keywords(content, false);
		String[] structureIds = CustomSQLUtil.keywords(structureId, false);
		String[] templateIds = CustomSQLUtil.keywords(templateId, false);

		return doFindByC_G_C_U_A_V_T_D_C_T_S_T_D_S_R(
			companyId, groupId, classNameId, uuids, articleIds, version, titles,
			descriptions, contents, type, structureIds, templateIds,
			displayDateGT, displayDateLT, status, reviewDate, andOperator,
			start, end, orderByComparator, false);
	}

	@SuppressWarnings("unchecked")
	protected int doCountByC_G_C_U_A_V_T_D_C_T_S_T_D_S_R(
			long companyId, long groupId, long classNameId, String[] uuids,
			String[] articleIds, Double version, String[] titles,
			String[] descriptions, String[] contents, String type,
			String[] structureIds, String[] templateIds, Date displayDateGT,
			Date displayDateLT, int status, Date reviewDate,
			boolean andOperator, boolean inlineSQLHelper)
		throws SystemException {

		uuids = CustomSQLUtil.keywords(uuids, false);
		articleIds = CustomSQLUtil.keywords(articleIds, false);
		titles = CustomSQLUtil.keywords(titles);
		descriptions = CustomSQLUtil.keywords(descriptions, false);
		contents = CustomSQLUtil.keywords(contents, false);
		structureIds = CustomSQLUtil.keywords(structureIds, false);
		templateIds = CustomSQLUtil.keywords(templateIds, false);
		Timestamp displayDateGT_TS = CalendarUtil.getTimestamp(displayDateGT);
		Timestamp displayDateLT_TS = CalendarUtil.getTimestamp(displayDateLT);
		Timestamp reviewDate_TS = CalendarUtil.getTimestamp(reviewDate);

		Session session = null;

		try {
			session = openSession();

			String sql = CustomSQLUtil.get(
				COUNT_BY_C_G_C_U_A_V_T_D_C_T_S_T_D_S_R);

			if (groupId <= 0) {
				sql = StringUtil.replace(sql, "(groupId = ?) AND", "");
			}

			sql = CustomSQLUtil.replaceKeywords(
				sql, "uuid_", StringPool.LIKE, false, uuids);

			sql = CustomSQLUtil.replaceKeywords(
				sql, "articleId", StringPool.LIKE, false, articleIds);

			if ((version == null) || (version <= 0)) {
				sql = StringUtil.replace(
					sql, "(version = ?) [$AND_OR_CONNECTOR$]", "");
			}

			sql = CustomSQLUtil.replaceKeywords(
				sql, "lower(title)", StringPool.LIKE, false, titles);
			sql = CustomSQLUtil.replaceKeywords(
				sql, "description", StringPool.LIKE, false, descriptions);
			sql = CustomSQLUtil.replaceKeywords(
				sql, "content", StringPool.LIKE, false, contents);

			if (status == WorkflowConstants.STATUS_ANY) {
				sql = StringUtil.replace(sql, "(status = ?) AND", "");
			}

			if (Validator.isNull(type)) {
				sql = StringUtil.replace(sql, _TYPE_SQL, StringPool.BLANK);
			}

			if (isNullArray(structureIds)) {
				sql = StringUtil.replace(
					sql, _STRUCTURE_ID_SQL, StringPool.BLANK);
			}
			else {
				sql = CustomSQLUtil.replaceKeywords(
					sql, "structureId", StringPool.LIKE, false, structureIds);
			}

			if (isNullArray(templateIds)) {
				sql = StringUtil.replace(
					sql, _TEMPLATE_ID_SQL, StringPool.BLANK);
			}
			else {
				sql = CustomSQLUtil.replaceKeywords(
					sql, "templateId", StringPool.LIKE, false, templateIds);
			}

			sql = CustomSQLUtil.replaceAndOperator(sql, andOperator);

			if ((articleIds != null) &&
				((articleIds.length > 1) ||
				 ((articleIds.length == 1) && (articleIds[0] != null))) &&
				(version == null)) {

				sql = StringUtil.replace(
					sql, "MAX(version) AS tempVersion",
					"version AS tempVersion");
				sql = StringUtil.replace(sql, "[$GROUP_BY_CLAUSE$]", "");
			}
			else {
				sql = StringUtil.replace(
					sql, "[$GROUP_BY_CLAUSE$]", "GROUP BY groupId, articleId");
			}

			if (inlineSQLHelper) {
				sql = InlineSQLHelperUtil.replacePermissionCheck(
					sql, JournalArticle.class.getName(),
					"JournalArticle.resourcePrimKey", groupId);

				sql = StringUtil.replace(
					sql, "(companyId", "(JournalArticle.companyId");
			}

			SQLQuery q = session.createSQLQuery(sql);

			q.addScalar(COUNT_COLUMN_NAME, Type.LONG);

			QueryPos qPos = QueryPos.getInstance(q);

			qPos.add(companyId);

			if (groupId > 0) {
				qPos.add(groupId);
			}

			qPos.add(classNameId);
			qPos.add(uuids, 2);
			qPos.add(articleIds, 2);

			if ((version != null) && (version > 0)) {
				qPos.add(version);
			}

			qPos.add(titles, 2);
			qPos.add(descriptions, 2);
			qPos.add(contents, 2);
			qPos.add(displayDateGT_TS);
			qPos.add(displayDateGT_TS);
			qPos.add(displayDateLT_TS);
			qPos.add(displayDateLT_TS);

			if (status != WorkflowConstants.STATUS_ANY) {
				qPos.add(status);
			}

			qPos.add(reviewDate_TS);
			qPos.add(reviewDate_TS);

			if (Validator.isNotNull(type)) {
				qPos.add(type);
				qPos.add(type);
			}

			if (!isNullArray(structureIds)) {
				qPos.add(structureIds, 2);
			}

			if (!isNullArray(templateIds)) {
				qPos.add(templateIds, 2);
			}

			qPos.add(companyId);

			Iterator<Long> itr = q.iterate();

			if (itr.hasNext()) {
				Long count = itr.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (Exception e) {
			throw new SystemException(e);
		}
		finally {
			closeSession(session);
		}
	}

	@SuppressWarnings("unchecked")
	protected List<JournalArticle> doFindByC_G_C_U_A_V_T_D_C_T_S_T_D_S_R(
			long companyId, long groupId, long classNameId, String[] uuids,
			String[] articleIds, Double version, String[] titles,
			String[] descriptions, String[] contents, String type,
			String[] structureIds, String[] templateIds, Date displayDateGT,
			Date displayDateLT, int status, Date reviewDate,
			boolean andOperator, int start, int end,
			OrderByComparator orderByComparator, boolean inlineSQLHelper)
		throws SystemException {

		uuids = CustomSQLUtil.keywords(uuids, false);
		articleIds = CustomSQLUtil.keywords(articleIds, false);
		titles = CustomSQLUtil.keywords(titles);
		descriptions = CustomSQLUtil.keywords(descriptions, false);
		contents = CustomSQLUtil.keywords(contents, false);
		structureIds = CustomSQLUtil.keywords(structureIds, false);
		templateIds = CustomSQLUtil.keywords(templateIds, false);
		Timestamp displayDateGT_TS = CalendarUtil.getTimestamp(displayDateGT);
		Timestamp displayDateLT_TS = CalendarUtil.getTimestamp(displayDateLT);
		Timestamp reviewDate_TS = CalendarUtil.getTimestamp(reviewDate);

		Session session = null;

		try {
			session = openSession();

			String sql = CustomSQLUtil.get(
				FIND_BY_C_G_C_U_A_V_T_D_C_T_S_T_D_S_R);

			if (groupId <= 0) {
				sql = StringUtil.replace(sql, "(groupId = ?) AND", "");
			}

			sql = CustomSQLUtil.replaceKeywords(
				sql, "uuid_", StringPool.LIKE, false, uuids);

			sql = CustomSQLUtil.replaceKeywords(
				sql, "articleId", StringPool.LIKE, false, articleIds);

			if ((version == null) || (version <= 0)) {
				sql = StringUtil.replace(
					sql, "(version = ?) [$AND_OR_CONNECTOR$]", "");
			}

			sql = CustomSQLUtil.replaceKeywords(
				sql, "lower(title)", StringPool.LIKE, false, titles);
			sql = CustomSQLUtil.replaceKeywords(
				sql, "description", StringPool.LIKE, false, descriptions);
			sql = CustomSQLUtil.replaceKeywords(
				sql, "content", StringPool.LIKE, false, contents);

			if (status == WorkflowConstants.STATUS_ANY) {
				sql = StringUtil.replace(sql, "(status = ?) AND", "");
			}

			if (Validator.isNull(type)) {
				sql = StringUtil.replace(sql, _TYPE_SQL, StringPool.BLANK);
			}

			if (isNullArray(structureIds)) {
				sql = StringUtil.replace(
					sql, _STRUCTURE_ID_SQL, StringPool.BLANK);
			}
			else {
				sql = CustomSQLUtil.replaceKeywords(
					sql, "structureId", StringPool.LIKE, false, structureIds);
			}

			if (isNullArray(templateIds)) {
				sql = StringUtil.replace(
					sql, _TEMPLATE_ID_SQL, StringPool.BLANK);
			}
			else {
				sql = CustomSQLUtil.replaceKeywords(
					sql, "templateId", StringPool.LIKE, false, templateIds);
			}

			sql = CustomSQLUtil.replaceAndOperator(sql, andOperator);

			if ((articleIds != null) &&
				((articleIds.length > 1) ||
				 ((articleIds.length == 1) && (articleIds[0] != null))) &&
				(version == null)) {

				sql = StringUtil.replace(
					sql, "MAX(version) AS tempVersion",
					"version AS tempVersion");
				sql = StringUtil.replace(sql, "[$GROUP_BY_CLAUSE$]", "");
			}
			else {
				sql = StringUtil.replace(
					sql, "[$GROUP_BY_CLAUSE$]", "GROUP BY groupId, articleId");
			}

			sql = CustomSQLUtil.replaceOrderBy(sql, orderByComparator);

			if (inlineSQLHelper) {
				sql = InlineSQLHelperUtil.replacePermissionCheck(
					sql, JournalArticle.class.getName(),
					"JournalArticle.resourcePrimKey", groupId);

				sql = StringUtil.replace(
					sql, "(companyId", "(JournalArticle.companyId");
			}

			SQLQuery q = session.createSQLQuery(sql);

			q.addEntity("JournalArticle", JournalArticleImpl.class);

			QueryPos qPos = QueryPos.getInstance(q);

			qPos.add(companyId);

			if (groupId > 0) {
				qPos.add(groupId);
			}

			qPos.add(classNameId);
			qPos.add(uuids, 2);
			qPos.add(articleIds, 2);

			if ((version != null) && (version > 0)) {
				qPos.add(version);
			}

			qPos.add(titles, 2);
			qPos.add(descriptions, 2);
			qPos.add(contents, 2);
			qPos.add(displayDateGT_TS);
			qPos.add(displayDateGT_TS);
			qPos.add(displayDateLT_TS);
			qPos.add(displayDateLT_TS);

			if (status != WorkflowConstants.STATUS_ANY) {
				qPos.add(status);
			}

			qPos.add(reviewDate_TS);
			qPos.add(reviewDate_TS);

			if (Validator.isNotNull(type)) {
				qPos.add(type);
				qPos.add(type);
			}

			if (!isNullArray(structureIds)) {
				qPos.add(structureIds, 2);
			}

			if (!isNullArray(templateIds)) {
				qPos.add(templateIds, 2);
			}

			qPos.add(companyId);

			return (List<JournalArticle>)QueryUtil.list(
				q, getDialect(), start, end);
		}
		catch (Exception e) {
			throw new SystemException(e);
		}
		finally {
			closeSession(session);
		}
	}

	private static final String _STRUCTURE_ID_SQL =
		"(structureId LIKE ? [$AND_OR_NULL_CHECK$]) [$AND_OR_CONNECTOR$]";

	private static final String _TEMPLATE_ID_SQL =
		"(templateId LIKE ? [$AND_OR_NULL_CHECK$]) [$AND_OR_CONNECTOR$]";

	private static final String _TYPE_SQL =
		"(type_ = ? [$AND_OR_NULL_CHECK$]) [$AND_OR_CONNECTOR$]";

}