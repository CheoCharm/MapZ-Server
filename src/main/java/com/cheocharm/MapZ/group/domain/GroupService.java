package com.cheocharm.MapZ.group.domain;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import com.cheocharm.MapZ.common.exception.group.DuplicatedGroupException;
import com.cheocharm.MapZ.common.exception.group.NotFoundGroupException;
import com.cheocharm.MapZ.common.exception.user.ExitGroupChiefException;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.exception.user.NotFoundUserException;
import com.cheocharm.MapZ.common.exception.usergroup.NotFoundUserGroupException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.common.util.S3Utils;
import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.diary.domain.respository.DiaryRepository;
import com.cheocharm.MapZ.group.domain.dto.*;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.user.domain.repository.UserRepository;
import com.cheocharm.MapZ.usergroup.InvitationStatus;
import com.cheocharm.MapZ.usergroup.UserGroupEntity;
import com.cheocharm.MapZ.usergroup.UserRole;
import com.cheocharm.MapZ.usergroup.repository.UserGroupRepository;
import com.cheocharm.MapZ.usergroup.repository.vo.ChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.repository.vo.CountUserGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.cheocharm.MapZ.common.util.PagingUtils.*;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GroupService {

    private final DiaryRepository diaryRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;

    private final S3Utils s3Utils;

    @Transactional
    public void createGroup(CreateGroupDto createGroupDto, MultipartFile multipartFile) {
        final UserEntity userEntity = UserThreadLocal.get();

        String groupName = createGroupDto.getGroupName().trim();
        if (groupRepository.findByGroupName(groupName).isPresent()) {
            throw new DuplicatedGroupException();
        }

        final GroupEntity groupEntity = GroupEntity.builder()
                .groupName(groupName)
                .bio(createGroupDto.getBio())
                .groupUUID(UUID.randomUUID().toString())
                .openStatus(createGroupDto.getChangeStatus())
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

    public PagingGetGroupListDto getGroup(String groupName, Long cursorId, Integer page) {
        Slice<GroupEntity> content = groupRepository.findByGroupName(
                groupName,
                applyCursorId(cursorId),
                applyDescPageConfigBy(page, GROUP_SIZE, FIELD_CREATED_AT)
        );

        List<GroupEntity> groupEntityList = content.getContent();

        List<CountUserGroupVO> countUserGroupVOS = userGroupRepository.countByGroupEntity(groupEntityList);
        List<ChiefUserImageVO> chiefUserImageVOS = userGroupRepository.findChiefUserImage(groupEntityList);

        List<PagingGetGroupListDto.GroupList> groupList = groupEntityList.stream()
                .map(groupEntity ->
                        PagingGetGroupListDto.GroupList.builder()
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

        return new PagingGetGroupListDto(content.hasNext(), groupList);
    }

    @Transactional
    public void changeGroupStatus(ChangeGroupStatusDto changeGroupStatusDto) {
        final UserEntity userEntity = UserThreadLocal.get();

        List<UserGroupEntity> userGroupEntityList = userGroupRepository.fetchJoinByUserEntity(userEntity);
        final UserGroupEntity findUserGroup = findUserGroupEntity(changeGroupStatusDto.getGroup(), userGroupEntityList);

        if (findUserGroup.getUserRole().equals(UserRole.MEMBER)) {
            throw new NoPermissionUserException();
        }

        findUserGroup.getGroupEntity().changeGroupStatus(changeGroupStatusDto.getChangeStatus());
    }

    @Transactional
    public JoinGroupResultDto joinGroup(JoinGroupDto joinGroupDto) {
        final UserEntity userEntity = UserThreadLocal.get();
        final GroupEntity groupEntity = groupRepository.findByGroupName(joinGroupDto.getGroupName())
                .orElseThrow(NotFoundGroupException::new);

        Optional<UserGroupEntity> userGroupEntity = userGroupRepository.findByUserEntityAndGroupEntity(userEntity, groupEntity);

        if (userGroupEntity.isPresent()) {
            return JoinGroupResultDto.builder()
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
        return JoinGroupResultDto.builder()
                .alreadyJoin(false)
                .status(InvitationStatus.PENDING.getStatus())
                .build();
    }

    @Transactional
    public void changeInvitationStatus(ChangeInvitationStatusDto changeInvitationStatusDto) {
        final UserEntity userEntity = UserThreadLocal.get();

        final GroupEntity groupEntity = groupRepository.findByGroupName(changeInvitationStatusDto.getGroupName())
                .orElseThrow(NotFoundGroupException::new);

        final UserGroupEntity userGroupEntity = userGroupRepository.findByUserEntityAndGroupEntity(userEntity, groupEntity)
                .orElseThrow(NotFoundUserException::new);

        if (userGroupEntity.getUserRole() != UserRole.CHIEF) {
            throw new NoPermissionUserException();
        }

        final UserEntity changeUser = userRepository.findByUsername(changeInvitationStatusDto.getUsername())
                .orElseThrow(NotFoundUserException::new);

        final UserGroupEntity findUserGroupEntity = userGroupRepository.findByUserEntityAndGroupEntity(changeUser, groupEntity)
                .orElseThrow(NotFoundUserGroupException::new);

        if (changeInvitationStatusDto.getStatus()) {
            findUserGroupEntity.acceptUser();
        } else if (!changeInvitationStatusDto.getStatus()) {
            findUserGroupEntity.refuseUser();
        }
    }

    @Transactional
    public void exitGroup(ExitGroupDto exitGroupDto) {
        List<UserGroupEntity> userGroupEntityList = userGroupRepository.fetchJoinByUserEntity(UserThreadLocal.get());

        UserGroupEntity userGroupEntity = findUserGroupEntity(exitGroupDto.getGroupName(), userGroupEntityList);
        if (userGroupEntity.getUserRole() == UserRole.CHIEF) {
            throw new ExitGroupChiefException();
        }
        List<DiaryEntity> diaryEntityList = diaryRepository.findAllByUserEntityAndGroupEntity(userGroupEntity.getUserEntity(), userGroupEntity.getGroupEntity());

        userGroupEntity.delete();
        diaryEntityList.forEach(BaseEntity::delete);
    }

    @Transactional
    public void changeChief(ChangeChiefDto changeChiefDto) {
        final UserEntity userEntity = UserThreadLocal.get();
        final GroupEntity groupEntity = groupRepository.findByGroupName(changeChiefDto.getGroupName())
                .orElseThrow(NotFoundGroupException::new);

        UserGroupEntity userGroupEntity = userGroupRepository.findByUserEntityAndGroupEntity(userEntity, groupEntity)
                .orElseThrow(NotFoundUserGroupException::new);
        if (userGroupEntity.getUserRole() == UserRole.MEMBER) {
            throw new NoPermissionUserException();
        }
        final UserEntity targetUserEntity = userRepository.findByUsername(changeChiefDto.getTargetUsername())
                .orElseThrow(NotFoundUserException::new);

        final UserGroupEntity targetUserGroupEntity = userGroupRepository.findByUserEntityAndGroupEntity(targetUserEntity, groupEntity)
                .orElseThrow(NotFoundUserException::new);

        targetUserGroupEntity.changeChief(userGroupEntity, targetUserGroupEntity);
    }

    @Transactional
    public void inviteUser(InviteUserListDto inviteUserListDto) {
        final UserEntity userEntity = UserThreadLocal.get();

        final GroupEntity groupEntity = groupRepository.findByGroupName(inviteUserListDto.getGroupName())
                .orElseThrow(NotFoundGroupException::new);

        final UserGroupEntity userGroupEntity = userGroupRepository.findByUserEntityAndGroupEntity(userEntity, groupEntity)
                .orElseThrow(NotFoundUserGroupException::new);
        if (userGroupEntity.getInvitationStatus() != InvitationStatus.ACCEPT) {
            throw new NoPermissionUserException();
        }

        List<UserEntity> userEntityList = userRepository.getUserEntityListByUsernameList(inviteUserListDto.getUsernameList());
        for (UserEntity user : userEntityList) {
            userGroupRepository.save(
                    UserGroupEntity.builder()
                            .userEntity(user)
                            .groupEntity(groupEntity)
                            .invitationStatus(InvitationStatus.PENDING)
                            .userRole(UserRole.MEMBER)
                            .build()
            );
        }
    }

    public List<GetGroupListDto> searchMyGroup() {
        final UserEntity userEntity = UserThreadLocal.get();

        List<GroupEntity> groupEntities = userGroupRepository.getGroupEntityList(userEntity);

        List<CountUserGroupVO> countUserGroupVOS = userGroupRepository.countByGroupEntity(groupEntities);
        List<ChiefUserImageVO> chiefUserImageVOS = userGroupRepository.findChiefUserImage(groupEntities);

        return groupEntities.stream()
                .map(groupEntity ->
                        GetGroupListDto.builder()
                                .groupName(groupEntity.getGroupName())
                                .groupImageUrl(groupEntity.getGroupImageUrl())
                                .count(getCount(groupEntity,countUserGroupVOS))
                                .chiefUserImage(getChiefUserImage(groupEntity, chiefUserImageVOS))
                                .build()
                )
                .collect(Collectors.toList());
    }

    private Long getCount(GroupEntity groupEntity, List<CountUserGroupVO> countUserGroupVOS) {
        Long count = 0L;
        for (CountUserGroupVO countUserGroupVO : countUserGroupVOS) {
            if (groupEntity.getId().equals(countUserGroupVO.getId())) {
                count = countUserGroupVO.getCnt();
                break;
            }
        }
        if (count > 4) {
            return count - 4;
        }
        return 0L;
    }

    private String getChiefUserImage(GroupEntity groupEntity, List<ChiefUserImageVO> chiefUserImageVOS) {
        for (ChiefUserImageVO chiefUserImageVO : chiefUserImageVOS) {
            if (groupEntity.getId().equals(chiefUserImageVO.getId())) {
                return chiefUserImageVO.getChiefUserImage();
            }
        }
        return null;
    }

    private UserGroupEntity findUserGroupEntity(String groupName, List<UserGroupEntity> userGroupEntityList) {
        return userGroupEntityList.stream()
                .filter(userGroupEntity -> userGroupEntity.getGroupEntity()
                        .getGroupName().equals(groupName))
                .findAny()
                .orElseThrow(NotFoundGroupException::new);
    }
}
