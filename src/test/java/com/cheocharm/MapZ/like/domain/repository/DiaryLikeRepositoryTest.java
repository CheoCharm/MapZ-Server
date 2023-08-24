package com.cheocharm.MapZ.like.domain.repository;

import com.cheocharm.MapZ.RepositoryTest;
import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.diary.domain.repository.DiaryRepository;
import com.cheocharm.MapZ.diary.domain.repository.vo.MyLikeDiaryVO;
import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.like.domain.DiaryLike;
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

import static com.cheocharm.MapZ.common.fixtures.DiaryFixtures.createDiary;
import static com.cheocharm.MapZ.common.fixtures.GroupFixtures.createOpenGroup;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.googleSignUpUser;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.mapZSignUpUser;
import static com.cheocharm.MapZ.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.cheocharm.MapZ.common.util.PagingUtils.MY_LIKE_DIARY_SIZE;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyCursorId;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyDescPageConfigBy;
import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class DiaryLikeRepositoryTest {

    @Autowired
    private DiaryLikeRepository diaryLikeRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("DiaryLike 엔티티를 Diary, User 엔티티를 조건으로 찾는다.")
    void findByDiaryAndUser() {

        //given
        User mapZUser = mapZSignUpUser();
        userRepository.save(mapZUser);

        Group group = createOpenGroup();
        groupRepository.save(group);

        Diary diary = createDiary(mapZUser, group);
        diaryRepository.save(diary);

        DiaryLike diaryLike = DiaryLike.of(diary, mapZUser);
        diaryLikeRepository.save(diaryLike);
        //when, then
        assertThat(diaryLikeRepository.findByDiaryAndUser(diary, mapZUser)).isNotEmpty();

    }

    @Test
    @DisplayName("다이어리 아이디를 조건으로 좋아요한 ")
    void findByDiaryId() {

        //given
        User mapZUser = mapZSignUpUser();
        User googleUser = googleSignUpUser();
        Group group = createOpenGroup();

        userRepository.saveAll(List.of(mapZUser, googleUser));
        groupRepository.save(group);

        Diary diary = createDiary(mapZUser, group);
        diaryRepository.save(diary);

        List<DiaryLike> diaryLikes = List.of(
                DiaryLike.of(diary, mapZUser),
                DiaryLike.of(diary, googleUser)
        );
        diaryLikeRepository.saveAll(diaryLikes);

        //when
        List<DiaryLike> actualDiaryLikes = diaryLikeRepository.findByDiaryId(diary.getId());

        //then
        assertThat(actualDiaryLikes.size()).isEqualTo(diaryLikes.size());
    }

    @Test
    @DisplayName("좋아요한 게시글을 찾는다.")
    void findDiary() {

        //given
        User mapZUser = mapZSignUpUser();
        User googleUser = googleSignUpUser();
        userRepository.save(mapZUser);
        userRepository.save(googleUser);

        Group group = createOpenGroup();
        groupRepository.save(group);

        int randomNumber = ThreadLocalRandom.current().nextInt(5) + 1;
        ArrayList<Diary> diaries = new ArrayList<>();
        ArrayList<DiaryLike> diaryLikes = new ArrayList<>();
        for (int i = 0; i < MY_LIKE_DIARY_SIZE + randomNumber; i++) {
            Diary diary = createDiary(mapZUser, group);
            diaries.add(diary);

            diaryLikes.add(DiaryLike.of(diary, mapZUser));
        }
        diaryRepository.saveAll(diaries);
        diaryLikeRepository.saveAll(diaryLikes);

        //when
        Slice<MyLikeDiaryVO> firstContent = diaryLikeRepository.findByUserId(
                mapZUser.getId(),
                applyCursorId(0L),
                applyDescPageConfigBy(0, MY_LIKE_DIARY_SIZE, FIELD_CREATED_AT)
        );
        Slice<MyLikeDiaryVO> secondContent = diaryLikeRepository.findByUserId(
                mapZUser.getId(),
                applyCursorId(firstContent.getContent().get(MY_LIKE_DIARY_SIZE - 1).getDiaryId()),
                applyDescPageConfigBy(1, MY_LIKE_DIARY_SIZE, FIELD_CREATED_AT)
        );

        //then
        assertThat(firstContent.hasNext()).isTrue();
        assertThat(firstContent.getContent().size()).isEqualTo(MY_LIKE_DIARY_SIZE);
        assertThat(secondContent.hasNext()).isFalse();
        assertThat(secondContent.getContent().size()).isEqualTo(randomNumber);
    }

    @Test
    @DisplayName("Diary 엔티티 리스트를 파라미터로 DiaryLike Entity를 삭제한다.")
    void deleteAllByDiaries() {

        //given
        User mapZUser = mapZSignUpUser();
        User googleUser = googleSignUpUser();
        userRepository.saveAll(List.of(mapZUser, googleUser));

        Group group = createOpenGroup();
        groupRepository.save(group);

        Diary firstDiary = createDiary(mapZUser, group);
        Diary secondDiary = createDiary(mapZUser, group);

        List<Diary> diaries = List.of(firstDiary, secondDiary);
        diaryRepository.saveAll(diaries);

        DiaryLike firstDiaryLikeMapZUser = DiaryLike.of(firstDiary, mapZUser);
        DiaryLike firstDiaryLikeGoogleUser = DiaryLike.of(firstDiary, googleUser);
        DiaryLike secondDiaryLikeMapZUser = DiaryLike.of(secondDiary, mapZUser);
        List<DiaryLike> diaryLikes = List.of(firstDiaryLikeMapZUser, firstDiaryLikeGoogleUser, secondDiaryLikeMapZUser);

        diaryLikeRepository.saveAll(diaryLikes);

        //when
        diaryLikeRepository.deleteAllByDiaries(diaries);
        entityManager.clear();

        //then
        Long id = firstDiary.getId();
        assertThat(diaryLikeRepository.findById(firstDiary.getId())).isEmpty();
    }
}