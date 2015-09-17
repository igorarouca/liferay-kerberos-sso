package com.liferay.portlet.journal.service;

import java.util.LinkedHashMap;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.transaction.Isolation;
import com.liferay.portal.kernel.transaction.Transactional;
@Transactional(isolation = Isolation.PORTAL, rollbackFor =  {
		PortalException.class, SystemException.class})
public interface CambiaJournalArticleLocalService
	extends JournalArticleLocalService {

	public Hits search(
			long companyId, long groupId, long classNameId, String uuid,
			String articleId, String title, String description, String content,
			String type, String status, String structureId, String templateId,
			LinkedHashMap<String, Object> params, boolean andSearch, int start,
			int end, Sort sort)
		throws SystemException;

}