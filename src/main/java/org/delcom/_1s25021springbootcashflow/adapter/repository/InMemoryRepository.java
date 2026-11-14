package org.delcom._1s25021springbootcashflow.adapter.repository;

import org.delcom._1s25021springbootcashflow.domain.repository.IRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/** Implementasi in-memory generik, dipakai untuk unit test. */
public abstract class InMemoryRepository<T> implements IRepository<T, Integer> {
    protected final Map<Integer, T> store = new LinkedHashMap<>();
    private int nextId = 1;

    protected abstract int getId(T entity);

    protected abstract void setId(T entity, int id);

    @Override
    public T save(T entity) {
        if (getId(entity) == 0) {
            setId(entity, nextId++);
        }
        store.put(getId(entity), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<T> findBy(Predicate<T> predicate) {
        return store.values().stream().filter(predicate).toList();
    }

    @Override
    public boolean deleteById(Integer id) {
        return store.remove(id) != null;
    }
}
