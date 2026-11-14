package org.delcom._1s25021springbootcashflow.adapter.repository;

import org.delcom._1s25021springbootcashflow.domain.entity.User;
import org.delcom._1s25021springbootcashflow.domain.repository.IUserRepository;
import java.util.Optional;

public class UserRepository extends InMemoryRepository<User> implements IUserRepository {
    @Override
    protected int getId(User entity) {
        return entity.getId();
    }

    @Override
    protected void setId(User entity, int id) {
        entity.setId(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return store.values().stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }
}
