package com.mapz.api.comment.application;

import com.mapz.api.common.exception.diary.NotFoundDiaryException;
import com.mapz.api.common.interceptor.UserThreadLocal;
import com.mapz.api.common.util.PagingUtils;
import com.mapz.domain.domains.comment.entity.Comment;
import com.mapz.domain.domains.comment.vo.CommentVO;
import com.mapz.api.comment.presentation.dto.request.CreateCommentRequest;
import com.mapz.api.comment.presentation.dto.response.GetCommentResponse;
import com.mapz.domain.domains.comment.repository.CommentRepository;
import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.repository.DiaryRepository;
import com.mapz.domain.domains.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.mapz.api.common.util.PagingUtils.applyAscPageConfigBy;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final DiaryRepository diaryRepository;

    private static final Long ROOT_COMMENT_PARENT_ID = 0L;

    @Transactional
    public void createComment(CreateCommentRequest request) {
        final User user = UserThreadLocal.get();
        Diary diary = diaryRepository.findById(request.getDiaryId())
                .orElseThrow(NotFoundDiaryException::new);

        commentRepository.save(
                Comment.of(request.getContent(), request.getParentId(), user, diary)
        );
    }

    @Transactional
    public void deleteComment(Long parentId, Long commentId) {
        commentRepository.deleteById(commentId);
        checkParentCommentAndDeleteChildComment(parentId, commentId);
    }

    private void checkParentCommentAndDeleteChildComment(Long parentId, Long commentId) {
        if (Objects.equals(parentId, ROOT_COMMENT_PARENT_ID)) {
            commentRepository.deleteAllByIdInQuery(commentId);
        }
    }

    public GetCommentResponse getComment(Long diaryId, Integer page, Long cursorId) {
        Long userId = UserThreadLocal.get().getId();
        Slice<CommentVO> content = commentRepository.findByDiaryId(
                userId,
                diaryId,
                cursorId,
                applyAscPageConfigBy(page, PagingUtils.COMMENT_SIZE, PagingUtils.FIELD_CREATED_AT)
        );

        List<CommentVO> commentVOS = content.getContent();
        return GetCommentResponse.of(content.hasNext(), commentVOS);
    }

}