/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.cambiahealth.portal.cleanup.service;

import com.liferay.portal.kernel.bean.PortletBeanLocatorUtil;
import com.liferay.portal.kernel.util.ReferenceRegistry;
import com.liferay.portal.service.InvokableLocalService;

/**
 * The utility for the group user relation local service. This utility wraps {@link com.cambiahealth.portal.cleanup.service.impl.GroupUserRelationLocalServiceImpl} and is the primary access point for service operations in application layer code running on the local server.
 *
 * <p>
 * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the same VM.
 * </p>
 *
 * @author Igor Arouca
 * @see GroupUserRelationLocalService
 * @see com.cambiahealth.portal.cleanup.service.base.GroupUserRelationLocalServiceBaseImpl
 * @see com.cambiahealth.portal.cleanup.service.impl.GroupUserRelationLocalServiceImpl
 * @generated
 */
public class GroupUserRelationLocalServiceUtil {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Add custom service methods to {@link com.cambiahealth.portal.cleanup.service.impl.GroupUserRelationLocalServiceImpl} and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	* Returns the Spring bean ID for this bean.
	*
	* @return the Spring bean ID for this bean
	*/
	public static java.lang.String getBeanIdentifier() {
		return getService().getBeanIdentifier();
	}

	/**
	* Sets the Spring bean ID for this bean.
	*
	* @param beanIdentifier the Spring bean ID for this bean
	*/
	public static void setBeanIdentifier(java.lang.String beanIdentifier) {
		getService().setBeanIdentifier(beanIdentifier);
	}

	public static java.lang.Object invokeMethod(java.lang.String name,
		java.lang.String[] parameterTypes, java.lang.Object[] arguments)
		throws java.lang.Throwable {
		return getService().invokeMethod(name, parameterTypes, arguments);
	}

	public static int getGroupUserRelationsCount(long groupId)
		throws com.liferay.portal.kernel.exception.SystemException {
		return getService().getGroupUserRelationsCount(groupId);
	}

	public static void deleteGroupUserRelations(long groupId)
		throws com.liferay.portal.kernel.exception.SystemException {
		getService().deleteGroupUserRelations(groupId);
	}

	public static void clearService() {
		_service = null;
	}

	public static GroupUserRelationLocalService getService() {
		if (_service == null) {
			InvokableLocalService invokableLocalService = (InvokableLocalService)PortletBeanLocatorUtil.locate(ClpSerializer.getServletContextName(),
					GroupUserRelationLocalService.class.getName());

			if (invokableLocalService instanceof GroupUserRelationLocalService) {
				_service = (GroupUserRelationLocalService)invokableLocalService;
			}
			else {
				_service = new GroupUserRelationLocalServiceClp(invokableLocalService);
			}

			ReferenceRegistry.registerReference(GroupUserRelationLocalServiceUtil.class,
				"_service");
		}

		return _service;
	}

	/**
	 * @deprecated
	 */
	public void setService(GroupUserRelationLocalService service) {
	}

	private static GroupUserRelationLocalService _service;
}