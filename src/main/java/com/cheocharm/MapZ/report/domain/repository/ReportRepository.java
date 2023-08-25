package com.cheocharm.MapZ.report.domain.repository;

import com.cheocharm.MapZ.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    @Query("select r from Report r where r.userId in :userId and r.diaryId in :diaryId")
    Optional<Report> findReportById(@Param("userId") Long userId, @Param("diaryId") Long diaryId);

    int countReportByDiaryId(Long diaryId);
}
