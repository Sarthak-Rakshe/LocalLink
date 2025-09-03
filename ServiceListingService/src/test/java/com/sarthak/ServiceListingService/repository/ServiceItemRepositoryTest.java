package com.sarthak.ServiceListingService.repository;

import com.sarthak.ServiceListingService.model.ServiceItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ServiceItemRepositoryTest {

    @Autowired
    private ServiceItemRepository repository;

    @Test
    @DisplayName("Save and retrieve a ServiceItem")
    void testSaveAndFind() {
        ServiceItem item = new ServiceItem(null, "Plumbing", "Home Repair", "Fix leaks", 50.0, 101L, 10.0);
        ServiceItem saved = repository.save(item);
        assertNotNull(saved.getServiceId());

        Optional<ServiceItem> found = repository.findById(saved.getServiceId());
        assertTrue(found.isPresent());
        assertEquals("Plumbing", found.get().getServiceName());
    }

    @Test
    @DisplayName("Find returns empty for unknown id")
    void testFindUnknown() {
        Optional<ServiceItem> found = repository.findById(9999L);
        assertTrue(found.isEmpty());
    }
}

