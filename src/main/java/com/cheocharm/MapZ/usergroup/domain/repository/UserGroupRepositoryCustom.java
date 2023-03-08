package com.cheocharm.MapZ.usergroup.domain.repository;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.domain.UserGroupEntity;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.ChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.CountUserGroupVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.MyInvitationVO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;

public interface UserGroupRepositoryCustom {

    List<GroupEntity> getGroupEntityList(UserEntity userEntity);

    List<ChiefUserImageVO> findChiefUserImage(List<GroupEntity> groupEntityList);

    List<UserGroupEntity> findBySearchNameAndGroupId(String searchName, Long groupId);

    Optional<UserGroupEntity> findByGroupIdAndUserId(Long groupId, Long userId);

    List<UserGroupEntity> findByGroupId(Long groupId);

    List<CountUserGroupVO> countByGroupEntity(List<GroupEntity> groupEntityList);

    Long countByGroupId(Long groupId);

    Slice<MyInvitationVO> getInvitationSlice(Long userId, Long cursorId, Pageable pageable);
}
