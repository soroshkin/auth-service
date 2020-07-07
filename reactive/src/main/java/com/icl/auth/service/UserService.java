package com.icl.auth.service;

import com.icl.auth.model.User;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> save(User user);

    Mono<User> authorize(String login, String password);

    Mono<User> findById(Long id);
}
