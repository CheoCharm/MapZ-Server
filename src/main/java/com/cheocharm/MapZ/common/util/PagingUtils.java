package com.cheocharm.MapZ.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PagingUtils {

    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_GROUP_NAME = "groupName";

    public static final Integer GROUP_SIZE = 10;
    public static final Integer USER_SIZE = 10;
    public static final Integer MY_LIKE_DIARY_SIZE = 20;
    public static final Integer MY_DIARY_SIZE = 20;

    private static final Long DEFAULT_CURSOR_ID = Long.MAX_VALUE;

    public static Pageable applyAscPageConfigBy(int page, int size, String property) {
        return PageRequest.of(page, size, Sort.Direction.ASC, property);
    }

    public static Pageable applyDescPageConfigBy(int page, int size, String property) {
        return PageRequest.of(page, size, Sort.Direction.DESC, property);
    }

    public static Long applyCursorId(Long cursorId) {
        if (cursorId == 0) {
            return DEFAULT_CURSOR_ID;
        }
        return cursorId;
    }
}
