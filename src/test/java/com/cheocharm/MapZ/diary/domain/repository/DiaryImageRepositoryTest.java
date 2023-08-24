package com.cheocharm.MapZ.diary.domain.repository;

import com.cheocharm.MapZ.RepositoryTest;
import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.diary.domain.DiaryImage;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryPreviewVO;
import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.group.domain.repository.GroupRepository;
import com.cheocharm.MapZ.like.domain.repository.DiaryLikeRepository;
import com.cheocharm.MapZ.user.domain.User;
import com.cheocharm.MapZ.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.cheocharm.MapZ.common.fixtures.DiaryFixtures.createDiary;
import static com.cheocharm.MapZ.common.fixtures.DiaryImageFixtures.createDiaryImage;
import static com.cheocharm.MapZ.common.fixtures.GroupFixtures.createOpenGroup;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.googleSignUpUser;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.mapZSignUpUser;
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

        Group group = createOpenGroup();
        groupRepository.save(group);

        Diary diary = createDiary(mapZUser, group);
        diaryRepository.save(diary);

        int randomNumber = ThreadLocalRandom.current().nextInt(1, 5);
        ArrayList<DiaryImage> diaryImages = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            diaryImages.add(createDiaryImage(diary));
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

        Group group = createOpenGroup();
        groupRepository.save(group);

        Diary diary = createDiary(mapZUser, group);
        diaryRepository.save(diary);

        int randomNumber = ThreadLocalRandom.current().nextInt(1, 5);
        ArrayList<DiaryImage> diaryImages = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            diaryImages.add(createDiaryImage(diary));
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

        Group group = createOpenGroup();
        groupRepository.save(group);

        Diary firstDiary = createDiary(mapZUser, group);
        Diary secondDiary = createDiary(googleUser, group);
        diaryRepository.saveAll(List.of(firstDiary, secondDiary));

        int randomNumber = ThreadLocalRandom.current().nextInt(1, 2);
        ArrayList<DiaryImage> diaryImages = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            diaryImages.add(createDiaryImage(firstDiary));
            diaryImages.add(createDiaryImage(secondDiary));
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

        Group group = createOpenGroup();
        groupRepository.save(group);

        Diary diary = createDiary(mapZUser, group);
        diaryRepository.save(diary);

        int randomNumber = ThreadLocalRandom.current().nextInt(1, 5);
        ArrayList<DiaryImage> diaryImages = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            diaryImages.add(createDiaryImage(diary));
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