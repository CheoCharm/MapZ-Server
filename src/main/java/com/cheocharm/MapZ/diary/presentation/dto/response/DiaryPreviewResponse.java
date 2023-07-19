package com.cheocharm.MapZ.diary.presentation.dto.response;

import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryCoordinateVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryImagePreviewVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;
import java.util.Objects;
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
        for (DiaryCoordinateVO diaryCoordinateVO : diaryCoordinateVOS) {
            if (Objects.equals(diaryId, diaryCoordinateVO.getDiaryId())) {
                return new Coordinate(diaryCoordinateVO.getLatitude(), diaryCoordinateVO.getLongitude());
            }
        }
        return null;
    }
}
