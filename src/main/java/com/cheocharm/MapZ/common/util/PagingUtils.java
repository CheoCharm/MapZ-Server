package com.cheocharm.MapZ.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PagingUtils {

    public static final String FIELD_CREATED_AT = "createdAt";

    public static final Integer GROUP_SIZE = 10;

    public static Pageable applyPageConfigBy(int page, int size) {
        return PageRequest.of(page, size);
    }

    public static Pageable applyAscPageConfigBy(int page, int size, String property) {
        return PageRequest.of(page, size, Sort.Direction.ASC, property);
    }

    public static Pageable applyDescPageConfigBy(int page, int size, String property) {
        return PageRequest.of(page, size, Sort.Direction.DESC, property);
    }
}
