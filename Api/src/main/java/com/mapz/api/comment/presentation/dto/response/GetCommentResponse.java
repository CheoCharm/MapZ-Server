package com.mapz.api.comment.presentation.dto.response;

import com.mapz.domain.domains.comment.vo.CommentVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class GetCommentResponse {

    private boolean hasNext;
    private List<Comment> commentList;

    @Getter
    @Builder
    public static class Comment {
        private String imageUrl;
        private String username;
        private String createdAt;
        private String content;
        private Long commentId;
        private Long parentId;
        private Boolean isWriter;
        private boolean canDelete;
    }

    public static GetCommentResponse of(boolean hasNext, List<CommentVO> commentVOS) {
        List<GetCommentResponse.Comment> list = commentVOS.stream()
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

        return new GetCommentResponse(hasNext, list);
    }
}
