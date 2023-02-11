package com.cheocharm.MapZ.report.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Schema(description = "게시글 신고 Request Body")
@Getter
public class ReportDto {

    @NotNull
    private Long diaryId;
}
