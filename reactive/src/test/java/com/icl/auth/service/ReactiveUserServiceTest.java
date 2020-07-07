package com.icl.auth.service;

import com.icl.auth.exception.UserNotFoundException;
import com.icl.auth.exception.WrongPasswordException;
import com.icl.auth.model.User;
import com.icl.auth.repository.ReactiveUserRepository;
import com.icl.auth.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class ReactiveUserServiceTest {
    @MockBean
    private ReactiveUserRepository userRepository;

    @Autowired
    private UserService userService;

    private User user = new User("newUser", "password",
            LocalDate.now().minus(5, ChronoUnit.YEARS), Role.ADMIN);

    @Test
    public void saveShouldCreateNewUser() {
        when(userRepository.create(any(User.class))).thenReturn(Mono.just(user));
        StepVerifier
                .create(userService.save(user))
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    public void authorizeShouldThrowUserNotFound() {
        when(userRepository.findByLogin(anyString())).thenReturn(Mono.empty());

        StepVerifier
                .create(userService.authorize("login", "password"))
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    public void authorizeShouldThrowWrongPassword() {
        when(userRepository.findByLogin(anyString())).thenReturn(Mono.just(user));

        StepVerifier
                .create(userService.authorize("login", "password"))
                .expectError(WrongPasswordException.class)
                .verify();
    }

    @Test
    public void findByIdShouldFindUser() {
        when(userRepository.findById(anyLong())).thenReturn(Mono.just(user));

        StepVerifier
                .create(userService.findById(1L))
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    public void findByIdShouldThrowUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier
                .create(userService.findById(user.getId()))
                .expectError(UserNotFoundException.class);
    }
}