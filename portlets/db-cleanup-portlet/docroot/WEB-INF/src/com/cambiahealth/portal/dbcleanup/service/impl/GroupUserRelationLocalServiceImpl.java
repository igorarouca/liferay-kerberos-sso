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

import com.cambiahealth.portal.dbcleanup.service.base.GroupUserRelationLocalServiceBaseImpl;
import com.cambiahealth.portal.dbcleanup.util.DbUtil;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.InfrastructureUtil;
import com.liferay.portal.service.persistence.GroupUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * The implementation of the group user relation local service.
 *
 * <p>
 * All custom service methods should be put in this class. Whenever methods are added, rerun ServiceBuilder to copy their definitions into the {@link com.cambiahealth.portal.dbcleanup.service.GroupUserRelationLocalService} interface.
 *
 * <p>
 * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the same VM.
 * </p>
 *
 * @author Igor Arouca
 * @see com.cambiahealth.portal.dbcleanup.service.base.GroupUserRelationLocalServiceBaseImpl
 * @see com.cambiahealth.portal.dbcleanup.service.GroupUserRelationLocalServiceUtil
 */
public class GroupUserRelationLocalServiceImpl
	extends GroupUserRelationLocalServiceBaseImpl {

	public void deleteGroupUserRelations(long groupId) throws SystemException {
		GroupUtil.clearUsers(groupId);
	}

	public int getGroupUserRelationsCount(long groupId) throws SystemException {
		int count = 0;

		DataSource dataSource = InfrastructureUtil.getDataSource();

		Connection connection = null;
		PreparedStatement countStatement = null;
		ResultSet resultSet = null;

		try {
			connection = dataSource.getConnection();
			countStatement = connection.prepareStatement(
				_SQL_COUNT_USERS_GROUPS_BY_GROUP_ID);

			countStatement.setLong(1, groupId);

			resultSet = countStatement.executeQuery();

			if (resultSet.next()) {
				count = resultSet.getInt(1);
			}

		}
		catch (SQLException sqle) {
			_log.error(">>> Error deleting layouts for groupId: ", sqle);
		}
		finally {
			DbUtil.closeQuietly(connection, countStatement, resultSet);
		}

		return count;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		GroupUserRelationLocalServiceImpl.class);

	private static final String _SQL_COUNT_USERS_GROUPS_BY_GROUP_ID =
		"SELECT COUNT(*) FROM Users_Groups WHERE groupId = ?";

}