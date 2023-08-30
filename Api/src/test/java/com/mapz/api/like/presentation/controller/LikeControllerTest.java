package com.mapz.api.like.presentation.controller;

import com.mapz.api.ControllerTest;
import com.mapz.api.like.application.LikeService;
import com.mapz.api.like.presentation.dto.request.LikeDiaryRequest;
import com.mapz.api.like.presentation.dto.response.DiaryLikePeopleResponse;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.user.enums.UserProvider;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.List;

import static com.mapz.api.common.fixtures.UserFixtures.VALID_EMAIL;
import static com.mapz.api.common.fixtures.UserFixtures.VALID_PASSWORD;
import static com.mapz.api.common.fixtures.UserFixtures.VALID_USERNAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LikeControllerTest extends ControllerTest {

    @MockBean
    private LikeService likeService;

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
    @DisplayName("유저는 일기에 좋아요를 할 수 있다.")
    void likeDiary() throws Exception{

        //given
        String accessToken = getAccessToken();
        LikeDiaryRequest request = new LikeDiaryRequest(1L);
        willDoNothing()
                .given(likeService).likeDiary(any(LikeDiaryRequest.class));

        //when, then
        mockMvc.perform(post("/api/like")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저는 다이어리 좋아요 누른 사람을 조회할 수 있다.")
    void getDiaryLikePeople() throws Exception {

        //given
        String accessToken = getAccessToken();
        given(likeService.getDiaryLikePeople(anyLong()))
                .willReturn(List.of(
                        easyRandom.nextObject(DiaryLikePeopleResponse.class),
                        easyRandom.nextObject(DiaryLikePeopleResponse.class)
                ));

        //when, then
        mockMvc.perform(get("/api/like")
                        .param("diaryId", "1")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저는 좋아요를 눌렀던 다이어리를 조회할 수 있다.")
    void getMyLikeDiary() throws Exception {

        //given
        String accessToken = getAccessToken();

        //when, then
        mockMvc.perform(get("/api/like/mylike/{page}", 1)
                        .param("cursorId", "20")
                        .header(AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }
}