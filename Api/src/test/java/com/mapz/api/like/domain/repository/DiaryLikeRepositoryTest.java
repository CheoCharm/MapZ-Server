package com.mapz.api.like.domain.repository;

import com.mapz.api.RepositoryTest;
import com.mapz.api.common.fixtures.DiaryFixtures;
import com.mapz.api.common.fixtures.GroupFixtures;
import com.mapz.api.common.fixtures.UserFixtures;
import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.repository.DiaryRepository;
import com.mapz.domain.domains.diary.vo.MyLikeDiaryVO;
import com.mapz.domain.domains.group.entity.Group;
import com.mapz.domain.domains.group.repository.GroupRepository;
import com.mapz.domain.domains.like.entity.DiaryLike;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.user.repository.UserRepository;
import com.mapz.domain.domains.like.repository.DiaryLikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.mapz.api.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.mapz.api.common.util.PagingUtils.MY_LIKE_DIARY_SIZE;
import static com.mapz.api.common.util.PagingUtils.applyCursorId;
import static com.mapz.api.common.util.PagingUtils.applyDescPageConfigBy;
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
        User mapZUser = UserFixtures.mapZSignUpUser();
        userRepository.save(mapZUser);

        Group group = GroupFixtures.createOpenGroup();
        groupRepository.save(group);

        Diary diary = DiaryFixtures.createDiary(mapZUser, group);
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
        User mapZUser = UserFixtures.mapZSignUpUser();
        User googleUser = UserFixtures.googleSignUpUser();
        Group group = GroupFixtures.createOpenGroup();

        userRepository.saveAll(List.of(mapZUser, googleUser));
        groupRepository.save(group);

        Diary diary = DiaryFixtures.createDiary(mapZUser, group);
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
        User mapZUser = UserFixtures.mapZSignUpUser();
        User googleUser = UserFixtures.googleSignUpUser();
        userRepository.save(mapZUser);
        userRepository.save(googleUser);

        Group group = GroupFixtures.createOpenGroup();
        groupRepository.save(group);

        int randomNumber = ThreadLocalRandom.current().nextInt(5) + 1;
        ArrayList<Diary> diaries = new ArrayList<>();
        ArrayList<DiaryLike> diaryLikes = new ArrayList<>();
        for (int i = 0; i < MY_LIKE_DIARY_SIZE + randomNumber; i++) {
            Diary diary = DiaryFixtures.createDiary(mapZUser, group);
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
        User mapZUser = UserFixtures.mapZSignUpUser();
        User googleUser = UserFixtures.googleSignUpUser();
        userRepository.saveAll(List.of(mapZUser, googleUser));

        Group group = GroupFixtures.createOpenGroup();
        groupRepository.save(group);

        Diary firstDiary = DiaryFixtures.createDiary(mapZUser, group);
        Diary secondDiary = DiaryFixtures.createDiary(mapZUser, group);

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