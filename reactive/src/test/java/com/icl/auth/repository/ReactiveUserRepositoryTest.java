package com.icl.auth.repository;

import com.icl.auth.exception.UserNotFoundException;
import com.icl.auth.model.User;
import com.icl.auth.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReactiveUserRepositoryTest {
    @Autowired
    private ReactiveUserRepositoryImpl userRepository;

    @Test
    public void findByIdShouldReturnMono() {
        StepVerifier
                .create(userRepository.findById(1L))
                .expectNextMatches((user) ->
                        user.getLogin().equals("john") &&
                                user.getRole().equals(Role.USER))
                .verifyComplete();
    }

    @Test
    public void findByIdShouldThrowException() {
        StepVerifier
                .create(userRepository.findById(100L))
                .verifyError(UserNotFoundException.class);
    }

    @Test
    public void findByLoginShouldReturnMono() {
        String login = "john";
        StepVerifier
                .create(userRepository.findByLogin(login))
                .expectNextMatches(user -> user.getLogin().equals(login))
                .verifyComplete();
    }

    @Test
    public void saveShouldReturnMono() {
        User user = new User("newUser",
                "1w?",
                LocalDate.now().minus(5, ChronoUnit.DAYS),
                Role.USER);

        StepVerifier
                .create(userRepository.create(user))
                .expectNextMatches(savedUser -> savedUser.getLogin().equals(user.getLogin()))
                .verifyComplete();
    }

    @Test
    public void updateShouldReturnUpdatedUser() {
        User user = new User(1L, "john",
                "1w?",
                LocalDate.now().minus(5, ChronoUnit.DAYS),
                Role.USER);

        StepVerifier
                .create(userRepository.update(user))
                .expectNextMatches(updatedUser -> updatedUser.getLogin().equals(user.getLogin()))
                .verifyComplete();
    }

    @Test
    public void updateShouldThrowException() {
        User user = new User(100L, "john",
                "1w?",
                LocalDate.now().minus(5, ChronoUnit.DAYS),
                Role.USER);

        StepVerifier
                .create(userRepository.update(user))
                .verifyError(UserNotFoundException.class);
    }


    @Test
    public void findAllShouldReturnFluxUsers() {
        userRepository.findAll().subscribe(System.out::println);
        StepVerifier
                .create(userRepository.findAll())
                .expectNextMatches(firstUser -> firstUser.getLogin().equals("john"))
                .expectNextMatches(secondUser -> secondUser.getLogin().equals("q"))
                .verifyComplete();
    }

    @Test
    public void deleteShouldDeleteUser() {
        userRepository.deleteById(1L).subscribe();

        StepVerifier
                .create(userRepository.findAll())
                .expectNextMatches(secondUser -> secondUser.getLogin().equals("q"))
                .verifyComplete();
    }

    @Test
    public void deleteShouldThrowException() {
        StepVerifier
                .create(userRepository.deleteById(100L))
                .verifyError(UserNotFoundException.class);
    }
}
