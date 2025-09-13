package com.sarthak.AvailabilityService.service;

import com.sarthak.AvailabilityService.mapper.AvailabilityMapper;
import com.sarthak.AvailabilityService.model.AvailabilityRules;
import com.sarthak.AvailabilityService.model.ExceptionType;
import com.sarthak.AvailabilityService.model.ProviderExceptions;
import com.sarthak.AvailabilityService.repository.AvailabilityRulesRepository;
import com.sarthak.AvailabilityService.repository.ProviderExceptionsRepository;
import com.sarthak.AvailabilityService.dto.request.AvailabilityStatusRequest;
import com.sarthak.AvailabilityService.dto.response.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceBookingIntegrationTest {

    @Mock
    private AvailabilityRulesRepository rulesRepository;
    @Mock
    private ProviderExceptionsRepository exceptionsRepository;

    private AvailabilityService service;

    private final Long PROVIDER_ID = 99L;
    private final LocalDate DATE = LocalDate.of(2025, 9, 6); // Saturday

    @BeforeEach
    void setUp(){
        service = new AvailabilityService(rulesRepository, exceptionsRepository, new AvailabilityMapper());
    }

    private AvailabilityStatusRequest req(LocalTime start, LocalTime end){
        AvailabilityStatusRequest r = new AvailabilityStatusRequest();
        r.setServiceProviderId(PROVIDER_ID);
        r.setDate(DATE);
        r.setStartTime(start);
        r.setEndTime(end);
        return r;
    }

    @Test
    @DisplayName("Boundary inclusive: start==rule.start and end==rule.end => AVAILABLE")
    void boundaryInclusive(){
        AvailabilityRules rule = new AvailabilityRules(null, PROVIDER_ID, LocalTime.of(9,0), LocalTime.of(17,0), DayOfWeek.SATURDAY);
        when(rulesRepository.findByServiceProviderIdAndDayOfWeek(PROVIDER_ID, DayOfWeek.SATURDAY)).thenReturn(List.of(rule));
        when(exceptionsRepository.findByServiceProviderIdAndExceptionDate(PROVIDER_ID, DATE)).thenReturn(List.of());
        var res = service.checkAvailability(req(LocalTime.of(9,0), LocalTime.of(17,0)));
        assertEquals(Status.AVAILABLE.name(), res.getStatus());
    }

    @Test
    @DisplayName("Exception for different date should be ignored")
    void exceptionDifferentDateIgnored(){
        AvailabilityRules rule = new AvailabilityRules(null, PROVIDER_ID, LocalTime.of(9,0), LocalTime.of(17,0), DayOfWeek.SATURDAY);
        when(rulesRepository.findByServiceProviderIdAndDayOfWeek(PROVIDER_ID, DayOfWeek.SATURDAY)).thenReturn(List.of(rule));
        ProviderExceptions ex = new ProviderExceptions();
        ex.setServiceProviderId(PROVIDER_ID);
        ex.setExceptionDate(DATE.plusDays(1));
        ex.setNewStartTime(LocalTime.of(12,0));
        ex.setNewEndTime(LocalTime.of(13,0));
        ex.setExceptionReason("other day");
        ex.setExceptionType(ExceptionType.BLOCKED);
        when(exceptionsRepository.findByServiceProviderIdAndExceptionDate(PROVIDER_ID, DATE)).thenReturn(List.of(ex));
        var res = service.checkAvailability(req(LocalTime.of(10,0), LocalTime.of(11,0)));
        assertEquals(Status.AVAILABLE.name(), res.getStatus());
    }

    @Test
    @DisplayName("Outside rule but inside OVERRIDE exception => AVAILABLE")
    void outsideRuleInsideException(){
        when(rulesRepository.findByServiceProviderIdAndDayOfWeek(PROVIDER_ID, DayOfWeek.SATURDAY)).thenReturn(List.of());
        ProviderExceptions ex = new ProviderExceptions();
        ex.setServiceProviderId(PROVIDER_ID);
        ex.setExceptionDate(DATE);
        ex.setNewStartTime(LocalTime.of(6,0));
        ex.setNewEndTime(LocalTime.of(7,0));
        ex.setExceptionReason("early override");
        ex.setExceptionType(ExceptionType.OVERRIDE);
        when(exceptionsRepository.findByServiceProviderIdAndExceptionDate(PROVIDER_ID, DATE)).thenReturn(List.of(ex));
        var res = service.checkAvailability(req(LocalTime.of(6,0), LocalTime.of(7,0)));
        assertEquals(Status.AVAILABLE.name(), res.getStatus());
    }
}
