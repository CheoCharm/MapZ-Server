package com.cheocharm.MapZ.group.presentation.controller;

import com.cheocharm.MapZ.ControllerTest;
import com.cheocharm.MapZ.common.exception.user.ExitGroupChiefException;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.exception.usergroup.GroupMemberSizeExceedException;
import com.cheocharm.MapZ.common.exception.usergroup.SelfKickException;
import com.cheocharm.MapZ.group.application.GroupService;
import com.cheocharm.MapZ.group.presentation.dto.request.AcceptInvitationRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.ChangeChiefRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.CreateGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.ExitGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.InviteGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.JoinGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.KickUserRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.UpdateGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.UpdateInvitationStatusRequest;
import com.cheocharm.MapZ.group.presentation.dto.response.JoinGroupResultResponse;
import com.cheocharm.MapZ.group.presentation.dto.response.PagingGroupListResponse;
import com.cheocharm.MapZ.user.domain.User;
import com.cheocharm.MapZ.user.domain.UserProvider;
import com.cheocharm.MapZ.user.domain.repository.UserRepository;
import com.cheocharm.MapZ.usergroup.domain.InvitationStatus;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GroupControllerTest extends ControllerTest {

    @MockBean
    private GroupService groupService;

    @Autowired
    protected UserRepository userRepository;

    private static EasyRandom easyRandom = new EasyRandom();
    private static User user;

    @BeforeEach
    void beforeEach() {
        user = userRepository.save(
                User.builder()
                        .username("최강맵지")
                        .email("mapzbest@gmail.com")
                        .password("password1")
                        .userProvider(UserProvider.MAPZ)
                        .build()
        );

    }

    @Test
    @DisplayName("그룹을 생성한다.")
    void createGroup() throws Exception {
        //given
        CreateGroupRequest request = new CreateGroupRequest(
                "그룹명",
                "소개글",
                true
        );
        String accessToken = getAccessToken();
        String dtoJson = objectMapper.writeValueAsString(request);
        MockMultipartFile dto = new MockMultipartFile(
                "dto", "dto", "application/json",
                dtoJson.getBytes(StandardCharsets.UTF_8));

        //when, then
        mockMvc.perform(multipart("/api/group")
                        .file(getMockMultipartFile("file"))
                        .file(dto)
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("잘못된 그룹명으로 그룹을 생성하면 예외가 발생한다.")
    void createGroupByWrongGroupName() throws Exception {

        //given
        CreateGroupRequest request = new CreateGroupRequest(
                "그룹명~!",
                "소개글",
                true
        );
        String accessToken = getAccessToken();
        String dtoJson = objectMapper.writeValueAsString(request);
        MockMultipartFile dto = new MockMultipartFile(
                "dto", "dto", "application/json",
                dtoJson.getBytes(StandardCharsets.UTF_8));

        //when,then
        mockMvc.perform(multipart("/api/group")
                        .file(getMockMultipartFile("file"))
                        .file(dto)
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("그룹 소개글을 30자 넘겨서 만들 수 없다.")
    void createGroupByWrongBio() throws Exception {
        //given
        CreateGroupRequest request = new CreateGroupRequest(
                "그룹명",
                "소개글은 30자를 넘길 수 없습니다.소개글은 30자를 넘길 수 없습니다.소개글은 30자를 넘길 수 없습니다.소개글은 30자를 넘길 수 없습니다.",
                true
        );
        String accessToken = getAccessToken();
        String dtoJson = objectMapper.writeValueAsString(request);
        MockMultipartFile dto = new MockMultipartFile(
                "dto", "dto", "application/json",
                dtoJson.getBytes(StandardCharsets.UTF_8));

        //when,then
        mockMvc.perform(multipart("/api/group")
                        .file(getMockMultipartFile("file"))
                        .file(dto)
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저는 그룹 참가를 위해 그룹을 검색할 수 있다.")
    void userCanSearchGroup() throws Exception {

        //given
        String accessToken = getAccessToken();
        PagingGroupListResponse response = new PagingGroupListResponse(false, Collections.emptyList());
        given(groupService.getGroup(anyString(), anyLong(), anyInt()))
                .willReturn(response);

        //when, then
        mockMvc.perform(get("/api/group/{page}", 0)
                        .param("groupName", "mapz")
                        .param("cursorId", "0")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("그룹장은 그룹 정보를 갱신할 수 있다.")
    void updateGroup() throws Exception {

        //given
        UpdateGroupRequest request = easyRandom.nextObject(UpdateGroupRequest.class);
        String dtoJson = objectMapper.writeValueAsString(request);
        MockMultipartFile dto = new MockMultipartFile(
                "dto", "dto", "application/json",
                dtoJson.getBytes(StandardCharsets.UTF_8));
        String accessToken = getAccessToken();
        willDoNothing()
                .given(groupService)
                .updateGroup(any(), any());


        //when, then
        MockMultipartHttpServletRequestBuilder builder = multipart("/api/group");
        builder.with(req -> {
            req.setMethod("PATCH");
            return req;
        });

        mockMvc.perform(builder
                        .file(dto)
                        .file(getMockMultipartFile("file"))
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("유저는 그룹에 참가 신청을 할 수 있다.")
    void joinGroup() throws Exception {

        //given
        JoinGroupRequest request = new JoinGroupRequest(1L);
        String accessToken = getAccessToken();
        given(groupService.joinGroup(any()))
                .willReturn(new JoinGroupResultResponse(false, InvitationStatus.PENDING.getStatus()));

        //when, then
        mockMvc.perform(post("/api/group/join")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저가 그룹에 여러번 참가신청을 해도 실패하지 않는다.")
    void joinGroupRequestSeveralTimeBeforeAccepted() throws Exception {

        //given
        JoinGroupRequest request = new JoinGroupRequest(1L);
        String accessToken = getAccessToken();
        given(groupService.joinGroup(any()))
                .willReturn(new JoinGroupResultResponse(true, InvitationStatus.PENDING.getStatus()));

        //when, then
        mockMvc.perform(post("/api/group/join")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("유저의 그룹 참가를 수락한다.")
    void updateInvitationStatus() throws Exception{

        //given
        String accessToken = getAccessToken();
        UpdateInvitationStatusRequest request = easyRandom.nextObject(UpdateInvitationStatusRequest.class);
        willDoNothing()
                .given(groupService).updateInvitationStatus(any(UpdateInvitationStatusRequest.class));
        //when,then
        mockMvc.perform(patch("/api/group/join")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저는 그룹을 나갈 수 있다.")
    void exitGroup() throws Exception {

        //given
        String accessToken = getAccessToken();
        ExitGroupRequest request = new ExitGroupRequest(1L);
        willDoNothing()
                .given(groupService).exitGroup(any(ExitGroupRequest.class));

        //when, then
        mockMvc.perform(patch("/api/group/exit")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("그룹장인 상태로는 그룹을 나갈 수 없다.")
    void groupChiefCannotExitGroup() throws Exception{

        //given
        String accessToken = getAccessToken();
        ExitGroupRequest request = new ExitGroupRequest(1L);
        willThrow(new ExitGroupChiefException())
                .given(groupService).exitGroup(any(ExitGroupRequest.class));

        //when, then
        mockMvc.perform(patch("/api/group/exit")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("그룹장은 다른 그룹원에게 그룹장을 위임할 수 있다.")
    void changeGroupChief() throws Exception {

        //given
        String accessToken = getAccessToken();
        ChangeChiefRequest request = easyRandom.nextObject(ChangeChiefRequest.class);
        willDoNothing()
                .given(groupService).updateChief(any(ChangeChiefRequest.class));

        //when, then
        mockMvc.perform(patch("/api/group/chief")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("수락대기중인 유저에게 그룹장을 위임하면 예외가 발생한다")
    void notAcceptedUserCannotBeGroupChief() throws Exception {

        //given
        String accessToken = getAccessToken();
        ChangeChiefRequest request = easyRandom.nextObject(ChangeChiefRequest.class);
        willThrow(new NoPermissionUserException())
                .given(groupService).updateChief(any(ChangeChiefRequest.class));
        //when, then
        mockMvc.perform(patch("/api/group/chief")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("그룹장만 그룹원에게 역할을 넘길 수 있다.")
    void onlyGroupChiefCanChangeChief() throws Exception{

        //given
        String accessToken = getAccessToken();
        ChangeChiefRequest request = easyRandom.nextObject(ChangeChiefRequest.class);
        willThrow(new NoPermissionUserException())
                .given(groupService).updateChief(any(ChangeChiefRequest.class));

        //when,then
        mockMvc.perform(patch("/api/group/chief")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("그룹원은 유저를 초대할 수 있다.")
    void inviteUser() throws Exception{

        //given
        String accessToken = getAccessToken();
        InviteGroupRequest request = new InviteGroupRequest(1L, List.of(1L, 2L));
        willDoNothing()
                .given(groupService).inviteUser(any(InviteGroupRequest.class));

        //when, then
        mockMvc.perform(post("/api/group/invite")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("그룹원이 아닌 유저는 다른 유저를 초대할 수 있는 권한이 없다.")
    void noAcceptedUserCannotInviteOtherUser() throws Exception{

        //given
        String accessToken = getAccessToken();
        InviteGroupRequest request = new InviteGroupRequest(1L, List.of(1L, 2L));
        willThrow(new NoPermissionUserException())
                .given(groupService).inviteUser(any(InviteGroupRequest.class));

        //when, then
        mockMvc.perform(post("/api/group/invite")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("그룹장은 그룹원을 추방할 수 있다.")
    void kickUser() throws Exception {

        //given
        String accessToken = getAccessToken();
        KickUserRequest request = new KickUserRequest(1L, 1L);
        willDoNothing()
                .given(groupService).kickUser(anyLong(), anyLong());

        //when, then
        mockMvc.perform(delete("/api/group/user/{groupId}/{userId}", 1L, 1L)
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("그룹원은 자신을 스스로 추방할 수 없다.")
    void groupMemberCannotKickSelf() throws Exception {

        //given
        String accessToken = getAccessToken();
        willThrow(new SelfKickException())
                .given(groupService).kickUser(anyLong(), anyLong());

        //when, then
        mockMvc.perform(delete("/api/group/user/{groupId}/{userId}", 1L, 1L)
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("그룹원은 그룹원을 추방할 권한이 없다.")
    void groupMemberCannotKickMember() throws Exception {

        //given
        String accessToken = getAccessToken();
        willThrow(new NoPermissionUserException())
                .given(groupService).kickUser(anyLong(), anyLong());

        //when, then
        mockMvc.perform(delete("/api/group/user/{groupId}/{userId}", 1L, 1L)
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("그룹에 초대된 유저는 초대를 수락할 수 있다.")
    void acceptInvitation() throws Exception {

        //given
        String accessToken = getAccessToken();
        AcceptInvitationRequest request = new AcceptInvitationRequest(1L);
        willDoNothing()
                .given(groupService).acceptInvitation(any(AcceptInvitationRequest.class));

        //when, then
        mockMvc.perform(patch("/api/group/invite")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("그룹에 참가할 때 제한인원보다 그룹원의 수가 많거나 같으면 예외가 발생한다.")
    void cannotJoinGroupWhenMemberCountExceed() throws Exception {

        //given
        String accessToken = getAccessToken();
        AcceptInvitationRequest request = new AcceptInvitationRequest(1L);
        willThrow(new GroupMemberSizeExceedException())
                .given(groupService).acceptInvitation(any(AcceptInvitationRequest.class));

        //when, then
        mockMvc.perform(patch("/api/group/invite")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저는 그룹의 초대를 거절할 수 있다.")
    void refuseInvitation() throws Exception {

        //given
        String accessToken = getAccessToken();
        willDoNothing()
                .given(groupService).refuseInvitation(anyLong());

        //when,then
        mockMvc.perform(delete("/api/group/invite/{groupId}", 1L)
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

}