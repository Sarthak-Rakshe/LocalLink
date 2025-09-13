package com.sarthak.CommunicationService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MessageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    record CreateConversationRequest(String requesterId, String senderId){}
    record ChatSendRequest(Long conversationId, String content){}

    @Test
    void sendAndFetchMessages_success() throws Exception {
        // Create a conversation between u1 and u2
        CreateConversationRequest cReq = new CreateConversationRequest("u1", "u2");
        MvcResult convRes = mockMvc.perform(post("/api/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn();

        long conversationId = objectMapper.readTree(convRes.getResponse().getContentAsString()).get("id").asLong();

        // Send a message
        ChatSendRequest mReq = new ChatSendRequest(conversationId, "Hello there");
        mockMvc.perform(post("/api/messages")
                        .header("X-User-Id", "u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.conversationId", is((int) conversationId)))
                .andExpect(jsonPath("$.senderId", is("u1")))
                .andExpect(jsonPath("$.content", is("Hello there")));

        // Fetch messages (paged)
        mockMvc.perform(get("/api/messages/{conversationId}", conversationId)
                        .header("X-User-Id", "u1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].content", is("Hello there")));
    }

    @Test
    void sendMessage_unauthorizedUser_shouldFail() throws Exception {
        // Create a conversation between u1 and u2
        CreateConversationRequest cReq = new CreateConversationRequest("u1", "u2");
        MvcResult convRes = mockMvc.perform(post("/api/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cReq)))
                .andExpect(status().isCreated())
                .andReturn();
        long conversationId = objectMapper.readTree(convRes.getResponse().getContentAsString()).get("id").asLong();

        // Try sending from u3 (not part of conversation)
        ChatSendRequest mReq = new ChatSendRequest(conversationId, "Hi");
        mockMvc.perform(post("/api/messages")
                        .header("X-User-Id", "u3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMessages_conflictingParams_shouldFail() throws Exception {
        // Create a conversation and send one message
        CreateConversationRequest cReq = new CreateConversationRequest("u1", "u2");
        MvcResult convRes = mockMvc.perform(post("/api/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cReq)))
                .andExpect(status().isCreated())
                .andReturn();
        long conversationId = objectMapper.readTree(convRes.getResponse().getContentAsString()).get("id").asLong();

        ChatSendRequest mReq = new ChatSendRequest(conversationId, "Message");
        mockMvc.perform(post("/api/messages")
                        .header("X-User-Id", "u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mReq)))
                .andExpect(status().isCreated());

        // Call with both before and after
        mockMvc.perform(get("/api/messages/{conversationId}", conversationId)
                        .header("X-User-Id", "u1")
                        .param("before", "2025-01-01T00:00:00Z")
                        .param("after", "2025-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendMessage_validation_shouldFail() throws Exception {
        // Create a conversation
        CreateConversationRequest cReq = new CreateConversationRequest("u1", "u2");
        MvcResult convRes = mockMvc.perform(post("/api/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cReq)))
                .andExpect(status().isCreated())
                .andReturn();
        long conversationId = objectMapper.readTree(convRes.getResponse().getContentAsString()).get("id").asLong();

        // Empty content should fail via validation or service check
        ChatSendRequest mReq = new ChatSendRequest(conversationId, "   ");
        mockMvc.perform(post("/api/messages")
                        .header("X-User-Id", "u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mReq)))
                .andExpect(status().isBadRequest());
    }
}
