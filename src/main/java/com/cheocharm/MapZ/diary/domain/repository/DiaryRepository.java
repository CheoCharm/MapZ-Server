package com.cheocharm.MapZ.diary.domain.repository;

import com.cheocharm.MapZ.common.util.NativeQueryUtils;
import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryCoordinateVO;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long>, DiaryRepositoryCustom{
    List<DiaryEntity> findAllByUserEntityAndGroupEntity(UserEntity userEntity, GroupEntity groupEntity);

    @Query(value = NativeQueryUtils.GET_COORDINATE_FROM_DIARY, nativeQuery = true)
    List<DiaryCoordinateVO> findByDiaryCoordinate(@Param("point") Point point, @Param("groupIds") List<Long> groupIds, double distance);
}
