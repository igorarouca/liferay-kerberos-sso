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

package com.cambiahealth.portal.dbcleanup.service.impl;

import com.cambiahealth.portal.dbcleanup.service.base.CorruptedLayoutLocalServiceBaseImpl;
import com.cambiahealth.portal.dbcleanup.util.DbUtil;
import com.cambiahealth.portal.dbcleanup.util.StagingAdvicesUtil;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.InfrastructureUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.PortletConstants;
import com.liferay.portal.model.ResourcePermission;
import com.liferay.portal.util.PortletKeys;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.List;

import javax.sql.DataSource;

import static com.cambiahealth.portal.dbcleanup.DbCleanupConstants.GROUP_ID_QUERY_PROPERTY;

/**
 * The implementation of the corrupted layout local service.
 *
 * <p>
 * All custom service methods should be put in this class. Whenever methods are added, rerun ServiceBuilder to copy their definitions into the {@link com.cambiahealth.portal.dbcleanup.service.CorruptedLayoutLocalService} interface.
 *
 * <p>
 * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the same VM.
 * </p>
 *
 * @author Igor Arouca
 * @see com.cambiahealth.portal.dbcleanup.service.base.CorruptedLayoutLocalServiceBaseImpl
 * @see com.cambiahealth.portal.dbcleanup.service.CorruptedLayoutLocalServiceUtil
 */
public class CorruptedLayoutLocalServiceImpl
	extends CorruptedLayoutLocalServiceBaseImpl {

	public void deleteCorruptedLayout(Layout layout) {
		try {
			layoutLocalService.deleteLayout(layout);
		}
		catch (SystemException e) {
		}

		deleteCorruptedLayoutReferences(layout);
	}

	public void deleteCorruptedLayoutReferences(Layout layout) {
		deletePortletPreferences(layout);
		deleteJournalArticleReferences(layout);
		deleteJournalContentSearches(layout);
		deleteExpandoValues(layout);
		deleteScopeGroup(layout);
		deleteResourcePermissions(layout);
	}

	@SuppressWarnings("unchecked")
	public void deleteCorruptedLayouts(long groupId) {
		DynamicQuery query = layoutLocalService.dynamicQuery();
		query.add(GROUP_ID_QUERY_PROPERTY.eq(groupId));

		/* Disable StagingAdvices to avoid exception when wrapping returned
		 * layouts, which happens due to the fact that the layouts are pointing
		 * to a nonexistent group:
		 * https://gist.github.com/anonymous/130ba785fe02ad02cd86
		 */
		boolean originalValue = StagingAdvicesUtil.disableStagingAdvices();

		List<Layout> layouts = null;
		try {
			layouts = layoutLocalService.dynamicQuery(query);

			if ((layouts == null) || layouts.isEmpty()) {
				return;
			}
		}
		catch (SystemException e) {
			_log.error(
				">>> Error retrieving corrupted private layouts for groupId: " +
					groupId, e);
		}
		finally {
			StagingAdvicesUtil.resetStagingAdvices(originalValue);
		}

		for (Layout layout : layouts) {
			deleteCorruptedLayoutReferences(layout);
		}

		doDeleteCorruptedLayouts(groupId);
	}

	protected void deleteExpandoValues(Layout layout) {
		try {
			expandoValueLocalService.deleteValues(
				_LAYOUT_CLASS_NAME, layout.getPlid());
		}
		catch (SystemException se) {
			_log.error(
				">>> Error deleting expando values for layout: " +
					layout.getPlid(), se);
		}
	}

	protected void deleteJournalArticleReferences(Layout layout) {
		try {
			journalArticleLocalService.deleteLayoutArticleReferences(
				layout.getGroupId(), layout.getUuid());
		}
		catch (SystemException se) {
			_log.error(
				">>> Error deleting journal article references for layout: " +
					layout.getPlid(), se);
		}
	}

	protected void deleteJournalContentSearches(Layout layout) {
		try {
			journalContentSearchLocalService.deleteLayoutContentSearches(
				layout.getGroupId(), layout.isPrivateLayout(),
				layout.getLayoutId());
		}
		catch (SystemException se) {
			_log.error(
				">>> Error deleting journal content searches for layout: " +
					layout.getPlid(), se);
		}
	}

	protected void deletePortletPreferences(Layout layout) {
		try {
			portletPreferencesLocalService.deletePortletPreferences(
				PortletKeys.PREFS_OWNER_ID_DEFAULT,
				PortletKeys.PREFS_OWNER_TYPE_LAYOUT, layout.getPlid());
		}
		catch (SystemException se) {
			_log.error(
				">>> Error deleting portlet preferences for layout: " +
					layout.getPlid(), se);
		}
	}

	protected void deleteResourcePermissions(Layout layout) {
		String primKey =
			layout.getPlid() + PortletConstants.LAYOUT_SEPARATOR + "%";

		try {
			List<ResourcePermission> resourcePermissions =
				resourcePermissionPersistence.findByC_P(
					layout.getCompanyId(), primKey);

			for (ResourcePermission resourcePermission : resourcePermissions) {
				resourcePermissionLocalService.deleteResourcePermission(
					resourcePermission);
			}
		}
		catch (SystemException e) {
			_log.error(
				">>> Error deleting resource permissions for layout: " +
					layout.getPlid(), e);
		}
	}

	protected void deleteScopeGroup(Layout layout) {
		try {
			Group scopeGroup = layout.getScopeGroup();

			if (scopeGroup != null) {
				groupLocalService.deleteGroup(scopeGroup.getGroupId());
			}
		}
		catch (PortalException | SystemException e) {
			_log.error(
				">>> Error deleting scope group for layout: " +
					layout.getPlid(), e);
		}
	}

	protected void doDeleteCorruptedLayouts(long groupId) {
		DataSource dataSource = InfrastructureUtil.getDataSource();

		Connection connection = null;
		PreparedStatement deleteStatement = null;

		try {
			connection = dataSource.getConnection();
			deleteStatement = connection.prepareStatement(
				_SQL_DELETE_LAYOUTS_BY_GROUP_ID);

			deleteStatement.setLong(1, groupId);

			int deleteCount = deleteStatement.executeUpdate();

			if (deleteCount > 0) {
				_log.info(">>> Deleted " + deleteCount +
					" corrupted layouts associated with groupId: " + groupId);
			}
		}
		catch (SQLException sqle) {
			_log.error(">>> Error deleting layouts for groupId: ", sqle);
		}
		finally {
			DbUtil.closeQuietly(connection);
			DbUtil.closeQuietly(deleteStatement);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CorruptedLayoutLocalServiceImpl.class);

	private static final String _LAYOUT_CLASS_NAME = Layout.class.getName();

	private static final String _SQL_DELETE_LAYOUTS_BY_GROUP_ID =
		"DELETE Layout WHERE groupId = ?";

}