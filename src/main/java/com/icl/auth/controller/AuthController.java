package com.icl.auth.controller;

import com.icl.auth.exception.UserNotFoundException;
import com.icl.auth.exception.WrongPasswordException;
import com.icl.auth.model.User;
import com.icl.auth.service.UserAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class AuthController {
    private UserAuthorizationService userAuthorizationService;

    @Autowired
    public AuthController(UserAuthorizationService userAuthorizationService) {
        this.userAuthorizationService = userAuthorizationService;
    }

    @GetMapping(path = "/")
    public String mainPage(HttpSession session, Model model) {
        User userFromSession = (User) session.getAttribute("user");
        if (userFromSession != null) {
            model.addAttribute("user", userFromSession);
            return "securedPage";
        } else {
            return "login";
        }
    }

    @GetMapping(path = "/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping(path = "/register")
    public String registerNewUser(User user, Model model) {
        userAuthorizationService.save(user);
        model.addAttribute("login", user.getLogin());
        return "login";
    }

    @PostMapping(path = "/login")
    public String login(String login, String password, Model model, HttpSession session)
            throws UserNotFoundException, WrongPasswordException {
        Optional<User> user = userAuthorizationService.authorize(login, password);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            session.setAttribute("user", user.get());
            return "securedPage";
        }

        return "login";
    }

    @GetMapping(path = "/logout")
    @ResponseStatus(code = HttpStatus.OK)
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "login";
    }
}
