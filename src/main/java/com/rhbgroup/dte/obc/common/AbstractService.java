package com.rhbgroup.dte.obc.common;

import com.rhbgroup.dte.obc.common.pojo.Page;
import com.rhbgroup.dte.obc.common.utils.PagingUtils;
import com.rhbgroup.dte.obc.common.utils.func.Functions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static com.rhbgroup.dte.obc.common.utils.func.Functions.of;

public abstract class AbstractService<ID, E extends Entity<ID>, O> {

    public abstract DbMapper<E, O> getMapper();

    public abstract JpaRepository<O, ID> getRepository();

    public E create(E e) {
        return save(e);
    }

    public E update(E e) {
        return save(e);
    }

    public E save(E e) {
        return Functions.of(getMapper()::toOrm)
                .andThen(getRepository()::saveAndFlush)
                .andThen(getMapper()::toEntity)
                .apply(e);
    }

    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }

    public void delete(E e) {
        of(E::getId)
                .andThen(Functions.peek(getRepository()::deleteById))
                .apply(e);
    }

    public Page<E> findAll(int page, int size, String[] sort) {
        return Functions.of(PagingUtils::toPageRequest)
                .andThen(getRepository()::findAll)
                .andThen(PagingUtils::toPage)
                .apply(page, size, sort)
                .map(getMapper()::toEntity);
    }


    public Optional<E> findById(ID id) {
        return getRepository().findById(id).map(getMapper()::toEntity);
    }
}
