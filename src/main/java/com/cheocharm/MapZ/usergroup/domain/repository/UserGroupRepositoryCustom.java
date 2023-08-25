package com.cheocharm.MapZ.usergroup.domain.repository;

import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.user.domain.User;
import com.cheocharm.MapZ.usergroup.domain.UserGroup;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.ChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.CountUserGroupVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.MyInvitationVO;
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
