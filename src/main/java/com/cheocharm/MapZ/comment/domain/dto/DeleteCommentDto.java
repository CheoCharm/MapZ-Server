package com.cheocharm.MapZ.comment.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Schema(description = "댓글 삭제 Request Body")
@Getter
@Builder
public class DeleteCommentDto {

    @Schema(description = "원본 댓글일 경우 0")
    @NotNull
    private Long parentId;

    @NotNull
    private Long commentId;
}
