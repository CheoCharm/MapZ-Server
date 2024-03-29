package com.mapz.api.comment.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Schema(description = "댓글 삭제 Request Body")
@AllArgsConstructor
@Getter
public class DeleteCommentRequest {

    @Schema(description = "원본 댓글일 경우 0")
    @NotNull
    private Long parentId;

    @NotNull
    private Long commentId;
}
