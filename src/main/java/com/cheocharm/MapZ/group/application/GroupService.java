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
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.common.util.S3Utils;
import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.diary.domain.repository.DiaryImageRepository;
import com.cheocharm.MapZ.group.domain.GroupLimit;
import com.cheocharm.MapZ.group.presentation.dto.request.AcceptInvitationRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.RefuseInvitationRequest;
import com.cheocharm.MapZ.group.presentation.dto.response.MyInvitationResponse;
import com.cheocharm.MapZ.like.domain.repository.DiaryLikeRepository;
import com.cheocharm.MapZ.diary.domain.repository.DiaryRepository;
import com.cheocharm.MapZ.group.domain.GroupEntity;
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
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.user.domain.repository.UserRepository;
import com.cheocharm.MapZ.usergroup.domain.InvitationStatus;
import com.cheocharm.MapZ.usergroup.domain.UserGroupEntity;
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

    private final S3Utils s3Utils;

    @Transactional
    public void createGroup(CreateGroupRequest request, MultipartFile multipartFile) {
        final UserEntity userEntity = UserThreadLocal.get();
        String groupName = request.getGroupName().trim();

        checkDuplicateGroupName(groupName);

        final GroupEntity groupEntity = createGroup(request, multipartFile, groupName);
        saveGroupAndUserGroup(userEntity, groupEntity);
    }

    private void checkDuplicateGroupName(String groupName) {
        if (groupRepository.existsByGroupName(groupName)) {
            throw new DuplicatedGroupException();
        }
    }

    private void saveGroupAndUserGroup(UserEntity userEntity, GroupEntity groupEntity) {
        groupRepository.save(groupEntity);
        userGroupRepository.save(
                UserGroupEntity.of(groupEntity, userEntity, InvitationStatus.ACCEPT, UserRole.CHIEF)
        );
    }

    private GroupEntity createGroup(CreateGroupRequest request, MultipartFile multipartFile, String groupName) {
        final GroupEntity groupEntity = GroupEntity.of(groupName, request.getBio(), request.getChangeStatus());

        if (!multipartFile.isEmpty()) {
            groupEntity.updateGroupImageUrl(s3Utils.uploadGroupImage(multipartFile, groupEntity.getGroupUUID()));
        }
        return groupEntity;
    }

    @Transactional(readOnly = true)
    public PagingGroupListResponse getGroup(String groupName, Long cursorId, Integer page) {
        Slice<GroupEntity> content = groupRepository.findByGroupName(
                groupName,
                applyCursorId(cursorId),
                applyDescPageConfigBy(page, GROUP_SIZE, FIELD_CREATED_AT)
        );

        List<GroupEntity> groupEntities = content.getContent();

        List<CountUserGroupVO> countUserGroupVOS = userGroupRepository.countByGroupEntity(groupEntities);
        List<ChiefUserImageVO> chiefUserImageVOS = userGroupRepository.findChiefUserImage(groupEntities);

        return PagingGroupListResponse.of(groupEntities, content.hasNext(), countUserGroupVOS, chiefUserImageVOS);
    }

    @Transactional
    public void updateGroup(UpdateGroupRequest request, MultipartFile multipartFile) {
        final UserEntity userEntity = UserThreadLocal.get();
        final UserGroupEntity userGroupEntity = validateUserRoleIsChiefAndReturnUserGroup(request.getGroupId(), userEntity.getId());

        GroupEntity groupEntity = userGroupEntity.getGroupEntity();
        checkDuplicateGroupName(request.getGroupName(), groupEntity);
        if (!multipartFile.isEmpty()) {
            groupEntity.updateGroupImageUrl(s3Utils.uploadGroupImage(multipartFile, groupEntity.getGroupUUID()));
        }

        groupEntity.updateGroupInfo(request);
    }

    private void checkDuplicateGroupName(String requestGroupName, GroupEntity groupEntity) {
        if (groupEntity.getGroupName().equals(requestGroupName)) {
            throw new DuplicatedGroupException();
        }
    }

    @Transactional
    public JoinGroupResultResponse joinGroup(JoinGroupRequest request) {
        final UserEntity userEntity = UserThreadLocal.get();
        final GroupEntity groupEntity = groupRepository.findById(request.getGroupId())
                .orElseThrow(NotFoundGroupException::new);

        return userGroupRepository.findByGroupIdAndUserId(groupEntity.getId(), userEntity.getId())
                .map(this::buildResponseFromExistingUserGroup)
                .orElseGet(()-> buildResponseFromNewUserGroup(userEntity, groupEntity));
    }

    private JoinGroupResultResponse buildResponseFromNewUserGroup(UserEntity userEntity, GroupEntity groupEntity) {
        final UserGroupEntity userGroup = userGroupRepository.save(
                UserGroupEntity.of(groupEntity, userEntity, InvitationStatus.PENDING, UserRole.MEMBER)
        );
        return JoinGroupResultResponse.builder()
                .alreadyJoin(false)
                .status(userGroup.getInvitationStatus().getStatus())
                .build();
    }

    private JoinGroupResultResponse buildResponseFromExistingUserGroup(UserGroupEntity userGroupEntity) {
        return JoinGroupResultResponse.builder()
                .alreadyJoin(true)
                .status(userGroupEntity.getInvitationStatus().getStatus())
                .build();
    }

    @Transactional
    public void updateInvitationStatus(UpdateInvitationStatusRequest request) {
        final UserEntity userEntity = UserThreadLocal.get();
        validateUserRoleIsChiefAndReturnUserGroup(request.getGroupId(), userEntity.getId());

        final UserGroupEntity targetUserGroup = validateUserGroupAndReturn(request.getGroupId(), request.getUserId());

        processInvitationStatus(request.getStatus(), targetUserGroup);
    }

    private void processInvitationStatus(boolean status, UserGroupEntity targetUserGroup) {
        if (status) {
            targetUserGroup.updateInvitationStatus();
            return;
        }
        userGroupRepository.deleteById(targetUserGroup.getId());
    }

    @Transactional
    public void exitGroup(ExitGroupRequest request) {
        Long userId = UserThreadLocal.get().getId();

        final UserGroupEntity userGroupEntity = validateUserGroupAndReturn(request.getGroupId(), userId);

        if (Objects.equals(userGroupEntity.getUserRole(), UserRole.CHIEF)) {
            throw new ExitGroupChiefException();
        }
        deleteGroupActivityOfUser(userId, userGroupEntity.getId());
    }

    @Transactional
    public void updateChief(ChangeChiefRequest request) {
        final UserEntity userEntity = UserThreadLocal.get();

        final UserGroupEntity userGroupEntity = validateUserRoleIsChiefAndReturnUserGroup(request.getGroupId(), userEntity.getId());
        final UserGroupEntity targetUserGroupEntity = validateUserGroupAndReturn(request.getGroupId(), request.getUserId());
        validateInvitationStatus(targetUserGroupEntity.getInvitationStatus());

        targetUserGroupEntity.updateChief(userGroupEntity, targetUserGroupEntity);
    }

    @Transactional
    public void inviteUser(InviteGroupRequest request) {
        final UserEntity userEntity = UserThreadLocal.get();

        final UserGroupEntity userGroupEntity = validateUserGroupAndReturn(request.getGroupId(), userEntity.getId());
        validateInvitationStatus(userGroupEntity.getInvitationStatus());

        saveInvitedUser(request, userGroupEntity);
    }

    private void saveInvitedUser(InviteGroupRequest request, UserGroupEntity userGroupEntity) {
        List<UserEntity> userEntityList = userRepository.getUserEntityListByUserIdList(request.getUserIdList());
        for (UserEntity user : userEntityList) {
            userGroupRepository.save(
                    UserGroupEntity.of(userGroupEntity.getGroupEntity(), user, InvitationStatus.PENDING, UserRole.MEMBER)
            );
        }
    }

    @Transactional(readOnly = true)
    public List<GetMyGroupResponse> searchMyGroup() {
        final UserEntity userEntity = UserThreadLocal.get();

        List<GroupEntity> groupEntities = userGroupRepository.getGroupEntityList(userEntity);

        List<CountUserGroupVO> countUserGroupVOS = userGroupRepository.countByGroupEntity(groupEntities);
        List<ChiefUserImageVO> chiefUserImageVOS = userGroupRepository.findChiefUserImage(groupEntities);

        return GetMyGroupResponse.of(groupEntities, countUserGroupVOS, chiefUserImageVOS);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getMember(Long groupId) {
        UserEntity userEntity = UserThreadLocal.get();

        List<UserGroupEntity> userGroupEntities = userGroupRepository.findByGroupId(groupId);
        validateUserExistInGroup(userEntity, userGroupEntities);

        return GroupMemberResponse.of(userGroupEntities);
    }

    private void validateUserExistInGroup(UserEntity userEntity, List<UserGroupEntity> userGroupEntities) {
        if (isGroupUser(userEntity.getId(), userGroupEntities)) {
            return;
        }
        throw new NoPermissionUserException();
    }

    @Transactional
    public void kickUser(KickUserRequest request) {

        UserEntity userEntity = UserThreadLocal.get();

        validateSameUser(request.getUserId(), userEntity.getId());
        validateUserRoleIsChiefAndReturnUserGroup(request.getGroupId(), userEntity.getId());

        UserGroupEntity targetUserGroupEntity = validateUserGroupAndReturn(request.getGroupId(), request.getUserId());

        Long targetUserId = targetUserGroupEntity.getUserEntity().getId();
        deleteGroupActivityOfUser(targetUserId, targetUserGroupEntity.getId());
    }

    private void validateSameUser(Long kickUserId, Long userId) {
        if (kickUserId.equals(userId)) {
            throw new SelfKickException();
        }
    }

    @Transactional
    public void acceptInvitation(AcceptInvitationRequest request) {
        final UserEntity userEntity = UserThreadLocal.get();

        final Long requestGroupId = request.getGroupId();
        checkGroupMemberExceed(requestGroupId);

        acceptUser(userEntity, requestGroupId);
    }

    private void acceptUser(UserEntity userEntity, Long requestGroupId) {
        final UserGroupEntity userGroupEntity = validateUserGroupAndReturn(requestGroupId, userEntity.getId());
        userGroupEntity.updateInvitationStatus();
    }

    private void checkGroupMemberExceed(Long requestGroupId) {
        final Long nowGroupMemberSize = userGroupRepository.countByGroupId(requestGroupId);
        if (nowGroupMemberSize >= GroupLimit.LIMIT_GROUP_PEOPLE.getLimitSize()) {
            throw new GroupMemberSizeExceedException();
        }
    }

    @Transactional
    public void refuseInvitation(RefuseInvitationRequest refuseInvitationRequest) {
        final UserEntity userEntity = UserThreadLocal.get();
        final UserGroupEntity userGroupEntity = validateUserGroupAndReturn(refuseInvitationRequest.getGroupId(), userEntity.getId());

        userGroupRepository.delete(userGroupEntity);
    }

    @Transactional(readOnly = true)
    public MyInvitationResponse getInvitation(Long cursorId, Integer page) {
        final UserEntity userEntity = UserThreadLocal.get();
        final Slice<MyInvitationVO> invitationSlice = userGroupRepository.getInvitationSlice(
                userEntity.getId(),
                applyCursorId(cursorId),
                applyDescPageConfigBy(page, MY_INVITATION_SIZE, FIELD_CREATED_AT)
        );
        final List<MyInvitationVO> myInvitations = invitationSlice.getContent();

        return MyInvitationResponse.of(myInvitations, invitationSlice.hasNext());
    }

    private UserGroupEntity validateUserGroupAndReturn(Long groupId, Long userId) {
        return userGroupRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(NotFoundUserGroupException::new);
    }

    private void validateInvitationStatus(InvitationStatus targetUserInvitationStatus) {
        if (ObjectUtils.notEqual(targetUserInvitationStatus, InvitationStatus.ACCEPT)) {
            throw new NoPermissionUserException();
        }
    }

    private UserGroupEntity validateUserRoleIsChiefAndReturnUserGroup(Long groupId, Long userId) {
        final UserGroupEntity userGroupEntity = userGroupRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(NotFoundUserException::new);

        validateUserRoleIsChief(userGroupEntity);
        return userGroupEntity;
    }

    private void validateUserRoleIsChief(UserGroupEntity userGroupEntity) {
        if (Objects.equals(userGroupEntity.getUserRole(), UserRole.MEMBER)) {
            throw new NoPermissionUserException();
        }
    }

    private void deleteGroupActivityOfUser(Long deleteUserId, Long deleteUserGroupEntityId) {
        List<DiaryEntity> diaryEntities = diaryRepository.findAllByUserId(deleteUserId);

        diaryLikeRepository.deleteAllByDiaryEntityList(diaryEntities);
        commentRepository.deleteAllByDiaryEntityList(diaryEntities);
        diaryImageRepository.deleteAllByDiaryEntityList(diaryEntities);
        diaryRepository.deleteAllByUserId(deleteUserId);

        userGroupRepository.deleteById(deleteUserGroupEntityId);
    }

    private boolean isGroupUser(Long userId, List<UserGroupEntity> userGroupEntities) {
        return userGroupEntities.stream()
                .anyMatch(userGroup -> userId.equals(userGroup.getUserEntity().getId()));
    }
}
