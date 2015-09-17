package com.liferay.portlet.journal.service;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.persistence.JournalArticleFinder;

import java.util.Date;
import java.util.List;
public interface CambiaJournalArticleFinder extends JournalArticleFinder {

	public int filterCountByC_G_C_U_A_V_T_D_C_T_S_T_D_S_R(
			long companyId, long groupId, long classNameId, String uuid,
			String articleId, Double version, String title, String description,
			String content, String type, String structureId, String templateId,
			Date displayDateGT, Date displayDateLT, int status, Date reviewDate,
			boolean andOperator)
		throws SystemException;

	public List<JournalArticle> findByC_G_C_U_A_V_T_D_C_T_S_T_D_S_R(
			long companyId, long groupId, long classNameId, String uuid,
			String articleId, Double version, String title, String description,
			String content, String type, String structureId, String templateId,
			Date displayDateGT, Date displayDateLT, int status, Date reviewDate,
			boolean andOperator, int start, int end,
			OrderByComparator orderByComparator)
		throws SystemException;

}