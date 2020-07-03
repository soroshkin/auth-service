package com.icl.auth.service;

import com.icl.auth.exception.UserNotFoundException;
import com.icl.auth.exception.WrongPasswordException;
import com.icl.auth.model.User;
import com.icl.auth.repository.UserRepository;
import com.icl.auth.security.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserAuthorizationServiceImpl implements UserAuthorizationService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserAuthorizationServiceImpl(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = encoder;
    }

    /**
     * Saves or updates user to database, if user is new, then method sets default {@link Role} USER and
     * encodes password via {@link PasswordEncoder} passwordEncoder
     *
     * @param user object, which has to be saved into database
     * @return updated or newly created user
     */
    @Override
    public User save(User user) {
        if (user.isNew()) {
            user.setRole(Role.USER);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    /**
     * Method verifies if user is present in database and password is correct
     *
     * @param login    - user's login
     * @param password - user's password
     * @return user with given login and password
     * @throws UserNotFoundException,  when user with given login not found
     * @throws WrongPasswordException, when wrong password is given
     */
    @Override
    public Optional<User> authorize(String login, String password)
            throws UserNotFoundException, WrongPasswordException {
        Optional<User> user = userRepository.findByLogin(login);

        if (!user.isPresent()) {
            throw new UserNotFoundException(String.format("user with login %s not found", login));
        }

        if (!passwordEncoder.matches(password, user.get().getPassword())) {
            throw new WrongPasswordException(String.format("wrong password for login %s", login));
        }

        return user;
    }
}
