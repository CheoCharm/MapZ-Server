package com.mapz.api.like.application;

import com.mapz.api.common.exception.diary.AlreadyLikedDiaryException;
import com.mapz.api.common.interceptor.UserThreadLocal;
import com.mapz.api.common.util.PagingUtils;
import com.mapz.api.like.presentation.dto.request.LikeDiaryRequest;
import com.mapz.api.like.presentation.dto.response.DiaryLikePeopleResponse;
import com.mapz.api.like.presentation.dto.response.MyLikeDiaryResponse;
import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.repository.DiaryRepository;
import com.mapz.domain.domains.diary.vo.MyLikeDiaryVO;
import com.mapz.domain.domains.like.entity.DiaryLike;
import com.mapz.domain.domains.like.repository.DiaryLikeRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static com.mapz.api.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.mapz.api.common.util.PagingUtils.MY_LIKE_DIARY_SIZE;
import static com.mapz.api.common.util.PagingUtils.applyDescPageConfigBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Autowired
    private LikeService likeService;

    @MockBean
    private DiaryRepository diaryRepository;

    @MockBean
    private DiaryLikeRepository diaryLikeRepository;

    private static MockedStatic<UserThreadLocal> utl;
    private static User user;
    private static final EasyRandom easyRandom = new EasyRandom();

    @BeforeAll
    static void beforeAll() {
        utl = mockStatic(UserThreadLocal.class);
        user = easyRandom.nextObject(User.class);
        utl.when(UserThreadLocal::get).thenReturn(LikeServiceTest.user);
    }

    @AfterAll
    static void afterAll() {
        utl.close();
    }

    @Test
    @DisplayName("유저는 다이어리에 좋아요를 할 수 있다")
    void likeDiary() {

        //given
        Diary diary = Diary.builder()
                .id(ThreadLocalRandom.current().nextLong() + 1)
                .build();
        LikeDiaryRequest request = new LikeDiaryRequest(diary.getId());
        given(diaryRepository.findById(request.getDiaryId())).willReturn(Optional.of(diary));
        given(diaryLikeRepository.findByDiaryAndUser(diary, user)).willReturn(Optional.empty());

        //when
        likeService.likeDiary(request);

        //then
        then(diaryLikeRepository).should().save(any(DiaryLike.class));

    }

    @Test
    @DisplayName("유저가 한 게시글에 좋아요를 여러번 할 수 없다")
    void likeDiaryCannotSeveralTime() {

        //given
        Diary diary = Diary.builder()
                .id(ThreadLocalRandom.current().nextLong() + 1)
                .build();
        LikeDiaryRequest request = new LikeDiaryRequest(diary.getId());
        given(diaryRepository.findById(request.getDiaryId()))
                .willReturn(Optional.of(diary));
        given(diaryLikeRepository.findByDiaryAndUser(diary, user))
                .willReturn(Optional.of(DiaryLike.of(diary, user)));


        //when, then
        assertThatThrownBy(() -> likeService.likeDiary(request))
                .isInstanceOf(AlreadyLikedDiaryException.class);
    }

    @Test
    @DisplayName("다이어리 좋아요를 누른 사람을 확인할 수 있다")
    void getDiaryLikePeople() {

        //given
        long diaryId = ThreadLocalRandom.current().nextLong() + 1;
        Diary diary = Diary.builder()
                .user(user)
                .build();
        DiaryLike diaryLike1 = DiaryLike.of(diary, user);
        DiaryLike diaryLike2 = DiaryLike.of(diary, user);

        List<DiaryLike> diaryLikes = List.of(diaryLike1, diaryLike2);
        given(diaryLikeRepository.findByDiaryId(diaryId))
                .willReturn(diaryLikes);

        //when
        List<DiaryLikePeopleResponse> response = likeService.getDiaryLikePeople(diaryId);

        //then
        assertThat(response.size()).isEqualTo(diaryLikes.size());
    }

    @Test
    @DisplayName("유저는 좋아요를 누른 다이어리를 조회할 수 있다.")
    void getMyLikeDiary() {

        //given
        SliceImpl<MyLikeDiaryVO> slice = new SliceImpl<>(
                List.of(
                        easyRandom.nextObject(MyLikeDiaryVO.class),
                        easyRandom.nextObject(MyLikeDiaryVO.class)
                ),
                applyDescPageConfigBy(0, MY_LIKE_DIARY_SIZE, FIELD_CREATED_AT),
                false
        );

        given(diaryLikeRepository.findByUserId(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(slice);

        //when
        MyLikeDiaryResponse response = likeService.getMyLikeDiary(0, 0L);

        //then
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getDiaryList().size()).isEqualTo(slice.getContent().size());
    }
}