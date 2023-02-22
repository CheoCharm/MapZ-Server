package com.cheocharm.MapZ.report.presentation.controller;

import com.cheocharm.MapZ.common.CommonResponse;
import com.cheocharm.MapZ.report.application.ReportService;
import com.cheocharm.MapZ.report.presentation.dto.ReportRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "ReportController")
@RequiredArgsConstructor
@RequestMapping("/api/report")
@RestController
public class ReportController {

    private final ReportService reportService;

    @Operation(description = "게시글 신고")
    @Parameter(name = "accessToken", in = ParameterIn.HEADER, required = true)
    @PostMapping
    public CommonResponse<?> reportDiary(@RequestBody @Valid ReportRequest reportRequest) {
        reportService.reportDiary(reportRequest);
        return CommonResponse.success();
    }
}
