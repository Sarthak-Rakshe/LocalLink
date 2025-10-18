package com.sarthak.ServiceListingService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthak.ServiceListingService.dto.ServiceItemDto;
import com.sarthak.ServiceListingService.exception.GlobalExceptionHandler;
import com.sarthak.ServiceListingService.exception.ServiceNotFoundException;
import com.sarthak.ServiceListingService.service.ServiceItemsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ServiceItemControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ServiceItemsService serviceItemsService;

    @InjectMocks
    private ServiceItemController serviceItemController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(serviceItemController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ServiceItemDto buildDto(Long id) {
        ServiceItemDto dto = new ServiceItemDto();
        dto.setServiceId(id);
        dto.setServiceName("Plumbing");
        dto.setServiceCategory("Home Repair");
        dto.setServiceDescription("Fix leaks");
        dto.setServicePricePerHour(50.0);
        dto.setServiceProviderId(101L);
        dto.setServiceRadius(10.0);
        return dto;
    }

    @Test
    @DisplayName("GET /api/services/{id} returns 200 and body when found")
    void getById_found() throws Exception {
        when(serviceItemsService.getServiceById(1L)).thenReturn(buildDto(1L));

        mockMvc.perform(get("/api/services/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceId", is(1)))
                .andExpect(jsonPath("$.serviceName", is("Plumbing")));
    }

    @Test
    @DisplayName("GET /api/services/{id} returns 404 when not found")
    void getById_notFound() throws Exception {
        when(serviceItemsService.getServiceById(99L)).thenThrow(new ServiceNotFoundException("Service not found"));

        mockMvc.perform(get("/api/services/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is("404")))
                .andExpect(jsonPath("$.message", is("Service not found")));
    }

    @Test
    @DisplayName("POST /api/services returns 201 and created resource")
    void createService_created() throws Exception {
        ServiceItemDto request = buildDto(null);
        ServiceItemDto response = buildDto(10L);
        when(serviceItemsService.createService(any(ServiceItemDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serviceId", is(10)))
                .andExpect(jsonPath("$.serviceProviderId", is(101)));
    }

    @Test
    @DisplayName("DELETE /api/services?id=ID returns 200 on success")
    void deleteService_success() throws Exception {
        when(serviceItemsService.deleteService(5L, userPrincipal.getUserId())).thenReturn("Service deleted successfully");

        mockMvc.perform(delete("/api/services").param("id", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Service deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/services?id=ID returns 404 when not found")
    void deleteService_notFound() throws Exception {
        when(serviceItemsService.deleteService(eq(55L), userPrincipal.getUserId())).thenThrow(new ServiceNotFoundException("Service not found"));

        mockMvc.perform(delete("/api/services").param("id", "55"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is("404")))
                .andExpect(jsonPath("$.message", is("Service not found")));
    }
}
