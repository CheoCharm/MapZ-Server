package com.cheocharm.MapZ.like.application;

import com.cheocharm.MapZ.common.exception.diary.AlreadyLikedDiaryException;
import com.cheocharm.MapZ.common.exception.diary.NotFoundDiaryException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.like.domain.DiaryLike;
import com.cheocharm.MapZ.like.presentation.dto.response.DiaryLikePeopleResponse;
import com.cheocharm.MapZ.like.presentation.dto.request.LikeDiaryRequest;
import com.cheocharm.MapZ.like.presentation.dto.response.MyLikeDiaryResponse;
import com.cheocharm.MapZ.like.domain.repository.DiaryLikeRepository;
import com.cheocharm.MapZ.diary.domain.repository.DiaryRepository;
import com.cheocharm.MapZ.diary.domain.repository.vo.MyLikeDiaryVO;
import com.cheocharm.MapZ.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cheocharm.MapZ.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.cheocharm.MapZ.common.util.PagingUtils.MY_LIKE_DIARY_SIZE;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyCursorId;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyDescPageConfigBy;

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
