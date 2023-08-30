package com.mapz.api.diary.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class WriteDiaryImageResponse {
    private Long diaryId;
    private List<String> imageURLs;
    private List<String> imageName;

    public static WriteDiaryImageResponse of(Long diaryId, List<String> imageURLs, List<MultipartFile> files) {
        return new WriteDiaryImageResponse(diaryId, imageURLs, getImageName(files));
    }

    private static List<String> getImageName(List<MultipartFile> files) {
        return files.stream()
                .map(MultipartFile::getOriginalFilename)
                .collect(Collectors.toList());
    }
}
