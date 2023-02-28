package com.rhbgroup.dte.obc.common;

public interface DbMapper<E extends Entity, O> {
    E toEntity(O o);

    O toOrm(E e);
}
