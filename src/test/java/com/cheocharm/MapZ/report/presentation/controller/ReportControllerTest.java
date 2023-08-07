package com.cheocharm.MapZ.report.presentation.controller;

import com.cheocharm.MapZ.ControllerTest;
import com.cheocharm.MapZ.common.exception.report.AlreadyReportedDiary;
import com.cheocharm.MapZ.report.application.ReportService;
import com.cheocharm.MapZ.report.presentation.dto.ReportRequest;
import com.cheocharm.MapZ.user.domain.User;
import com.cheocharm.MapZ.user.domain.UserProvider;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static com.cheocharm.MapZ.common.fixtures.UserFixtures.VALID_EMAIL;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.VALID_PASSWORD;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.VALID_USERNAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportControllerTest extends ControllerTest {

    @MockBean
    private ReportService reportService;

    private static EasyRandom easyRandom = new EasyRandom();
    private static User user;

    @BeforeEach
    void beforeEach() {
        user = userRepository.save(
                User.builder()
                        .username(VALID_USERNAME)
                        .email(VALID_EMAIL)
                        .password(VALID_PASSWORD)
                        .refreshToken("refreshTokenValue")
                        .userProvider(UserProvider.MAPZ)
                        .build()
        );
    }

    @Test
    @DisplayName("유저는 게시글을 신고할 수 있다.")
    void reportDiary() throws Exception {

        //given
        String accessToken = getAccessToken();
        ReportRequest request = new ReportRequest(1L);
        willDoNothing()
                .given(reportService).reportDiary(any(ReportRequest.class));

        //when, then
        mockMvc.perform(post("/api/report")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("유저는 중복해서 같은 다이어리를 신고하면 400을 반환한다.")
    void reportSameDiaryReturn400() throws Exception {

        //given
        String accessToken = getAccessToken();
        ReportRequest request = new ReportRequest(1L);
        willThrow(new AlreadyReportedDiary())
                .given(reportService).reportDiary(any(ReportRequest.class));
        //when, then
        mockMvc.perform(post("/api/report")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}