package com.cheocharm.MapZ.diary.domain;

import com.cheocharm.MapZ.common.exception.diary.AlreadyLikedDiaryException;
import com.cheocharm.MapZ.common.exception.diary.NotFoundDiaryException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.diary.domain.dto.LikeDiaryDto;
import com.cheocharm.MapZ.diary.domain.respository.DiaryLikeRepository;
import com.cheocharm.MapZ.diary.domain.respository.DiaryRepository;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryLikeRepository diaryLikeRepository;

    @Transactional
    public void likeDiary(LikeDiaryDto likeDiaryDto) {
        final UserEntity userEntity = UserThreadLocal.get();
        final DiaryEntity diaryEntity = diaryRepository.findById(likeDiaryDto.getDiaryId())
                .orElseThrow(NotFoundDiaryException::new);

        diaryLikeRepository.findByDiaryEntityAndUserEntity(diaryEntity, userEntity)
                .ifPresent(diaryLikeEntity -> {
                    throw new AlreadyLikedDiaryException();
                });

        DiaryLikeEntity.builder()
                .diaryEntity(diaryEntity)
                .userEntity(userEntity)
                .build();
    }
}
