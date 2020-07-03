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

    /**
     * Method checks if session contains {@link User} object, which is logged in.
     * @param session HttpSession, which holds user logged in.
     * @param model holds {@link User} object from session.
     * @return main page depending on whether user logged in or not.
     */
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

    /**
     * @return view with name "register"
     */
    @GetMapping(path = "/register")
    public String showRegisterPage() {
        return "register";
    }

    /**
     * Creates new {@link User} object and saves it into database
     * @param user to be saved into database
     * @return name of the view, which should be rendered
     */
    @PostMapping(path = "/register")
    public String registerNewUser(User user, Model model) {
        userAuthorizationService.save(user);
        model.addAttribute("login", user.getLogin());
        return "login";
    }

    /**
     * Processes user authentication
     * @param login - user's login
     * @param password - user's password
     * @param model {@link Model} - takes user as attribute and transfers it to the view
     * @param session - {@link HttpSession} object, needed to hold authenticated user
     * @return name of the view to be rendered
     * @throws UserNotFoundException, when user with that login not found
     * @throws WrongPasswordException, when wrong password is given
     */
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

    /**
     * Deletes user from {@link HttpSession} session
     * @return login view name
     */
    @GetMapping(path = "/logout")
    @ResponseStatus(code = HttpStatus.OK)
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "login";
    }
}
