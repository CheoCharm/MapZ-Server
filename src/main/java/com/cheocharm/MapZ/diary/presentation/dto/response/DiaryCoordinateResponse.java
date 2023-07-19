package com.cheocharm.MapZ.diary.presentation.dto.response;

import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryCoordinateVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class DiaryCoordinateResponse {
    private Long diaryId;
    private Double longitude;
    private Double latitude;

    public static List<DiaryCoordinateResponse> of(List<DiaryCoordinateVO> diaryCoordinateVOS) {
        return diaryCoordinateVOS.stream()
                .map(diaryCoordinateVO -> new DiaryCoordinateResponse(
                        diaryCoordinateVO.getDiaryId(),
                        diaryCoordinateVO.getLongitude(),
                        diaryCoordinateVO.getLatitude()
                ))
                .collect(Collectors.toList());
    }
}
