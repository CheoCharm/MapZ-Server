package com.cheocharm.MapZ.diary.application;

import com.cheocharm.MapZ.ServiceTest;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.image.ImageDirectory;
import com.cheocharm.MapZ.common.image.ImageHandler;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.diary.domain.DiaryImage;
import com.cheocharm.MapZ.diary.domain.repository.DiaryRepository;
import com.cheocharm.MapZ.diary.presentation.dto.request.DeleteDiaryRequest;
import com.cheocharm.MapZ.diary.presentation.dto.request.DeleteTempDiaryRequest;
import com.cheocharm.MapZ.diary.presentation.dto.request.WriteDiaryImageRequest;
import com.cheocharm.MapZ.diary.presentation.dto.request.WriteDiaryRequest;
import com.cheocharm.MapZ.diary.presentation.dto.response.WriteDiaryImageResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.WriteDiaryResponse;
import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.user.domain.User;
import org.assertj.core.api.Assertions;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest extends ServiceTest {

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private static MockedStatic<UserThreadLocal> utl;
    private static User user;
    private static final EasyRandom easyRandom = new EasyRandom();

    @BeforeAll
    static void beforeAll() {
        utl = mockStatic(UserThreadLocal.class);
        user = easyRandom.nextObject(User.class);
        utl.when(UserThreadLocal::get).thenReturn(DiaryServiceTest.user);
    }

    @AfterAll
    static void afterAll() {
        utl.close();
    }

    // https://github.com/spring-projects/spring-framework/issues/18907
    @TestConfiguration
    static class MockitoPublisherConfiguration {
        @Bean
        @Primary
        ApplicationEventPublisher publisher() {
            return mock(ApplicationEventPublisher.class);
        }
    }

    @Test
    @DisplayName("일기 내용 작성")
    void writeDiaryContent() {

        //given
        final Diary diary = Diary.builder()
                .build();
        given(diaryRepository.findById(anyLong())).willReturn(Optional.of(diary));
        final WriteDiaryRequest request = easyRandom.nextObject(WriteDiaryRequest.class);
        //when
        final WriteDiaryResponse writeDiaryResponse = diaryService.writeDiary(request);

        //then
        assertThat(writeDiaryResponse.getDiaryId()).isEqualTo(request.getDiaryId());
        assertThat(diary.getContent()).isEqualTo(request.getContent());
        assertThat(diary.getTitle()).isEqualTo(request.getTitle());
    }

    @Test
    @DisplayName("작성된 일기는 작성자만 삭제할 수 있다")
    void sameUserCanDeleteDiary() {

        //given
        final Diary diary = Diary.builder()
                .user(user)
                .build();
        final DeleteDiaryRequest request = easyRandom.nextObject(DeleteDiaryRequest.class);
        given(diaryRepository.findById(request.getDiaryId())).willReturn(Optional.of(diary));

        //when
        diaryService.deleteDiary(request);

        //then
        then(diaryRepository).should(times(1)).deleteById(request.getDiaryId());

    }

    @Test
    @DisplayName("작성자가 아닌 사람은 일기를 삭제할 수 없다")
    void otherUserCannotDeleteDiary() {

        //given
        final Diary diary = Diary.builder()
                .user(easyRandom.nextObject(User.class))
                .build();
        final DeleteDiaryRequest request = easyRandom.nextObject(DeleteDiaryRequest.class);
        given(diaryRepository.findById(request.getDiaryId())).willReturn(Optional.of(diary));

        //when, then
        assertThatThrownBy(() -> diaryService.deleteDiary(request))
                .isInstanceOf(NoPermissionUserException.class);
    }

    @Test
    @DisplayName("일기 작성시 사용할 이미지 업로드")
    void writeDiaryImage() {

        //given
        final Group group = easyRandom.nextObject(Group.class);
        final Diary diary = Diary.builder()
                .id(50L)
                .user(user)
                .group(group)
                .build();

        final WriteDiaryImageRequest request = new WriteDiaryImageRequest(
                group.getId(), "주소", 127.01234, 37.12345
        );
        final List<MultipartFile> files = List.of(
                getMockMultipartFile("image1"),
                getMockMultipartFile("image2")
        );
        final List<CompletableFuture<String>> futures = List.of(
                CompletableFuture.completedFuture("image1"),
                CompletableFuture.completedFuture("image2")
        );

        given(diaryRepository.save(any()))
                .willReturn(diary);
        given(groupRepository.findById(request.getGroupId()))
                .willReturn(Optional.of(group));
        given(imageHandler.uploadImageAsync(any(MultipartFile.class), eq(ImageDirectory.DIARY)))
                .willReturn(futures.get(0))
                .willReturn(futures.get(1));
        //when
        final WriteDiaryImageResponse response = diaryService.writeDiaryImage(request, files);

        //then
        assertThat(response).isNotNull();
        assertThat(files.size()).isEqualTo(response.getImageURLs().size());
        assertThat(response.getDiaryId()).isEqualTo(diary.getId());
    }

    @Test
    @DisplayName("끝까지 일기 작성하지 않고 중간에 취소할 시 일기 데이터 삭제")
    void deleteTempDiary() {

        //given
        final Diary diary = Diary.builder()
                .id(ThreadLocalRandom.current().nextLong() + 1)
                .build();
        final List<DiaryImage> diaryImages = List.of(
                DiaryImage.builder()
                        .diary(diary)
                        .diaryImageUrl("imageURL1")
                        .build(),
                DiaryImage.builder()
                        .diary(diary)
                        .diaryImageUrl("imageURL2")
                        .build()
        );

        final DeleteTempDiaryRequest request = new DeleteTempDiaryRequest(diary.getId());
        given(diaryImageRepository.findAllByDiaryId(request.getDiaryId()))
                .willReturn(diaryImages);
        //when
        diaryService.deleteTempDiary(request);

        //then
        then(diaryRepository).should(times(1)).deleteById(anyLong());
        then(diaryImageRepository).should(times(1)).deleteAllByDiaryId(anyLong());
        then(eventPublisher).should(times(diaryImages.size())).publishEvent(any(DiaryImage.class));
//        then(imageHandler).should(times(diaryImages.size())).deleteImage(anyString());
    }

    @Test
    @DisplayName("일기 상세 정보 조회")
    void getDiaryDetail() {

    }

    @Test
    @DisplayName("일기 좌표 조회 반경이 넓은 경우")
    void getDiaryCoordinateWhenZoomLevelLow() {

    }

    @Test
    @DisplayName("일기 좌표 조회 반경이 좁은 경우")
    void getDiaryCoordinateWhenZoomLevelHigh() {

    }
}