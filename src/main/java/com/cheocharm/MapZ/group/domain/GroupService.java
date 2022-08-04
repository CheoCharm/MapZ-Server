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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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

        if (groupRepository.findByGroupName(createGroupDto.getGroupName()).isPresent()) {
            throw new DuplicatedGroupException();
        }

        final GroupEntity groupEntity = GroupEntity.builder()
                .groupName(createGroupDto.getGroupName().trim())
                .bio(createGroupDto.getBio())
                .groupUUID(UUID.randomUUID().toString())
                .openStatus(createGroupDto.getChangeStatus())
                .build();

        if (!multipartFile.isEmpty()) {
            groupEntity.updateGroupImageUrl(s3Utils.uploadGroupImage(multipartFile, groupEntity.getGroupUUID()));
        }

        userGroupRepository.save(
                UserGroupEntity.builder()
                        .userEntity(userEntity)
                        .groupEntity(groupEntity)
                        .invitationStatus(InvitationStatus.ACCEPT)
                        .userRole(UserRole.CHIEF)
                        .build()
        );
        groupRepository.save(groupEntity);
    }

    public GetGroupListDto getGroup(SearchGroupDto searchGroupDto) {
        UserEntity userEntity = UserThreadLocal.get();

        Slice<UserGroupEntity> content = userGroupRepository.fetchByUserEntityAndSearchNameAndOrderByUserName(
                userEntity,
                searchGroupDto.getSearchName(),
                applyPageConfigBy(searchGroupDto.getPage(), GROUP_SIZE)
        );

        List<UserGroupEntity> userGroupEntityList = content.getContent();

        List<GetGroupListDto.GroupList> GroupList = userGroupEntityList.stream()
                .map(userGroupEntity ->
                        GetGroupListDto.GroupList.builder()
                                .groupName(userGroupEntity.getGroupEntity().getGroupName())
                                .groupImageUrl(userGroupEntity.getGroupEntity().getGroupImageUrl())
                                .userImageUrlList(userGroupRepository.findUserImage(userGroupEntity.getGroupEntity()))
                                .count(getCount(userGroupEntity))
                                .build()
                )
                .collect(Collectors.toList());

        return GetGroupListDto.builder()
                .hasNextPage(content.hasNext())
                .groupList(GroupList)
                .build();
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
    public void joinGroup(JoinGroupDto joinGroupDto) {
        final UserEntity userEntity = UserThreadLocal.get();

        final GroupEntity groupEntity = groupRepository.findByGroupName(joinGroupDto.getGroupName())
                .orElseThrow(NotFoundGroupException::new);

        userGroupRepository.save(
                UserGroupEntity.builder()
                        .groupEntity(groupEntity)
                        .userEntity(userEntity)
                        .invitationStatus(InvitationStatus.PENDING)
                        .userRole(UserRole.MEMBER)
                        .build()
        );

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

    private int getCount(UserGroupEntity userGroupEntity) {
        int count = userGroupRepository.countByGroupEntity(userGroupEntity.getGroupEntity());
        if (count > 4) {
            return count - 4;
        }
        return 0;
    }

    private UserGroupEntity findUserGroupEntity(String groupName, List<UserGroupEntity> userGroupEntityList) {
        return userGroupEntityList.stream()
                .filter(userGroupEntity -> userGroupEntity.getGroupEntity()
                        .getGroupName().equals(groupName))
                .findAny()
                .orElseThrow(NotFoundGroupException::new);
    }
}
