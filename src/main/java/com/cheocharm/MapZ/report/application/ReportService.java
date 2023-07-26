package com.cheocharm.MapZ.report.application;

import com.cheocharm.MapZ.common.exception.diary.NotFoundDiaryException;
import com.cheocharm.MapZ.common.exception.report.AlreadyReportedDiary;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.diary.domain.repository.DiaryRepository;
import com.cheocharm.MapZ.report.domain.Report;
import com.cheocharm.MapZ.report.presentation.dto.ReportRequest;
import com.cheocharm.MapZ.report.domain.repository.ReportRepository;
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
