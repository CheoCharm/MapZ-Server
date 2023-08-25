package com.cheocharm.MapZ.group.application;

import com.cheocharm.MapZ.ServiceTest;
import com.cheocharm.MapZ.common.exception.group.DuplicatedGroupException;
import com.cheocharm.MapZ.common.exception.user.ExitGroupChiefException;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.exception.usergroup.GroupMemberSizeExceedException;
import com.cheocharm.MapZ.common.exception.usergroup.SelfKickException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.common.util.PagingUtils;
import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.group.domain.GroupLimit;
import com.cheocharm.MapZ.group.presentation.dto.request.AcceptInvitationRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.ChangeChiefRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.CreateGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.ExitGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.InviteGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.JoinGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.UpdateGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.UpdateInvitationStatusRequest;
import com.cheocharm.MapZ.group.presentation.dto.response.GetMyGroupResponse;
import com.cheocharm.MapZ.group.presentation.dto.response.GroupMemberResponse;
import com.cheocharm.MapZ.group.presentation.dto.response.JoinGroupResultResponse;
import com.cheocharm.MapZ.group.presentation.dto.response.MyInvitationResponse;
import com.cheocharm.MapZ.group.presentation.dto.response.PagingGroupListResponse;
import com.cheocharm.MapZ.user.domain.User;
import com.cheocharm.MapZ.usergroup.domain.InvitationStatus;
import com.cheocharm.MapZ.usergroup.domain.UserGroup;
import com.cheocharm.MapZ.usergroup.domain.UserRole;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.ChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.CountUserGroupVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.MyInvitationVO;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.cheocharm.MapZ.common.fixtures.UserGroupFixtures.userGroupMember;
import static com.cheocharm.MapZ.common.fixtures.UserGroupFixtures.userGroupPendingMember;
import static com.cheocharm.MapZ.common.fixtures.UserGroupFixtures.userGroupChief;
import static com.cheocharm.MapZ.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.cheocharm.MapZ.common.util.PagingUtils.GROUP_SIZE;
import static com.cheocharm.MapZ.common.util.PagingUtils.MY_INVITATION_SIZE;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyDescPageConfigBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

class GroupServiceTest extends ServiceTest {

    @Autowired
    private GroupService groupService;

    private static MockedStatic<UserThreadLocal> utl;
    private static User user;
    private static final EasyRandom easyRandom = new EasyRandom();

    @BeforeAll
    static void beforeAll() {
        utl = mockStatic(UserThreadLocal.class);
        user = easyRandom.nextObject(User.class);
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
        final MockMultipartFile groupImage = getMockMultipartFileHasContent("groupImage");

        //when,then
        assertDoesNotThrow(() -> groupService.createGroup(createGroupRequest, groupImage));
        then(groupRepository).should().save(any(Group.class));
        then(userGroupRepository).should().save(any(UserGroup.class));
    }

    @Test
    @DisplayName("그룹 생성 시 동일한 그룹명이 존재하면 실패한다.")
    void createGroupFailWhenDuplicatedGroupName() {

        //given
        given(groupRepository.existsByGroupName(anyString()))
                .willReturn(true);
        final CreateGroupRequest request = new CreateGroupRequest(" 그룹명", "그룹소개", true);
        final MockMultipartFile groupImage = getMockMultipartFile("groupImage");

        //when, then
        assertThatThrownBy(() -> groupService.createGroup(request, groupImage))
                .isInstanceOf(DuplicatedGroupException.class);
    }

    @Test
    @DisplayName("검색을 통해 그룹 리스트를 획득할 수 있다.")
    void getGroup() {

        //given
        Group group = easyRandom.nextObject(Group.class);
        Group group1 = easyRandom.nextObject(Group.class);
        SliceImpl<Group> groupSlice = new SliceImpl<>(
                List.of(group, group1),
                applyDescPageConfigBy(0, GROUP_SIZE, FIELD_CREATED_AT),
                false
        );

        given(groupRepository.findByGroupName(anyString(), anyLong(), any(Pageable.class)))
                .willReturn(groupSlice);
        given(userGroupRepository.countByGroup(anyList()))
                .willReturn(List.of(
                        new CountUserGroupVO(5L, group.getId()),
                        new CountUserGroupVO(7L, group1.getId())
                ));
        given(userGroupRepository.findChiefUserImage(anyList()))
                .willReturn(List.of(
                        new ChiefUserImageVO("image1", group.getId()),
                        new ChiefUserImageVO("image2", group1.getId())
                ));
        //when
        PagingGroupListResponse response = groupService.getGroup("groupName", 0L, 0);

        //then
        assertThat(response.getHasNextPage()).isFalse();
        assertThat(response.getGroupList().size()).isEqualTo(groupSlice.getContent().size());
    }

    @Test
    @DisplayName("그룹 정보는 업데이트 가능하다.")
    void updateGroup() {

        //given
        final Group group = easyRandom.nextObject(Group.class);
        final UserGroup userGroupChief = userGroupChief(user, group);

        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(userGroupChief));

        final UpdateGroupRequest request = new UpdateGroupRequest(userGroupChief.getGroup().getId(), "updateGroupName",
                "updateBio", false);
        final MockMultipartFile image = getMockMultipartFileHasContent("image");

        //when
        groupService.updateGroup(request, image);

        //then
        assertThat(userGroupChief.getGroup().getGroupName())
                .isEqualTo(request.getGroupName());

    }

    @Test
    @DisplayName("그룹명 업데이트를 중복된 그룹명으로 할 수 없다.")
    void cannotUpdateDuplicatedGroupName() {

        //given
        Group group = easyRandom.nextObject(Group.class);
        UserGroup userGroupChief = userGroupChief(user, group);
        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(userGroupChief));
        given(groupRepository.existsByGroupName(anyString()))
                .willReturn(true);

        final UpdateGroupRequest request = new UpdateGroupRequest(userGroupChief.getGroup().getId(), "updateGroupName",
                "updateBio", false);
        MultipartFile image = getMockMultipartFile("image");

        //when, then
        assertThatThrownBy(() -> groupService.updateGroup(request, image))
                .isInstanceOf(DuplicatedGroupException.class);
    }

    @Test
    @DisplayName("그룹 정보는 그룹장만 업데이트 가능하다.")
    void updateGroupFailWhenNoPermission() {

        //given
        final Group group = easyRandom.nextObject(Group.class);
        final UserGroup userGroupMember = userGroupMember(user, group);

        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(userGroupMember));

        final UpdateGroupRequest request = new UpdateGroupRequest(
                userGroupMember.getGroup().getId(),
                "updateGroupName",
                "updateBio",
                false
        );
        final MockMultipartFile image = getMockMultipartFile("image");

        //when, then
        assertThatThrownBy(() -> groupService.updateGroup(request, image))
                .isInstanceOf(NoPermissionUserException.class);
    }

    @Test
    @DisplayName("그룹에 유저가 참여한다")
    void joinGroup() {

        //given
        final Group group = easyRandom.nextObject(Group.class);
        final UserGroup userGroupPendingMember = userGroupPendingMember(user, group);

        given(groupRepository.findById(anyLong()))
                .willReturn(Optional.of(group));
        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.empty());
        given(userGroupRepository.save(any(UserGroup.class)))
                .willReturn(userGroupPendingMember);

        final JoinGroupRequest request = new JoinGroupRequest(userGroupPendingMember.getGroup().getId());

        //when
        final JoinGroupResultResponse response = groupService.joinGroup(request);

        //then
        assertThat(response.getAlreadyJoin())
                .isEqualTo(false);
        assertThat(response.getStatus())
                .isEqualTo(userGroupPendingMember.getInvitationStatus().getStatus());
        then(userGroupRepository).should().save(any(UserGroup.class));
    }

    @Test
    @DisplayName("이미 그룹에 참여되어 있는 유저는 참여할 수 없다.")
    void joinGroupWhenUserNotAttend() {

        //given
        final Group group = easyRandom.nextObject(Group.class);
        final UserGroup userGroup = easyRandom.nextObject(UserGroup.class);

        given(groupRepository.findById(anyLong()))
                .willReturn(Optional.of(group));
        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(userGroup));

        final JoinGroupRequest joinGroupRequest = new JoinGroupRequest(userGroup.getGroup().getId());

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
        Group group = easyRandom.nextObject(Group.class);
        UserGroup userGroup = userGroupChief(user, group);

        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), user.getId()))
                .willReturn(Optional.of(userGroup));

        User targetUser = easyRandom.nextObject(User.class);
        UserGroup targetUserGroup = userGroupPendingMember(targetUser, group);

        UpdateInvitationStatusRequest request = new UpdateInvitationStatusRequest(
                group.getId(), true, targetUser.getId()
        );
        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), request.getUserId()))
                .willReturn(Optional.of(targetUserGroup));

        //when
        groupService.updateInvitationStatus(request);

        //then
        assertThat(targetUserGroup.getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);
    }

    @Test
    @DisplayName("그룹장은 그룹신청을 거절할 수 있다.")
    void groupChiefCanRefuseApplication() {

        //given
        Group group = easyRandom.nextObject(Group.class);
        User targetUser = easyRandom.nextObject(User.class);
        UpdateInvitationStatusRequest request = new UpdateInvitationStatusRequest(
                group.getId(),
                false,
                targetUser.getId()
        );
        UserGroup userGroupChief = userGroupChief(user, group);
        UserGroup userGroupPendingMember = userGroupPendingMember(targetUser, group);

        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), user.getId()))
                .willReturn(Optional.of(userGroupChief));
        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), request.getUserId()))
                .willReturn(Optional.of(userGroupPendingMember));

        //when
        groupService.updateInvitationStatus(request);

        //then
        then(userGroupRepository).should().deleteById(userGroupPendingMember.getId());
    }

    @Test
    @DisplayName("그룹장만 그룹 가입 신청한 유저를 수락할 수 있다.")
    void updateInvitationStatusFailWhenNotChief() {

        //given
        final Group group = easyRandom.nextObject(Group.class);
        final UserGroup userGroupMember = userGroupMember(user, group);

        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(userGroupMember));

        final UpdateInvitationStatusRequest updateInvitationStatusRequest = new UpdateInvitationStatusRequest(group.getId(), true, user.getId());

        //when, then
        assertThatThrownBy(() -> groupService.updateInvitationStatus(updateInvitationStatusRequest))
                .isInstanceOf(NoPermissionUserException.class);

    }

    @Test
    @DisplayName("그룹은 나갈 수 있다.")
    void exitGroup() {

        //given
        Group group = easyRandom.nextObject(Group.class);
        UserGroup userGroupMember = userGroupMember(user, group);
        ExitGroupRequest request = new ExitGroupRequest(group.getId());
        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(userGroupMember));
        //when
        groupService.exitGroup(request);

        //then
        then(userGroupRepository).should().deleteById(userGroupMember.getId());

    }

    @Test
    @DisplayName("그룹장은 그룹을 나갈 수 없다")
    void exitGroupFailWhenUserRoleIsChief() {

        //given
        final Group group = easyRandom.nextObject(Group.class);
        final UserGroup userGroup = userGroupChief(user, group);

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
        final Group group = easyRandom.nextObject(Group.class);
        final UserGroup userGroup = userGroupChief(user, group);
        final User targetUser = easyRandom.nextObject(User.class);
        final UserGroup targetUserGroup = userGroupMember(targetUser, group);

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
    @DisplayName("그룹장이 아닌 유저는 그룹장 역할을 넘길 수 없다.")
    void updateChiefFailWhenNotChiefUser() {

        //given
        final Group group = easyRandom.nextObject(Group.class);
        final UserGroup userGroup = userGroupMember(user, group);

        given(userGroupRepository.findByGroupIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(userGroup));

        final ChangeChiefRequest changeChiefRequest = new ChangeChiefRequest(group.getId(), user.getId());


        //when, then
        assertThatThrownBy(() -> groupService.updateChief(changeChiefRequest))
                .isInstanceOf(NoPermissionUserException.class);
    }

    @Test
    @DisplayName("그룹장은 초대상태가 수락된 유저에게만 넘길 수 있다.")
    void acceptedUserCanBeChief() {

        //given
        final Group group = easyRandom.nextObject(Group.class);
        final UserGroup userGroup = userGroupChief(user, group);
        final User targetUser = easyRandom.nextObject(User.class);
        final UserGroup targetUserGroup = userGroupPendingMember(targetUser, group);

        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), user.getId()))
                .willReturn(Optional.of(userGroup));
        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), targetUser.getId()))
                .willReturn(Optional.of(targetUserGroup));
        ChangeChiefRequest request = new ChangeChiefRequest(group.getId(), targetUser.getId());

        //when, then
        assertThatThrownBy(() -> groupService.updateChief(request))
                .isInstanceOf(NoPermissionUserException.class);
    }

    @Test
    @DisplayName("그룹에 참가되어 있는 유저들을 그룹원을 초대할 수 있다.")
    void inviteUser() {

        //given
        final Group group = easyRandom.nextObject(Group.class);
        final UserGroup userGroupMember = userGroupMember(user, group);
        given(userGroupRepository.findByGroupIdAndUserId(any(), any()))
                .willReturn(Optional.of(userGroupMember));

        final int randomNumber = new Random().nextInt(8) + 1;

        final ArrayList<Long> userIds = new ArrayList<>();
        final ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            final User user = easyRandom.nextObject(User.class);
            userIds.add(user.getId());
            users.add(user);
        }
        given(userRepository.getUserListByUserIdList(userIds))
                .willReturn(users);
        final InviteGroupRequest inviteGroupRequest = new InviteGroupRequest(group.getId(), userIds);

        //when
        groupService.inviteUser(inviteGroupRequest);

        //then
        then(userGroupRepository).should(times(userIds.size())).save(any(UserGroup.class));
    }

    @Test
    @DisplayName("내가 속한 그룹을 조회할 수 있다.")
    void getMyGroup() {

        //given
        Group group = easyRandom.nextObject(Group.class);
        Group group1 = easyRandom.nextObject(Group.class);

        List<Group> groups = List.of(group, group1);
        given(userGroupRepository.getGroups(user))
                .willReturn(groups);
        given(userGroupRepository.countByGroup(anyList()))
                .willReturn(List.of(
                        new CountUserGroupVO(5L, group.getId()),
                        new CountUserGroupVO(7L, group1.getId())
                ));
        given(userGroupRepository.findChiefUserImage(anyList()))
                .willReturn(List.of(
                        new ChiefUserImageVO("image1", group.getId()),
                        new ChiefUserImageVO("image2", group1.getId())
                ));
        //when
        List<GetMyGroupResponse> response = groupService.searchMyGroup();

        //then
        assertThat(response.size()).isEqualTo(groups.size());

    }

    @Test
    @DisplayName("그룹안에 속한 유저만 그룹관리 페이지를 조회할 수 있다.")
    void getMember() {

        //given
        Group group = easyRandom.nextObject(Group.class);

        List<UserGroup> userGroups = List.of(
                userGroupMember(easyRandom.nextObject(User.class), group),
                userGroupChief(user, group),
                userGroupPendingMember(easyRandom.nextObject(User.class), group)
        );
        given(userGroupRepository.findByGroupId((anyLong())))
                .willReturn(userGroups);

        //when
        List<GroupMemberResponse> response = groupService.getMember(group.getId());

        //then
        assertThat(response.size()).isEqualTo(userGroups.size());
    }

    @Test
    @DisplayName("그룹안에 속한 유저가 아니면 그룹관리의 멤버들을 조회할 수 없다.")
    void groupMemberCanGet() {

        //given
        Group group = easyRandom.nextObject(Group.class);
        List<UserGroup> userGroups = List.of(
                userGroupMember(easyRandom.nextObject(User.class), group),
                userGroupChief(easyRandom.nextObject(User.class), group),
                userGroupPendingMember(easyRandom.nextObject(User.class), group)
        );
        given(userGroupRepository.findByGroupId((anyLong())))
                .willReturn(userGroups);
        //when, then
        assertThatThrownBy(() -> groupService.getMember(group.getId()))
                .isInstanceOf(NoPermissionUserException.class);
    }


    @Test
    @DisplayName("그룹장은 유저를 내보낼 수 있다.")
    void kickUser() {

        //given
        final Group group = easyRandom.nextObject(Group.class);
        final UserGroup userGroup = userGroupChief(user, group);

        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), user.getId()))
                .willReturn(Optional.of(userGroup));

        final User targetUser = easyRandom.nextObject(User.class);
        final UserGroup targetUserGroup = userGroupMember(targetUser, group);
        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), targetUser.getId()))
                .willReturn(Optional.of(targetUserGroup));

        //when
        groupService.kickUser(group.getId(), targetUser.getId());

        //then
        then(userGroupRepository).should(times(1)).deleteById(targetUserGroup.getId());
    }

    @Test
    @DisplayName("그룹장이 아닌 유저가 다른 유저를 강퇴할 수 없다.")
    void chiefCannotKickSelf() {

        //given
        Group group = easyRandom.nextObject(Group.class);

        //when,then
        assertThatThrownBy(() -> groupService.kickUser(group.getId(), user.getId()))
                .isInstanceOf(SelfKickException.class);
    }

    @Test
    @DisplayName("초대받은 유저는 초대를 승낙할 수 있다.")
    void acceptInvitation() {

        //given
        Group group = easyRandom.nextObject(Group.class);
        UserGroup userGroup = userGroupPendingMember(user, group);

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
        final Group group = easyRandom.nextObject(Group.class);

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
        final Group group = easyRandom.nextObject(Group.class);
        final UserGroup userGroup = userGroupPendingMember(user, group);

        given(userGroupRepository.findByGroupIdAndUserId(group.getId(), user.getId()))
                .willReturn(Optional.of(userGroup));

        //when
        groupService.refuseInvitation(group.getId());

        //then
        then(userGroupRepository).should(times(1)).delete(userGroup);
    }

    @Test
    @DisplayName("유저는 초대장을 조회할 수 있다.")
    void getInvitation() {

        //given
        SliceImpl<MyInvitationVO> slice = new SliceImpl<>(
                List.of(
                        easyRandom.nextObject(MyInvitationVO.class),
                        easyRandom.nextObject(MyInvitationVO.class)
                ),
                applyDescPageConfigBy(0, MY_INVITATION_SIZE, FIELD_CREATED_AT),
                false
        );

        given(userGroupRepository.getInvitationSlice(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(slice);

        //when
        MyInvitationResponse response = groupService.getInvitation(0L, 0);

        //then
        assertThat(response.getInvitationList().size()).isEqualTo(slice.getContent().size());
        assertThat(response.isHasNext()).isEqualTo(slice.hasNext());
    }

}