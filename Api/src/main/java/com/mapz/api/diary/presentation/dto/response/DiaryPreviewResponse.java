package com.mapz.api.diary.presentation.dto.response;

import com.mapz.api.common.exception.diary.NoMatchDiaryIdException;
import com.mapz.domain.domains.diary.vo.DiaryCoordinateVO;
import com.mapz.domain.domains.diary.vo.DiaryImagePreviewVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class DiaryPreviewResponse {
    private Long diaryId;
    private String previewImageURL;
    private Double longitude;
    private Double latitude;

    public static List<DiaryPreviewResponse> of(List<DiaryImagePreviewVO> previewImageVOS, List<DiaryCoordinateVO> diaryCoordinateVOS) {
        return previewImageVOS.stream()
                .map(diaryImagePreviewVO -> {
                    final Coordinate coordinate = getCoordinate(diaryImagePreviewVO.getDiaryId(), diaryCoordinateVOS);
                    return new DiaryPreviewResponse(
                            diaryImagePreviewVO.getDiaryId(),
                            diaryImagePreviewVO.getMainDiaryImageURL(),
                            coordinate.getY(),
                            coordinate.getX()
                    );
                })
                .collect(Collectors.toList());
    }

    private static Coordinate getCoordinate(Long diaryId, List<DiaryCoordinateVO> diaryCoordinateVOS) {
        return diaryCoordinateVOS.stream()
                .filter(vo -> diaryId.equals(vo.getDiaryId()))
                .map(vo -> new Coordinate(vo.getLatitude(), vo.getLongitude()))
                .findAny()
                .orElseThrow(NoMatchDiaryIdException::new);
    }
}
