package com.mapz.api.common.fixtures;

import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.entity.DiaryImage;

public class DiaryImageFixtures {

    public static DiaryImage createDiaryImage(Diary diary) {
        return DiaryImage.builder()
                .diary(diary)
                .diaryImageUrl("url")
                .build();
    }
}
