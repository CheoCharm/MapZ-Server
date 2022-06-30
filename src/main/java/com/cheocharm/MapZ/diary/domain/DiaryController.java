package com.cheocharm.MapZ.diary.domain;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DiaryController")
@RequiredArgsConstructor
@RequestMapping("/api/diary")
@RestController
public class DiaryController {

}
