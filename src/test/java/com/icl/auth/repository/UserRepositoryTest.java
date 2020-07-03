package com.icl.auth.repository;

import com.icl.auth.exception.UserNotFoundException;
import com.icl.auth.model.User;
import com.icl.auth.security.Role;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@SqlGroup(value = @Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD))
public class UserRepositoryTest {

    @Autowired
    private JpaUserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("1234",
                "1w?",
                LocalDate.now().minus(5, ChronoUnit.DAYS),
                Role.USER);
    }

    @Test
    public void saveShouldReturnNewUser() {
        assertThat(userRepository.save(user)).isEqualTo(user);
    }

    @Test
    public void saveShouldUpdateUser() {
        assertThat(userRepository.save(user).getRole()).isEqualTo(Role.USER);
        user.setRole(Role.ADMIN);
        assertThat(userRepository.save(user).getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    public void deleteUserShouldDeleteUser() throws UserNotFoundException {
        assertThat(userRepository.save(user)).isEqualTo(user);
        userRepository.deleteById(user.getId());
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    public void deleteUserShouldThrowNotFoundException() {
        assertThat(userRepository.save(user)).isEqualTo(user);
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userRepository.deleteById(Long.MAX_VALUE));
    }

    @Test
    public void findByLoginShouldReturnUser() {
        assertThat(userRepository.save(user)).isEqualTo(user);
        assertThat(userRepository.findByLogin(user.getLogin())).isEqualTo(Optional.of(user));
    }

    @Test
    public void findAllShouldReturnUserList() {
        assertThat(userRepository.findAll().get(0).getLogin()).isEqualTo("john");
        assertThat(userRepository.findAll().get(1).getDateOfBirth()).isEqualTo(LocalDate.of(2000, 11, 17));
    }
}
