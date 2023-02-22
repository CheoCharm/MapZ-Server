package com.cheocharm.MapZ.group.presentation.controller;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.group.application.GroupService;
import com.cheocharm.MapZ.group.presentation.dto.request.ChangeChiefRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.ChangeGroupInfoRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.ChangeInvitationStatusRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.CreateGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.ExitGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.InviteGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.JoinGroupRequest;
import com.cheocharm.MapZ.group.presentation.dto.request.KickUserRequest;
import com.cheocharm.MapZ.group.presentation.dto.response.GetMyGroupResponse;
import com.cheocharm.MapZ.group.presentation.dto.response.GroupMemberResponse;
import com.cheocharm.MapZ.group.presentation.dto.response.JoinGroupResultResponse;
import com.cheocharm.MapZ.group.presentation.dto.response.PagingGroupListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "GroupController")
@RequiredArgsConstructor
@RequestMapping("/api/group")
@RestController
public class GroupController {

    private final GroupService groupService;

    @Operation(description = "그룹 생성")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PostMapping
    public CommonResponse<?> createGroup(
            @RequestPart(value = "dto") @Valid CreateGroupRequest createGroupRequest,
            @RequestPart(value = "file", required = false) MultipartFile multipartFile) {
        groupService.createGroup(createGroupRequest, multipartFile);
        return CommonResponse.success();
    }

    @Operation(description = "그룹 참가를 위한 그룹 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping
    public CommonResponse<PagingGroupListResponse> getGroup(@RequestParam String groupName, @RequestParam Long cursorId, @RequestParam Integer page) {
        return CommonResponse.success(groupService.getGroup(groupName, cursorId, page));
    }

    @Operation(description = "그룹 정보 변경")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PatchMapping
    public CommonResponse<?> changeGroupInfo(
            @RequestPart(value = "dto") @Valid ChangeGroupInfoRequest changeGroupInfoRequest,
            @RequestPart(value="file", required = false) MultipartFile multipartFile) {
        groupService.changeGroupInfo(changeGroupInfoRequest, multipartFile);
        return CommonResponse.success();
    }

    @Operation(description = "그룹 참가 신청")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PostMapping("/join")
    public CommonResponse<JoinGroupResultResponse> joinGroup(@RequestBody @Valid JoinGroupRequest joinGroupRequest) {
        return CommonResponse.success(groupService.joinGroup(joinGroupRequest));
    }

    @Operation(description = "그룹 참가 신청 변경")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PatchMapping("/join")
    public CommonResponse<?> changeInvitationStatus(@RequestBody @Valid ChangeInvitationStatusRequest changeInvitationStatusRequest) {
        groupService.changeInvitationStatus(changeInvitationStatusRequest);
        return CommonResponse.success();
    }

    @Operation(description = "그룹 나가기")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PatchMapping("/exit")
    public CommonResponse<?> exitGroup(@RequestBody @Valid ExitGroupRequest exitGroupRequest) {
        groupService.exitGroup(exitGroupRequest);
        return CommonResponse.success();
    }

    @Operation(description = "그룹장 위임")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PatchMapping("/chief")
    public CommonResponse<?> changeChief(@RequestBody @Valid ChangeChiefRequest changeChiefRequest) {
        groupService.changeChief(changeChiefRequest);
        return CommonResponse.success();
    }

    @Operation(description = "그룹 초대")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PostMapping("/invite")
    public CommonResponse<?> inviteUser(@RequestBody @Valid InviteGroupRequest inviteGroupRequest) {
        groupService.inviteUser(inviteGroupRequest);
        return CommonResponse.success();
    }

    @Operation(description = "내 그룹 검색")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/mygroup")
    public CommonResponse<List<GetMyGroupResponse>> searchMyGroup() {
        return CommonResponse.success(groupService.searchMyGroup());
    }

    @Operation(description = "그룹 멤버 관리시 멤버 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/member")
    public CommonResponse<List<GroupMemberResponse>> getMember(@RequestParam Long groupId) {
        return CommonResponse.success(groupService.getMember(groupId));
    }

    @Operation(description = "그룹에서 내보내기")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @DeleteMapping("/user")
    public CommonResponse<?> kickUser(@RequestBody @Valid KickUserRequest kickUserRequest) {
        groupService.kickUser(kickUserRequest);
        return CommonResponse.success();
    }
}