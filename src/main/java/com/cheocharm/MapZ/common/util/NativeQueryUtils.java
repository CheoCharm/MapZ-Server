package com.cheocharm.MapZ.common.util;

public final class NativeQueryUtils {
    private NativeQueryUtils() {
    }

    public static final String GET_COORDINATE_FROM_DIARY = "SELECT " +
            "diary.id as diaryId, Y(diary.point) as longitude, X(diary.point) as latitude " +
            "FROM Diary diary " +
            "WHERE ST_Distance_Sphere(point, :point) <= :distance " +
            "AND diary.group_id IN (:groupIds)";
}
