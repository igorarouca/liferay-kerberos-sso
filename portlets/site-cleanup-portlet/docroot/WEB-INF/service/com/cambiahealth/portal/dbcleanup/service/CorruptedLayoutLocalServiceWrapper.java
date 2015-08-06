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
 * This class is a wrapper for {@link CorruptedLayoutLocalService}.
 * </p>
 *
 * @author    Igor Arouca
 * @see       CorruptedLayoutLocalService
 * @generated
 */
public class CorruptedLayoutLocalServiceWrapper
	implements CorruptedLayoutLocalService,
		ServiceWrapper<CorruptedLayoutLocalService> {
	public CorruptedLayoutLocalServiceWrapper(
		CorruptedLayoutLocalService corruptedLayoutLocalService) {
		_corruptedLayoutLocalService = corruptedLayoutLocalService;
	}

	/**
	* Returns the Spring bean ID for this bean.
	*
	* @return the Spring bean ID for this bean
	*/
	public java.lang.String getBeanIdentifier() {
		return _corruptedLayoutLocalService.getBeanIdentifier();
	}

	/**
	* Sets the Spring bean ID for this bean.
	*
	* @param beanIdentifier the Spring bean ID for this bean
	*/
	public void setBeanIdentifier(java.lang.String beanIdentifier) {
		_corruptedLayoutLocalService.setBeanIdentifier(beanIdentifier);
	}

	public java.lang.Object invokeMethod(java.lang.String name,
		java.lang.String[] parameterTypes, java.lang.Object[] arguments)
		throws java.lang.Throwable {
		return _corruptedLayoutLocalService.invokeMethod(name, parameterTypes,
			arguments);
	}

	public void deleteCorruptedLayouts(long groupId) {
		_corruptedLayoutLocalService.deleteCorruptedLayouts(groupId);
	}

	/**
	 * @deprecated Renamed to {@link #getWrappedService}
	 */
	public CorruptedLayoutLocalService getWrappedCorruptedLayoutLocalService() {
		return _corruptedLayoutLocalService;
	}

	/**
	 * @deprecated Renamed to {@link #setWrappedService}
	 */
	public void setWrappedCorruptedLayoutLocalService(
		CorruptedLayoutLocalService corruptedLayoutLocalService) {
		_corruptedLayoutLocalService = corruptedLayoutLocalService;
	}

	public CorruptedLayoutLocalService getWrappedService() {
		return _corruptedLayoutLocalService;
	}

	public void setWrappedService(
		CorruptedLayoutLocalService corruptedLayoutLocalService) {
		_corruptedLayoutLocalService = corruptedLayoutLocalService;
	}

	private CorruptedLayoutLocalService _corruptedLayoutLocalService;
}