package com.cambiahealth.portal.dbcleanup.cleaner.articles;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Node;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portlet.dynamicdatamapping.util.DDMXMLUtil;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;
class JournalArticleMigrationImpl implements Runnable {

	@Override
	public void run() {
		try {
			String content = _article.getContent();
			Document document = SAXReaderUtil.read(content);
			Node node = document.selectSingleNode(
				_GLOBAL_CONTENT_ID_STRUCTURE_FIELD);

			String articleId = node.getText();
			if (articleId.length() > 0) {
				JournalArticle referencedArticle =
					JournalArticleLocalServiceUtil.getArticle(
						_article.getGroupId(), articleId);

				node.setText(referencedArticle.getArticleResourceUuid());
				_article.setContent(DDMXMLUtil.formatXML(document));

				JournalArticleLocalServiceUtil.updateJournalArticle(_article);
			}
		}
		catch (Exception e) {
			_log.error(
				">>> Error migrating article with ID " +
					_article.getArticleId());

			throw new JournalArticleMigrationException(e);
		}
	}

	JournalArticleMigrationImpl(JournalArticle article) {
		_article = article;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		JournalArticleMigrationImpl.class);

	private static final String _GLOBAL_CONTENT_ID_STRUCTURE_FIELD =
		"/root/dynamic-element[@name='global-content-id']/dynamic-content";

	private JournalArticle _article;

}