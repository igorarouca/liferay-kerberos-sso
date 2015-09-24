package com.cambiahealth.portal.dbcleanup.cleaner.site.customfield;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
public interface CustomFieldHelper {

	void addCustomField(String name) throws PortalException;

	String getCustomFieldValue(String name);
	void setCustomFieldValue(String name, String value);

	long getGroupId();
	void setGroupId(long groupId);

	boolean hasCustomField(String name);

	void removeCustomField(String name) throws PortalException, SystemException;

}