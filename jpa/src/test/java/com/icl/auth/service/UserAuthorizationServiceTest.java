package com.icl.auth.service;

import com.icl.auth.exception.UserNotFoundException;
import com.icl.auth.exception.WrongPasswordException;
import com.icl.auth.model.User;
import com.icl.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserAuthorizationServiceTest {

    @InjectMocks
    private UserAuthorizationServiceImpl userAuthorizationService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private User user = Mockito.mock(User.class);

    @Test
    public void saveShouldReturnSavedUser() {
        when(userRepository.save(user)).thenReturn(user);
        assertThat(userAuthorizationService.save(user)).isEqualTo(user);
    }

    @Test
    public void authorizeShouldReturnUser() throws UserNotFoundException, WrongPasswordException {
        when(userRepository.findByLogin(anyString())).thenReturn(Optional.of(user));
        when(encoder.matches(anyString(), anyString())).thenReturn(true);
        when(user.getPassword()).thenReturn(anyString());
        assertThat(userAuthorizationService.authorize("login", "password"))
                .isEqualTo(Optional.of(user));
    }

    @Test
    public void authorizeShouldThrowWrongPasswordException() {
        when(userRepository.findByLogin(anyString())).thenReturn(Optional.of(user));
        when(encoder.matches(anyString(), anyString())).thenReturn(false);
        when(user.getPassword()).thenReturn(anyString());
        assertThatExceptionOfType(WrongPasswordException.class)
                .isThrownBy(() -> userAuthorizationService.authorize("login", "password"));
    }

    @Test
    public void authorizeShouldThrowUserNotFoundException() {
        when(userRepository.findByLogin(anyString())).thenReturn(Optional.empty());
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userAuthorizationService.authorize("login", "password"));
    }
}
