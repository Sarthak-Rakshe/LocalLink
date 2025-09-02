package com.sarthak.ServiceListingService.service;

import com.sarthak.ServiceListingService.dto.ServiceItemDto;
import com.sarthak.ServiceListingService.exception.ServiceNotFoundException;
import com.sarthak.ServiceListingService.mapper.ServiceItemsMapper;
import com.sarthak.ServiceListingService.model.ServiceItem;
import com.sarthak.ServiceListingService.repository.ServiceItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceItemsServiceTest {

    @Mock
    private ServiceItemRepository repository;

    private ServiceItemsMapper mapper; // real mapper

    @InjectMocks
    private ServiceItemsService service;

    @BeforeEach
    void setUp() {
        mapper = new ServiceItemsMapper();
        service = new ServiceItemsService(repository, mapper);
    }

    private ServiceItem buildEntity(Long id) {
        return new ServiceItem(id, "Plumbing", "Home Repair", "Fix leaks", 50.0, 101L, 10.0);
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
    void getServiceById_found() {
        ServiceItem entity = buildEntity(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        ServiceItemDto dto = service.getServiceById(1L);

        assertEquals(1L, dto.getServiceId());
        verify(repository).findById(1L);
    }

    @Test
    void getServiceById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ServiceNotFoundException.class, () -> service.getServiceById(99L));
    }

    @Test
    void createService_savesAndReturnsDto() {
        ServiceItemDto input = buildDto(null);
        ServiceItem toSave = buildEntity(null);
        ServiceItem saved = buildEntity(10L);

        // Capture argument passed to save
        when(repository.save(any(ServiceItem.class))).thenReturn(saved);

        ServiceItemDto result = service.createService(input);

        assertNotNull(result.getServiceId());
        assertEquals(10L, result.getServiceId());
        ArgumentCaptor<ServiceItem> captor = ArgumentCaptor.forClass(ServiceItem.class);
        verify(repository).save(captor.capture());
        assertNull(captor.getValue().getServiceId());
    }

    @Test
    void deleteService_success() {
        ServiceItem existing = buildEntity(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));

        String msg = service.deleteService(5L);
        assertEquals("Service deleted successfully", msg);
        verify(repository).delete(existing);
    }

    @Test
    void deleteService_notFound() {
        when(repository.findById(123L)).thenReturn(Optional.empty());
        assertThrows(ServiceNotFoundException.class, () -> service.deleteService(123L));
        verify(repository, never()).delete(any());
    }
}

