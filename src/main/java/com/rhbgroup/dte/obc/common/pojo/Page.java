package com.rhbgroup.dte.obc.common.pojo;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class Page<T> {
    int totalPages;
    long totalElements;
    List<T> items;

    public <R> Page<R> map(Function<? super T, ? extends R> converter) {
        return Page.<R>builder()
                .totalPages(totalPages)
                .totalElements(totalElements)
                .items(items.stream().map(converter).collect(Collectors.toList()))
                .build();
    }
}
