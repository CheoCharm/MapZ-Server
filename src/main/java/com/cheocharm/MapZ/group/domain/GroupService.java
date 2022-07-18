package com.cheocharm.MapZ.group.domain;

import com.cheocharm.MapZ.common.exception.group.DuplicatedGroupException;
import com.cheocharm.MapZ.common.exception.group.NotFoundGroupException;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.common.util.S3Utils;
import com.cheocharm.MapZ.group.domain.dto.ChangeGroupStatusDto;
import com.cheocharm.MapZ.group.domain.dto.CreateGroupDto;
import com.cheocharm.MapZ.group.domain.dto.GetGroupListDto;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.InvitationStatus;
import com.cheocharm.MapZ.usergroup.UserGroupEntity;
import com.cheocharm.MapZ.usergroup.UserRole;
import com.cheocharm.MapZ.usergroup.repository.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;

    private final S3Utils s3Utils;

    @Transactional
    public void createGroup(CreateGroupDto createGroupDto, MultipartFile multipartFile) {
        final UserEntity userEntity = UserThreadLocal.get();

        if (groupRepository.findByGroupName(createGroupDto.getGroupName()).isPresent()) {
            throw new DuplicatedGroupException();
        }

        final GroupEntity groupEntity = GroupEntity.builder()
                .groupName(createGroupDto.getGroupName())
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

    public List<GetGroupListDto> getGroup() {
        UserEntity userEntity = UserThreadLocal.get();

        List<UserGroupEntity> userGroupEntityList = userGroupRepository.fetchJoinByUserEntity(userEntity);

        return userGroupEntityList.stream()
                .map(userGroupEntity ->
                    GetGroupListDto.builder()
                            .groupName(userGroupEntity.getGroupEntity().getGroupName())
                            .groupImageUrl(userGroupEntity.getGroupEntity().getGroupImageUrl())
                            .userImageUrlList(userGroupRepository.findUserImage(userGroupEntity.getGroupEntity()))
                            .count(getCount(userGroupEntity))
                            .build()
                )
                .collect(Collectors.toList());
    }

    @Transactional
    public void changeGroupStatus(ChangeGroupStatusDto changeGroupStatusDto) {
        final UserEntity userEntity = UserThreadLocal.get();

        List<UserGroupEntity> userGroupEntityList = userGroupRepository.fetchJoinByUserEntity(userEntity);
        final UserGroupEntity findUserGroup = userGroupEntityList.stream()
                .filter(userGroupEntity -> userGroupEntity.getGroupEntity()
                        .getGroupName().equals(changeGroupStatusDto.getGroup()))
                .findAny()
                .orElseThrow(NotFoundGroupException::new);

        if (findUserGroup.getUserRole().equals(UserRole.MEMBER)) {
            throw new NoPermissionUserException();
        }

        findUserGroup.getGroupEntity().changeGroupStatus(changeGroupStatusDto.getChangeStatus());
    }

    private int getCount(UserGroupEntity userGroupEntity) {
        int count = userGroupRepository.countByGroupEntity(userGroupEntity.getGroupEntity());
        if (count > 4) {
            return count - 4;
        }
        return 0;
    }
}
