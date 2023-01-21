package com.cheocharm.MapZ.diary.domain;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.diary.domain.dto.LikeDiaryDto;
import com.cheocharm.MapZ.diary.domain.dto.WriteDiaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "DiaryController")
@RequiredArgsConstructor
@RequestMapping("/api/diary")
@RestController
public class DiaryController {

    private final DiaryService diaryService;

    @Operation(description = "일기 좋아요")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PutMapping("/like")
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
}
