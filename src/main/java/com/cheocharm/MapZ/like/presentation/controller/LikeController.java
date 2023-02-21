package com.cheocharm.MapZ.like.presentation.controller;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.like.presentation.dto.response.DiaryLikePeopleResponse;
import com.cheocharm.MapZ.like.presentation.dto.request.LikeDiaryRequest;
import com.cheocharm.MapZ.like.presentation.dto.response.MyLikeDiaryResponse;
import com.cheocharm.MapZ.like.application.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "DiaryController")
@RequiredArgsConstructor
@RequestMapping("/api/like")
@RestController
public class LikeController {

    private final LikeService likeService;

    @Operation(description = "일기 좋아요")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PostMapping
    public CommonResponse<?> likeDiary(@Parameter @RequestBody @Valid LikeDiaryRequest likeDiaryRequest) {
        likeService.likeDiary(likeDiaryRequest);
        return CommonResponse.success();
    }

    @Operation(description = "일기 좋아요 누른 명단 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping
    public CommonResponse<List<DiaryLikePeopleResponse>> getDiaryLikePeople(@Parameter @RequestParam Long diaryId) {
        return CommonResponse.success(likeService.getDiaryLikePeople(diaryId));
    }

    @Operation(description = "좋아요한 일기 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/mylike")
    public CommonResponse<MyLikeDiaryResponse> getMyLikeDiary(@Parameter @RequestParam Long cursorId, @RequestParam Integer page) {
        return CommonResponse.success(likeService.getMyLikeDiary(cursorId, page));
    }
}
