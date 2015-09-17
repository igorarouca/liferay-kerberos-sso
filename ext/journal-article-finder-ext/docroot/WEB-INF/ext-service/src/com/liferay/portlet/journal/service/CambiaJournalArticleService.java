package com.liferay.portlet.journal.service;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.jsonwebservice.JSONWebService;
import com.liferay.portal.kernel.transaction.Isolation;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portlet.journal.model.JournalArticle;

import java.util.Date;
import java.util.List;

@JSONWebService
@Transactional(isolation = Isolation.PORTAL, rollbackFor =  {
	PortalException.class, SystemException.class})
public interface CambiaJournalArticleService extends JournalArticleService {

	public List<JournalArticle> search(
			long companyId, long groupId, long classNameId, String uuid,
			String articleId, Double version, String title, String description,
			String content, String type, String structureId, String templateId,
			Date displayDateGT, Date displayDateLT, int status, Date reviewDate,
			boolean andOperator, int start, int end, OrderByComparator obc)
		throws SystemException;

	public int searchCount(
			long companyId, long groupId, long classNameId, String uuid,
			String articleId, Double version, String title, String description,
			String content, String type, String structureId, String templateId,
			Date displayDateGT, Date displayDateLT, int status, Date reviewDate,
			boolean andOperator)
		throws SystemException;

}