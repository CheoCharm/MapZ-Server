package com.mapz.api.comment.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "댓글 작성 Request Body")
@AllArgsConstructor
@Getter
public class CreateCommentRequest {

    @Schema(description = "300자 제한")
    @Size(max = 300)
    @NotNull
    private String content;

    @Schema(description = "대댓글일 경우 원본 댓글의 id, 원본 댓글일 경우 0")
    @NotNull
    private Long parentId;

    @NotNull
    private Long diaryId;
}
