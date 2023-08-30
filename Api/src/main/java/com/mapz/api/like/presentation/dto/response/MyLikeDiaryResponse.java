package com.mapz.api.like.presentation.dto.response;

import com.mapz.domain.domains.diary.vo.MyLikeDiaryVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class MyLikeDiaryResponse {

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

    public static MyLikeDiaryResponse of(boolean hasNext, List<MyLikeDiaryVO> myLikeDiaryVOS) {
        List<MyLikeDiaryResponse.Diary> diaries = myLikeDiaryVOS.stream()
                .map(myLikeDiary ->
                        MyLikeDiaryResponse.Diary.builder()
                                .title(myLikeDiary.getTitle())
                                .createdAt(myLikeDiary.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                                .diaryId(myLikeDiary.getDiaryId())
                                .groupId(myLikeDiary.getGroupId())
                                .commentCount(myLikeDiary.getCommentCount())
                                //일기 대표 이미지 빌더에 추가
                                .build()
                )
                .collect(Collectors.toList());

        return new MyLikeDiaryResponse(hasNext, diaries);
    }
}
