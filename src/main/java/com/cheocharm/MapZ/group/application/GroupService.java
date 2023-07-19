package com.cheocharm.MapZ.group.application;

import com.cheocharm.MapZ.comment.domain.repository.CommentRepository;
import com.cheocharm.MapZ.common.exception.group.DuplicatedGroupException;
import com.cheocharm.MapZ.common.exception.group.NotFoundGroupException;
import com.cheocharm.MapZ.common.exception.user.ExitGroupChiefException;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.exception.user.NotFoundUserException;
import com.cheocharm.MapZ.common.exception.usergroup.GroupMemberSizeExceedException;
import com.cheocharm.MapZ.common.exception.usergroup.NotAcceptedUserException;
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

import java.util.ArrayList;
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
        if (groupRepository.existsByGroupName(groupName)) {
            throw new DuplicatedGroupException();
        }

        final GroupEntity groupEntity = GroupEntity.of(groupName, request.getBio(), request.getChangeStatus());

        if (!multipartFile.isEmpty()) {
            groupEntity.updateGroupImageUrl(s3Utils.uploadGroupImage(multipartFile, groupEntity.getGroupUUID()));
        }

        groupRepository.save(groupEntity);

        userGroupRepository.save(
                UserGroupEntity.of(groupEntity, userEntity, InvitationStatus.ACCEPT, UserRole.CHIEF)
        );
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
    public void updateGroup(UpdateGroupRequest updateGroupRequest, MultipartFile multipartFile) {
        final UserEntity userEntity = UserThreadLocal.get();
        final UserGroupEntity userGroupEntity = validateUserRoleIsChief(updateGroupRequest.getGroupId(), userEntity.getId());

        GroupEntity groupEntity = userGroupEntity.getGroupEntity();
        if (groupEntity.getGroupName().equals(updateGroupRequest.getGroupName())) {
            throw new DuplicatedGroupException();
        }
        if (!multipartFile.isEmpty()) {
            groupEntity.updateGroupImageUrl(s3Utils.uploadGroupImage(multipartFile, groupEntity.getGroupUUID()));
        }

        groupEntity.updateGroupInfo(updateGroupRequest);
    }

    @Transactional
    public JoinGroupResultResponse joinGroup(JoinGroupRequest joinGroupRequest) {
        final UserEntity userEntity = UserThreadLocal.get();
        final GroupEntity groupEntity = groupRepository.findById(joinGroupRequest.getGroupId())
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
        validateUserRoleIsChief(request.getGroupId(), userEntity.getId());

        final UserGroupEntity targetUserGroup = userGroupRepository.findByGroupIdAndUserId(request.getGroupId(), request.getUserId())
                .orElseThrow(NotFoundUserGroupException::new);

        processInvitationStatus(request.getStatus(), targetUserGroup);
    }

    private void processInvitationStatus(boolean status, UserGroupEntity targetUserGroup) {
        if (status) {
            targetUserGroup.acceptUser();
            return;
        }
        userGroupRepository.deleteById(targetUserGroup.getId());
    }

    @Transactional
    public void exitGroup(ExitGroupRequest exitGroupRequest) {
        Long userId = UserThreadLocal.get().getId();

        final UserGroupEntity userGroupEntity = userGroupRepository.findByGroupIdAndUserId(exitGroupRequest.getGroupId(), userId)
                .orElseThrow(NotFoundUserGroupException::new);

        if (Objects.equals(userGroupEntity.getUserRole(), UserRole.CHIEF)) {
            throw new ExitGroupChiefException();
        }
        deleteGroupActivityOfUser(userId, userGroupEntity.getId());
    }

    @Transactional
    public void updateChief(ChangeChiefRequest changeChiefRequest) {
        final UserEntity userEntity = UserThreadLocal.get();
        final UserGroupEntity userGroupEntity = validateUserRoleIsChief(changeChiefRequest.getGroupId(), userEntity.getId());

        final UserGroupEntity targetUserGroupEntity = userGroupRepository.findByGroupIdAndUserId(changeChiefRequest.getGroupId(), changeChiefRequest.getUserId())
                .orElseThrow(NotFoundUserException::new);
        if (ObjectUtils.notEqual(targetUserGroupEntity.getInvitationStatus(), InvitationStatus.ACCEPT)) {
            throw new NotAcceptedUserException();
        }
        targetUserGroupEntity.updateChief(userGroupEntity, targetUserGroupEntity);
    }

    @Transactional
    public void inviteUser(InviteGroupRequest inviteGroupRequest) {
        final UserEntity userEntity = UserThreadLocal.get();

        final UserGroupEntity userGroupEntity = userGroupRepository.findByGroupIdAndUserId(inviteGroupRequest.getGroupId(), userEntity.getId())
                .orElseThrow(NotFoundUserGroupException::new);
        if (ObjectUtils.notEqual(userGroupEntity.getInvitationStatus(), InvitationStatus.ACCEPT)) {
            throw new NoPermissionUserException();
        }

        List<UserEntity> userEntityList = userRepository.getUserEntityListByUserIdList(inviteGroupRequest.getUserIdList());
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
        if (isNotUser(userEntity.getId(), userGroupEntities)) {
            throw new NoPermissionUserException();
        }

        return GroupMemberResponse.of(userGroupEntities);
    }

    @Transactional
    public void kickUser(KickUserRequest kickUserRequest) {

        UserEntity userEntity = UserThreadLocal.get();

        if (userEntity.getId().equals(kickUserRequest.getUserId())) {
            throw new SelfKickException();
        }
        validateUserRoleIsChief(kickUserRequest.getGroupId(), userEntity.getId());

        UserGroupEntity targetUserGroupEntity = userGroupRepository.findByGroupIdAndUserId(kickUserRequest.getGroupId(), kickUserRequest.getUserId())
                .orElseThrow(NotFoundUserGroupException::new);

        Long targetUserId = targetUserGroupEntity.getUserEntity().getId();
        deleteGroupActivityOfUser(targetUserId, targetUserGroupEntity.getId());
    }

    @Transactional
    public void acceptInvitation(AcceptInvitationRequest acceptInvitationRequest) {
        final UserEntity userEntity = UserThreadLocal.get();
        final Long requestGroupId = acceptInvitationRequest.getGroupId();

        final Long nowGroupMemberSize = userGroupRepository.countByGroupId(requestGroupId);
        if (nowGroupMemberSize >= GroupLimit.LIMIT_GROUP_PEOPLE.getLimitSize()) {
            throw new GroupMemberSizeExceedException();
        }

        final UserGroupEntity userGroupEntity = userGroupRepository.findByGroupIdAndUserId(requestGroupId, userEntity.getId())
                .orElseThrow(NotFoundUserGroupException::new);

        userGroupEntity.acceptUser();
    }

    @Transactional
    public void refuseInvitation(RefuseInvitationRequest refuseInvitationRequest) {
        final UserEntity userEntity = UserThreadLocal.get();

        final UserGroupEntity userGroupEntity = userGroupRepository.findByGroupIdAndUserId(refuseInvitationRequest.getGroupId(), userEntity.getId())
                .orElseThrow(NotFoundUserGroupException::new);

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

    private UserGroupEntity validateUserRoleIsChief(Long groupId, Long userId) {
        final UserGroupEntity userGroupEntity = userGroupRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(NotFoundUserException::new);

        if (Objects.equals(userGroupEntity.getUserRole(), UserRole.MEMBER)) {
            throw new NoPermissionUserException();
        }
        return userGroupEntity;
    }

    private void deleteGroupActivityOfUser(Long deleteUserId, Long deleteUserGroupEntityId) {
        List<DiaryEntity> diaryEntities = diaryRepository.findAllByUserId(deleteUserId);

        diaryLikeRepository.deleteAllByDiaryEntityList(diaryEntities);
        commentRepository.deleteAllByDiaryEntityList(diaryEntities);
        diaryImageRepository.deleteAllByDiaryEntityList(diaryEntities);
        diaryRepository.deleteAllByUserId(deleteUserId);

        userGroupRepository.deleteById(deleteUserGroupEntityId);
    }

    private boolean isNotUser(Long userId, List<UserGroupEntity> userGroupEntities) {
        ArrayList<Long> list = new ArrayList<>();
        for (UserGroupEntity userGroupEntity : userGroupEntities) {
            list.add(userGroupEntity.getUserEntity().getId());
        }
        return list.contains(userId);
    }
}
