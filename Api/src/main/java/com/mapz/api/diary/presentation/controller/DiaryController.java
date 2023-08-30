package com.mapz.api.diary.presentation.controller;

import com.mapz.api.common.CommonResponse;
import com.mapz.api.diary.application.DiaryService;
import com.mapz.api.diary.presentation.dto.request.WriteDiaryImageRequest;
import com.mapz.api.diary.presentation.dto.request.WriteDiaryRequest;
import com.mapz.api.diary.presentation.dto.response.DiaryCoordinateResponse;
import com.mapz.api.diary.presentation.dto.response.DiaryDetailResponse;
import com.mapz.api.diary.presentation.dto.response.DiaryPreviewDetailResponse;
import com.mapz.api.diary.presentation.dto.response.DiaryPreviewResponse;
import com.mapz.api.diary.presentation.dto.response.GetDiaryListResponse;
import com.mapz.api.diary.presentation.dto.response.MyDiaryResponse;
import com.mapz.api.diary.presentation.dto.response.WriteDiaryImageResponse;
import com.mapz.api.diary.presentation.dto.response.WriteDiaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    @DeleteMapping("/image/{diaryId}")
    public CommonResponse<?> deleteTempDiary(@Parameter @PathVariable Long diaryId) {
        diaryService.deleteTempDiary(diaryId);
        return CommonResponse.success();
    }

    @Operation(description = "일기 페이징 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/{page}")
    public CommonResponse<GetDiaryListResponse> getDiary(
            @Parameter @RequestParam Long groupId, @RequestParam Long cursorId, @PathVariable Integer page) {
        return CommonResponse.success(diaryService.getDiary(groupId, cursorId, page));
    }

    @Operation(description = "일기 삭제")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @DeleteMapping("/{diaryId}")
    public CommonResponse<?> deleteDiary(@Parameter @PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return CommonResponse.success();
    }

    @Operation(description = "내가 쓴 일기 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/my/{page}")
    public CommonResponse<MyDiaryResponse> getMyDiary(@PathVariable Integer page, @Parameter @RequestParam Long cursorId) {
        return CommonResponse.success(diaryService.getMyDiary(page, cursorId));
    }

    @Operation(description = "일기 디테일 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/detail/{diaryId}")
    public CommonResponse<DiaryDetailResponse> getDiaryDetail(@Parameter @PathVariable Long diaryId) {
        return CommonResponse.success(diaryService.getDiaryDetail(diaryId));
    }

    @Operation(description = "일기 줌 레벨 낮은 경우(반경이 넓은 경우)")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/low")
    public CommonResponse<List<DiaryCoordinateResponse>> getDiaryCoordinate(@Parameter @RequestParam Double longitude, @RequestParam Double latitude) {
        return CommonResponse.success(diaryService.getDiaryCoordinate(longitude, latitude));
    }

    @Operation(description = "일기 중 레벨 높은 경우(반경이 좁은 경우)")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/high")
    public CommonResponse<List<DiaryPreviewResponse>> getDiaryByMap(@Parameter @RequestParam Double longitude, @RequestParam Double latitude, @RequestParam Double zoomLevel) {
        return CommonResponse.success(diaryService.getDiaryByMap(longitude, latitude, zoomLevel));
    }

    @Operation(description = "지도에서 일기 클릭해서 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/preview/{diaryId}")
    public CommonResponse<DiaryPreviewDetailResponse> getDiaryPreviewDetail(@Parameter @PathVariable Long diaryId) {
        return CommonResponse.success(diaryService.getDiaryPreviewDetail(diaryId));
    }
}
