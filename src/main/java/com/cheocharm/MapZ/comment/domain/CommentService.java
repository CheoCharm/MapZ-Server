package com.cheocharm.MapZ.comment.domain;

import com.cheocharm.MapZ.comment.domain.dto.CreateCommentDto;
import com.cheocharm.MapZ.comment.domain.repository.CommentRepository;
import com.cheocharm.MapZ.common.exception.diary.NotFoundDiaryException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.diary.domain.respository.DiaryRepository;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final DiaryRepository diaryRepository;

    @Transactional
    public void createComment(CreateCommentDto createCommentDto) {
        final UserEntity userEntity = UserThreadLocal.get();
        DiaryEntity diaryEntity = diaryRepository.findById(createCommentDto.getDiaryId()).orElseThrow(() -> new NotFoundDiaryException());
        commentRepository.save(
                CommentEntity.builder()
                        .content(createCommentDto.getContent())
                        .parentId(createCommentDto.getParentId())
                        .userEntity(userEntity)
                        .diaryEntity(diaryEntity)
                        .build()
        );
    }
}
