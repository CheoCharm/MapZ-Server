package com.cheocharm.MapZ.diary.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class WriteDiaryImageResponse {
    private Long diaryId;
    private List<String> imageURLs;
}
