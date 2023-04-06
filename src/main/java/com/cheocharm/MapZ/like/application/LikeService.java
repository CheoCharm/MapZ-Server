package com.cheocharm.MapZ.like.application;

import com.cheocharm.MapZ.common.exception.diary.AlreadyLikedDiaryException;
import com.cheocharm.MapZ.common.exception.diary.NotFoundDiaryException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.like.presentation.dto.response.DiaryLikePeopleResponse;
import com.cheocharm.MapZ.like.presentation.dto.request.LikeDiaryRequest;
import com.cheocharm.MapZ.like.presentation.dto.response.MyLikeDiaryResponse;
import com.cheocharm.MapZ.like.domain.repository.DiaryLikeRepository;
import com.cheocharm.MapZ.diary.domain.repository.DiaryRepository;
import com.cheocharm.MapZ.diary.domain.repository.vo.MyLikeDiaryVO;
import com.cheocharm.MapZ.like.domain.DiaryLikeEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
    public void likeDiary(LikeDiaryRequest likeDiaryRequest) {
        final UserEntity userEntity = UserThreadLocal.get();
        final DiaryEntity diaryEntity = diaryRepository.findById(likeDiaryRequest.getDiaryId())
                .orElseThrow(NotFoundDiaryException::new);

        diaryLikeRepository.findByDiaryEntityAndUserEntity(diaryEntity, userEntity)
                .ifPresent(diaryLikeEntity -> {
                    throw new AlreadyLikedDiaryException();
                });

        DiaryLikeEntity diaryLikeEntity = DiaryLikeEntity.builder()
                .diaryEntity(diaryEntity)
                .userEntity(userEntity)
                .build();

        diaryLikeRepository.save(diaryLikeEntity);
    }

    @Transactional(readOnly = true)
    public List<DiaryLikePeopleResponse> getDiaryLikePeople(Long diaryId) {
        List<DiaryLikeEntity> diaryLikeEntities = diaryLikeRepository.findByDiaryId(diaryId);

        return diaryLikeEntities.stream()
                .map(diaryLikeEntity -> {
                            UserEntity userEntity = diaryLikeEntity.getUserEntity();
                            return DiaryLikePeopleResponse.builder()
                                    .userImageUrl(userEntity.getUserImageUrl())
                                    .username(userEntity.getUsername())
                                    .build();
                        }
                )
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MyLikeDiaryResponse getMyLikeDiary(Long cursorId, Integer page) {
        UserEntity userEntity = UserThreadLocal.get();

        Slice<MyLikeDiaryVO> content = diaryLikeRepository.findByUserId(
                userEntity.getId(),
                applyCursorId(cursorId),
                applyDescPageConfigBy(page, MY_LIKE_DIARY_SIZE, FIELD_CREATED_AT)
        );

        List<MyLikeDiaryVO> myLikeDiaryVOS = content.getContent();

        List<MyLikeDiaryResponse.Diary> diaries = myLikeDiaryVOS.stream()
                .map(myLikeDiary ->
                        MyLikeDiaryResponse.Diary.builder()
                                .title(myLikeDiary.getTitle())
                                .createdAt(myLikeDiary.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                                .diaryId(myLikeDiary.getDiaryId())
                                .groupId(myLikeDiary.getGroupId())
                                .commentCount(myLikeDiary.getCommentCount())
                                //일기 대표 이미지 빌더에 추가
                                .build()
                )
                .collect(Collectors.toList());

        return new MyLikeDiaryResponse(content.hasNext(), diaries);
    }
}
