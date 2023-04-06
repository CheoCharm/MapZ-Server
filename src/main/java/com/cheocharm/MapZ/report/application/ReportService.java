package com.cheocharm.MapZ.report.application;

import com.cheocharm.MapZ.common.exception.diary.NotFoundDiaryException;
import com.cheocharm.MapZ.common.exception.report.AlreadyReportedDiary;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.diary.domain.repository.DiaryRepository;
import com.cheocharm.MapZ.report.domain.ReportEntity;
import com.cheocharm.MapZ.report.presentation.dto.ReportRequest;
import com.cheocharm.MapZ.report.domain.repository.ReportRepository;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final DiaryRepository diaryRepository;

    @Transactional
    public void reportDiary(ReportRequest reportRequest) {
        final UserEntity userEntity = UserThreadLocal.get();

        Long userId = userEntity.getId();
        Long diaryId = reportRequest.getDiaryId();

        diaryRepository.findById(diaryId).orElseThrow(NotFoundDiaryException::new);

        reportRepository.findReportById(userId, diaryId).ifPresent(ReportEntity -> {
            throw new AlreadyReportedDiary();
        });

        int count = reportRepository.countReportEntityByDiaryId(diaryId);

        if (count >= 2) {
            diaryRepository.deleteById(diaryId);
            return;
        }

        ReportEntity reportEntity = ReportEntity.builder()
                .userId(userId)
                .diaryId(diaryId)
                .build();

        reportRepository.save(reportEntity);

    }
}
