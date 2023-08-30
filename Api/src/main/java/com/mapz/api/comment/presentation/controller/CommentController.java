package com.mapz.api.comment.presentation.controller;

import com.mapz.api.comment.application.CommentService;
import com.mapz.api.comment.presentation.dto.request.CreateCommentRequest;
import com.mapz.api.comment.presentation.dto.response.GetCommentResponse;
import com.mapz.api.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "CommentController")
@RequiredArgsConstructor
@RequestMapping("/api/comment")
@RestController
public class CommentController {

    private final CommentService commentService;

    @Operation(description = "댓글 작성")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PostMapping
    public CommonResponse<?> createComment(@RequestBody @Valid CreateCommentRequest createCommentDto) {
        commentService.createComment(createCommentDto);
        return CommonResponse.success();
    }

    @Operation(description = "댓글 삭제")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @DeleteMapping("/{parentId}/{commentId}")
    public CommonResponse<?> deleteComment(@PathVariable Long parentId, @PathVariable Long commentId) {
        commentService.deleteComment(parentId, commentId);
        return CommonResponse.success();
    }

    @Operation(description = "댓글 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping("/{diaryId}/{page}")
    public CommonResponse<GetCommentResponse> getComment(@Parameter @PathVariable Long diaryId, @PathVariable Integer page, @RequestParam Long cursorId) {
        return CommonResponse.success(commentService.getComment(diaryId, page, cursorId));
    }
}
