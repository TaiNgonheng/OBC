package com.rhbgroup.dte.obc.common.func;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class Functions {
  private Functions() {}

  public static <T, R> Function<T, R> of(Function<T, R> function) {
    return function;
  }

  public static <T, U, R> BiFunction<T, U, R> of(BiFunction<T, U, R> function) {
    return function;
  }

  public static <T, U, R> Function<T, Function<U, R>> curry(BiFunction<T, U, R> function) {
    return t -> u -> function.apply(t, u);
  }

  public static <T, U, R> BiFunction<U, T, R> reverse(BiFunction<T, U, R> function) {
    return (u, t) -> function.apply(t, u);
  }

  public static <T, U, R> Function<U, Function<T, R>> curryRight(BiFunction<T, U, R> function) {
    return curry(reverse(function));
  }

  public static <T, U, R> Function<U, R> partial(BiFunction<T, U, R> biFunction, T t) {
    return u -> biFunction.apply(t, u);
  }

  public static <T, U> Consumer<U> partial(BiConsumer<T, U> biConsumer, T t) {
    return u -> biConsumer.accept(t, u);
  }

  public static <T, U, R> Function<T, R> partialRight(BiFunction<T, U, R> biFunction, U u) {
    return t -> biFunction.apply(t, u);
  }

  public static <T> Function<T, T> peek(Consumer<T> function) {
    return t -> {
      function.accept(t);
      return t;
    };
  }
}
