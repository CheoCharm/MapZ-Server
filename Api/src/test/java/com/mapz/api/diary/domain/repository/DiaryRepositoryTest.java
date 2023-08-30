package com.mapz.api.diary.domain.repository;

import com.mapz.api.RepositoryTest;
import com.mapz.api.common.fixtures.DiaryFixtures;
import com.mapz.api.common.fixtures.GroupFixtures;
import com.mapz.api.common.fixtures.UserFixtures;
import com.mapz.domain.domains.comment.entity.Comment;
import com.mapz.domain.domains.comment.repository.CommentRepository;
import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.vo.DiaryDetailVO;
import com.mapz.domain.domains.diary.vo.DiarySliceVO;
import com.mapz.domain.domains.group.entity.Group;
import com.mapz.domain.domains.group.repository.GroupRepository;
import com.mapz.domain.domains.like.entity.DiaryLike;
import com.mapz.domain.domains.like.repository.DiaryLikeRepository;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.user.repository.UserRepository;
import com.mapz.domain.domains.diary.repository.DiaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.mapz.api.common.fixtures.CommentFixtures.createComment;
import static com.mapz.api.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.mapz.api.common.util.PagingUtils.MY_DIARY_SIZE;
import static com.mapz.api.common.util.PagingUtils.applyCursorId;
import static com.mapz.api.common.util.PagingUtils.applyDescPageConfigBy;
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
        User mapZUser = UserFixtures.mapZSignUpUser();
        userRepository.save(mapZUser);

        Group group = GroupFixtures.createOpenGroup();
        groupRepository.save(group);

        int randomNumber = ThreadLocalRandom.current().nextInt(1, 5);
        ArrayList<Diary> diaries = new ArrayList<>();
        for (int i = 0; i < MY_DIARY_SIZE + randomNumber; i++) {
            Diary diary = DiaryFixtures.createDiary(mapZUser, group);
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
        User mapZUser = UserFixtures.mapZSignUpUser();
        userRepository.save(mapZUser);

        Group group = GroupFixtures.createOpenGroup();
        groupRepository.save(group);

        int randomNumber = ThreadLocalRandom.current().nextInt(5);

        ArrayList<Diary> diaries = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            diaries.add(DiaryFixtures.createDiary(mapZUser, group));
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
        User mapZUser = UserFixtures.mapZSignUpUser();
        userRepository.save(mapZUser);

        Group group = GroupFixtures.createOpenGroup();
        groupRepository.save(group);

        int randomNumber = ThreadLocalRandom.current().nextInt(5);

        ArrayList<Diary> diaries = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            diaries.add(DiaryFixtures.createDiary(mapZUser, group));
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
        User mapZUser = UserFixtures.mapZSignUpUser();
        userRepository.save(mapZUser);

        Group group = GroupFixtures.createOpenGroup();
        groupRepository.save(group);

        Diary diary = DiaryFixtures.createDiary(mapZUser, group);
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