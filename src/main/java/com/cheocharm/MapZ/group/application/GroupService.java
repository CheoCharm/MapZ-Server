package com.cheocharm.MapZ.group.application;

import com.cheocharm.MapZ.comment.domain.repository.CommentRepository;
import com.cheocharm.MapZ.common.exception.group.DuplicatedGroupException;
import com.cheocharm.MapZ.common.exception.group.NotFoundGroupException;
import com.cheocharm.MapZ.common.exception.user.ExitGroupChiefException;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.exception.user.NotFoundUserException;
import com.cheocharm.MapZ.common.exception.usergroup.NotAcceptedUserException;
import com.cheocharm.MapZ.common.exception.usergroup.NotFoundUserGroupException;
import com.cheocharm.MapZ.common.exception.usergroup.SelfKickException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.common.util.S3Utils;
import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.like.domain.repository.DiaryLikeRepository;
import com.cheocharm.MapZ.diary.domain.respository.DiaryRepository;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.group.presentation.dto.request.ChangeChiefRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.ChangeGroupInfoRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.ChangeInvitationStatusRequest;
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
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.cheocharm.MapZ.common.util.PagingUtils.applyCursorId;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyDescPageConfigBy;
import static com.cheocharm.MapZ.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.cheocharm.MapZ.common.util.PagingUtils.GROUP_SIZE;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GroupService {

    private final DiaryRepository diaryRepository;
    private final DiaryLikeRepository diaryLikeRepository;
    private final CommentRepository commentRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;

    private final S3Utils s3Utils;

    @Transactional
    public void createGroup(CreateGroupRequest createGroupRequest, MultipartFile multipartFile) {
        final UserEntity userEntity = UserThreadLocal.get();

        String groupName = createGroupRequest.getGroupName().trim();
        if (groupRepository.findByGroupName(groupName).isPresent()) {
            throw new DuplicatedGroupException();
        }

        final GroupEntity groupEntity = GroupEntity.builder()
                .groupName(groupName)
                .bio(createGroupRequest.getBio())
                .groupUUID(UUID.randomUUID().toString())
                .openStatus(createGroupRequest.getChangeStatus())
                .build();

        if (!multipartFile.isEmpty()) {
            groupEntity.updateGroupImageUrl(s3Utils.uploadGroupImage(multipartFile, groupEntity.getGroupUUID()));
        }

        groupRepository.save(groupEntity);

        userGroupRepository.save(
                UserGroupEntity.builder()
                        .userEntity(userEntity)
                        .groupEntity(groupEntity)
                        .invitationStatus(InvitationStatus.ACCEPT)
                        .userRole(UserRole.CHIEF)
                        .build()
        );
    }

    public PagingGroupListResponse getGroup(String groupName, Long cursorId, Integer page) {
        Slice<GroupEntity> content = groupRepository.findByGroupName(
                groupName,
                applyCursorId(cursorId),
                applyDescPageConfigBy(page, GROUP_SIZE, FIELD_CREATED_AT)
        );

        List<GroupEntity> groupEntityList = content.getContent();

        List<CountUserGroupVO> countUserGroupVOS = userGroupRepository.countByGroupEntity(groupEntityList);
        List<ChiefUserImageVO> chiefUserImageVOS = userGroupRepository.findChiefUserImage(groupEntityList);

        List<PagingGroupListResponse.GroupList> groupList = groupEntityList.stream()
                .map(groupEntity ->
                        PagingGroupListResponse.GroupList.builder()
                                .groupName(groupEntity.getGroupName())
                                .groupImageUrl(groupEntity.getGroupImageUrl())
                                .bio(groupEntity.getBio())
                                .createdAt(groupEntity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                                .chiefUserImage(getChiefUserImage(groupEntity, chiefUserImageVOS))
                                .count(getCount(groupEntity, countUserGroupVOS))
                                .groupId(groupEntity.getId())
                                .build()
                )
                .collect(Collectors.toList());

        return new PagingGroupListResponse(content.hasNext(), groupList);
    }

    @Transactional
    public void changeGroupInfo(ChangeGroupInfoRequest changeGroupInfoRequest, MultipartFile multipartFile) {
        final UserEntity userEntity = UserThreadLocal.get();

        final UserGroupEntity userGroupEntity = userGroupRepository.findByGroupIdAndUserId(changeGroupInfoRequest.getGroupId(), userEntity.getId())
                .orElseThrow(NotFoundUserGroupException::new);

        if (Objects.equals(userGroupEntity.getUserRole(), UserRole.MEMBER)) {
            throw new NoPermissionUserException();
        }

        GroupEntity groupEntity = userGroupEntity.getGroupEntity();
        if (groupEntity.getGroupName().equals(changeGroupInfoRequest.getGroupName())) {
            throw new DuplicatedGroupException();
        }
        if (!multipartFile.isEmpty()) {
            groupEntity.updateGroupImageUrl(s3Utils.uploadGroupImage(multipartFile, groupEntity.getGroupUUID()));
        }

        groupEntity.changeGroupInfo(changeGroupInfoRequest);
    }

    @Transactional
    public JoinGroupResultResponse joinGroup(JoinGroupRequest joinGroupRequest) {
        final UserEntity userEntity = UserThreadLocal.get();
        final GroupEntity groupEntity = groupRepository.findById(joinGroupRequest.getGroupId())
                .orElseThrow(NotFoundGroupException::new);

        Optional<UserGroupEntity> userGroupEntity = userGroupRepository.findByUserEntityAndGroupEntity(userEntity, groupEntity);

        if (userGroupEntity.isPresent()) {
            return JoinGroupResultResponse.builder()
                    .alreadyJoin(true)
                    .status(userGroupEntity.get().getInvitationStatus().getStatus())
                    .build();
        }

        userGroupRepository.save(
                UserGroupEntity.builder()
                        .groupEntity(groupEntity)
                        .userEntity(userEntity)
                        .invitationStatus(InvitationStatus.PENDING)
                        .userRole(UserRole.MEMBER)
                        .build()
        );
        return JoinGroupResultResponse.builder()
                .alreadyJoin(false)
                .status(InvitationStatus.PENDING.getStatus())
                .build();
    }

    @Transactional
    public void changeInvitationStatus(ChangeInvitationStatusRequest changeInvitationStatusRequest) {
        final UserEntity userEntity = UserThreadLocal.get();

        final UserGroupEntity userGroupEntity = userGroupRepository.findByGroupIdAndUserId(changeInvitationStatusRequest.getGroupId(), userEntity.getId())
                .orElseThrow(NotFoundUserException::new);

        if (Objects.equals(userGroupEntity.getUserRole(), UserRole.MEMBER)) {
            throw new NoPermissionUserException();
        }

        final UserGroupEntity findUserGroupEntity = userGroupRepository.findByGroupIdAndUserId(changeInvitationStatusRequest.getGroupId(), changeInvitationStatusRequest.getUserId())
                .orElseThrow(NotFoundUserGroupException::new);

        if (changeInvitationStatusRequest.getStatus()) {
            findUserGroupEntity.acceptUser();
        } else if (!changeInvitationStatusRequest.getStatus()) {
            userGroupRepository.deleteById(findUserGroupEntity.getId());
        }
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
    public void changeChief(ChangeChiefRequest changeChiefRequest) {
        final UserEntity userEntity = UserThreadLocal.get();

        final UserGroupEntity userGroupEntity = userGroupRepository.findByGroupIdAndUserId(changeChiefRequest.getGroupId(), userEntity.getId())
                .orElseThrow(NotFoundUserGroupException::new);

        if (Objects.equals(userGroupEntity.getUserRole(), UserRole.MEMBER)) {
            throw new NoPermissionUserException();
        }

        final UserGroupEntity targetUserGroupEntity = userGroupRepository.findByGroupIdAndUserId(changeChiefRequest.getGroupId(), changeChiefRequest.getUserId())
                .orElseThrow(NotFoundUserException::new);
        if (ObjectUtils.notEqual(targetUserGroupEntity.getInvitationStatus(), InvitationStatus.ACCEPT)) {
            throw new NotAcceptedUserException();
        }
        targetUserGroupEntity.changeChief(userGroupEntity, targetUserGroupEntity);
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
                    UserGroupEntity.builder()
                            .userEntity(user)
                            .groupEntity(userGroupEntity.getGroupEntity())
                            .invitationStatus(InvitationStatus.PENDING)
                            .userRole(UserRole.MEMBER)
                            .build()
            );
        }
    }

    public List<GetMyGroupResponse> searchMyGroup() {
        final UserEntity userEntity = UserThreadLocal.get();

        List<GroupEntity> groupEntities = userGroupRepository.getGroupEntityList(userEntity);

        List<CountUserGroupVO> countUserGroupVOS = userGroupRepository.countByGroupEntity(groupEntities);
        List<ChiefUserImageVO> chiefUserImageVOS = userGroupRepository.findChiefUserImage(groupEntities);

        return groupEntities.stream()
                .map(groupEntity ->
                        GetMyGroupResponse.builder()
                                .groupName(groupEntity.getGroupName())
                                .groupImageUrl(groupEntity.getGroupImageUrl())
                                .groupId(groupEntity.getId())
                                .count(getCount(groupEntity,countUserGroupVOS))
                                .chiefUserImage(getChiefUserImage(groupEntity, chiefUserImageVOS))
                                .build()
                )
                .collect(Collectors.toList());
    }

    public List<GroupMemberResponse> getMember(Long groupId) {
        UserEntity userEntity = UserThreadLocal.get();

        List<UserGroupEntity> userGroupEntities = userGroupRepository.findByGroupId(groupId);
        if (isNotUser(userEntity.getId(), userGroupEntities)) {
            throw new NoPermissionUserException();
        }

        return userGroupEntities.stream()
                .map(userGroupEntity ->
                        {
                            UserEntity user = userGroupEntity.getUserEntity();
                            return GroupMemberResponse.builder()
                                    .username(user.getUsername())
                                    .userImageUrl(user.getUserImageUrl())
                                    .userId(user.getId())
                                    .invitationStatus(userGroupEntity.getInvitationStatus())
                                    .build();
                        }
                )
                .collect(Collectors.toList());
    }

    @Transactional
    public void kickUser(KickUserRequest kickUserRequest) {

        UserEntity userEntity = UserThreadLocal.get();

        if (userEntity.getId().equals(kickUserRequest.getUserId())) {
            throw new SelfKickException();
        }
        UserGroupEntity userGroupEntity = userGroupRepository.findByGroupIdAndUserId(kickUserRequest.getGroupId(), userEntity.getId())
                .orElseThrow(NotFoundUserGroupException::new);

        if (Objects.equals(userGroupEntity.getUserRole(), UserRole.MEMBER)) {
            throw new NoPermissionUserException();
        }

        UserGroupEntity targetUserGroupEntity = userGroupRepository.findByGroupIdAndUserId(kickUserRequest.getGroupId(), kickUserRequest.getUserId())
                .orElseThrow(NotFoundUserGroupException::new);

        Long targetUserId = targetUserGroupEntity.getUserEntity().getId();
        deleteGroupActivityOfUser(targetUserId, targetUserGroupEntity.getId());

    }

    private void deleteGroupActivityOfUser(Long deleteUserId, Long deleteUserGroupEntityId) {
        List<DiaryEntity> diaryEntities = diaryRepository.findAllByUserId(deleteUserId);

        diaryLikeRepository.deleteAllByDiaryEntityList(diaryEntities);
        commentRepository.deleteAllByDiaryEntityList(diaryEntities);
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

    private Long getCount(GroupEntity groupEntity, List<CountUserGroupVO> countUserGroupVOS) {
        Long count = 1L;
        for (CountUserGroupVO countUserGroupVO : countUserGroupVOS) {
            if (groupEntity.getId().equals(countUserGroupVO.getId())) {
                count = countUserGroupVO.getCnt();
                break;
            }
        }
        return count - 1;
    }

    private String getChiefUserImage(GroupEntity groupEntity, List<ChiefUserImageVO> chiefUserImageVOS) {
        for (ChiefUserImageVO chiefUserImageVO : chiefUserImageVOS) {
            if (groupEntity.getId().equals(chiefUserImageVO.getId())) {
                return chiefUserImageVO.getChiefUserImage();
            }
        }
        return null;
    }
}