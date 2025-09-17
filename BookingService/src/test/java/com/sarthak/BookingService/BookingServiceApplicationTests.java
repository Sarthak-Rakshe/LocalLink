package com.sarthak.BookingService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.sarthak.BookingService.client.AvailabilityServiceClient;

@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.cloud.openfeign.enabled=false"
})
@EnableAutoConfiguration(exclude = {FeignAutoConfiguration.class})
@ActiveProfiles("test")
class BookingServiceApplicationTests {

    @MockBean
    private AvailabilityServiceClient availabilityServiceClient;

    @Test
    void contextLoads() {
    }
}
