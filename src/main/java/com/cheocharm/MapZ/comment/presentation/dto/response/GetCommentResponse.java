package com.cheocharm.MapZ.comment.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

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
}
