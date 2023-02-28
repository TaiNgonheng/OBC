package com.rhbgroup.dte.obc.exceptions;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@RequiredArgsConstructor(staticName = "of")
public class ValidationErrors {
    List<String> errors;

    public static ValidationErrors of(String error) {
        return of(List.of(error));
    }
}
