package com.mapz.domain.domains.diary.repository;

import com.mapz.domain.common.util.NativeQueryUtils;
import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.vo.DiaryCoordinateVO;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long>, DiaryRepositoryCustom{

    @Query(value = NativeQueryUtils.GET_COORDINATE_FROM_DIARY, nativeQuery = true)
    List<DiaryCoordinateVO> findByDiaryCoordinate(@Param("point") Point point, @Param("groupIds") List<Long> groupIds, double distance);
}
