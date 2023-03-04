package com.cheocharm.MapZ.comment.presentation.controller;

import com.cheocharm.MapZ.comment.application.CommentService;
import com.cheocharm.MapZ.comment.presentation.dto.request.CreateCommentRequest;
import com.cheocharm.MapZ.comment.presentation.dto.request.DeleteCommentRequest;
import com.cheocharm.MapZ.comment.presentation.dto.response.GetCommentResponse;
import com.cheocharm.MapZ.common.CommonResponse;
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
    @DeleteMapping
    public CommonResponse<?> deleteComment(@RequestBody @Valid DeleteCommentRequest deleteCommentDto) {
        commentService.deleteComment(deleteCommentDto);
        return CommonResponse.success();
    }

    @Operation(description = "댓글 조회")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @GetMapping
    public CommonResponse<GetCommentResponse> getComment(@Parameter @RequestParam Long diaryId, @RequestParam Long cursorId, @RequestParam Integer page) {
        return CommonResponse.success(commentService.getComment(diaryId, cursorId, page));
    }
}
