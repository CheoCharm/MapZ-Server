package com.mapz.api.diary.domain.repository;

import com.mapz.api.RepositoryTest;
import com.mapz.api.common.fixtures.DiaryFixtures;
import com.mapz.api.common.fixtures.DiaryImageFixtures;
import com.mapz.api.common.fixtures.GroupFixtures;
import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.entity.DiaryImage;
import com.mapz.domain.domains.diary.vo.DiaryPreviewVO;
import com.mapz.domain.domains.group.entity.Group;
import com.mapz.domain.domains.group.repository.GroupRepository;
import com.mapz.domain.domains.like.repository.DiaryLikeRepository;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.user.repository.UserRepository;
import com.mapz.domain.domains.diary.repository.DiaryImageRepository;
import com.mapz.domain.domains.diary.repository.DiaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.mapz.api.common.fixtures.UserFixtures.googleSignUpUser;
import static com.mapz.api.common.fixtures.UserFixtures.mapZSignUpUser;
import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class DiaryImageRepositoryTest {

    @Autowired
    private DiaryImageRepository diaryImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private DiaryLikeRepository diaryLikeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("diaryId를 조건으로 DiaryImage를 찾는다.")
    void findAllByDiaryId() {

        //given
        User mapZUser = mapZSignUpUser();
        userRepository.save(mapZUser);

        Group group = GroupFixtures.createOpenGroup();
        groupRepository.save(group);

        Diary diary = DiaryFixtures.createDiary(mapZUser, group);
        diaryRepository.save(diary);

        int randomNumber = ThreadLocalRandom.current().nextInt(1, 5);
        ArrayList<DiaryImage> diaryImages = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            diaryImages.add(DiaryImageFixtures.createDiaryImage(diary));
        }
        diaryImageRepository.saveAll(diaryImages);

        //when
        List<DiaryImage> actual = diaryImageRepository.findAllByDiaryId(diary.getId());

        //then
        assertThat(actual.size()).isEqualTo(diaryImages.size());
    }

    @Test
    @DisplayName("diaryId를 조건으로 DiaryImage를 삭제한다.")
    void deleteAllByDiaryId() {

        //given
        User mapZUser = mapZSignUpUser();
        userRepository.save(mapZUser);

        Group group = GroupFixtures.createOpenGroup();
        groupRepository.save(group);

        Diary diary = DiaryFixtures.createDiary(mapZUser, group);
        diaryRepository.save(diary);

        int randomNumber = ThreadLocalRandom.current().nextInt(1, 5);
        ArrayList<DiaryImage> diaryImages = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            diaryImages.add(DiaryImageFixtures.createDiaryImage(diary));
        }
        diaryImageRepository.saveAll(diaryImages);

        //when
        diaryImageRepository.deleteAllByDiaryId(diary.getId());
        entityManager.clear();

        //then
        for (DiaryImage diaryImage : diaryImages) {
            assertThat(diaryImageRepository.findById(diaryImage.getId())).isEmpty();
        }

    }

    @Test
    @DisplayName("Diary 엔티티 리스트를 조건으로 DiaryImage를 삭제한다.")
    void deleteAllByDiaries() {

        //given
        User mapZUser = mapZSignUpUser();
        User googleUser = googleSignUpUser();
        userRepository.saveAll(List.of(mapZUser, googleUser));

        Group group = GroupFixtures.createOpenGroup();
        groupRepository.save(group);

        Diary firstDiary = DiaryFixtures.createDiary(mapZUser, group);
        Diary secondDiary = DiaryFixtures.createDiary(googleUser, group);
        diaryRepository.saveAll(List.of(firstDiary, secondDiary));

        int randomNumber = ThreadLocalRandom.current().nextInt(1, 2);
        ArrayList<DiaryImage> diaryImages = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            diaryImages.add(DiaryImageFixtures.createDiaryImage(firstDiary));
            diaryImages.add(DiaryImageFixtures.createDiaryImage(secondDiary));
        }
        diaryImageRepository.saveAll(diaryImages);

        //when
        diaryImageRepository.deleteAllByDiaries(List.of(firstDiary, secondDiary));
        entityManager.clear();

        //then
        for (DiaryImage diaryImage : diaryImages) {
            assertThat(diaryImageRepository.findById(diaryImage.getId())).isEmpty();
        }
    }

    @Test
    @DisplayName("d")
    void getPreviewImage() {

        //given

        //when
//        List<DiaryImagePreviewVO> previewImages = diaryImageRepository.findPreviewImage();

        //then

    }

    @Test
    @DisplayName("diaryId, userId를 파라미터로 일기 클릭 시 미리보기 정보를 획득한다.")
    void getDiaryPreview() {

        //given
        User mapZUser = mapZSignUpUser();
        userRepository.save(mapZUser);

        Group group = GroupFixtures.createOpenGroup();
        groupRepository.save(group);

        Diary diary = DiaryFixtures.createDiary(mapZUser, group);
        diaryRepository.save(diary);

        int randomNumber = ThreadLocalRandom.current().nextInt(1, 5);
        ArrayList<DiaryImage> diaryImages = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            diaryImages.add(DiaryImageFixtures.createDiaryImage(diary));
        }
        diaryImageRepository.saveAll(diaryImages);

        //when
        List<DiaryPreviewVO> actual = diaryImageRepository.getDiaryPreview(
                diary.getId(), mapZUser.getId()
        );

        //then
        assertThat(actual.size()).isEqualTo(diaryImages.size());
        assertThat(actual.get(0).isLike()).isFalse();
    }
}