package com.icl.auth.controller;

import com.icl.auth.exception.UserNotFoundException;
import com.icl.auth.exception.WrongPasswordException;
import com.icl.auth.model.User;
import com.icl.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Optional;

@RestController
public class ReactiveController {
    private UserService userService;

    @Autowired
    public ReactiveController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates new {@link User} object and saves it into database
     *
     * @param user to be saved into database
     * @return name of the view, which should be rendered
     */
    @PostMapping(path = "/register")
    public Mono<ResponseEntity<User>> registerNewUser(@Valid User user) {
        return userService.save(user)
                .map(savedUser -> ResponseEntity.of(Optional.of(savedUser)));
    }

    /**
     * Processes user authentication
     *
     * @param exchange is {@link ServerWebExchange} object, which contains login form data
     * @param session  will contain {@link User} object, if logging is successful
     * @return Mono of ResponseEntity with {@link User} inside body if user is found, or null if not
     */
    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<ResponseEntity<User>> login(ServerWebExchange exchange, WebSession session) {
        Mono<MultiValueMap<String, String>> data = exchange.getFormData()
                .flatMap(formData -> {
                    MultiValueMap<String, String> formDataResponse = new LinkedMultiValueMap<>();
                    formDataResponse.addAll(formData);
                    return Mono.just(formDataResponse);
                });

        return data.flatMap(formData ->
                userService.authorize(formData.getFirst("login"), formData.getFirst("password"))
                        .map(user -> {
                            session.getAttributes().putIfAbsent("user", user);
                            return ResponseEntity.ok().body(user);
                        })
                        .onErrorResume(WrongPasswordException.class, exception ->
                                Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(null)))
                        .onErrorResume(UserNotFoundException.class, exception ->
                                Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null))));
    }

    /**
     * Deletes user from {@link WebSession} session
     *
     * @return ResponseEntity with {@link HttpStatus} OK
     */
    @GetMapping(path = "/logout")
    public ResponseEntity<HttpStatus> logout(WebSession session) {
        session.getAttributes().remove("user");
        return ResponseEntity.ok(HttpStatus.OK);
    }

    /**
     * Searches user in database
     *
     * @param id user id to be found
     * @return Mono of ResponseEntity with Optional {@link User} inside body if user is found, or Optional.empty() if not
     */
    @GetMapping(path = "/user/{id}")
    public Mono<ResponseEntity<User>> findUserById(@PathVariable Long id) {
        return Mono.from(userService.findById(id))
                .map(user -> ResponseEntity.of(Optional.of(user)))
                .onErrorReturn(ex -> ex instanceof UserNotFoundException,
                        ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }
}
