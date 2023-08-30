package com.mapz.api.like.application;

import com.mapz.api.common.exception.diary.AlreadyLikedDiaryException;
import com.mapz.api.common.exception.diary.NotFoundDiaryException;
import com.mapz.api.common.interceptor.UserThreadLocal;
import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.like.entity.DiaryLike;
import com.mapz.api.like.presentation.dto.response.DiaryLikePeopleResponse;
import com.mapz.api.like.presentation.dto.request.LikeDiaryRequest;
import com.mapz.api.like.presentation.dto.response.MyLikeDiaryResponse;
import com.mapz.domain.domains.like.repository.DiaryLikeRepository;
import com.mapz.domain.domains.diary.repository.DiaryRepository;
import com.mapz.domain.domains.diary.vo.MyLikeDiaryVO;
import com.mapz.domain.domains.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mapz.api.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.mapz.api.common.util.PagingUtils.MY_LIKE_DIARY_SIZE;
import static com.mapz.api.common.util.PagingUtils.applyCursorId;
import static com.mapz.api.common.util.PagingUtils.applyDescPageConfigBy;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final DiaryRepository diaryRepository;
    private final DiaryLikeRepository diaryLikeRepository;

    @Transactional
    public void likeDiary(LikeDiaryRequest request) {
        final User user = UserThreadLocal.get();
        final Diary diary = diaryRepository.findById(request.getDiaryId())
                .orElseThrow(NotFoundDiaryException::new);

        checkAlreadyLikeDiary(user, diary);

        diaryLikeRepository.save(DiaryLike.of(diary, user));
    }

    private void checkAlreadyLikeDiary(User user, Diary diary) {
        diaryLikeRepository.findByDiaryAndUser(diary, user)
                .ifPresent(diaryLikeEntity -> {
                    throw new AlreadyLikedDiaryException();
                });
    }

    @Transactional(readOnly = true)
    public List<DiaryLikePeopleResponse> getDiaryLikePeople(Long diaryId) {
        List<DiaryLike> diaryLikes = diaryLikeRepository.findByDiaryId(diaryId);
        return DiaryLikePeopleResponse.of(diaryLikes);
    }

    @Transactional(readOnly = true)
    public MyLikeDiaryResponse getMyLikeDiary(Integer page, Long cursorId) {
        User user = UserThreadLocal.get();

        Slice<MyLikeDiaryVO> content = diaryLikeRepository.findByUserId(
                user.getId(),
                applyCursorId(cursorId),
                applyDescPageConfigBy(page, MY_LIKE_DIARY_SIZE, FIELD_CREATED_AT)
        );

        List<MyLikeDiaryVO> myLikeDiaryVOS = content.getContent();

        return MyLikeDiaryResponse.of(content.hasNext(), myLikeDiaryVOS);
    }
}
