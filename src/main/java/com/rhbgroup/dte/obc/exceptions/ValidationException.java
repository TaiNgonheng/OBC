package com.rhbgroup.dte.obc.exceptions;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class ValidationException extends RuntimeException {
    ValidationErrors errors;
}
