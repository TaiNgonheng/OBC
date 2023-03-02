package com.rhbgroup.dte.obc.common.func;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class Predicates {
  public static <T> Predicate<T> of(Predicate<T> p) {
    return p;
  }

  public static <T> boolean any(T t) {
    return true;
  }

  public static <T> boolean none(T t) {
    return false;
  }

  public static <T, R> boolean any(T t, R r) {
    return true;
  }

  public static <T, R> boolean none(T t, R r) {
    return false;
  }

  public static <T> Predicate<T> equal(T t) {
    return t::equals;
  }

  public static <T extends Comparable<T>> Predicate<T> eq(T t) {
    return v -> v.compareTo(t) == 0;
  }

  public static <T extends Comparable<T>> Predicate<T> lt(T t) {
    return v -> v.compareTo(t) < 0;
  }

  public static <T extends Comparable<T>> Predicate<T> ltEq(T t) {
    return v -> v.compareTo(t) <= 0;
  }

  public static <T extends Comparable<T>> Predicate<T> gt(T t) {
    return v -> v.compareTo(t) > 0;
  }

  public static <T extends Comparable<T>> Predicate<T> gtEq(T t) {
    return v -> v.compareTo(t) > 0;
  }

  public static <T, U> Predicate<T> partialRight(BiFunction<T, U, Boolean> biFunction, U u) {
    return t -> biFunction.apply(t, u);
  }

  public static <T, U> Predicate<U> partial(BiFunction<T, U, Boolean> biFunction, T t) {
    return u -> biFunction.apply(t, u);
  }
}
