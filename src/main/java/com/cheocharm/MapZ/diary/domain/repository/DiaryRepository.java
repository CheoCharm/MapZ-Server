package com.cheocharm.MapZ.diary.domain.repository;

import com.cheocharm.MapZ.common.util.NativeQueryUtils;
import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryCoordinateVO;
import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.user.domain.User;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long>, DiaryRepositoryCustom{
    List<Diary> findAllByUserAndGroup(User user, Group group);

    @Query(value = NativeQueryUtils.GET_COORDINATE_FROM_DIARY, nativeQuery = true)
    List<DiaryCoordinateVO> findByDiaryCoordinate(@Param("point") Point point, @Param("groupIds") List<Long> groupIds, double distance);
}
