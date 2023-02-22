package com.cheocharm.MapZ.diary.presentation.controller;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.diary.application.DiaryService;
import com.cheocharm.MapZ.diary.presentation.dto.request.DeleteDiaryRequest;
import com.cheocharm.MapZ.diary.presentation.dto.request.DeleteTempDiaryRequest;
import com.cheocharm.MapZ.diary.presentation.dto.request.WriteDiaryImageRequest;
import com.cheocharm.MapZ.diary.presentation.dto.request.WriteDiaryRequest;
import com.cheocharm.MapZ.diary.presentation.dto.response.MyDiaryResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.WriteDiaryImageResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.WriteDiaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "DiaryController")
@RequiredArgsConstructor
@RequestMapping("/api/diary")
@RestController
public class DiaryController {

    private final DiaryService diaryService;

    @Operation(description = "일기 작성시 이미지 데이터 생성 (1차 요청)")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PostMapping("/image")
    public CommonResponse<WriteDiaryImageResponse> writeDiaryImage(
            @RequestPart(value = "dto") @Valid WriteDiaryImageRequest writeDiaryImageRequest,
            @Parameter @RequestPart(value = "files") List<MultipartFile> files) {
        return CommonResponse.success(diaryService.writeDiaryImage(writeDiaryImageRequest, files));
    }

    @Operation(description = "일기 본문까지 작성 완료 후 업데이트 (2차 요청)")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PostMapping("/write")
    public CommonResponse<WriteDiaryResponse> writeDiary(@Parameter @RequestBody @Valid WriteDiaryRequest writeDiaryRequest) {
        return CommonResponse.success(diaryService.writeDiary(writeDiaryRequest));
    }

    @Operation(description = "예외 사항으로 인해 일기 작성을 취소하면 일기, 이미지 데이터 삭제 (1차 요청 후 상황에 맞게 호출) ")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @DeleteMapping("/image")
    public CommonResponse<?> deleteTempDiary(@Parameter @RequestBody @Valid DeleteTempDiaryRequest deleteTempDiaryRequest) {
        diaryService.deleteTempDiary(deleteTempDiaryRequest);
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
    public CommonResponse<?> deleteDiary(@Parameter @RequestBody DeleteDiaryRequest deleteDiaryRequest) {
        diaryService.deleteDiary(deleteDiaryRequest);
        return CommonResponse.success();
    }

    @Operation(description = "내가 쓴 일기 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/my")
    public CommonResponse<MyDiaryResponse> getMyDiary(@Parameter @RequestParam Long cursorId, @RequestParam Integer page) {
        return CommonResponse.success(diaryService.getMyDiary(cursorId, page));
    }
}