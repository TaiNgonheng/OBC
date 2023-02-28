package com.rhbgroup.dte.obc.exceptions;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(staticName = "of")
public class ValidationError {
    String message;
}
