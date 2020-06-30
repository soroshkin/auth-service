package com.icl.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void ifUserPasswordIsOkReturnOk() throws Exception {
        mockMvc.perform(post("/login")
                .param("login", "q")
                .param("password", "q"))
                .andExpect(status().isOk())
                .andExpect(view().name("securedPage"));
    }

    @Test
    public void ifUserPasswordIsWrongThrowException() throws Exception {
        mockMvc.perform(post("/login")
                .param("login", "q")
                .param("password", "123"))
                .andExpect(status().isForbidden());
    }
}
