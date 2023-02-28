package com.rhbgroup.dte.obc.exceptions;

import java.util.function.Function;
import java.util.function.Supplier;

public class ExceptionSuppliers {
    public static Supplier<IllegalStateException> illegalState(String msg) {
        return () -> new IllegalStateException(msg);
    }

    public static Supplier<IllegalArgumentException> illegalArgument(String msg) {
        return () -> new IllegalArgumentException(msg);
    }

    public static <T> Function<ValidationErrors, T> throwValidationException() {
        return errors -> {
            throw new ValidationException(errors);
        };
    }
}
