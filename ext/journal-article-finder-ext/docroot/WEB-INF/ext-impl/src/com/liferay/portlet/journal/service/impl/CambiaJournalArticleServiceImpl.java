package com.liferay.portlet.journal.service.impl;

import java.util.Date;
import java.util.List;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.CambiaJournalArticleFinder;
import com.liferay.portlet.journal.service.CambiaJournalArticleService;
public class CambiaJournalArticleServiceImpl extends JournalArticleServiceImpl
	implements CambiaJournalArticleService {

	@Override
	public List<JournalArticle> search(
			long companyId, long groupId, long classNameId, String uuid,
			String articleId, Double version, String title, String description,
			String content, String type, String structureId, String templateId,
			Date displayDateGT, Date displayDateLT, int status, Date reviewDate,
			boolean andOperator, int start, int end, OrderByComparator obc)
		throws SystemException {

		return getCambiaJournalArticleFinder()
			.findByC_G_C_U_A_V_T_D_C_T_S_T_D_S_R(
				companyId, groupId, classNameId, uuid, articleId, version, 
				title, description, content, type, structureId, templateId, 
				displayDateGT, displayDateLT, status, reviewDate, andOperator,
				start, end, obc);
	}

	@Override
	public int searchCount(
			long companyId, long groupId, long classNameId, String uuid,
			String articleId, Double version, String title, String description,
			String content, String type, String structureId, String templateId,
			Date displayDateGT, Date displayDateLT, int status, Date reviewDate,
			boolean andOperator)
		throws SystemException {

		return getCambiaJournalArticleFinder()
			.filterCountByC_G_C_U_A_V_T_D_C_T_S_T_D_S_R(
				companyId, groupId, classNameId, uuid, articleId, version,
				title, description, content, type, structureId, templateId,
				displayDateGT, displayDateLT, status, reviewDate, andOperator);
	}

	protected CambiaJournalArticleFinder getCambiaJournalArticleFinder() {		
		if (_cambiaJournalArticleFinder == null) {
			_cambiaJournalArticleFinder = 
				(CambiaJournalArticleFinder)journalArticleFinder;
		}

		return _cambiaJournalArticleFinder;
	}

	private CambiaJournalArticleFinder _cambiaJournalArticleFinder;

}