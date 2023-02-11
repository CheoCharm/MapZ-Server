package com.cheocharm.MapZ.report.domain;

import com.cheocharm.MapZ.common.exception.diary.NotFoundDiaryException;
import com.cheocharm.MapZ.common.exception.report.AlreadyReportedDiary;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.diary.domain.respository.DiaryRepository;
import com.cheocharm.MapZ.report.domain.dto.ReportDto;
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
    public void reportDiary(ReportDto reportDto) {
        final UserEntity userEntity = UserThreadLocal.get();

        Long userId = userEntity.getId();
        Long diaryId = reportDto.getDiaryId();

        diaryRepository.findById(diaryId).orElseThrow(() -> new NotFoundDiaryException());

        reportRepository.findReportById(userId, diaryId).ifPresent(ReportEntity -> {
            throw new AlreadyReportedDiary();
        });

        int count = reportRepository.countReportEntityByDiaryId(diaryId);

        if (count >= 2) {
            diaryRepository.deleteById(diaryId);
        } else {
            ReportEntity reportEntity = ReportEntity.builder()
                    .userId(userId)
                    .diaryId(diaryId)
                    .build();

            reportRepository.save(reportEntity);
        }
    }
}
