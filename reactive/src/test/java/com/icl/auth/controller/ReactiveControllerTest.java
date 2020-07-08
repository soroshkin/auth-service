package com.icl.auth.controller;

import com.icl.auth.config.AuthorizationFilter;
import com.icl.auth.exception.UserNotFoundException;
import com.icl.auth.exception.WrongPasswordException;
import com.icl.auth.model.User;
import com.icl.auth.security.Role;
import com.icl.auth.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest
public class ReactiveControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AuthorizationFilter filter;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder encoder;

    private User user = new User("newUser", "password?@",
            LocalDate.now().minus(5, ChronoUnit.YEARS), Role.ADMIN);

    @Test
    public void ifUserPasswordIsOkReturnOk() {
        when(userService.authorize(anyString(), anyString()))
                .thenReturn(Mono.just(user));

        webTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("login", "john")
                        .with("password", "rightPassword"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void ifUserPasswordIsWrongThrowException() {
        when(userService.authorize(anyString(), anyString()))
                .thenReturn(Mono.error(WrongPasswordException::new));

        webTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("login", "john")
                        .with("password", "wrongPassword"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void ifUserLoginIsWrongThrowException() {
        when(userService.authorize(anyString(), anyString()))
                .thenReturn(Mono.error(UserNotFoundException::new));

        webTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("login", "illegalLogin")
                        .with("password", "password"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void registerNewUserShouldReturnStatusOk() {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("login", user.getLogin());
        bodyBuilder.part("password", user.getPassword());
        bodyBuilder.part("role", user.getRole().toString());
        bodyBuilder.part("dateOfBirth", user.getDateOfBirth().toString());
        when(userService.save(any(User.class))).thenReturn(Mono.just(user));

        webTestClient.post()
                .uri("/register")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(User::getLogin, equalTo("newUser"));
    }

    @Test
    public void findByIdShouldFindUser() {
        filter.addToWhitelist("/user/1");
        when(userService.findById(anyLong())).thenReturn(Mono.just(user));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/user/{id}")
                        .build(1L))
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class);
    }

    @Test
    public void findByIdShouldFail() {
        when(userService.findById(anyLong())).thenReturn(Mono.just(user));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/user/{id}")
                        .build(1L))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}


