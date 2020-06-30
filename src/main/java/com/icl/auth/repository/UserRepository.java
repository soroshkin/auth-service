package com.icl.auth.repository;

import com.icl.auth.exception.UserNotFoundException;
import com.icl.auth.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);

    Optional<User> findByLogin(String login);

    User save(User user);

    void deleteById(Long id) throws UserNotFoundException;

    List<User> findAll();

    boolean existsById(Long id);
}
