package com.mapz.domain.domains.comment.vo;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentVO {
    private String imageUrl;
    private String username;
    private LocalDateTime createdAt;
    private String content;
    private Long userId;
    private Long commentId;
    private Long parentId;
    private boolean isWriter;
    private boolean canDelete;

    @QueryProjection
    public CommentVO(String imageUrl, String username, LocalDateTime createdAt, String content, Long userId, Long commentId, Long parentId, boolean isWriter, boolean canDelete) {
        this.imageUrl = imageUrl;
        this.username = username;
        this.createdAt = createdAt;
        this.content = content;
        this.userId = userId;
        this.commentId = commentId;
        this.parentId = parentId;
        this.isWriter = isWriter;
        this.canDelete = canDelete;
    }
}
