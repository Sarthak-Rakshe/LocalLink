package com.sarthak.AvailabilityService.service;

import com.sarthak.AvailabilityService.client.BookingClient;
import com.sarthak.AvailabilityService.dto.request.AvailabilityStatusRequest;
import com.sarthak.AvailabilityService.dto.response.AvailabilityStatusResponse;
import com.sarthak.AvailabilityService.dto.response.Status;
import com.sarthak.AvailabilityService.mapper.AvailabilityMapper;
import com.sarthak.AvailabilityService.model.AvailabilityRules;
import com.sarthak.AvailabilityService.model.ExceptionType;
import com.sarthak.AvailabilityService.model.ProviderExceptions;
import com.sarthak.AvailabilityService.repository.AvailabilityRulesRepository;
import com.sarthak.AvailabilityService.repository.ProviderExceptionsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class AvailabilityStatusTests {

    @Mock
    private AvailabilityRulesRepository availabilityRulesRepository;

    @Mock
    private ProviderExceptionsRepository providerExceptionsRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    @Test
    void testGetAvailabilityStatus_AVAILABLE() {
        Long serviceProviderId = 1L;
        Long serviceId = 1L;
        String date = "2025-09-21";
        String startTime = "11:00:00";
        String endTime = "11:30:00";

        AvailabilityRules rule = new AvailabilityRules();
        rule.setRuleId(1L);
        rule.setServiceProviderId(1L);
        rule.setServiceId(1L);
        rule.setDaysOfWeek(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY});
        rule.setStartTime(LocalTime.parse("10:00:00"));
        rule.setEndTime(LocalTime.parse("12:00:00"));
        rule.setCreatedAt(Instant.now());

        ProviderExceptions exception = new ProviderExceptions();
        exception.setExceptionId(1L);
        exception.setServiceProviderId(1L);
        exception.setExceptionDate(LocalDate.parse("2025-09-21"));
        exception.setNewStartTime(LocalTime.parse("11:30:00"));
        exception.setNewEndTime(LocalTime.parse("12:00:00"));
        exception.setExceptionType(ExceptionType.BLOCKED);
        exception.setCreatedAt(Instant.now());

        byte dayOfWeek = (byte) (LocalDate.parse("2025-09-21").getDayOfWeek().getValue() % 7);

        Mockito.when(availabilityRulesRepository.findByServiceProviderAndServiceAndDayOrdered(1L, 1L,dayOfWeek)).thenReturn(List.of(rule));
        Mockito.when(providerExceptionsRepository.findAllByServiceProviderIdAndExceptionDateOrderByNewStartTimeAsc(1L
                , LocalDate.parse(date))).thenReturn(List.of(exception));

        AvailabilityStatusResponse response = availabilityService.checkAvailability(
                new AvailabilityStatusRequest(
                        serviceProviderId,
                        serviceId,
                        LocalTime.parse(startTime),
                        LocalTime.parse(endTime),
                        LocalDate.parse(date)
                )
        );

        assertEquals(Status.AVAILABLE, response.getStatus());
        Mockito.verify(availabilityRulesRepository).findByServiceProviderAndServiceAndDayOrdered(1L, 1L, dayOfWeek);
    }

    @Test
    void testGetAvailabilityStatus_OUTSIDE_WORKING_HOURS() {
        Long serviceProviderId = 1L;
        Long serviceId = 1L;
        String date = "2025-09-21";
        String startTime = "11:00:00";
        String endTime = "13:00:00";

        AvailabilityRules rule = new AvailabilityRules();
        rule.setRuleId(1L);
        rule.setServiceProviderId(1L);
        rule.setServiceId(1L);
        rule.setDaysOfWeek(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY});
        rule.setStartTime(LocalTime.parse("10:00:00"));
        rule.setEndTime(LocalTime.parse("12:00:00"));
        rule.setCreatedAt(Instant.now());

        byte dayOfWeek = (byte) (LocalDate.parse("2025-09-21").getDayOfWeek().getValue() % 7);

        Mockito.when(availabilityRulesRepository.findByServiceProviderAndServiceAndDayOrdered(1L, 1L,dayOfWeek)).thenReturn(List.of(rule));
        Mockito.when(providerExceptionsRepository.findAllByServiceProviderIdAndExceptionDateOrderByNewStartTimeAsc(1L, LocalDate.parse(date))).thenReturn(List.of());

        AvailabilityStatusResponse response = availabilityService.checkAvailability(
                new AvailabilityStatusRequest(
                        serviceProviderId,
                        serviceId,
                        LocalTime.parse(startTime),
                        LocalTime.parse(endTime),
                        LocalDate.parse(date)
                )
        );

        assertEquals(Status.OUTSIDE_WORKING_HOURS, response.getStatus());
        Mockito.verify(availabilityRulesRepository).findByServiceProviderAndServiceAndDayOrdered(1L, 1L, dayOfWeek);
    }

    @Test
    void testGetAvailabilityStatus_BLOCKED() {
        Long serviceProviderId = 1L;
        Long serviceId = 1L;
        String date = "2025-09-21";
        String startTime = "11:00:00";
        String endTime = "12:00:00";

        AvailabilityRules rule = new AvailabilityRules();
        rule.setRuleId(1L);
        rule.setServiceProviderId(1L);
        rule.setServiceId(1L);
        rule.setDaysOfWeek(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY});
        rule.setStartTime(LocalTime.parse("10:00:00"));
        rule.setEndTime(LocalTime.parse("12:00:00"));
        rule.setCreatedAt(Instant.now());

        ProviderExceptions exception = new ProviderExceptions();
        exception.setExceptionId(1L);
        exception.setServiceProviderId(1L);
        exception.setExceptionDate(LocalDate.parse("2025-09-21"));
        exception.setNewStartTime(LocalTime.parse("11:30:00"));
        exception.setNewEndTime(LocalTime.parse("12:00:00"));
        exception.setExceptionType(ExceptionType.BLOCKED);
        exception.setCreatedAt(Instant.now());

        byte dayOfWeek = (byte) (LocalDate.parse("2025-09-21").getDayOfWeek().getValue() % 7);

        Mockito.when(availabilityRulesRepository.findByServiceProviderAndServiceAndDayOrdered(1L, 1L,dayOfWeek)).thenReturn(List.of(rule));
        Mockito.when(providerExceptionsRepository.findAllByServiceProviderIdAndExceptionDateOrderByNewStartTimeAsc(1L
                , LocalDate.parse(date))).thenReturn(List.of(exception));

        AvailabilityStatusResponse response = availabilityService.checkAvailability(
                new AvailabilityStatusRequest(
                        serviceProviderId,
                        serviceId,
                        LocalTime.parse(startTime),
                        LocalTime.parse(endTime),
                        LocalDate.parse(date)
                )
        );

        assertEquals(Status.BLOCKED, response.getStatus());
        Mockito.verify(availabilityRulesRepository).findByServiceProviderAndServiceAndDayOrdered(1L, 1L, dayOfWeek);
    }

    @Test
    void testGetAvailabilityStatus_OVERRIDE() {
        Long serviceProviderId = 1L;
        Long serviceId = 1L;
        String date = "2025-09-21";
        String startTime = "10:00:00";
        String endTime = "12:00:00";

        AvailabilityRules rule = new AvailabilityRules();
        rule.setRuleId(1L);
        rule.setServiceProviderId(1L);
        rule.setServiceId(1L);
        rule.setDaysOfWeek(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY});
        rule.setStartTime(LocalTime.parse("10:00:00"));
        rule.setEndTime(LocalTime.parse("12:00:00"));
        rule.setCreatedAt(Instant.now());

        ProviderExceptions exception = new ProviderExceptions();
        exception.setExceptionId(1L);
        exception.setServiceProviderId(1L);
        exception.setExceptionDate(LocalDate.parse("2025-09-21"));
        exception.setNewStartTime(LocalTime.parse("10:40:00"));
        exception.setNewEndTime(LocalTime.parse("12:30:00"));
        exception.setExceptionType(ExceptionType.OVERRIDE);
        exception.setCreatedAt(Instant.now());

        byte dayOfWeek = (byte) (LocalDate.parse("2025-09-21").getDayOfWeek().getValue() % 7);

        Mockito.when(availabilityRulesRepository.findByServiceProviderAndServiceAndDayOrdered(1L, 1L,dayOfWeek)).thenReturn(List.of(rule));
        Mockito.when(providerExceptionsRepository.findAllByServiceProviderIdAndExceptionDateOrderByNewStartTimeAsc(1L
                , LocalDate.parse(date))).thenReturn(List.of(exception));

        AvailabilityStatusResponse response = availabilityService.checkAvailability(
                new AvailabilityStatusRequest(
                        serviceProviderId,
                        serviceId,
                        LocalTime.parse(startTime),
                        LocalTime.parse(endTime),
                        LocalDate.parse(date)
                )
        );

        assertEquals(Status.OUTSIDE_WORKING_HOURS, response.getStatus());
        Mockito.verify(availabilityRulesRepository).findByServiceProviderAndServiceAndDayOrdered(1L, 1L, dayOfWeek);
    }

    @Test
    void testGetAvailabilityStatus_RULES_EMPTY_BUT_EXCEPTIONS_AVAILABLE() {
        Long serviceProviderId = 1L;
        Long serviceId = 1L;
        String date = "2025-09-21";
        String startTime = "11:00:00";
        String endTime = "12:00:00";

//        AvailabilityRules rule = new AvailabilityRules();
//        rule.setRuleId(1L);
//        rule.setServiceProviderId(1L);
//        rule.setServiceId(1L);
//        rule.setDaysOfWeek(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY});
//        rule.setStartTime(LocalTime.parse("10:00:00"));
//        rule.setEndTime(LocalTime.parse("12:00:00"));
//        rule.setCreatedAt(Instant.now());

        ProviderExceptions exception = new ProviderExceptions();
        exception.setExceptionId(1L);
        exception.setServiceProviderId(1L);
        exception.setExceptionDate(LocalDate.parse("2025-09-21"));
        exception.setNewStartTime(LocalTime.parse("10:30:00"));
        exception.setNewEndTime(LocalTime.parse("12:00:00"));
        exception.setExceptionType(ExceptionType.OVERRIDE);
        exception.setCreatedAt(Instant.now());

        byte dayOfWeek = (byte) (LocalDate.parse("2025-09-21").getDayOfWeek().getValue() % 7);

        Mockito.when(availabilityRulesRepository.findByServiceProviderAndServiceAndDayOrdered(1L, 1L,dayOfWeek)).thenReturn(List.of());
        Mockito.when(providerExceptionsRepository.findAllByServiceProviderIdAndExceptionDateOrderByNewStartTimeAsc(1L
                , LocalDate.parse(date))).thenReturn(List.of(exception));

        AvailabilityStatusResponse response = availabilityService.checkAvailability(
                new AvailabilityStatusRequest(
                        serviceProviderId,
                        serviceId,
                        LocalTime.parse(startTime),
                        LocalTime.parse(endTime),
                        LocalDate.parse(date)
                )
        );

        assertEquals(Status.AVAILABLE, response.getStatus());
//        Mockito.verify(availabilityRulesRepository).findByServiceProviderAndServiceAndDayOrdered(1L, 1L, dayOfWeek);
    }


}
