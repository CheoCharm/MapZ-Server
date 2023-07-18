package com.cheocharm.MapZ.group.application;

import com.cheocharm.MapZ.ServiceTest;
import com.cheocharm.MapZ.common.exception.group.DuplicatedGroupException;
import com.cheocharm.MapZ.common.exception.user.ExitGroupChiefException;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.exception.usergroup.GroupMemberSizeExceedException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.group.domain.GroupLimit;
import com.cheocharm.MapZ.group.presentation.dto.request.AcceptInvitationRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.ChangeChiefRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.CreateGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.ExitGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.InviteGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.JoinGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.KickUserRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.RefuseInvitationRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.UpdateGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.UpdateInvitationStatusRequest;
import com.cheocharm.MapZ.group.presentation.dto.response.JoinGroupResultResponse;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.domain.InvitationStatus;
import com.cheocharm.MapZ.usergroup.domain.UserGroupEntity;
import com.cheocharm.MapZ.usergroup.domain.UserRole;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest extends ServiceTest {

    @Autowired
    private GroupService groupService;

    private static MockedStatic<UserThreadLocal> utl;
    private static UserEntity user;
    private static final EasyRandom easyRandom = new EasyRandom();

    @BeforeAll
    static void beforeAll() {
        utl = mockStatic(UserThreadLocal.class);
        user = easyRandom.nextObject(UserEntity.class);
        utl.when(UserThreadLocal::get).thenReturn(GroupServiceTest.user);
    }

    @AfterAll
    static void afterAll() {
        utl.close();
    }

    @Test
    @DisplayName("그룹 생성")
    void createGroup() {

        //given
        final CreateGroupRequest createGroupRequest = new CreateGroupRequest(" 그룹명", "그룹소개", true);
        final MockMultipartFile groupImage = getMockMultipartFile("groupImage");

        //when,then
        assertDoesNotThrow(() -> groupService.createGroup(createGroupRequest, groupImage));
    }

    @Test
    @DisplayName("그룹 생성 시 동일한 그룹명이 존재하면 실패한다.")
    void createGroupFailWhenDuplicatedGroupName() {

        //given
        final CreateGroupRequest createGroupRequest = new CreateGroupRequest(" 그룹명", "그룹소개", true);
        final MockMultipartFile groupImage = getMockMultipartFile("groupImage");

        //when
        given(groupRepository.existsByGroupName(anyString()))
                .willReturn(true);
        //then
        assertThatThrownBy(() -> groupService.createGroup(createGroupRequest, groupImage))
                .isInstanceOf(DuplicatedGroupException.class);
    }

    @Test
    @DisplayName("그룹 정보는 업데이트 가능하다.")
    void updateGroup() {

        //given
        final GroupEntity group = easyRandom.nextObject(GroupEntity.class);

        final UserGroupEntity userGroup = UserGroupEntity.builder()
                .groupEntity(group)
                .userEntity(user)
                .invitationStatus(InvitationStatus.ACCEPT)
                .userRole(UserRole.CHIEF)
                .build();

        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(userGroup));

        final UpdateGroupRequest updateGroupRequest = new UpdateGroupRequest(userGroup.getGroupEntity().getId(), "updateGroupName",
                "updateBio", false);
        final MockMultipartFile image = getMockMultipartFile("image");

        //when
        groupService.updateGroup(updateGroupRequest, image);

        //then
        assertThat(userGroup.getGroupEntity().getGroupName())
                .isEqualTo(updateGroupRequest.getGroupName());

    }

    @Test
    @DisplayName("그룹 정보는 그룹장만 업데이트 가능하다.")
    void updateGroupFailWhenNoPermission() {

        //given
        final GroupEntity group = easyRandom.nextObject(GroupEntity.class);

        final UserGroupEntity userGroup = UserGroupEntity.builder()
                .groupEntity(group)
                .userEntity(user)
                .invitationStatus(InvitationStatus.ACCEPT)
                .userRole(UserRole.MEMBER)
                .build();

        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(userGroup));

        final UpdateGroupRequest updateGroupRequest = new UpdateGroupRequest(userGroup.getGroupEntity().getId(), "updateGroupName",
                "updateBio", false);
        final MockMultipartFile image = getMockMultipartFile("image");

        //when, then
        assertThatThrownBy(() -> groupService.updateGroup(updateGroupRequest, image))
                .isInstanceOf(NoPermissionUserException.class);
    }

    @Test
    @DisplayName("그룹에 유저가 참여한다")
    void joinGroup() {

        //given
        final GroupEntity group = easyRandom.nextObject(GroupEntity.class);

        final UserGroupEntity userGroup = UserGroupEntity.builder()
                .groupEntity(group)
                .userEntity(user)
                .invitationStatus(InvitationStatus.PENDING)
                .build();

        given(groupRepository.findById(anyLong()))
                .willReturn(Optional.of(group));
        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.empty());
        given(userGroupRepository.save(any(UserGroupEntity.class)))
                .willReturn(userGroup);

        final JoinGroupRequest joinGroupRequest = new JoinGroupRequest(userGroup.getGroupEntity().getId());

        //when
        final JoinGroupResultResponse response = groupService.joinGroup(joinGroupRequest);

        //then
        assertThat(response.getAlreadyJoin())
                .isEqualTo(false);
        assertThat(response.getStatus())
                .isEqualTo(userGroup.getInvitationStatus().getStatus());
    }

    @Test
    @DisplayName("이미 그룹에 참여되어 있는 유저는 참여할 수 없다.")
    void joinGroupWhenUserNotAttend() {

        //given
        final GroupEntity group = easyRandom.nextObject(GroupEntity.class);
        final UserGroupEntity userGroup = easyRandom.nextObject(UserGroupEntity.class);

        given(groupRepository.findById(anyLong()))
                .willReturn(Optional.of(group));
        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(userGroup));

        final JoinGroupRequest joinGroupRequest = new JoinGroupRequest(userGroup.getGroupEntity().getId());

        //when
        final JoinGroupResultResponse response = groupService.joinGroup(joinGroupRequest);

        //then
        assertThat(response.getAlreadyJoin()).isEqualTo(true);
        assertThat(response.getStatus()).isEqualTo(userGroup.getInvitationStatus().getStatus());
    }

    @Test
    @DisplayName("그룹에 가입 신청한 유저는 그룹장에 의해 수락된다.")
    void updateInvitationStatus() {

        //given
        final GroupEntity group = easyRandom.nextObject(GroupEntity.class);

        final UserGroupEntity userGroup = UserGroupEntity.builder()
                .groupEntity(group)
                .userEntity(user)
                .invitationStatus(InvitationStatus.ACCEPT)
                .userRole(UserRole.CHIEF)
                .build();
        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), user.getId()))
                .willReturn(Optional.of(userGroup));

        final UserEntity otherUser = easyRandom.nextObject(UserEntity.class);
        final UserGroupEntity otherUserGroup = UserGroupEntity.builder()
                .groupEntity(group)
                .userEntity(otherUser)
                .invitationStatus(InvitationStatus.PENDING)
                .userRole(UserRole.MEMBER)
                .build();

        final UpdateInvitationStatusRequest updateInvitationStatusRequest = new UpdateInvitationStatusRequest(group.getId(), true, otherUser.getId());
        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), updateInvitationStatusRequest.getUserId()))
                .willReturn(Optional.of(otherUserGroup));

        //when
        groupService.updateInvitationStatus(updateInvitationStatusRequest);

        //then
        assertThat(otherUserGroup.getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);
    }

    @Test
    @DisplayName("그룹장만 그룹 가입 신청한 유저를 수락할 수 있다.")
    void updateInvitationStatusFailWhenNotChief() {

        //given
        final GroupEntity group = easyRandom.nextObject(GroupEntity.class);
        final UserGroupEntity userGroup = UserGroupEntity.builder()
                .userEntity(user)
                .groupEntity(group)
                .invitationStatus(InvitationStatus.PENDING)
                .userRole(UserRole.MEMBER)
                .build();

        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(userGroup));

        final UpdateInvitationStatusRequest updateInvitationStatusRequest = new UpdateInvitationStatusRequest(group.getId(), true, user.getId());

        //when, then
        assertThatThrownBy(() -> groupService.updateInvitationStatus(updateInvitationStatusRequest))
                .isInstanceOf(NoPermissionUserException.class);

    }

    @Test
    @DisplayName("그룹은 나갈 수 있다.")
    void exitGroup() {

    }

    @Test
    @DisplayName("그룹장은 그룹을 나갈 수 없다")
    void exitGroupFailWhenUserRoleIsChief() {

        //given
        final GroupEntity group = easyRandom.nextObject(GroupEntity.class);
        final UserGroupEntity userGroup = UserGroupEntity.builder()
                .userEntity(user)
                .groupEntity(group)
                .invitationStatus(InvitationStatus.ACCEPT)
                .userRole(UserRole.CHIEF)
                .build();

        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(userGroup));

        final ExitGroupRequest exitGroupRequest = new ExitGroupRequest(group.getId());

        //when, then
        assertThatThrownBy(() -> groupService.exitGroup(exitGroupRequest))
                .isInstanceOf(ExitGroupChiefException.class);
    }

    @Test
    @DisplayName("그룹장은 다른 그룹원에게 넘길 수 있다.")
    void updateChief() {

        //given
        final GroupEntity group = easyRandom.nextObject(GroupEntity.class);
        final UserGroupEntity userGroup = UserGroupEntity.builder()
                .userEntity(user)
                .groupEntity(group)
                .invitationStatus(InvitationStatus.ACCEPT)
                .userRole(UserRole.CHIEF)
                .build();
        final UserEntity targetUser = easyRandom.nextObject(UserEntity.class);
        final UserGroupEntity targetUserGroup = UserGroupEntity.builder()
                .userEntity(targetUser)
                .groupEntity(group)
                .invitationStatus(InvitationStatus.ACCEPT)
                .userRole(UserRole.MEMBER)
                .build();
        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), user.getId()))
                .willReturn(Optional.of(userGroup));
        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), targetUser.getId()))
                .willReturn(Optional.of(targetUserGroup));

        final ChangeChiefRequest changeChiefRequest = new ChangeChiefRequest(group.getId(), targetUser.getId());

        //when
        groupService.updateChief(changeChiefRequest);

        //then
        assertThat(targetUserGroup.getUserRole()).isEqualTo(UserRole.CHIEF);
    }

    @Test
    @DisplayName("그룹장만 역할을 넘길 수 있다.")
    void updateChiefFailWhenNotChiefUser() {

        //given
        final GroupEntity group = easyRandom.nextObject(GroupEntity.class);
        final UserGroupEntity userGroup = UserGroupEntity.builder()
                .userEntity(user)
                .groupEntity(group)
                .invitationStatus(InvitationStatus.ACCEPT)
                .userRole(UserRole.MEMBER)
                .build();

        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(userGroup));

        final ChangeChiefRequest changeChiefRequest = new ChangeChiefRequest(group.getId(), user.getId());


        //when, then
        assertThatThrownBy(() -> groupService.updateChief(changeChiefRequest))
                .isInstanceOf(NoPermissionUserException.class);
    }

    @Test
    @DisplayName("그룹에 유저들을 초대할 수 있다.")
    void inviteUser() {

        //given
        final GroupEntity group = easyRandom.nextObject(GroupEntity.class);
        final UserGroupEntity userGroup = UserGroupEntity.builder()
                .userRole(UserRole.MEMBER)
                .invitationStatus(InvitationStatus.ACCEPT)
                .groupEntity(group)
                .userEntity(user)
                .build();
        given(userGroupRepository.findByGroupIdAndUserId(any(), any()))
                .willReturn(Optional.of(userGroup));

        final int randomNumber = new Random().nextInt(8) + 1;

        final ArrayList<Long> userIds = new ArrayList<>();
        final ArrayList<UserEntity> users = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            final UserEntity userEntity = easyRandom.nextObject(UserEntity.class);
            userIds.add(userEntity.getId());
            users.add(userEntity);
        }
        given(userRepository.getUserEntityListByUserIdList(userIds))
                .willReturn(users);
        final InviteGroupRequest inviteGroupRequest = new InviteGroupRequest(group.getId(), userIds);

        //when
        groupService.inviteUser(inviteGroupRequest);

        //then
        then(userGroupRepository).should(times(userIds.size())).save(any(UserGroupEntity.class));
    }

    @Test
    @DisplayName("그룹장은 유저를 내보낼 수 있다.")
    void kickUser() {

        //given
        final GroupEntity group = easyRandom.nextObject(GroupEntity.class);
        final UserGroupEntity userGroup = UserGroupEntity.builder()
                .userEntity(user)
                .groupEntity(group)
                .invitationStatus(InvitationStatus.ACCEPT)
                .userRole(UserRole.CHIEF)
                .build();

        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), user.getId()))
                .willReturn(Optional.of(userGroup));

        final UserEntity targetUser = easyRandom.nextObject(UserEntity.class);
        final UserGroupEntity targetUserGroup = UserGroupEntity.builder()
                .userEntity(targetUser)
                .groupEntity(group)
                .invitationStatus(InvitationStatus.ACCEPT)
                .userRole(UserRole.MEMBER)
                .build();
        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), targetUser.getId()))
                .willReturn(Optional.of(targetUserGroup));

        final KickUserRequest kickUserRequest = new KickUserRequest(targetUser.getId(), group.getId());

        //when
        groupService.kickUser(kickUserRequest);

        //then
        then(userGroupRepository).should(times(1)).deleteById(targetUserGroup.getId());
    }

    @Test
    @DisplayName("초대받은 유저는 초대를 승낙할 수 있다.")
    void acceptInvitation() {

        //given
        final GroupEntity group = easyRandom.nextObject(GroupEntity.class);
        final UserGroupEntity userGroup = UserGroupEntity.builder()
                .userEntity(user)
                .groupEntity(group)
                .invitationStatus(InvitationStatus.PENDING)
                .userRole(UserRole.MEMBER)
                .build();

        given(userGroupRepository.countByGroupId(anyLong()))
                .willReturn(GroupLimit.LIMIT_GROUP_PEOPLE.getLimitSize() - 1);
        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), user.getId()))
                .willReturn(Optional.of(userGroup));

        final AcceptInvitationRequest acceptInvitationRequest = new AcceptInvitationRequest(group.getId());

        //when
        groupService.acceptInvitation(acceptInvitationRequest);

        //then
        assertThat(userGroup.getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);
    }

    @Test
    @DisplayName("그룹인원 제한에 걸리면 참여할 수 없다.")
    void joinFailWhenGroupOverCapacity() {

        //given
        final GroupEntity group = easyRandom.nextObject(GroupEntity.class);

        given(userGroupRepository.countByGroupId(anyLong()))
                .willReturn(GroupLimit.LIMIT_GROUP_PEOPLE.getLimitSize());

        final AcceptInvitationRequest acceptInvitationRequest = new AcceptInvitationRequest(group.getId());

        //when, then
        assertThatThrownBy(() -> groupService.acceptInvitation(acceptInvitationRequest))
                .isInstanceOf(GroupMemberSizeExceedException.class);
    }

    @Test
    @DisplayName("초대받은 유저는 초대를 거절할 수 있다.")
    void refuseInvitation() {

        //given
        final GroupEntity group = easyRandom.nextObject(GroupEntity.class);
        final UserGroupEntity userGroup = UserGroupEntity.builder()
                .userEntity(user)
                .groupEntity(group)
                .invitationStatus(InvitationStatus.PENDING)
                .userRole(UserRole.MEMBER)
                .build();

        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), user.getId()))
                .willReturn(Optional.of(userGroup));

        final RefuseInvitationRequest refuseInvitationRequest = new RefuseInvitationRequest(group.getId());

        //when
        groupService.refuseInvitation(refuseInvitationRequest);

        //then
        then(userGroupRepository).should(times(1)).delete(userGroup);
    }

}