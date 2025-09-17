package com.sarthak.AvailabilityService.service;

import com.sarthak.AvailabilityService.dto.AvailabilityRulesDto;
import com.sarthak.AvailabilityService.dto.ProviderExceptionDto;
import com.sarthak.AvailabilityService.mapper.AvailabilityMapper;
import com.sarthak.AvailabilityService.model.AvailabilityRules;
import com.sarthak.AvailabilityService.model.ExceptionType;
import com.sarthak.AvailabilityService.model.ProviderExceptions;
import com.sarthak.AvailabilityService.repository.AvailabilityRulesRepository;
import com.sarthak.AvailabilityService.repository.ProviderExceptionsRepository;
import com.sarthak.AvailabilityService.dto.request.AvailabilityStatusRequest;
import com.sarthak.AvailabilityService.dto.response.AvailabilityStatusResponse;
import com.sarthak.AvailabilityService.dto.response.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private AvailabilityRulesRepository rulesRepository;
    @Mock
    private ProviderExceptionsRepository exceptionsRepository;

    private AvailabilityMapper mapper;
    private AvailabilityService service;

    private final Long PROVIDER_ID = 7L;
    private final Long SERVICE_ID = 101L;
    private final LocalDate DATE = LocalDate.of(2025, 9, 5); // Friday

    @BeforeEach
    void setUp(){
        mapper = new AvailabilityMapper();
        service = new AvailabilityService(rulesRepository, exceptionsRepository, mapper);
    }

    private AvailabilityStatusRequest req(LocalTime start, LocalTime end){
        AvailabilityStatusRequest r = new AvailabilityStatusRequest();
        r.setServiceProviderId(PROVIDER_ID);
        r.setServiceId(SERVICE_ID);
        r.setDate(DATE);
        r.setStartTime(start);
        r.setEndTime(end);
        return r;
    }

    private AvailabilityRules ruleFor(DayOfWeek... days){
        AvailabilityRules rule = new AvailabilityRules();
        rule.setServiceProviderId(PROVIDER_ID);
        rule.setServiceId(SERVICE_ID);
        rule.setStartTime(LocalTime.of(9,0));
        rule.setEndTime(LocalTime.of(17,0));
        rule.setDaysOfWeek(days);
        return rule;
    }

    @Test
    @DisplayName("checkAvailability -> OVERRIDE exception covering time => AVAILABLE")
    void checkAvailability_overrideAvailable(){
        when(rulesRepository.findAllByServiceProviderIdAndServiceId(PROVIDER_ID, SERVICE_ID))
                .thenReturn(List.of());
        ProviderExceptions ex = new ProviderExceptions();
        ex.setServiceProviderId(PROVIDER_ID);
        ex.setServiceId(SERVICE_ID);
        ex.setExceptionDate(DATE);
        ex.setNewStartTime(LocalTime.of(10,0));
        ex.setNewEndTime(LocalTime.of(12,0));
        ex.setExceptionReason("override window");
        ex.setExceptionType(ExceptionType.OVERRIDE);
        when(exceptionsRepository.findAllByServiceProviderIdAndExceptionDate(PROVIDER_ID, DATE))
                .thenReturn(List.of(ex));

        AvailabilityStatusResponse res = service.checkAvailability(req(LocalTime.of(10,30), LocalTime.of(11,30)));
        assertEquals(Status.AVAILABLE, res.getStatus());
        assertEquals(PROVIDER_ID, res.getServiceProviderId());
        assertEquals(DATE.toString(), res.getDate());
    }

    @Test
    @DisplayName("checkAvailability -> BLOCKED exception covering time => BLOCKED")
    void checkAvailability_blocked(){
        when(rulesRepository.findAllByServiceProviderIdAndServiceId(PROVIDER_ID, SERVICE_ID))
                .thenReturn(List.of());
        ProviderExceptions ex = new ProviderExceptions();
        ex.setServiceProviderId(PROVIDER_ID);
        ex.setServiceId(SERVICE_ID);
        ex.setExceptionDate(DATE);
        ex.setNewStartTime(LocalTime.of(9,0));
        ex.setNewEndTime(LocalTime.of(17,0));
        ex.setExceptionReason("blocked day");
        ex.setExceptionType(ExceptionType.BLOCKED);
        when(exceptionsRepository.findAllByServiceProviderIdAndExceptionDate(PROVIDER_ID, DATE))
                .thenReturn(List.of(ex));

        AvailabilityStatusResponse res = service.checkAvailability(req(LocalTime.of(13,0), LocalTime.of(14,0)));
        assertEquals(Status.BLOCKED, res.getStatus());
    }

    @Test
    @DisplayName("checkAvailability -> inside working rule and no blocking exception => AVAILABLE (end exclusive)")
    void checkAvailability_ruleAvailable(){
        AvailabilityRules rule = ruleFor(DayOfWeek.FRIDAY);
        when(rulesRepository.findAllByServiceProviderIdAndServiceId(PROVIDER_ID, SERVICE_ID))
                .thenReturn(List.of(rule));
        when(exceptionsRepository.findAllByServiceProviderIdAndExceptionDate(PROVIDER_ID, DATE))
                .thenReturn(List.of());

        AvailabilityStatusResponse res = service.checkAvailability(req(LocalTime.of(9,0), LocalTime.of(16,59,59)));
        assertEquals(Status.AVAILABLE, res.getStatus());
    }

    @Test
    @DisplayName("checkAvailability -> outside rules and not in exception => OUTSIDE_WORKING_HOURS")
    void checkAvailability_outside(){
        when(rulesRepository.findAllByServiceProviderIdAndServiceId(PROVIDER_ID, SERVICE_ID))
                .thenReturn(List.of());
        when(exceptionsRepository.findAllByServiceProviderIdAndExceptionDate(PROVIDER_ID, DATE))
                .thenReturn(List.of());

        AvailabilityStatusResponse res = service.checkAvailability(req(LocalTime.of(8,0), LocalTime.of(9,0)));
        assertEquals(Status.OUTSIDE_WORKING_HOURS, res.getStatus());
    }

    @Test
    @DisplayName("createAvailabilityRule persists and maps back to DTO with id")
    void createAvailabilityRule_success(){
        AvailabilityRulesDto dto = new AvailabilityRulesDto();
        dto.setServiceProviderId(PROVIDER_ID);
        dto.setServiceId(SERVICE_ID);
        dto.setDaysOfWeek(new DayOfWeek[]{DayOfWeek.FRIDAY});
        dto.setStartTime("09:00");
        dto.setEndTime("17:00");

        ArgumentCaptor<AvailabilityRules> captor = ArgumentCaptor.forClass(AvailabilityRules.class);
        when(rulesRepository.save(captor.capture())).thenAnswer(inv -> {
            AvailabilityRules r = inv.getArgument(0);
            r.setRuleId(123L);
            return r;
        });

        AvailabilityRulesDto saved = service.createAvailabilityRule(dto);
        assertNotNull(saved.getRuleId());
        assertEquals(123L, saved.getRuleId());
        AvailabilityRules persisted = captor.getValue();
        assertEquals(PROVIDER_ID, persisted.getServiceProviderId());
        assertEquals(SERVICE_ID, persisted.getServiceId());
        assertEquals(LocalTime.of(9,0), persisted.getStartTime());
        assertEquals(LocalTime.of(17,0), persisted.getEndTime());
        assertTrue(persisted.isAvailableOn(DayOfWeek.FRIDAY));
    }

    @Test
    @DisplayName("createProviderException persists and maps back to DTO with id")
    void createProviderException_success(){
        ProviderExceptionDto dto = new ProviderExceptionDto();
        dto.setServiceProviderId(PROVIDER_ID);
        dto.setServiceId(SERVICE_ID);
        dto.setExceptionDate(DATE.toString());
        dto.setNewStartTime("10:00");
        dto.setNewEndTime("12:00");
        dto.setExceptionReason("personal");
        dto.setExceptionType(ExceptionType.OVERRIDE);

        ArgumentCaptor<ProviderExceptions> captor = ArgumentCaptor.forClass(ProviderExceptions.class);
        when(exceptionsRepository.save(captor.capture())).thenAnswer(inv -> {
            ProviderExceptions e = inv.getArgument(0);
            e.setExceptionId(456L);
            return e;
        });

        ProviderExceptionDto saved = service.createProviderException(dto);
        assertNotNull(saved.getExceptionId());
        assertEquals(456L, saved.getExceptionId());
        ProviderExceptions persisted = captor.getValue();
        assertEquals(PROVIDER_ID, persisted.getServiceProviderId());
        assertEquals(SERVICE_ID, persisted.getServiceId());
        assertEquals(DATE, persisted.getExceptionDate());
        assertEquals(LocalTime.of(10,0), persisted.getNewStartTime());
        assertEquals(LocalTime.of(12,0), persisted.getNewEndTime());
        assertEquals(ExceptionType.OVERRIDE, persisted.getExceptionType());
    }
}
