package com.cheocharm.MapZ.comment.domain;

import com.cheocharm.MapZ.comment.domain.dto.CreateCommentDto;
import com.cheocharm.MapZ.comment.domain.dto.DeleteCommentDto;
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
    public CommonResponse<?> createComment(@RequestBody @Valid CreateCommentDto createCommentDto) {
        commentService.createComment(createCommentDto);
        return CommonResponse.success();
    }

    @Operation(description = "댓글 삭제")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @DeleteMapping
    public CommonResponse<?> deleteComment(@RequestBody @Valid DeleteCommentDto deleteCommentDto) {
        commentService.deleteComment(deleteCommentDto);
        return CommonResponse.success();
    }
}
