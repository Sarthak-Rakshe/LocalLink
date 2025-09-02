package com.sarthak.ServiceListingService.mapper;

import com.sarthak.ServiceListingService.dto.ServiceItemDto;
import com.sarthak.ServiceListingService.model.ServiceItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceItemsMapperTest {

    private final ServiceItemsMapper mapper = new ServiceItemsMapper();

    @Test
    void testEntityToDto() {
        ServiceItem entity = new ServiceItem(1L, "Plumbing", "Home Repair", "Fix leaks", 50.0, 101L, 10.0);
        ServiceItemDto dto = mapper.entityToDto(entity);
        assertEquals(entity.getServiceId(), dto.getServiceId());
        assertEquals(entity.getServiceName(), dto.getServiceName());
        assertEquals(entity.getServiceCategory(), dto.getServiceCategory());
        assertEquals(entity.getServiceDescription(), dto.getServiceDescription());
        assertEquals(entity.getServicePricePerHour(), dto.getServicePricePerHour());
        assertEquals(entity.getServiceProviderId(), dto.getServiceProviderId());
        assertEquals(entity.getServiceRadius(), dto.getServiceRadius());
    }

    @Test
    void testDtoToEntity() {
        ServiceItemDto dto = new ServiceItemDto();
        dto.setServiceId(2L);
        dto.setServiceName("Electrician");
        dto.setServiceCategory("Home Repair");
        dto.setServiceDescription("Fix wiring");
        dto.setServicePricePerHour(70.0);
        dto.setServiceProviderId(202L);
        dto.setServiceRadius(15.0);

        ServiceItem entity = mapper.dtoToEntity(dto);
        assertEquals(dto.getServiceId(), entity.getServiceId());
        assertEquals(dto.getServiceName(), entity.getServiceName());
        assertEquals(dto.getServiceCategory(), entity.getServiceCategory());
        assertEquals(dto.getServiceDescription(), entity.getServiceDescription());
        assertEquals(dto.getServicePricePerHour(), entity.getServicePricePerHour());
        assertEquals(dto.getServiceProviderId(), entity.getServiceProviderId());
        assertEquals(dto.getServiceRadius(), entity.getServiceRadius());
    }
}

