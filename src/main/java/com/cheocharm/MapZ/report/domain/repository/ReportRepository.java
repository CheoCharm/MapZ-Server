package com.cheocharm.MapZ.report.domain.repository;

import com.cheocharm.MapZ.report.domain.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    @Query("select r from ReportEntity r where r.userId in :userId and r.diaryId in :diaryId")
    Optional<ReportEntity> findReportById(@Param("userId") Long userId, @Param("diaryId") Long diaryId);

    int countReportEntityByDiaryId(Long diaryId);
}
