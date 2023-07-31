package com.cheocharm.MapZ.comment.application;

import com.cheocharm.MapZ.comment.domain.Comment;
import com.cheocharm.MapZ.comment.domain.repository.vo.CommentVO;
import com.cheocharm.MapZ.comment.presentation.dto.request.CreateCommentRequest;
import com.cheocharm.MapZ.comment.presentation.dto.request.DeleteCommentRequest;
import com.cheocharm.MapZ.comment.presentation.dto.response.GetCommentResponse;
import com.cheocharm.MapZ.comment.domain.repository.CommentRepository;
import com.cheocharm.MapZ.common.exception.diary.NotFoundDiaryException;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.diary.domain.repository.DiaryRepository;
import com.cheocharm.MapZ.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.cheocharm.MapZ.common.util.PagingUtils.COMMENT_SIZE;
import static com.cheocharm.MapZ.common.util.PagingUtils.FIELD_CREATED_AT;
import static com.cheocharm.MapZ.common.util.PagingUtils.applyAscPageConfigBy;

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
                Comment.of(request, user, diary)
        );
    }

    @Transactional
    public void deleteComment(DeleteCommentRequest request) {
        final Long commentId = request.getCommentId();
        commentRepository.deleteById(commentId);
        checkParentCommentAndDeleteChildComment(request.getParentId(), commentId);
    }

    private void checkParentCommentAndDeleteChildComment(Long parentId, Long commentId) {
        if (Objects.equals(parentId, ROOT_COMMENT_PARENT_ID)) {
            commentRepository.deleteAllByIdInQuery(commentId);
        }
    }

    public GetCommentResponse getComment(Long diaryId, Long cursorId, Integer page) {
        Long userId = UserThreadLocal.get().getId();
        Slice<CommentVO> content = commentRepository.findByDiaryId(
                userId,
                diaryId,
                cursorId,
                applyAscPageConfigBy(page, COMMENT_SIZE, FIELD_CREATED_AT)
        );

        List<CommentVO> commentVOS = content.getContent();
        return GetCommentResponse.of(content.hasNext(), commentVOS);
    }

}