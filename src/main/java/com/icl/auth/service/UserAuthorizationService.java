package com.icl.auth.service;

import com.icl.auth.exception.UserNotFoundException;
import com.icl.auth.exception.WrongPasswordException;
import com.icl.auth.model.User;

import java.util.Optional;

public interface UserAuthorizationService {
    User save(User user);
    Optional<User> authorize(String login, String password) throws UserNotFoundException, WrongPasswordException;
}
