package com.mapz.api.comment.application;

import com.mapz.api.comment.presentation.dto.request.CreateCommentRequest;
import com.mapz.api.common.interceptor.UserThreadLocal;
import com.mapz.domain.domains.comment.entity.Comment;
import com.mapz.domain.domains.comment.repository.CommentRepository;
import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.repository.DiaryRepository;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Autowired
    private CommentService commentService;

    @MockBean
    private CommentRepository commentRepository;

    @MockBean
    private DiaryRepository diaryRepository;

    private static MockedStatic<UserThreadLocal> utl;
    private static User user;
    private static final EasyRandom easyRandom = new EasyRandom();

    @BeforeAll
    static void beforeAll() {
        utl = mockStatic(UserThreadLocal.class);
        user = easyRandom.nextObject(User.class);
        utl.when(UserThreadLocal::get).thenReturn(CommentServiceTest.user);
    }

    @AfterAll
    static void afterAll() {
        utl.close();
    }

    @Test
    @DisplayName("다이어리에 댓글을 작성할 수 있다.")
    void createComment() {

        //given
        Diary diary = Diary.builder()
                .id(ThreadLocalRandom.current().nextLong() + 1)
                .build();
        CreateCommentRequest request = new CreateCommentRequest(
                "content", ThreadLocalRandom.current().nextLong() + 1, diary.getId()
        );
        given(diaryRepository.findById(request.getDiaryId()))
                .willReturn(Optional.of(diary));

        //when
        commentService.createComment(request);

        //then
        then(commentRepository).should().save(any(Comment.class));
    }

    @Test
    @DisplayName("다이어리의 댓글을 삭제할때 상위댓글이 있을땐 삭제요청 댓글만 삭제된다.")
    void deleteComment() {

        //given
        Long parentId = ThreadLocalRandom.current().nextLong() + 1;
        Long commentId = ThreadLocalRandom.current().nextLong() + 1;

        //when
        commentService.deleteComment(parentId, commentId);

        //then
        then(commentRepository).should().deleteById(anyLong());
        then(commentRepository).should(never()).deleteAllByIdInQuery(anyLong());
    }

    @Test
    @DisplayName("다이어리의 댓글이 최상위인 경우 대댓글도 삭제된다")
    void childCommentDeleteWhenRootCommentDelete() {

        //given
        Long parentId = 0L;
        Long commentId = ThreadLocalRandom.current().nextLong() + 1;

        //when
        commentService.deleteComment(parentId, commentId);

        //then
        then(commentRepository).should().deleteById(anyLong());
        then(commentRepository).should().deleteAllByIdInQuery(anyLong());
    }
}