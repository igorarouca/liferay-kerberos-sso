package com.cambiahealth.portal.cleanup.action;

import com.cambiahealth.portal.cleanup.DbCleanupConstants;
import com.cambiahealth.portal.cleanup.service.CorruptedLayoutLocalServiceUtil;
import com.cambiahealth.portal.cleanup.service.GroupUserRelationLocalServiceUtil;

import com.liferay.portal.NoSuchGroupException;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.SimpleAction;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.GroupConstants;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutSetBranch;
import com.liferay.portal.model.PortletItem;
import com.liferay.portal.model.ResourceTypePermission;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.LayoutSetBranchLocalServiceUtil;
import com.liferay.portal.service.PortletItemLocalServiceUtil;
import com.liferay.portal.service.ResourceTypePermissionLocalServiceUtil;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.model.AssetTag;
import com.liferay.portlet.asset.model.AssetVocabulary;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetTagLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetVocabularyLocalServiceUtil;
import com.liferay.portlet.documentlibrary.model.DLFileVersion;
import com.liferay.portlet.documentlibrary.service.DLFileVersionLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.model.DDMContent;
import com.liferay.portlet.dynamicdatamapping.service.DDMContentLocalServiceUtil;
import com.liferay.portlet.journal.model.JournalContentSearch;
import com.liferay.portlet.journal.service.JournalContentSearchLocalServiceUtil;
import com.liferay.portlet.messageboards.model.MBThread;
import com.liferay.portlet.messageboards.service.MBThreadLocalServiceUtil;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.cambiahealth.portal.cleanup.DbCleanupConstants.CLASS_NAME_ID_QUERY_PROPERTY;
import static com.cambiahealth.portal.cleanup.DbCleanupConstants.GROUP_ID_QUERY_PROPERTY;
import static com.cambiahealth.portal.cleanup.DbCleanupConstants.LAYOUT_CLASS_NAME_ID;
public class DbCleanupAction extends SimpleAction {

	public DbCleanupAction() {
		super();
	}

	public void run(String[] ids) throws ActionException {
		_log.info(
			">>> Started DB cleanup action on portal instance: " + ids[0]);

		String groupIdCommaSeparatedList =
			DbCleanupConstants.DANGLING_GROUP_IDS;

		if (groupIdCommaSeparatedList.isEmpty()) {
			_log.warn(">>> Property 'db.cleanup.dangling.group.ids' is empty");
			return;
		}

		long[] danglingGroupIds = StringUtil.split(
			groupIdCommaSeparatedList, 0l);

		_log.info(
			">>> 'db.cleanup.dangling.group.ids' = " +
				Arrays.toString(danglingGroupIds));

		long startTime = System.currentTimeMillis();

		removeCorruptedLayoutScopeGroups();
		removeInvalidResourceTypePermissions();

		for (long groupId : danglingGroupIds) {
			if (groupId > 0) {
				removeOrphanRecordsFor(groupId);
			}
		}

		long duration = System.currentTimeMillis() - startTime;

		_log.info(
			">>> Finshed DB cleanup action in " +
				TimeUnit.MILLISECONDS.toSeconds(duration) + " seconds");
	}

	@SuppressWarnings("unchecked")
	protected void removeAssetEntries(long groupId) {
		DynamicQuery query = AssetEntryLocalServiceUtil.dynamicQuery();
		query.add(GROUP_ID_QUERY_PROPERTY.eq(groupId));

		List<AssetEntry> assetEntries = null;
		try {
			assetEntries = AssetEntryLocalServiceUtil.dynamicQuery(query);
		}
		catch (SystemException se) {
			_log.error(
				">>> Error retrieving asset entries for groupId: " + groupId,
				se);
		}

		if (assetEntries == null) {
			return;
		}

		for (AssetEntry entry : assetEntries) {
			try {
				AssetEntryLocalServiceUtil.deleteEntry(entry);

				_log.info(
					">>> Deleted asset entry: " + entry.getEntryId() +
						" for groupId: " + groupId);
			}
			catch (PortalException | SystemException e) {
				_log.error(
					">>> Error deleting asset entry: " + entry.getPrimaryKey() +
						" for groupId: " + entry.getGroupId(), e);
			}
		}
	}

	protected void removeAssetTag(AssetTag tag) {
		try {
			AssetTagLocalServiceUtil.deleteTag(tag);

			_log.info(
				">>> Deleted asset tag: " + tag.getTagId() + " for groupId: " +
					tag.getGroupId());
		}
		catch (PortalException | SystemException e) {
			_log.error(
				">>> Error deleting asset tag: " + tag.getPrimaryKey() +
					" for groupId: " + tag.getGroupId(), e);
		}
	}

	protected void removeAssetTags(long groupId) {
		List<AssetTag> tags = null;
		try {
			tags = AssetTagLocalServiceUtil.getGroupTags(groupId);
		}
		catch (SystemException se) {
			_log.error(
				">>> Error retrieving asset tags for groupId: " + groupId, se);
		}

		if (tags == null) {
			return;
		}

		for (AssetTag tag : tags) {
			removeAssetTag(tag);
		}
	}

	protected void removeAssetVocabularies(long groupId) {
		try {
			List<AssetVocabulary> assetVocabularies =
				AssetVocabularyLocalServiceUtil.getGroupVocabularies(groupId);

			if ((assetVocabularies == null) || assetVocabularies.isEmpty()) {
				return;
			}
		}
		catch (NoSuchGroupException nsge) {
			return;
		}
		catch (PortalException | SystemException e) {
			_log.error(
				">>> Error retrieving asset vocabularies for groupId: " +
					groupId, e);
		}

		try {
			AssetVocabularyLocalServiceUtil.deleteVocabularies(groupId);

			_log.info(">>> Deleted asset vocabularies for groupId: " + groupId);
		}
		catch (PortalException | SystemException e) {
			_log.error(
				">>> Error deleting asset vocabularies for groupId: " + groupId,
				e);
		}
	}

	protected void removeCorruptedLayoutScopeGroup(Group group) {
		try {
			GroupLocalServiceUtil.deleteGroup(group);

			_log.info(
				">>> Deleted layout group with ID: " + group.getGroupId());
		}
		catch (PortalException | SystemException e) {
			_log.error(
				">>> Error deleting layout group with ID: " +
					group.getGroupId(), e);
		}
	}

	@SuppressWarnings("unchecked")
	protected void removeCorruptedLayoutScopeGroups() {
		DynamicQuery query = GroupLocalServiceUtil.dynamicQuery();
		query.add(CLASS_NAME_ID_QUERY_PROPERTY.eq(LAYOUT_CLASS_NAME_ID));

		try {
			List<Group> groups = GroupLocalServiceUtil.dynamicQuery(query);

			if (groups == null | groups.isEmpty()) {
				return;
			}

			for (Group group : groups) {
				Layout layout = LayoutLocalServiceUtil.fetchLayout(
					group.getClassPK());

				if (layout == null) {
					removeCorruptedLayoutScopeGroup(group);
				}
			}
		}
		catch (SystemException se) {
			_log.error(">>> Error deleting corrupted layout group", se);
		}
	}

	protected void removeDDMContents(long groupId) {
		try {
			List<DDMContent> ddmContents =
				DDMContentLocalServiceUtil.getContents(groupId);

			if ((ddmContents == null) || ddmContents.isEmpty()) {
				return;
			}
		}
		catch (SystemException se) {
			_log.error(
				">>> Error retrieving DDM contents for groupId: " +
					groupId, se);
		}

		try {
			DDMContentLocalServiceUtil.deleteContents(groupId);

			_log.info(">>> Deleted DDM contents for groupId: " + groupId);
		}
		catch (SystemException se) {
			_log.error(
				">>> Error deleting DDM contents for groupId: " + groupId, se);
		}
	}

	protected void removeDLFileVersion(DLFileVersion dlFileVersion) {
		try {
			DLFileVersionLocalServiceUtil.deleteDLFileVersion(dlFileVersion);

			_log.info(
				">>> Deleted DL file version: " +
					dlFileVersion.getPrimaryKey() + " for groupId: " +
						dlFileVersion.getGroupId());
		}
		catch (SystemException se) {
			_log.error(
				">>> Error deleting DL file version: " +
					dlFileVersion.getPrimaryKey() + " for groupId: "
						+ dlFileVersion.getGroupId(), se);
		}
	}

	@SuppressWarnings("unchecked")
	protected void removeDLFileVersions(long groupId) {
		DynamicQuery query = DLFileVersionLocalServiceUtil.dynamicQuery();
		query.add(GROUP_ID_QUERY_PROPERTY.eq(groupId));

		List<DLFileVersion> dlFileVersions = null;
		try {
			dlFileVersions = DLFileVersionLocalServiceUtil.dynamicQuery(query);
		}
		catch (SystemException se) {
			_log.error(">>> Error retrieving DL file versions for groupId: " +
				groupId, se);
		}

		if (dlFileVersions == null) {
			return;
		}

		for (DLFileVersion dlFileVersion : dlFileVersions) {
			removeDLFileVersion(dlFileVersion);
		}
	}

	protected void removeGroupUserRelations(long groupId) {
		try {
			int count =
				GroupUserRelationLocalServiceUtil.getGroupUserRelationsCount(
					groupId);

			if (count == 0) {
				return;
			}

			GroupUserRelationLocalServiceUtil.deleteGroupUserRelations(groupId);

			_log.info(
				">>> Deleted group-user relationship for groupId: " + groupId);
		}
		catch (SystemException se) {
			_log.error(
				">>> Error deleting user-group relations for groupId: "
					+ groupId, se);
		}
	}

	protected void removeInvalidResourceTypePermissions() {
		removeResourceTypePermissions(GroupConstants.DEFAULT_LIVE_GROUP_ID);
	}

	@SuppressWarnings("unchecked")
	protected void removeJournalContentSearchEntriesFor(long groupId) {
		DynamicQuery query =
			JournalContentSearchLocalServiceUtil.dynamicQuery();

		query.add(DbCleanupConstants.GROUP_ID_QUERY_PROPERTY.eq(groupId));

		try {
			List<JournalContentSearch> journalContentSearchEntries =
				JournalContentSearchLocalServiceUtil.dynamicQuery(query);

			if ((journalContentSearchEntries == null) ||
					journalContentSearchEntries.isEmpty()) {

				return;
			}

			JournalContentSearchLocalServiceUtil.deleteOwnerContentSearches(
				groupId, false);

			_log.info(
				">>> Deleted journal content search entries for groupId: " +
					groupId);
		}
		catch (SystemException se) {
			_log.error(
				">>> Error deleting journal content search entries for:" +
					groupId);
		}
	}

	protected void removeLayoutSetBranch(long groupId, boolean privateLayout) {
		String layoutSetBranchType = privateLayout ? "private" : "public";

		try {
			List<LayoutSetBranch> layoutSetBranches =
				LayoutSetBranchLocalServiceUtil.getLayoutSetBranches(
					groupId, privateLayout);

			if ((layoutSetBranches == null) || layoutSetBranches.isEmpty()) {
				return;
			}
		}
		catch (SystemException se) {
			_log.error(
				">>> Error retrieving " + layoutSetBranchType +
					" layout set branches for groupId: " + groupId, se);
		}

		try {
			LayoutSetBranchLocalServiceUtil.deleteLayoutSetBranches(
				groupId, privateLayout, true);

			_log.info(
				">>> Deleted " + layoutSetBranchType +
					" layout set branches for groupId: " + groupId);
		}
		catch (PortalException | SystemException e) {
			_log.error(
				">>> Error deleting " + layoutSetBranchType +
					" layout set branch for groupId: " + groupId, e);
		}
	}

	protected void removeLayoutSetBranches(long groupId) {
		removePublicLayouSetBranches(groupId);
		removePrivateLayoutSetBranches(groupId);
	}

	@SuppressWarnings("unchecked")
	protected void removeMBThreads(long groupId) {
		DynamicQuery query = MBThreadLocalServiceUtil.dynamicQuery();
		query.add(GROUP_ID_QUERY_PROPERTY.eq(groupId));

		try {
			List<MBThread> mbThreads = MBThreadLocalServiceUtil.dynamicQuery(
				query);

			if ((mbThreads == null) || mbThreads.isEmpty()) {
				return;
			}
		}
		catch (SystemException se) {
			_log.error(
				">>> Error retrieving MB threads for groupId: " + groupId, se);
		}

		try {
			MBThreadLocalServiceUtil.deleteThreads(groupId, -1);

			_log.info(">>> Deleted MB threads for groupId: " + groupId);
		}
		catch (PortalException | SystemException e) {
			_log.error(
				">>> Error deleting MB threads for groupId: " + groupId, e);
		}
	}

	protected void removeOrphanRecordsFor(final long groupId) {
		CorruptedLayoutLocalServiceUtil.deleteCorruptedLayouts(groupId);

		removeLayoutSetBranches(groupId);
		removePortletItems(groupId);
		removeMBThreads(groupId);
		removeAssetTags(groupId);
		removeAssetVocabularies(groupId);
		removeAssetEntries(groupId);
		removeDDMContents(groupId);
		removeJournalContentSearchEntriesFor(groupId);
		removeDLFileVersions(groupId);
		removeGroupUserRelations(groupId);
	}

	protected void removePorletItem(PortletItem portletItem) {
		try {
			PortletItemLocalServiceUtil.deletePortletItem(portletItem);

			_log.info(
				">>> Deleted portlet item: " + portletItem.getPrimaryKey() +
					" for group:Id " + portletItem.getGroupId());
		}
		catch (SystemException se) {
			_log.error(
				">>> Error deleting portlet item:" +
					portletItem.getPrimaryKey() + " for groupId: " +
						portletItem.getGroupId(), se);
		}
	}

	@SuppressWarnings("unchecked")
	protected void removePortletItems(long groupId) {
		DynamicQuery query = PortletItemLocalServiceUtil.dynamicQuery();
		query.add(GROUP_ID_QUERY_PROPERTY.eq(groupId));

		List<PortletItem> portletItems = null;
		try {
			portletItems = PortletItemLocalServiceUtil.dynamicQuery(query);
		}
		catch (SystemException se) {
			_log.error(">>> Error retrieving portlet items for groupId: " +
				groupId, se);
		}

		for (PortletItem portletItem : portletItems) {
			removePorletItem(portletItem);
		}
	}

	protected void removePrivateLayoutSetBranches(long groupId) {
		removeLayoutSetBranch(groupId, true);
	}

	protected void removePublicLayouSetBranches(long groupId) {
		removeLayoutSetBranch(groupId, false);
	}

	protected void removeResourceTypePermission(
		ResourceTypePermission resourceTypePermission) {

		try {
			ResourceTypePermissionLocalServiceUtil
				.deleteResourceTypePermission(resourceTypePermission);
		}
		catch (SystemException se) {
			_log.error(
				">>> Error deleting resource type permission: " +
					resourceTypePermission.getPrimaryKey() + " for groupId: " +
						resourceTypePermission.getGroupId(), se);
		}
	}

	@SuppressWarnings("unchecked")
	protected void removeResourceTypePermissions(long groupId) {
		DynamicQuery query =
			ResourceTypePermissionLocalServiceUtil.dynamicQuery();

		query.add(GROUP_ID_QUERY_PROPERTY.eq(groupId));

		List<ResourceTypePermission> resourceTypePermissions = null;
		try {
			resourceTypePermissions =
				ResourceTypePermissionLocalServiceUtil.dynamicQuery(query);

		}
		catch (SystemException se) {
			_log.error(
				">>> Error retrieving resoure type permissions for groupId: " +
					groupId, se);
		}

		if (resourceTypePermissions == null) {
			return;
		}

		for (ResourceTypePermission resourceTypePermission
				: resourceTypePermissions) {

			removeResourceTypePermission(resourceTypePermission);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DbCleanupAction.class);

}