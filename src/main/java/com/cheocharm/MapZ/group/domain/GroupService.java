package com.cheocharm.MapZ.group.domain;

import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.group.domain.dto.CreateGroupDto;
import com.cheocharm.MapZ.group.domain.dto.GroupListDto;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.InvitationStatus;
import com.cheocharm.MapZ.usergroup.UserGroupEntity;
import com.cheocharm.MapZ.usergroup.UserRole;
import com.cheocharm.MapZ.usergroup.repository.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;

    @Transactional
    public void createGroup(CreateGroupDto createGroupDto) {
        UserEntity userEntity = UserThreadLocal.get();

        GroupEntity groupEntity = GroupEntity.builder()
                .groupName(createGroupDto.getGroupName())
                .bio(createGroupDto.getBio())
                .groupImageUrl("")
                .groupUUID(UUID.randomUUID().toString())
                .build();

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

    public List<GroupListDto> getGroup() {
        UserEntity userEntity = UserThreadLocal.get();

        List<UserGroupEntity> userGroupEntityList = userGroupRepository.fetchJoinByUserEntity(userEntity);

        return userGroupEntityList.stream()
                .map(userGroupEntity ->
                    GroupListDto.builder()
                            .groupImageUrl(userGroupEntity.getGroupEntity().getGroupImageUrl())
                            .userImageUrlList(userGroupRepository.findUserImage(userGroupEntity.getGroupEntity()))
                            .count(getCount(userGroupEntity))
                            .build()
                )
                .collect(Collectors.toList());
    }

    private int getCount(UserGroupEntity userGroupEntity) {
        int count = userGroupRepository.countByGroupEntity(userGroupEntity.getGroupEntity());
        if (count > 4) {
            return count - 4;
        }
        return 0;
    }
}
