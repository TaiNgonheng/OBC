package com.rhbgroup.dte.obc.common.utils;

import com.rhbgroup.dte.obc.common.pojo.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PagingUtils {
    public static PageRequest toPageRequest(int page, int size, String[] sort) {
        return PageRequest.of(page, size, toSort(sort));
    }

    public static <T> Page<T> toPage(org.springframework.data.domain.Page<T> page) {
        return Page.<T>builder()
                .items(page.getContent())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

    public static <T> Page<T> toPage(List<T> contents, int totalPage) {
        return Page.<T>builder()
                .items(contents)
                .totalPages(totalPage)
                .totalElements(contents.size())
                .build();
    }

    private static Sort toSort(String[] sort) {
        return Sort.by(Arrays.stream(sort).map(PagingUtils::splitSort).map(PagingUtils::toOrder).collect(Collectors.toList()));
    }

    private static String[] splitSort(String sort) {
        return sort.split("\\|");
    }

    private static Sort.Order toOrder(String[] sortStringPair) {
        return new Sort.Order(getSortDirection(sortStringPair[1]), sortStringPair[0]);
    }

    private static Sort.Direction getSortDirection(String direction) {
        if ("asc".equals(direction)) {
            return Sort.Direction.ASC;
        }
        if ("desc".equals(direction)) {
            return Sort.Direction.DESC;
        }

        throw new IllegalArgumentException("error.sort.validation.direction.not.valid");
    }
}
