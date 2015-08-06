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

package com.cambiahealth.portal.dbcleanup.service;

import com.liferay.portal.service.ServiceWrapper;

/**
 * <p>
 * This class is a wrapper for {@link GroupUserRelationLocalService}.
 * </p>
 *
 * @author    Igor Arouca
 * @see       GroupUserRelationLocalService
 * @generated
 */
public class GroupUserRelationLocalServiceWrapper
	implements GroupUserRelationLocalService,
		ServiceWrapper<GroupUserRelationLocalService> {
	public GroupUserRelationLocalServiceWrapper(
		GroupUserRelationLocalService groupUserRelationLocalService) {
		_groupUserRelationLocalService = groupUserRelationLocalService;
	}

	/**
	* Returns the Spring bean ID for this bean.
	*
	* @return the Spring bean ID for this bean
	*/
	public java.lang.String getBeanIdentifier() {
		return _groupUserRelationLocalService.getBeanIdentifier();
	}

	/**
	* Sets the Spring bean ID for this bean.
	*
	* @param beanIdentifier the Spring bean ID for this bean
	*/
	public void setBeanIdentifier(java.lang.String beanIdentifier) {
		_groupUserRelationLocalService.setBeanIdentifier(beanIdentifier);
	}

	public java.lang.Object invokeMethod(java.lang.String name,
		java.lang.String[] parameterTypes, java.lang.Object[] arguments)
		throws java.lang.Throwable {
		return _groupUserRelationLocalService.invokeMethod(name,
			parameterTypes, arguments);
	}

	public void deleteGroupUserRelations(long groupId)
		throws com.liferay.portal.kernel.exception.SystemException {
		_groupUserRelationLocalService.deleteGroupUserRelations(groupId);
	}

	public int getGroupUserRelationsCount(long groupId)
		throws com.liferay.portal.kernel.exception.SystemException {
		return _groupUserRelationLocalService.getGroupUserRelationsCount(groupId);
	}

	/**
	 * @deprecated Renamed to {@link #getWrappedService}
	 */
	public GroupUserRelationLocalService getWrappedGroupUserRelationLocalService() {
		return _groupUserRelationLocalService;
	}

	/**
	 * @deprecated Renamed to {@link #setWrappedService}
	 */
	public void setWrappedGroupUserRelationLocalService(
		GroupUserRelationLocalService groupUserRelationLocalService) {
		_groupUserRelationLocalService = groupUserRelationLocalService;
	}

	public GroupUserRelationLocalService getWrappedService() {
		return _groupUserRelationLocalService;
	}

	public void setWrappedService(
		GroupUserRelationLocalService groupUserRelationLocalService) {
		_groupUserRelationLocalService = groupUserRelationLocalService;
	}

	private GroupUserRelationLocalService _groupUserRelationLocalService;
}