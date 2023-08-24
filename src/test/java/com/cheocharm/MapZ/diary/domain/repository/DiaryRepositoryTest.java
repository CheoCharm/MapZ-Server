package com.cheocharm.MapZ.diary.domain.repository;

import com.cheocharm.MapZ.RepositoryTest;
import com.cheocharm.MapZ.comment.domain.Comment;
import com.cheocharm.MapZ.comment.domain.repository.CommentRepository;
import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryDetailVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiarySliceVO;
import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.like.domain.DiaryLike;
import com.cheocharm.MapZ.like.domain.repository.DiaryLikeRepository;
import com.cheocharm.MapZ.user.domain.User;
import com.cheocharm.MapZ.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.cheocharm.MapZ.common.fixtures.CommentFixtures.createComment;
import static com.cheocharm.MapZ.common.fixtures.DiaryFixtures.createDiary;
import static com.cheocharm.MapZ.common.fixtures.GroupFixtures.createOpenGroup;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.mapZSignUpUser;
import static com.cheocharm.MapZ.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.cheocharm.MapZ.common.util.PagingUtils.MY_DIARY_SIZE;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyCursorId;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyDescPageConfigBy;
import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class DiaryRepositoryTest {

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DiaryLikeRepository diaryLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("그룹의 다이어리를 페이징 조회한다.")
    void getDiarySlice() {

        //given
        User mapZUser = mapZSignUpUser();
        userRepository.save(mapZUser);

        Group group = createOpenGroup();
        groupRepository.save(group);

        int randomNumber = ThreadLocalRandom.current().nextInt(1, 5);
        ArrayList<Diary> diaries = new ArrayList<>();
        for (int i = 0; i < MY_DIARY_SIZE + randomNumber; i++) {
            Diary diary = createDiary(mapZUser, group);
            diaries.add(diary);

        }
        diaryRepository.saveAll(diaries);

        //when
        Slice<DiarySliceVO> firstSlice = diaryRepository.getDiarySlice(
                mapZUser.getId(),
                group.getId(),
                applyCursorId(0L),
                applyDescPageConfigBy(0, MY_DIARY_SIZE, FIELD_CREATED_AT)
        );
        Slice<DiarySliceVO> secondSlice = diaryRepository.getDiarySlice(
                mapZUser.getId(),
                group.getId(),
                applyCursorId(firstSlice.getContent().get(MY_DIARY_SIZE - 1).getDiaryId()),
                applyDescPageConfigBy(0, MY_DIARY_SIZE, FIELD_CREATED_AT)
        );

        //then
        assertThat(firstSlice.hasNext()).isTrue();
        assertThat(secondSlice.hasNext()).isFalse();
        assertThat(firstSlice.getContent().size()).isEqualTo(MY_DIARY_SIZE);
        assertThat(secondSlice.getContent().size()).isEqualTo(randomNumber);
    }

    @Test
    @DisplayName("내가 작성한 다이어리를 페이징 조회한다.")
    void findByUserId() {

        //given

        //when

        //then
    }

    @Test
    @DisplayName("userId를 조건으로 다이어리들을 삭제한다.")
    void deleteAllByUserId() {

        //given
        User mapZUser = mapZSignUpUser();
        userRepository.save(mapZUser);

        Group group = createOpenGroup();
        groupRepository.save(group);

        int randomNumber = ThreadLocalRandom.current().nextInt(5);

        ArrayList<Diary> diaries = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            diaries.add(createDiary(mapZUser, group));
        }
        diaryRepository.saveAll(diaries);

        //when
        diaryRepository.deleteAllByUserId(mapZUser.getId());
        entityManager.clear();

        //then
        for (Diary diary : diaries) {
            assertThat(diaryRepository.findById(diary.getId())).isEmpty();
        }
    }

    @Test
    @DisplayName("userId를 조건으로 Diary 엔티티들을 찾는다.")
    void findAllByUserId() {

        //given
        User mapZUser = mapZSignUpUser();
        userRepository.save(mapZUser);

        Group group = createOpenGroup();
        groupRepository.save(group);

        int randomNumber = ThreadLocalRandom.current().nextInt(5);

        ArrayList<Diary> diaries = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            diaries.add(createDiary(mapZUser, group));
        }
        diaryRepository.saveAll(diaries);

        //when
        List<Diary> actualDiaries = diaryRepository.findAllByUserId(mapZUser.getId());

        //then
        assertThat(actualDiaries.size()).isEqualTo(diaries.size());
    }

    @Test
    @DisplayName("다이어리 상세 내용을 조회한다.")
    void getDiaryDetail() {

        //given
        User mapZUser = mapZSignUpUser();
        userRepository.save(mapZUser);

        Group group = createOpenGroup();
        groupRepository.save(group);

        Diary diary = createDiary(mapZUser, group);
        diaryRepository.save(diary);

        diaryLikeRepository.save(DiaryLike.of(diary, mapZUser));

        int randomNumber = ThreadLocalRandom.current().nextInt(5);
        ArrayList<Comment> comments = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            comments.add(createComment(0L, mapZUser, diary));
        }
        commentRepository.saveAll(comments);

        //when
        DiaryDetailVO actual = diaryRepository.getDiaryDetail(diary.getId(), mapZUser.getId());

        //then
        assertThat(actual.getUsername()).isEqualTo(mapZUser.getUsername());
        assertThat(actual.getContent()).isEqualTo(diary.getContent());
        assertThat(actual.isWriter()).isTrue();
        assertThat(actual.isLike()).isTrue();
        assertThat(actual.getCommentCount()).isEqualTo(comments.size());
    }

}