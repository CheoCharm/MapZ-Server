package com.mapz.api.diary.presentation.dto.response;

import com.mapz.domain.domains.diary.vo.MyDiaryVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class MyDiaryResponse {

    private boolean hasNext;
    private List<Diary> diaryList;

    @Getter
    @Builder
    public static class Diary {
        private String title;
        private String createdAt;
        private String ImageUrl;
        private Long groupId;
        private Long diaryId;
        private Long commentCount;
    }

    public static MyDiaryResponse of(boolean hasNext, List<MyDiaryVO> diaryVOS) {
        List<MyDiaryResponse.Diary> diaryList = diaryVOS.stream()
                .map(myDiaryVO ->
                        MyDiaryResponse.Diary.builder()
                                .title(myDiaryVO.getTitle())
                                .createdAt(myDiaryVO.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                                .diaryId(myDiaryVO.getDiaryId())
                                .groupId(myDiaryVO.getGroupId())
                                .commentCount(myDiaryVO.getCommentCount())
                                .build()
                )
                .collect(Collectors.toList());

        return new MyDiaryResponse(hasNext, diaryList);
    }
}
