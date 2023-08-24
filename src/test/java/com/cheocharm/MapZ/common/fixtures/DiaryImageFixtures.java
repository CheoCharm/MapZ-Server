package com.cheocharm.MapZ.common.fixtures;

import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.diary.domain.DiaryImage;

public class DiaryImageFixtures {

    public static DiaryImage createDiaryImage(Diary diary) {
        return DiaryImage.builder()
                .diary(diary)
                .diaryImageUrl("url")
                .build();
    }
}
