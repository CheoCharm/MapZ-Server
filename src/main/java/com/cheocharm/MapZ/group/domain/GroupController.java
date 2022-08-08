package com.cheocharm.MapZ.group.domain;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.group.domain.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

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
            @RequestPart(value = "dto") @Valid CreateGroupDto createGroupDto,
            @RequestPart(value = "file", required = false) MultipartFile multipartFile) {
        groupService.createGroup(createGroupDto, multipartFile);
        return CommonResponse.success();
    }

    @Operation(description = "그룹 선택을 위한 그룹 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping
    public CommonResponse<GetGroupListDto> getGroup(@RequestBody @Valid SearchGroupDto searchGroupDto) {
        return CommonResponse.success(groupService.getGroup(searchGroupDto));
    }

    @Operation(description = "그룹 공개여부 변경")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PatchMapping("/status")
    public CommonResponse<?> changeGroupStatus(@RequestBody @Valid ChangeGroupStatusDto changeGroupStatusDto) {
        groupService.changeGroupStatus(changeGroupStatusDto);
        return CommonResponse.success();
    }

    @Operation(description = "그룹 참가 신청")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PostMapping("/join")
    public CommonResponse<?> joinGroup(@RequestBody @Valid JoinGroupDto joinGroupDto) {
        groupService.joinGroup(joinGroupDto);
        return CommonResponse.success();
    }

    @Operation(description = "그룹 참가 신청 변경")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PatchMapping("/join")
    public CommonResponse<?> changeInvitationStatus(@RequestBody @Valid ChangeInvitationStatusDto changeInvitationStatusDto) {
        groupService.changeInvitationStatus(changeInvitationStatusDto);
        return CommonResponse.success();
    }

    @Operation(description = "그룹 나가기")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PatchMapping("/exit")
    public CommonResponse<?> exitGroup(@RequestBody @Valid ExitGroupDto exitGroupDto) {
        groupService.exitGroup(exitGroupDto);
        return CommonResponse.success();
    }

    @Operation(description = "그룹장 위임")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PatchMapping("/chief")
    public CommonResponse<?> changeChief(@RequestBody @Valid ChangeChiefDto changeChiefDto) {
        groupService.changeChief(changeChiefDto);
        return CommonResponse.success();
    }
}
