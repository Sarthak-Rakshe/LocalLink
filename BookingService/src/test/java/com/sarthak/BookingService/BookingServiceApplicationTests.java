package com.sarthak.BookingService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.sarthak.BookingService.client.AvailabilityServiceClient;

import static org.mockito.Mockito.mock;

@SpringBootTest
@ActiveProfiles("test")
class BookingServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        AvailabilityServiceClient availabilityServiceClient() {
            return mock(AvailabilityServiceClient.class);
        }
    }
}
