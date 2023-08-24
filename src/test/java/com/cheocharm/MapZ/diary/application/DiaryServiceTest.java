package com.cheocharm.MapZ.diary.application;

import com.cheocharm.MapZ.ServiceTest;
import com.cheocharm.MapZ.common.exception.user.NoPermissionUserException;
import com.cheocharm.MapZ.common.image.ImageDirectory;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.common.util.PagingUtils;
import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.diary.domain.DiaryImage;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryCoordinateVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryDetailVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryImagePreviewVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryPreviewVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiarySliceVO;
import com.cheocharm.MapZ.diary.presentation.dto.request.WriteDiaryImageRequest;
import com.cheocharm.MapZ.diary.presentation.dto.request.WriteDiaryRequest;
import com.cheocharm.MapZ.diary.presentation.dto.response.DiaryDetailResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.DiaryPreviewDetailResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.DiaryPreviewResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.GetDiaryListResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.WriteDiaryImageResponse;
import com.cheocharm.MapZ.diary.presentation.dto.response.WriteDiaryResponse;
import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.user.domain.User;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import static com.cheocharm.MapZ.common.fixtures.DiaryFixtures.ADDRESS;
import static com.cheocharm.MapZ.common.fixtures.DiaryFixtures.DIARY_TITLE;
import static com.cheocharm.MapZ.common.fixtures.DiaryFixtures.NO_IMAGE_CONTENT;
import static com.cheocharm.MapZ.common.util.PagingUtils.FIELD_CREATED_AT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
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
    @DisplayName("그룹의 일기를 페이징하여 조회할 수 있다.")
    void getDiary() {

        //given
        Slice<DiarySliceVO> diarySliceVOS = new SliceImpl<>(
                List.of(easyRandom.nextObject(DiarySliceVO.class),
                        easyRandom.nextObject(DiarySliceVO.class)),
                PagingUtils.applyDescPageConfigBy(0, PagingUtils.MY_DIARY_SIZE, FIELD_CREATED_AT),
                true
        );

        given(diaryRepository.getDiarySlice(anyLong(), anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(diarySliceVOS);

        //when
        GetDiaryListResponse response = diaryService.getDiary(1L, 0L, 0);

        //then
        assertThat(response.getDiaryList().size()).isEqualTo(diarySliceVOS.getContent().size());
        assertThat(response.isHasNext()).isEqualTo(diarySliceVOS.hasNext());
    }

    @Test
    @DisplayName("작성된 일기는 작성자만 삭제할 수 있다")
    void sameUserCanDeleteDiary() {

        //given
        final Diary diary = Diary.builder()
                .user(user)
                .id(1L)
                .build();
        given(diaryRepository.findById(diary.getId())).willReturn(Optional.of(diary));

        //when
        diaryService.deleteDiary(diary.getId());

        //then
        then(diaryRepository).should(times(1)).deleteById(diary.getId());

    }

    @Test
    @DisplayName("작성자가 아닌 사람은 일기를 삭제할 수 없다")
    void otherUserCannotDeleteDiary() {

        //given
        final Diary diary = Diary.builder()
                .user(easyRandom.nextObject(User.class))
                .id(1L)
                .build();
        given(diaryRepository.findById(diary.getId())).willReturn(Optional.of(diary));

        //when, then
        assertThatThrownBy(() -> diaryService.deleteDiary(diary.getId()))
                .isInstanceOf(NoPermissionUserException.class);
    }

    @Test
    @DisplayName("내가 작성한 일기를 페이징하여 조회한다.")
    void getMyDiary() {

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

        given(diaryImageRepository.findAllByDiaryId(diary.getId()))
                .willReturn(diaryImages);
        //when
        diaryService.deleteTempDiary(diary.getId());

        //then
        then(diaryRepository).should(times(1)).deleteById(anyLong());
        then(diaryImageRepository).should(times(1)).deleteAllByDiaryId(anyLong());
        then(eventPublisher).should(times(diaryImages.size())).publishEvent(any(DiaryImage.class));
//        then(imageHandler).should(times(diaryImages.size())).deleteImage(anyString());
    }

    @Test
    @DisplayName("일기 상세 정보 조회")
    void getDiaryDetail() {

        //given
        Diary diary = Diary.builder()
                .id(1L)
                .user(user)
                .title(DIARY_TITLE)
                .content(NO_IMAGE_CONTENT)
                .address(ADDRESS)
                .build();
        DiaryDetailVO diaryDetailVO = new DiaryDetailVO(
                diary.getTitle(),
                diary.getContent(),
                diary.getAddress(),
                LocalDateTime.now(),
                user.getUsername(),
                user.getUserImageUrl(),
                0,
                true,
                4L,
                true
        );
        given(diaryRepository.getDiaryDetail(anyLong(), anyLong()))
                .willReturn(diaryDetailVO);
        //when
        DiaryDetailResponse response = diaryService.getDiaryDetail(diary.getId());

        //then
        assertThat(response.getTitle()).isEqualTo(diaryDetailVO.getTitle());
        assertThat(response.getUsername()).isEqualTo(diaryDetailVO.getUsername());
    }

    @Test
    @DisplayName("일기 좌표 조회 반경이 넓은 경우")
    void getDiaryCoordinateWhenZoomLevelLow() {

    }

//    @Test
    @DisplayName("일기 좌표 조회 반경이 좁은 경우")
    void getDiaryCoordinateWhenZoomLevelHigh() {

        //given
        List<DiaryCoordinateVO> diaryCoordinateVOS = List.of(
                easyRandom.nextObject(DiaryCoordinateVO.class)
        );
        DiaryCoordinateVO diaryCoordinateVO = diaryCoordinateVOS.get(0);
        given(diaryRepository.findByDiaryCoordinate(any(), anyList(), anyDouble()))
                .willReturn(diaryCoordinateVOS);
        given(diaryImageRepository.findPreviewImage(anyList()))
                .willReturn(List.of(new DiaryImagePreviewVO(diaryCoordinateVO.getDiaryId(), "11")));
        //when
        List<DiaryPreviewResponse> response = diaryService.getDiaryByMap(
                127.77777, 13.23333, 15.2
        );

        //then
        assertThat(response.size()).isEqualTo(diaryCoordinateVOS.size());
    }

    @Test
    @DisplayName("지도에서 일기 클릭해서 조회")
    void getDiaryPreviewDetail() {

        //given
        List<DiaryPreviewVO> diaryPreviewVOS = List.of(
                easyRandom.nextObject(DiaryPreviewVO.class),
                easyRandom.nextObject(DiaryPreviewVO.class)
        );
        given(diaryImageRepository.getDiaryPreview(anyLong(), anyLong()))
                .willReturn(diaryPreviewVOS);

        //when
        DiaryPreviewDetailResponse response = diaryService.getDiaryPreviewDetail(1L);

        //then
        assertThat(response.getDiaryImageURLs().size()).isEqualTo(diaryPreviewVOS.size());
    }
}