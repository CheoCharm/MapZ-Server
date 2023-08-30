package com.mapz.api.comment.domain.repository;

import com.mapz.api.RepositoryTest;
import com.mapz.api.common.fixtures.CommentFixtures;
import com.mapz.api.common.fixtures.DiaryFixtures;
import com.mapz.api.common.fixtures.GroupFixtures;
import com.mapz.api.common.fixtures.UserFixtures;
import com.mapz.domain.domains.comment.entity.Comment;
import com.mapz.domain.domains.comment.vo.CommentVO;
import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.repository.DiaryRepository;
import com.mapz.domain.domains.group.entity.Group;
import com.mapz.domain.domains.group.repository.GroupRepository;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.user.repository.UserRepository;
import com.mapz.domain.domains.comment.repository.CommentRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static com.mapz.api.common.util.PagingUtils.COMMENT_SIZE;
import static com.mapz.api.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.mapz.api.common.util.PagingUtils.applyAscPageConfigBy;
import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Diary 엔티티 리스트를 파라미터로 댓글들을 삭제한다.")
    void deleteAllByDiaries() {

        //given
        User user = UserFixtures.googleSignUpUser();
        Group group = GroupFixtures.createOpenGroup();

        userRepository.save(user);
        groupRepository.save(group);
        Diary firstDiary = DiaryFixtures.createDiary(user, group);
        Diary secondDiary = DiaryFixtures.createDiary(user, group);

        List<Diary> diaries = List.of(firstDiary, secondDiary);
        diaryRepository.saveAll(diaries);

        Comment firstComment = CommentFixtures.createComment(0L, user, firstDiary);
        Comment secondComment = CommentFixtures.createComment(0L, user, secondDiary);
        List<Comment> comments = List.of(firstComment, secondComment);

        commentRepository.saveAll(comments);
        //when
        commentRepository.deleteAllByDiaries(diaries);
        entityManager.clear();

        //then
        Optional<Comment> commentOptional = commentRepository.findById(firstComment.getId());
        Assertions.assertThat(commentOptional).isEmpty();
        assertThat(commentRepository.findById(secondComment.getId())).isEmpty();

    }

    @Test
    @DisplayName("다이어리에 대한 댓글을 조회한다.")
    void findComment() {

        //given
        User user = UserFixtures.googleSignUpUser();
        userRepository.save(user);

        Group group = GroupFixtures.createOpenGroup();
        groupRepository.save(group);

        Diary diary = DiaryFixtures.createDiary(user, group);
        diaryRepository.save(diary);

        int randomNumber = ThreadLocalRandom.current().nextInt(1, 5);
        ArrayList<Comment> comments = new ArrayList<>();
        for (int i = 0; i < COMMENT_SIZE + randomNumber; i++) {
            comments.add(CommentFixtures.createComment(0L, user, diary));
        }
        commentRepository.saveAll(comments);

        //when
        Slice<CommentVO> firstSlice = commentRepository.findByDiaryId(
                user.getId(),
                diary.getId(),
                0L,
                applyAscPageConfigBy(0, COMMENT_SIZE, FIELD_CREATED_AT)
        );
        Long nextCursorId = firstSlice.getContent().get(COMMENT_SIZE - 1).getCommentId();
        Slice<CommentVO> secondSlice = commentRepository.findByDiaryId(
                user.getId(),
                diary.getId(),
                nextCursorId,
                applyAscPageConfigBy(1, COMMENT_SIZE, FIELD_CREATED_AT)
        );

        //then
        assertThat(firstSlice.getContent().size()).isEqualTo(COMMENT_SIZE);
        assertThat(secondSlice.getContent().size()).isEqualTo(randomNumber);
        assertThat(firstSlice.hasNext()).isTrue();
        assertThat(secondSlice.hasNext()).isFalse();
    }
}