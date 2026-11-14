package org.delcom._1s25021springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Port CRUD generik. save() = insert jika id belum ada (0), selain itu update. */
public interface IRepository<T, ID> {
    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    List<T> findBy(Predicate<T> predicate);

    boolean deleteById(ID id);
}
