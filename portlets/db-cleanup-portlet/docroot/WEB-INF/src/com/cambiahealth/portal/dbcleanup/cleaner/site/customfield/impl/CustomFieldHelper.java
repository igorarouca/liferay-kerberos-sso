package com.cambiahealth.portal.dbcleanup.cleaner.site.customfield.impl;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.Group;
import com.liferay.portlet.expando.model.ExpandoBridge;
import com.liferay.portlet.expando.model.ExpandoColumnConstants;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.util.ExpandoBridgeFactoryUtil;
class CustomFieldHelper {

	CustomFieldHelper(long companyId) {
		_expandoBridge =
			ExpandoBridgeFactoryUtil.getExpandoBridgeFactory().getExpandoBridge(
				companyId, _GROUP_CLASS_NAME);
	}

	void addCustomField(String name) throws PortalException {
		_expandoBridge.addAttribute(
			name, ExpandoColumnConstants.STRING, StringPool.BLANK);
	}

	String getCustomFieldValue(String name) {
		return (String)_expandoBridge.getAttribute(name);
	}

	void setCustomFieldValue(String name, String value) {
		if (!hasCustomField(name)) {
			try {
				addCustomField(name);
			}
			catch (PortalException pe) {
				_log.error(
					">>> Error creating new custom field " + name
						+ " for site " + _expandoBridge.getClassPK());
			}
		}

		_expandoBridge.setAttribute(name, value);

		if (_log.isInfoEnabled() && (value != null) && !value.isEmpty()) {
			_log.info(
				">>> Created new custom field " + name + " with value ["
					+ value + "] for site " + _expandoBridge.getClassPK());
		}
	}

	long getGroupId() {
		return _expandoBridge.getClassPK();
	}

	void setGroupId(long groupId) {
		_expandoBridge.setClassPK(groupId);
	}

	boolean hasCustomField(String name) {
		return _expandoBridge.hasAttribute(name);
	}

	void removeCustomField(String name)
		throws PortalException, SystemException {

		ExpandoColumnLocalServiceUtil.deleteColumn(
			_expandoBridge.getCompanyId(), _expandoBridge.getClassName(),
			ExpandoTableConstants.DEFAULT_TABLE_NAME, name);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CustomFieldHelper.class);

	private static final String _GROUP_CLASS_NAME = Group.class.getName();

	private ExpandoBridge _expandoBridge;

}