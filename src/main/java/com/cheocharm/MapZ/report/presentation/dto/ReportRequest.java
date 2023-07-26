package com.cheocharm.MapZ.report.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Schema(description = "게시글 신고 Request Body")
@AllArgsConstructor
@Getter
public class ReportRequest {

    @NotNull
    private Long diaryId;
}
