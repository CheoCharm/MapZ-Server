package com.cheocharm.MapZ.comment.application;

import com.cheocharm.MapZ.comment.domain.CommentEntity;
import com.cheocharm.MapZ.comment.domain.repository.vo.CommentVO;
import com.cheocharm.MapZ.comment.presentation.dto.request.CreateCommentRequest;
import com.cheocharm.MapZ.comment.presentation.dto.request.DeleteCommentRequest;
import com.cheocharm.MapZ.comment.presentation.dto.response.GetCommentResponse;
import com.cheocharm.MapZ.comment.domain.repository.CommentRepository;
import com.cheocharm.MapZ.common.exception.diary.NotFoundDiaryException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.diary.domain.respository.DiaryRepository;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.cheocharm.MapZ.common.util.PagingUtils.COMMENT_SIZE;
import static com.cheocharm.MapZ.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyAscPageConfigBy;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final DiaryRepository diaryRepository;

    @Transactional
    public void createComment(CreateCommentRequest createCommentDto) {
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

    @Transactional
    public void deleteComment(DeleteCommentRequest deleteCommentDto) {
        final Long commentId = deleteCommentDto.getCommentId();
        final Long parentId = deleteCommentDto.getParentId();

        commentRepository.deleteById(commentId);

        if (parentId == 0) {
            commentRepository.deleteAllByIdInQuery(commentId);
        }
    }

    public GetCommentResponse getComment(Long diaryId, Long cursorId, Integer page) {
        Slice<CommentVO> content = commentRepository.findByDiaryId(
                diaryId,
                cursorId,
                applyAscPageConfigBy(page, COMMENT_SIZE, FIELD_CREATED_AT)
        );

        List<CommentVO> commentVOS = content.getContent();

        List<GetCommentResponse.Comment> commentList = commentVOS.stream()
                .map(commentVO ->
                                GetCommentResponse.Comment.builder()
                                        .imageUrl(commentVO.getImageUrl())
                                        .username(commentVO.getUsername())
                                        .createdAt(commentVO.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                                        .content(commentVO.getContent())
                                        .commentId(commentVO.getCommentId())
                                        .parentId(commentVO.getParentId())
                                        .isWriter(commentVO.isWriter())
                                        .canDelete(commentVO.isCanDelete())
                                        .build()
                        )
                .collect(Collectors.toList());

        return new GetCommentResponse(content.hasNext(), commentList);

    }

}