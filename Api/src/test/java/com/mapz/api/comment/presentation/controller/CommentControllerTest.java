package com.mapz.api.comment.presentation.controller;

import com.mapz.api.ControllerTest;
import com.mapz.api.comment.application.CommentService;
import com.mapz.api.comment.presentation.dto.request.CreateCommentRequest;
import com.mapz.api.common.fixtures.CommentFixtures;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.user.enums.UserProvider;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static com.mapz.api.common.fixtures.UserFixtures.VALID_EMAIL;
import static com.mapz.api.common.fixtures.UserFixtures.VALID_PASSWORD;
import static com.mapz.api.common.fixtures.UserFixtures.VALID_USERNAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentControllerTest extends ControllerTest {

    @MockBean
    private CommentService commentService;

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
    @DisplayName("유저는 댓글을 작성할 수 있다.")
    void createComment() throws Exception{

        //given
        String accessToken = getAccessToken();
        CreateCommentRequest request = new CreateCommentRequest("content", 0L, 1L);
        willDoNothing()
                .given(commentService).createComment(any(CreateCommentRequest.class));
        //when, then
        mockMvc.perform(post("/api/comment")
                        .header(ControllerTest.AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저는 댓글 글자수 제한을 넘어서 댓글을 작성하면 400을 반환한다.")
    void createCommentContentLimit() throws Exception{

        //given
        String accessToken = getAccessToken();
        CreateCommentRequest request = new CreateCommentRequest(CommentFixtures.COMMENT_CONTENT_EXCEED_LIMIT, 0L, 1L);

        //when, then
        mockMvc.perform(post("/api/comment")
                        .header(ControllerTest.AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저는 자신이 작성한 댓글을 삭제할 수 있다.")
    void deleteComment() throws Exception {

        //given
        String accessToken = getAccessToken();
        willDoNothing()
                .given(commentService).deleteComment(anyLong(), anyLong());

        //when, then
        mockMvc.perform(delete("/api/comment/{parentId}/{commentId}", 0L, 1L)
                        .header(ControllerTest.AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("유저는 다이어리의 댓글을 조회할 수 있다.")
    void getComment() throws Exception {

        //given
        String accessToken = getAccessToken();

        //when, then
        mockMvc.perform(get("/api/comment/{diaryId}/{page}",1L, 1)
                        .param("cursorId", "21")
                        .header(ControllerTest.AUTHORIZATION_HEADER_NAME, accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }
}