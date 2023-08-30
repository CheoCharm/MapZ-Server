package com.mapz.api.group.application;

import com.mapz.api.common.exception.user.NotFoundUserException;
import com.mapz.domain.domains.comment.repository.CommentRepository;
import com.mapz.api.common.exception.group.DuplicatedGroupException;
import com.mapz.api.common.exception.group.NotFoundGroupException;
import com.mapz.api.common.exception.user.ExitGroupChiefException;
import com.mapz.api.common.exception.user.NoPermissionUserException;
import com.mapz.api.common.exception.usergroup.GroupMemberSizeExceedException;
import com.mapz.api.common.exception.usergroup.NotFoundUserGroupException;
import com.mapz.api.common.exception.usergroup.SelfKickException;
import com.mapz.api.common.image.ImageHandler;
import com.mapz.api.common.image.ImageDirectory;
import com.mapz.api.common.interceptor.UserThreadLocal;
import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.repository.DiaryImageRepository;
import com.mapz.domain.domains.group.entity.Group;
import com.mapz.api.group.domain.GroupLimit;
import com.mapz.api.group.presentation.dto.request.AcceptInvitationRequest;
import com.mapz.api.group.presentation.dto.response.MyInvitationResponse;
import com.mapz.domain.domains.like.repository.DiaryLikeRepository;
import com.mapz.domain.domains.diary.repository.DiaryRepository;
import com.mapz.domain.domains.group.repository.GroupRepository;
import com.mapz.api.group.presentation.dto.request.ChangeChiefRequest;
import com.mapz.api.group.presentation.dto.request.UpdateGroupRequest;
import com.mapz.api.group.presentation.dto.request.UpdateInvitationStatusRequest;
import com.mapz.api.group.presentation.dto.request.CreateGroupRequest;
import com.mapz.api.group.presentation.dto.request.ExitGroupRequest;
import com.mapz.api.group.presentation.dto.request.InviteGroupRequest;
import com.mapz.api.group.presentation.dto.request.JoinGroupRequest;
import com.mapz.api.group.presentation.dto.response.GetMyGroupResponse;
import com.mapz.api.group.presentation.dto.response.GroupMemberResponse;
import com.mapz.api.group.presentation.dto.response.JoinGroupResultResponse;
import com.mapz.api.group.presentation.dto.response.PagingGroupListResponse;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.user.repository.UserRepository;
import com.mapz.domain.domains.usergroup.enums.InvitationStatus;
import com.mapz.domain.domains.usergroup.entity.UserGroup;
import com.mapz.domain.domains.usergroup.enums.UserRole;
import com.mapz.domain.domains.usergroup.repository.UserGroupRepository;
import com.mapz.domain.domains.usergroup.vo.ChiefUserImageVO;
import com.mapz.domain.domains.usergroup.vo.CountUserGroupVO;
import com.mapz.domain.domains.usergroup.vo.MyInvitationVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

import static com.mapz.api.common.util.PagingUtils.MY_INVITATION_SIZE;
import static com.mapz.api.common.util.PagingUtils.applyCursorId;
import static com.mapz.api.common.util.PagingUtils.applyDescPageConfigBy;
import static com.mapz.api.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.mapz.api.common.util.PagingUtils.GROUP_SIZE;

@RequiredArgsConstructor
@Service
public class GroupService {

    private final DiaryRepository diaryRepository;
    private final DiaryLikeRepository diaryLikeRepository;
    private final DiaryImageRepository diaryImageRepository;
    private final CommentRepository commentRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;
    private final ImageHandler imageHandler;

    @Transactional
    public void createGroup(CreateGroupRequest request, MultipartFile multipartFile) {
        final User user = UserThreadLocal.get();
        String groupName = request.getGroupName().trim();

        checkDuplicateGroupName(groupName);

        final Group group = createGroup(request, multipartFile, groupName);
        saveGroupAndUserGroup(user, group);
    }

    private void saveGroupAndUserGroup(User user, Group group) {
        groupRepository.save(group);
        userGroupRepository.save(
                UserGroup.of(group, user, InvitationStatus.ACCEPT, UserRole.CHIEF)
        );
    }

    private Group createGroup(CreateGroupRequest request, MultipartFile multipartFile, String groupName) {
        final Group group = Group.of(groupName, request.getBio(), request.getChangeStatus());
        updateGroupImage(multipartFile, group);
        return group;
    }

    @Transactional(readOnly = true)
    public PagingGroupListResponse getGroup(String groupName, Long cursorId, Integer page) {
        Slice<Group> content = groupRepository.findByGroupName(
                groupName,
                applyCursorId(cursorId),
                applyDescPageConfigBy(page, GROUP_SIZE, FIELD_CREATED_AT)
        );

        List<Group> groupEntities = content.getContent();

        List<CountUserGroupVO> countUserGroupVOS = userGroupRepository.countByGroup(groupEntities);
        List<ChiefUserImageVO> chiefUserImageVOS = userGroupRepository.findChiefUserImage(groupEntities);

        return PagingGroupListResponse.of(groupEntities, content.hasNext(), countUserGroupVOS, chiefUserImageVOS);
    }

    @Transactional
    public void updateGroup(UpdateGroupRequest request, MultipartFile multipartFile) {
        final User user = UserThreadLocal.get();
        final UserGroup userGroup = validateUserRoleIsChiefAndReturnUserGroup(request.getGroupId(), user.getId());

        Group group = userGroup.getGroup();
        checkDuplicateGroupName(request.getGroupName().trim());
        updateGroupImage(multipartFile, group);

        group.updateGroupInfo(request.getGroupName(), request.getBio(), request.getIsOpenGroup());
    }

    @Transactional
    public JoinGroupResultResponse joinGroup(JoinGroupRequest request) {
        final User user = UserThreadLocal.get();
        final Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(NotFoundGroupException::new);

        return userGroupRepository.findByGroupIdAndUserId(group.getId(), user.getId())
                .map(this::buildResponseFromExistingUserGroup)
                .orElseGet(()-> buildResponseFromNewUserGroup(user, group));
    }

    private JoinGroupResultResponse buildResponseFromNewUserGroup(User user, Group group) {
        final UserGroup userGroup = userGroupRepository.save(
                UserGroup.of(group, user, InvitationStatus.PENDING, UserRole.MEMBER)
        );
        return JoinGroupResultResponse.builder()
                .alreadyJoin(false)
                .status(userGroup.getInvitationStatus().getStatus())
                .build();
    }

    private JoinGroupResultResponse buildResponseFromExistingUserGroup(UserGroup userGroup) {
        return JoinGroupResultResponse.builder()
                .alreadyJoin(true)
                .status(userGroup.getInvitationStatus().getStatus())
                .build();
    }

    @Transactional
    public void updateInvitationStatus(UpdateInvitationStatusRequest request) {
        final User user = UserThreadLocal.get();
        validateUserRoleIsChiefAndReturnUserGroup(request.getGroupId(), user.getId());

        final UserGroup targetUserGroup = validateUserGroupAndReturn(request.getGroupId(), request.getUserId());

        processInvitationStatus(request.getStatus(), targetUserGroup);
    }

    private void processInvitationStatus(boolean status, UserGroup targetUserGroup) {
        if (status) {
            targetUserGroup.updateInvitationStatus();
            return;
        }
        userGroupRepository.deleteById(targetUserGroup.getId());
    }

    @Transactional
    public void exitGroup(ExitGroupRequest request) {
        Long userId = UserThreadLocal.get().getId();

        final UserGroup userGroup = validateUserGroupAndReturn(request.getGroupId(), userId);

        if (Objects.equals(userGroup.getUserRole(), UserRole.CHIEF)) {
            throw new ExitGroupChiefException();
        }
        deleteGroupActivityOfUser(userId, userGroup.getId());
    }

    @Transactional
    public void updateChief(ChangeChiefRequest request) {
        final User user = UserThreadLocal.get();

        final UserGroup userGroup = validateUserRoleIsChiefAndReturnUserGroup(request.getGroupId(), user.getId());
        final UserGroup targetUserGroup = validateUserGroupAndReturn(request.getGroupId(), request.getUserId());
        validateInvitationStatus(targetUserGroup.getInvitationStatus());

        targetUserGroup.updateChief(userGroup, targetUserGroup);
    }

    @Transactional
    public void inviteUser(InviteGroupRequest request) {
        final User user = UserThreadLocal.get();

        final UserGroup userGroup = validateUserGroupAndReturn(request.getGroupId(), user.getId());
        validateInvitationStatus(userGroup.getInvitationStatus());

        saveInvitedUser(request, userGroup);
    }

    private void saveInvitedUser(InviteGroupRequest request, UserGroup userGroup) {
        List<User> userList = userRepository.getUserListByUserIdList(request.getUserIdList());
        for (User user : userList) {
            userGroupRepository.save(
                    UserGroup.of(userGroup.getGroup(), user, InvitationStatus.PENDING, UserRole.MEMBER)
            );
        }
    }

    @Transactional(readOnly = true)
    public List<GetMyGroupResponse> searchMyGroup() {
        final User user = UserThreadLocal.get();

        List<Group> groupEntities = userGroupRepository.getGroups(user);

        List<CountUserGroupVO> countUserGroupVOS = userGroupRepository.countByGroup(groupEntities);
        List<ChiefUserImageVO> chiefUserImageVOS = userGroupRepository.findChiefUserImage(groupEntities);

        return GetMyGroupResponse.of(groupEntities, countUserGroupVOS, chiefUserImageVOS);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getMember(Long groupId) {
        User user = UserThreadLocal.get();

        List<UserGroup> userGroupEntities = userGroupRepository.findByGroupId(groupId);
        validateUserExistInGroup(user, userGroupEntities);

        return GroupMemberResponse.of(userGroupEntities);
    }

    private void validateUserExistInGroup(User user, List<UserGroup> userGroups) {
        if (isGroupUser(user.getId(), userGroups)) {
            return;
        }
        throw new NoPermissionUserException();
    }

    @Transactional
    public void kickUser(Long groupId, Long userId) {

        User user = UserThreadLocal.get();

        validateSameUser(userId, user.getId());
        validateUserRoleIsChiefAndReturnUserGroup(groupId, user.getId());

        UserGroup targetUserGroup = validateUserGroupAndReturn(groupId, userId);

        Long targetUserId = targetUserGroup.getUser().getId();
        deleteGroupActivityOfUser(targetUserId, targetUserGroup.getId());
    }

    private void validateSameUser(Long kickUserId, Long userId) {
        if (kickUserId.equals(userId)) {
            throw new SelfKickException();
        }
    }

    @Transactional
    public void acceptInvitation(AcceptInvitationRequest request) {
        final User user = UserThreadLocal.get();

        final Long requestGroupId = request.getGroupId();
        checkGroupMemberExceed(requestGroupId);

        acceptUser(user, requestGroupId);
    }

    private void acceptUser(User user, Long requestGroupId) {
        final UserGroup userGroup = validateUserGroupAndReturn(requestGroupId, user.getId());
        userGroup.updateInvitationStatus();
    }

    private void checkGroupMemberExceed(Long requestGroupId) {
        final Long nowGroupMemberSize = userGroupRepository.countByGroupId(requestGroupId);
        if (nowGroupMemberSize >= GroupLimit.LIMIT_GROUP_PEOPLE.getLimitSize()) {
            throw new GroupMemberSizeExceedException();
        }
    }

    @Transactional
    public void refuseInvitation(Long groupId) {
        final User user = UserThreadLocal.get();
        final UserGroup userGroup = validateUserGroupAndReturn(groupId, user.getId());

        userGroupRepository.delete(userGroup);
    }

    @Transactional(readOnly = true)
    public MyInvitationResponse getInvitation(Long cursorId, Integer page) {
        final User user = UserThreadLocal.get();
        final Slice<MyInvitationVO> invitationSlice = userGroupRepository.getInvitationSlice(
                user.getId(),
                applyCursorId(cursorId),
                applyDescPageConfigBy(page, MY_INVITATION_SIZE, FIELD_CREATED_AT)
        );
        final List<MyInvitationVO> myInvitations = invitationSlice.getContent();

        return MyInvitationResponse.of(myInvitations, invitationSlice.hasNext());
    }

    private void updateGroupImage(MultipartFile multipartFile, Group group) {
        if (!multipartFile.isEmpty()) {
            group.updateGroupImageUrl(imageHandler.uploadImage(multipartFile, ImageDirectory.GROUP));
        }
    }

    private void checkDuplicateGroupName(String groupName) {
        if (groupRepository.existsByGroupName(groupName)) {
            throw new DuplicatedGroupException();
        }
    }

    private UserGroup validateUserGroupAndReturn(Long groupId, Long userId) {
        return userGroupRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(NotFoundUserGroupException::new);
    }

    private void validateInvitationStatus(InvitationStatus targetUserInvitationStatus) {
        if (ObjectUtils.notEqual(targetUserInvitationStatus, InvitationStatus.ACCEPT)) {
            throw new NoPermissionUserException();
        }
    }

    private UserGroup validateUserRoleIsChiefAndReturnUserGroup(Long groupId, Long userId) {
        final UserGroup userGroup = userGroupRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(NotFoundUserException::new);

        validateUserRoleIsChief(userGroup);
        return userGroup;
    }

    private void validateUserRoleIsChief(UserGroup userGroup) {
        if (Objects.equals(userGroup.getUserRole(), UserRole.MEMBER)) {
            throw new NoPermissionUserException();
        }
    }

    private void deleteGroupActivityOfUser(Long deleteUserId, Long deleteUserGroupId) {
        List<Diary> diaries = diaryRepository.findAllByUserId(deleteUserId);

        diaryLikeRepository.deleteAllByDiaries(diaries);
        commentRepository.deleteAllByDiaries(diaries);
        diaryImageRepository.deleteAllByDiaries(diaries);
        diaryRepository.deleteAllByUserId(deleteUserId);

        userGroupRepository.deleteById(deleteUserGroupId);
    }

    private boolean isGroupUser(Long userId, List<UserGroup> userGroups) {
        return userGroups.stream()
                .anyMatch(userGroup -> userId.equals(userGroup.getUser().getId()));
    }
}
