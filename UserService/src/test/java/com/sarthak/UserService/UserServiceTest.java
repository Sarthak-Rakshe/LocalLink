package com.sarthak.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateUser() throws Exception {
        String userJson = """
                {
                    "userName": "John Doe",
                    "userEmail": "johnDoe@spring.com",
                    "userContact": "1234567890",
                    "userType": "CUSTOMER",
                    "userPassword": "password123"
                }
                """;

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("John Doe"))
                .andExpect(jsonPath("$.userEmail").value("johnDoe@spring.com"))
                .andExpect(jsonPath("$.userContact").value("1234567890"))
                .andExpect(jsonPath("$.userType").value("CUSTOMER"));
    }
}
