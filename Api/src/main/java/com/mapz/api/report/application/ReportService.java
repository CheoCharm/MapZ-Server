package com.mapz.api.report.application;

import com.mapz.api.common.exception.diary.NotFoundDiaryException;
import com.mapz.api.common.exception.report.AlreadyReportedDiary;
import com.mapz.api.common.interceptor.UserThreadLocal;
import com.mapz.domain.domains.diary.repository.DiaryRepository;
import com.mapz.domain.domains.report.entity.Report;
import com.mapz.api.report.presentation.dto.ReportRequest;
import com.mapz.domain.domains.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final DiaryRepository diaryRepository;

    private static final int REPORTED_COUNT = 2;

    @Transactional
    public void reportDiary(ReportRequest request) {
        Long userId = UserThreadLocal.get().getId();
        Long diaryId = request.getDiaryId();

        validatePresentDiary(diaryId);
        checkSameUserReportDiary(userId, diaryId);

        if (isDiaryOverReported(diaryId)) {
            return;
        }
        saveReport(userId, diaryId);
    }

    private boolean isDiaryOverReported(Long diaryId) {
        int count = reportRepository.countReportByDiaryId(diaryId);

        if (count >= REPORTED_COUNT) {
            diaryRepository.deleteById(diaryId);
            return true;
        }
        return false;
    }

    private void saveReport(Long userId, Long diaryId) {
        reportRepository.save(
                Report.of(userId, diaryId)
        );
    }

    private void checkSameUserReportDiary(Long userId, Long diaryId) {
        reportRepository.findReportById(userId, diaryId).ifPresent(Report -> {
            throw new AlreadyReportedDiary();
        });
    }

    private void validatePresentDiary(Long diaryId) {
        diaryRepository.findById(diaryId)
                .orElseThrow(NotFoundDiaryException::new);
    }
}
