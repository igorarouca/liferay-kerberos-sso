package com.liferay.portlet.journal.service;

import java.util.LinkedHashMap;

import com.liferay.portal.kernel.bean.PortalBeanLocatorUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.util.ReferenceRegistry;

public class CambiaJournalArticleLocalServiceUtil {

	public static CambiaJournalArticleLocalService getService() {
		if (_service == null) {
			_service = (CambiaJournalArticleLocalService) PortalBeanLocatorUtil
				.locate(JournalArticleLocalService.class.getName());

			ReferenceRegistry.registerReference(
				CambiaJournalArticleLocalServiceUtil.class, "_service");
		}

		return _service;
	}

	public static Hits search(
			long companyId, long groupId, long classNameId, String uuid,
			String articleId, String title, String description, String content,
			String type, String status, String structureId, String templateId,
			LinkedHashMap<String, Object> params, boolean andSearch, int start,
			int end, Sort sort)
		throws SystemException {

		return getService().search(
			companyId, groupId, classNameId, uuid, articleId, title, 
			description, content, type, status, structureId, templateId, params,
			andSearch, start, end, sort);
	}

	private static CambiaJournalArticleLocalService _service;

}