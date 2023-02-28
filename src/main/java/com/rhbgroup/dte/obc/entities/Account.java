package com.rhbgroup.dte.obc.entities;

import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class Account {

    private String username;
    private String password;
}
