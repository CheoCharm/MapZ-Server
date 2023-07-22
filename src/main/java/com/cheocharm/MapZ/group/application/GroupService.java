package com.cheocharm.MapZ.group.application;

import com.cheocharm.MapZ.comment.domain.repository.CommentRepository;
import com.cheocharm.MapZ.common.exception.group.DuplicatedGroupException;
import com.cheocharm.MapZ.common.exception.group.NotFoundGroupException;
import com.cheocharm.MapZ.common.exception.user.ExitGroupChiefException;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.exception.user.NotFoundUserException;
import com.cheocharm.MapZ.common.exception.usergroup.GroupMemberSizeExceedException;
import com.cheocharm.MapZ.common.exception.usergroup.NotFoundUserGroupException;
import com.cheocharm.MapZ.common.exception.usergroup.SelfKickException;
import com.cheocharm.MapZ.common.image.ImageHandler;
import com.cheocharm.MapZ.common.image.ImageDirectory;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.diary.domain.repository.DiaryImageRepository;
import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.group.domain.GroupLimit;
import com.cheocharm.MapZ.group.presentation.dto.request.AcceptInvitationRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.RefuseInvitationRequest;
import com.cheocharm.MapZ.group.presentation.dto.response.MyInvitationResponse;
import com.cheocharm.MapZ.like.domain.repository.DiaryLikeRepository;
import com.cheocharm.MapZ.diary.domain.repository.DiaryRepository;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.group.presentation.dto.request.ChangeChiefRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.UpdateGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.UpdateInvitationStatusRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.CreateGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.ExitGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.InviteGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.JoinGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.KickUserRequest;
import com.cheocharm.MapZ.group.presentation.dto.response.GetMyGroupResponse;
import com.cheocharm.MapZ.group.presentation.dto.response.GroupMemberResponse;
import com.cheocharm.MapZ.group.presentation.dto.response.JoinGroupResultResponse;
import com.cheocharm.MapZ.group.presentation.dto.response.PagingGroupListResponse;
import com.cheocharm.MapZ.user.domain.User;
import com.cheocharm.MapZ.user.domain.repository.UserRepository;
import com.cheocharm.MapZ.usergroup.domain.InvitationStatus;
import com.cheocharm.MapZ.usergroup.domain.UserGroup;
import com.cheocharm.MapZ.usergroup.domain.UserRole;
import com.cheocharm.MapZ.usergroup.domain.repository.UserGroupRepository;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.ChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.CountUserGroupVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.MyInvitationVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

import static com.cheocharm.MapZ.common.util.PagingUtils.MY_INVITATION_SIZE;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyCursorId;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyDescPageConfigBy;
import static com.cheocharm.MapZ.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.cheocharm.MapZ.common.util.PagingUtils.GROUP_SIZE;

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

    private void checkDuplicateGroupName(String groupName) {
        if (groupRepository.existsByGroupName(groupName)) {
            throw new DuplicatedGroupException();
        }
    }

    private void saveGroupAndUserGroup(User user, Group group) {
        groupRepository.save(group);
        userGroupRepository.save(
                UserGroup.of(group, user, InvitationStatus.ACCEPT, UserRole.CHIEF)
        );
    }

    private Group createGroup(CreateGroupRequest request, MultipartFile multipartFile, String groupName) {
        final Group group = Group.of(groupName, request.getBio(), request.getChangeStatus());

        if (!multipartFile.isEmpty()) {
            group.updateGroupImageUrl(imageHandler.uploadImage(multipartFile, ImageDirectory.GROUP));
        }
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
        checkDuplicateGroupName(request.getGroupName(), group);
        if (!multipartFile.isEmpty()) {
            group.updateGroupImageUrl(imageHandler.uploadImage(multipartFile, ImageDirectory.GROUP));
        }

        group.updateGroupInfo(request);
    }

    private void checkDuplicateGroupName(String requestGroupName, Group group) {
        if (group.getGroupName().equals(requestGroupName)) {
            throw new DuplicatedGroupException();
        }
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

    private void validateUserExistInGroup(User user, List<UserGroup> userGroupEntities) {
        if (isGroupUser(user.getId(), userGroupEntities)) {
            return;
        }
        throw new NoPermissionUserException();
    }

    @Transactional
    public void kickUser(KickUserRequest request) {

        User user = UserThreadLocal.get();

        validateSameUser(request.getUserId(), user.getId());
        validateUserRoleIsChiefAndReturnUserGroup(request.getGroupId(), user.getId());

        UserGroup targetUserGroup = validateUserGroupAndReturn(request.getGroupId(), request.getUserId());

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
    public void refuseInvitation(RefuseInvitationRequest refuseInvitationRequest) {
        final User user = UserThreadLocal.get();
        final UserGroup userGroup = validateUserGroupAndReturn(refuseInvitationRequest.getGroupId(), user.getId());

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

    private void deleteGroupActivityOfUser(Long deleteUserId, Long deleteUserGroupEntityId) {
        List<Diary> diaryEntities = diaryRepository.findAllByUserId(deleteUserId);

        diaryLikeRepository.deleteAllByDiaries(diaryEntities);
        commentRepository.deleteAllByDiaries(diaryEntities);
        diaryImageRepository.deleteAllByDiaries(diaryEntities);
        diaryRepository.deleteAllByUserId(deleteUserId);

        userGroupRepository.deleteById(deleteUserGroupEntityId);
    }

    private boolean isGroupUser(Long userId, List<UserGroup> userGroupEntities) {
        return userGroupEntities.stream()
                .anyMatch(userGroup -> userId.equals(userGroup.getUser().getId()));
    }
}
