package com.cheocharm.MapZ.diary.domain;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.diary.domain.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "DiaryController")
@RequiredArgsConstructor
@RequestMapping("/api/diary")
@RestController
public class DiaryController {

    private final DiaryService diaryService;

    @Operation(description = "일기 좋아요")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PostMapping("/like")
    public CommonResponse<?> likeDiary(@Parameter @RequestBody @Valid LikeDiaryDto likeDiaryDto) {
        diaryService.likeDiary(likeDiaryDto);
        return CommonResponse.success();
    }

    @Operation(description = "일기 작성")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PostMapping
    public CommonResponse<?> writeDiary(@Parameter @RequestBody @Valid WriteDiaryDto writeDiaryDto) {
        diaryService.writeDiary(writeDiaryDto);
        return CommonResponse.success();
    }

    @Operation(description = "일기 페이징 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping
    public CommonResponse<?> getDiary(@Parameter @RequestParam Long groupId) {
        return CommonResponse.success(diaryService.getDiary(groupId));
    }

    @Operation(description = "일기 삭제")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @DeleteMapping
    public CommonResponse<?> deleteDiary(@Parameter @RequestBody DeleteDiaryDto deleteDiaryDto) {
        diaryService.deleteDiary(deleteDiaryDto);
        return CommonResponse.success();
    }

    @Operation(description = "일기 좋아요 누른 명단 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/like")
    public CommonResponse<List<DiaryLikePeopleDto>> getDiaryLikePeople(@Parameter @RequestParam Long diaryId) {
        return CommonResponse.success(diaryService.getDiaryLikePeople(diaryId));
    }

    @Operation(description = "좋아요한 일기 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/mylike")
    public CommonResponse<MyLikeDiaryDto> getMyLikeDiary(@Parameter @RequestParam Long cursorId, @RequestParam Integer page) {
        return CommonResponse.success(diaryService.getMyLikeDiary(cursorId, page));
    }

    @Operation(description = "내가 쓴 일기 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/my")
    public CommonResponse<MyDiaryDto> getMyDiary(@Parameter @RequestParam Long cursorId, @RequestParam Integer page) {
        return CommonResponse.success(diaryService.getMyDiary(cursorId, page));
    }
}
