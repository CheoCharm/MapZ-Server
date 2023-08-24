package com.cheocharm.MapZ.common.fixtures;

import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.user.domain.User;

public class DiaryFixtures {
    public static final String CURRENT_PLACE_LONGITUDE = "37.478505";
    public static final String CURRENT_PLACE_LATITUDE = "126.981383";

    public static final String LONGITUDE_PARAM_NAME = "longitude";
    public static final String LATITUDE_PARAM_NAME = "latitude";

    public static final String CURRENT_ZOOM_LEVEL = "17";
    public static final String ZOOM_LEVEL_PARAM_NAME = "zoomLevel";

    public static final String DIARY_TITLE = "상반기 구매한 옷들..";
    public static final String NO_IMAGE_CONTENT = "사진을 포함하지 않는 내용입니다~!";
    public static final String ADDRESS = "부산광역시";

    public static Diary createDiary(User user, Group group) {
        return Diary.builder()
                .title(DIARY_TITLE)
                .content(NO_IMAGE_CONTENT)
                .user(user)
                .group(group)
                .address(ADDRESS)
                .build();
    }

}
