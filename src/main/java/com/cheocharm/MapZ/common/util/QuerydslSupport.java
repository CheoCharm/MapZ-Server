package com.cheocharm.MapZ.common.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import java.util.List;

public class QuerydslSupport {

    public static <T> Slice<T> fetchSlice(JPAQuery<T> query, Pageable pageable) {
        int pageSize = pageable.getPageSize();

        List<T> content = query
                .offset(pageable.getOffset())
                .limit(pageSize + 1)
                .fetch();

        return new SliceImpl<>(content, pageable, isHasNext(pageSize, content));
    }

    public static <T> Slice<T> fetchSlice(Class<? extends T> type, PathMetadata pathMetadata, JPAQuery<T> query, Pageable pageable) {
        Sort.Order order = pageable.getSort().iterator().next();

        int pageSize = pageable.getPageSize();

        List<T> content = query
                .orderBy(getOrderSpecifier(type, pathMetadata, order))
                .offset(pageable.getOffset())
                .limit(pageSize + 1)
                .fetch();

        return new SliceImpl<>(content, pageable, isHasNext(pageSize, content));
    }

    private static <T> OrderSpecifier<?> getOrderSpecifier(Class<? extends T> type, PathMetadata pathMetadata, Sort.Order order) {
        PathBuilder<? extends T> pathBuilder = new PathBuilder<T>(type, pathMetadata);
        return new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(order.getProperty()));
    }

    private static <T> boolean isHasNext(int pageSize, List<T> content) {
        boolean hasNext = false;
        if (pageSize < content.size()) {
            hasNext = true;
            content.remove(pageSize);
        }
        return hasNext;
    }
}
