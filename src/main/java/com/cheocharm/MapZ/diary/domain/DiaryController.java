package com.cheocharm.MapZ.diary.domain;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.diary.domain.dto.LikeDiaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @PutMapping("/like")
    public CommonResponse<?> likeDiary(@Parameter @RequestBody @Valid LikeDiaryDto likeDiaryDto) {
        diaryService.likeDiary(likeDiaryDto);
        return CommonResponse.success();
    }
}
