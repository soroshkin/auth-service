package com.icl.auth.controller;

import com.icl.auth.exception.WrongPasswordException;
import com.icl.auth.model.User;
import com.icl.auth.service.UserAuthorizationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpSession;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAuthorizationService userAuthorizationService;

    @Test
    public void ifUserPasswordIsOkReturnOk() throws Exception {
        User user = Mockito.mock(User.class);
        when(userAuthorizationService.authorize(anyString(), anyString()))
                .thenReturn(Optional.ofNullable(user));

        HttpSession session = mockMvc.perform(post("/login")
                .param("login", "q")
                .param("password", "q"))
                .andExpect(status().isOk())
                .andExpect(view().name("securedPage"))
                .andReturn()
                .getRequest()
                .getSession();

        assertThat(session.getAttribute("user")).isNotNull();
    }

    @Test
    public void ifUserPasswordIsWrongThrowException() throws Exception {
        when(userAuthorizationService.authorize(anyString(), anyString())).thenThrow(WrongPasswordException.class);
        mockMvc.perform(post("/login")
                .param("login", "q")
                .param("password", "123"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void showRegisterPageTest() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(view().name("register"));
    }

    @Test
    public void registerNewUserShouldReturnView() throws Exception {
        User user = Mockito.mock(User.class);
        when(userAuthorizationService.save(any())).thenReturn(user);
        mockMvc.perform(post("/register"))
                .andExpect(view().name("login"));
    }

    @Test
    public void testLogout() throws Exception {
        HttpSession session = mockMvc.perform(get("/logout"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andReturn()
                .getRequest()
                .getSession();
        assertThat(session.getAttribute("user")).isNull();
    }

    @MockBean
    private MockHttpSession session;

    @Test
    public void testMainPage() throws Exception {
        mockMvc.perform(get("/")
                .sessionAttr("user", Mockito.mock(User.class)))
                .andExpect(view().name("securedPage"));

        mockMvc.perform(get("/"))
                .andExpect(view().name("login"));
    }
}
