package com.cambiahealth.portal.hook.search;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BaseIndexerPostProcessor;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portlet.journal.model.JournalArticle;
public class JournalArticleIndexerPostProcessor extends
		BaseIndexerPostProcessor {

	public void postProcessDocument(
		Document document, Object object) throws Exception {

		JournalArticle journalArticle = (JournalArticle) object;
		String uuid = journalArticle.getUuid();

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Post processing document for journal article with UUID: "
					+ uuid);
		}

		document.addKeyword(Field.UUID, uuid);
	}

	private static Log _log = LogFactoryUtil.getLog(
		JournalArticleIndexerPostProcessor.class);

}