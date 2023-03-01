package com.rhbgroup.dte.obc.common.func;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Consumers {

    public static <T, U> Function<T, Consumer<U>> curry(BiConsumer<T, U> consumer) {
        return t -> u -> consumer.accept(t, u);
    }

    public static <T, U> Consumer<U> partial(BiConsumer<T, U> biFunction, T t) {
        return u -> biFunction.accept(t, u);
    }
}
