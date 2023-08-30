package com.mapz.domain.domains.usergroup.repository;

import com.mapz.domain.domains.group.entity.Group;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.usergroup.entity.UserGroup;
import com.mapz.domain.domains.usergroup.vo.ChiefUserImageVO;
import com.mapz.domain.domains.usergroup.vo.CountUserGroupVO;
import com.mapz.domain.domains.usergroup.vo.MyInvitationVO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;

public interface UserGroupRepositoryCustom {

    List<Group> getGroups(User user);

    List<ChiefUserImageVO> findChiefUserImage(List<Group> groupList);

    List<UserGroup> findBySearchNameAndGroupId(String searchName, Long groupId);

    Optional<UserGroup> findByGroupIdAndUserId(Long groupId, Long userId);

    List<UserGroup> findByGroupId(Long groupId);

    List<CountUserGroupVO> countByGroup(List<Group> groupList);

    Long countByGroupId(Long groupId);

    Slice<MyInvitationVO> getInvitationSlice(Long userId, Long cursorId, Pageable pageable);

    List<Long> getGroupIdByUserId(Long userId);
}
