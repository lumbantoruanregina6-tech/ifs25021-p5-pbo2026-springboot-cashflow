package org.delcom._1s25021springbootcashflow.domain.repository;

import org.delcom._1s25021springbootcashflow.domain.entity.User;
import java.util.Optional;

public interface IUserRepository extends IRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}
