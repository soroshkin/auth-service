package com.icl.auth.service;

import com.icl.auth.exception.UserNotFoundException;
import com.icl.auth.exception.WrongPasswordException;
import com.icl.auth.model.User;
import com.icl.auth.repository.ReactiveUserRepository;
import com.icl.auth.security.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Service
public class ReactiveUserService implements UserService {
    private ReactiveUserRepository userRepository;
    private PasswordEncoder encoder;

    @Autowired
    public ReactiveUserService(ReactiveUserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    /**
     * Saves or updates user to database, if user is new, then method sets default {@link Role} USER and
     * encodes password via {@link PasswordEncoder} passwordEncoder
     *
     * @param user object, which has to be saved into database
     * @return Mono of updated or newly created  user
     */
    @Override
    public Mono<User> save(User user) {
        if (user.isNew()) {
            user.setRole(Role.ADMIN);
            user.setPassword(encoder.encode(user.getPassword()));
            return userRepository.create(user);
        } else {
            return userRepository.update(user);
        }
    }

    /**
     * Method verifies if user is present in database and password is correct
     *
     * @param login - user's login
     * @param password - user's password
     * @return Mono<User> with given login and password
     */
    @Override
    public Mono<User> authorize(String login, String password) {
        return userRepository.findByLogin(login)
                .flatMap(user -> {
                    if (encoder.matches(password, user.getPassword())) {
                        return Mono.just(user);
                    } else {
                        return Mono.error(WrongPasswordException::new);
                    }
                })
                .switchIfEmpty(Mono.error(UserNotFoundException::new));
    }

    /**
     * Searches user in database
     * @param id user id to be found
     * @return Mono<User> if user is found, or Optional.empty() if not
     */
    @Override
    public Mono<User> findById(Long id){
        return userRepository.findById(id);
    }
}
