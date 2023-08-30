package com.mapz.api.report.application;

import com.mapz.api.common.exception.diary.NotFoundDiaryException;
import com.mapz.api.common.exception.report.AlreadyReportedDiary;
import com.mapz.api.common.interceptor.UserThreadLocal;
import com.mapz.api.report.presentation.dto.ReportRequest;
import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.repository.DiaryRepository;
import com.mapz.domain.domains.report.entity.Report;
import com.mapz.domain.domains.report.repository.ReportRepository;
import com.mapz.domain.domains.user.entity.User;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @MockBean
    private ReportRepository reportRepository;

    @MockBean
    private DiaryRepository diaryRepository;

    private static MockedStatic<UserThreadLocal> utl;
    private static User user;
    private static final EasyRandom easyRandom = new EasyRandom();

    private static final int REPORTED_COUNT = 2;

    @BeforeAll
    static void beforeAll() {
        utl = mockStatic(UserThreadLocal.class);
        user = easyRandom.nextObject(User.class);
        utl.when(UserThreadLocal::get).thenReturn(ReportServiceTest.user);
    }

    @AfterAll
    static void afterAll() {
        utl.close();
    }

    @Test
    @DisplayName("유저는 부적절한 다이어리를 신고할 수 있다.")
    void reportDiary() {
        //given
        Diary diary = Diary.builder()
                .id(ThreadLocalRandom.current().nextLong())
                .build();
        ReportRequest request = new ReportRequest(diary.getId());
        given(diaryRepository.findById(request.getDiaryId()))
                .willReturn(Optional.of(diary));
        given(reportRepository.findReportById(user.getId(), request.getDiaryId()))
                .willReturn(Optional.empty());
        given(reportRepository.countReportByDiaryId(request.getDiaryId()))
                .willReturn(REPORTED_COUNT - 1);

        //when
        reportService.reportDiary(request);

        //then
        then(reportRepository).should().save(any(Report.class));
    }

    @Test
    @DisplayName("신고하려는 다이어리가 없으면 실패한다.")
    void reportDiaryFailWhenDiaryNotPresent() {

        //given
        ReportRequest request = new ReportRequest(ThreadLocalRandom.current().nextLong());
        given(diaryRepository.findById(request.getDiaryId()))
                .willReturn(Optional.empty());

        //when,then
        assertThatThrownBy(()-> reportService.reportDiary(request))
                .isInstanceOf(NotFoundDiaryException.class);
    }

    @Test
    @DisplayName("한 유저가 같은 다이어리를 여러번 신고할 수 없다.")
    void UserCannotReportSameDiarySeveralTime() {

        //given
        Diary diary = Diary.builder()
                .id(ThreadLocalRandom.current().nextLong())
                .build();
        Report report = Report.builder()
                .diaryId(diary.getId())
                .userId(user.getId())
                .build();
        ReportRequest request = new ReportRequest(diary.getId());
        given(diaryRepository.findById(request.getDiaryId()))
                .willReturn(Optional.of(diary));
        given(reportRepository.findReportById(user.getId(), request.getDiaryId()))
                .willReturn(Optional.of(report));

        //when,then
        assertThatThrownBy(() -> reportService.reportDiary(request))
                .isInstanceOf(AlreadyReportedDiary.class);
    }

    @Test
    @DisplayName("신고 당한 횟수가 여러번이면 작성된 다이어리는 삭제된다.")
    void diaryDeleteWhenDiaryReportedSeveralTime() {

        //given
        Diary diary = Diary.builder()
                .id(ThreadLocalRandom.current().nextLong())
                .build();
        ReportRequest request = new ReportRequest(diary.getId());
        given(diaryRepository.findById(request.getDiaryId()))
                .willReturn(Optional.of(diary));
        given(reportRepository.findReportById(user.getId(), request.getDiaryId()))
                .willReturn(Optional.empty());
        given(reportRepository.countReportByDiaryId(request.getDiaryId()))
                .willReturn(REPORTED_COUNT + 1);

        //when
        reportService.reportDiary(request);

        //then
        then(diaryRepository).should().deleteById(request.getDiaryId());
    }
}