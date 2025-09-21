package com.sarthak.AvailabilityService.service;

import com.sarthak.AvailabilityService.client.BookingClient;
import com.sarthak.AvailabilityService.dto.BookedSlotsResponse;
import com.sarthak.AvailabilityService.dto.Slot;
import com.sarthak.AvailabilityService.dto.request.AvailabilitySlotsRequest;
import com.sarthak.AvailabilityService.dto.response.AvailabilitySlotsResponse;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AvailabilitySlotsTests {

    @Mock
    private AvailabilityRulesRepository availabilityRulesRepository;
    @Mock
    private ProviderExceptionsRepository providerExceptionsRepository;

    @Mock
    private BookingClient bookingClient;

    @InjectMocks
    private AvailabilityService availabilityService;

    @Test
    void testGetAvailabilitySlots_AVAILABLE() {
        Long serviceProviderId = 1L;
        Long serviceId = 1L;
        LocalDate date = LocalDate.parse("2025-09-21");

        AvailabilityRules rule1 = new AvailabilityRules();
        rule1.setRuleId(1L);
        rule1.setServiceProviderId(1L);
        rule1.setServiceId(1L);
        rule1.setDaysOfWeek(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY});
        rule1.setStartTime(LocalTime.parse("10:00:00"));
        rule1.setEndTime(LocalTime.parse("12:00:00"));
        rule1.setCreatedAt(Instant.now());

        AvailabilityRules rule2 = new AvailabilityRules();
        rule2.setRuleId(2L);
        rule2.setServiceProviderId(1L);
        rule2.setServiceId(1L);
        rule2.setDaysOfWeek(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY});
        rule2.setStartTime(LocalTime.parse("16:00:00"));
        rule2.setEndTime(LocalTime.parse("20:00:00"));
        rule2.setCreatedAt(Instant.now());

        ProviderExceptions exception1 = new ProviderExceptions();
        exception1.setExceptionId(1L);
        exception1.setServiceProviderId(1L);
        exception1.setExceptionDate(LocalDate.parse("2025-09-21"));
        exception1.setNewStartTime(LocalTime.parse("11:30:00"));
        exception1.setNewEndTime(LocalTime.parse("12:00:00"));
        exception1.setExceptionType(ExceptionType.BLOCKED);
        exception1.setCreatedAt(Instant.now());

        ProviderExceptions exception2 = new ProviderExceptions();
        exception2.setExceptionId(2L);
        exception2.setServiceProviderId(1L);
        exception2.setExceptionDate(LocalDate.parse("2025-09-21"));
        exception2.setNewStartTime(LocalTime.parse("16:00:00"));
        exception2.setNewEndTime(LocalTime.parse("20:30:00"));
        exception2.setExceptionType(ExceptionType.OVERRIDE);
        exception2.setCreatedAt(Instant.now());

        BookedSlotsResponse response = new BookedSlotsResponse(
                1L,
                1L,
                List.of(
                        new Slot(LocalTime.parse("10:00:00"), LocalTime.parse("10:30:00")),
                        new Slot(LocalTime.parse("11:00:00"), LocalTime.parse("11:30:00")),
                        new Slot(LocalTime.parse("16:30:00"), LocalTime.parse("18:00:00"))
                ),
                "2025-09-21"
        );

        byte dayOfWeek = (byte) (LocalDate.parse("2025-09-21").getDayOfWeek().getValue() % 7);

        Mockito.when(availabilityRulesRepository.findByServiceProviderAndServiceAndDayOrdered(1L, 1L,dayOfWeek)).thenReturn(List.of(rule1, rule2));
        Mockito.when(providerExceptionsRepository.findAllByServiceProviderIdAndExceptionDateOrderByNewStartTimeAsc(1L
                , date)).thenReturn(List.of(exception1, exception2));

        Mockito.when(bookingClient.getBookedSlotsForProviderOnDate(1L, 1L, date))
                .thenReturn(response);

        AvailabilitySlotsResponse availabilitySlotsResponse = availabilityService.getAvailabilitySlots(
                serviceProviderId, serviceId, date
        );

        List<Slot> expectedSlots = List.of(
                new Slot(LocalTime.parse("10:30:00"), LocalTime.parse("11:00:00")),
                new Slot(LocalTime.parse("16:00:00"), LocalTime.parse("16:30:00")),
                new Slot(LocalTime.parse("18:00:00"), LocalTime.parse("20:30:00"))
        );

        // Compare expected slots with the available slots from the service response
        assertEquals(expectedSlots, availabilitySlotsResponse.availableSlots());
        assertTrue(availabilitySlotsResponse.isDayAvailable());
    }

    @Test
    void testGetAvailabilitySlots_UNAVAILABLE() {
        Long serviceProviderId = 1L;
        Long serviceId = 1L;
        LocalDate date = LocalDate.parse("2025-09-21");

        AvailabilityRules rule1 = new AvailabilityRules();
        rule1.setRuleId(1L);
        rule1.setServiceProviderId(1L);
        rule1.setServiceId(1L);
        rule1.setDaysOfWeek(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY});
        rule1.setStartTime(LocalTime.parse("10:00:00"));
        rule1.setEndTime(LocalTime.parse("12:00:00"));
        rule1.setCreatedAt(Instant.now());

        AvailabilityRules rule2 = new AvailabilityRules();
        rule2.setRuleId(2L);
        rule2.setServiceProviderId(1L);
        rule2.setServiceId(1L);
        rule2.setDaysOfWeek(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY});
        rule2.setStartTime(LocalTime.parse("16:00:00"));
        rule2.setEndTime(LocalTime.parse("20:00:00"));
        rule2.setCreatedAt(Instant.now());

        ProviderExceptions exception1 = new ProviderExceptions();
        exception1.setExceptionId(1L);
        exception1.setServiceProviderId(1L);
        exception1.setExceptionDate(LocalDate.parse("2025-09-21"));
        exception1.setNewStartTime(LocalTime.parse("11:30:00"));
        exception1.setNewEndTime(LocalTime.parse("12:00:00"));
        exception1.setExceptionType(ExceptionType.BLOCKED);
        exception1.setCreatedAt(Instant.now());

        ProviderExceptions exception2 = new ProviderExceptions();
        exception2.setExceptionId(2L);
        exception2.setServiceProviderId(1L);
        exception2.setExceptionDate(LocalDate.parse("2025-09-21"));
        exception2.setNewStartTime(LocalTime.parse("16:00:00"));
        exception2.setNewEndTime(LocalTime.parse("20:30:00"));
        exception2.setExceptionType(ExceptionType.OVERRIDE);
        exception2.setCreatedAt(Instant.now());

        BookedSlotsResponse response = new BookedSlotsResponse(
                1L,
                1L,
                List.of(
                        new Slot(LocalTime.parse("10:00:00"), LocalTime.parse("11:30:00")),
                        new Slot(LocalTime.parse("16:00:00"), LocalTime.parse("20:30:00"))
                ),
                "2025-09-21"
        );

        byte dayOfWeek = (byte) (LocalDate.parse("2025-09-21").getDayOfWeek().getValue() % 7);

        Mockito.when(availabilityRulesRepository.findByServiceProviderAndServiceAndDayOrdered(1L, 1L,dayOfWeek)).thenReturn(List.of(rule1, rule2));
        Mockito.when(providerExceptionsRepository.findAllByServiceProviderIdAndExceptionDateOrderByNewStartTimeAsc(1L
                , date)).thenReturn(List.of(exception1, exception2));

        Mockito.when(bookingClient.getBookedSlotsForProviderOnDate(1L, 1L, date))
                .thenReturn(response);

        AvailabilitySlotsResponse availabilitySlotsResponse = availabilityService.getAvailabilitySlots(
                serviceProviderId, serviceId, date
        );

        List<Slot> expectedSlots = List.of(
//                new Slot(LocalTime.parse("10:30:00"), LocalTime.parse("11:00:00")),
//                new Slot(LocalTime.parse("16:00:00"), LocalTime.parse("16:30:00")),
//                new Slot(LocalTime.parse("18:00:00"), LocalTime.parse("20:30:00"))
        );

        // Compare expected slots with the available slots from the service response
        assertEquals(expectedSlots, availabilitySlotsResponse.availableSlots());
        assertFalse(availabilitySlotsResponse.isDayAvailable());
    }

    @Test
    void testGetAvailabilitySlots_SLOTS_LESS_THAN_10_MIN() {
        Long serviceProviderId = 1L;
        Long serviceId = 1L;
        LocalDate date = LocalDate.parse("2025-09-21");

        AvailabilityRules rule1 = new AvailabilityRules();
        rule1.setRuleId(1L);
        rule1.setServiceProviderId(1L);
        rule1.setServiceId(1L);
        rule1.setDaysOfWeek(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY});
        rule1.setStartTime(LocalTime.parse("10:00:00"));
        rule1.setEndTime(LocalTime.parse("12:00:00"));
        rule1.setCreatedAt(Instant.now());

        AvailabilityRules rule2 = new AvailabilityRules();
        rule2.setRuleId(2L);
        rule2.setServiceProviderId(1L);
        rule2.setServiceId(1L);
        rule2.setDaysOfWeek(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY});
        rule2.setStartTime(LocalTime.parse("16:00:00"));
        rule2.setEndTime(LocalTime.parse("20:00:00"));
        rule2.setCreatedAt(Instant.now());

        ProviderExceptions exception1 = new ProviderExceptions();
        exception1.setExceptionId(1L);
        exception1.setServiceProviderId(1L);
        exception1.setExceptionDate(LocalDate.parse("2025-09-21"));
        exception1.setNewStartTime(LocalTime.parse("11:30:00"));
        exception1.setNewEndTime(LocalTime.parse("12:00:00"));
        exception1.setExceptionType(ExceptionType.BLOCKED);
        exception1.setCreatedAt(Instant.now());

        ProviderExceptions exception2 = new ProviderExceptions();
        exception2.setExceptionId(2L);
        exception2.setServiceProviderId(1L);
        exception2.setExceptionDate(LocalDate.parse("2025-09-21"));
        exception2.setNewStartTime(LocalTime.parse("16:00:00"));
        exception2.setNewEndTime(LocalTime.parse("20:30:00"));
        exception2.setExceptionType(ExceptionType.OVERRIDE);
        exception2.setCreatedAt(Instant.now());

        BookedSlotsResponse response = new BookedSlotsResponse(
                1L,
                1L,
                List.of(
                        new Slot(LocalTime.parse("10:00:00"), LocalTime.parse("10:55:00")),
                        new Slot(LocalTime.parse("11:00:00"), LocalTime.parse("11:30:00")),
                        new Slot(LocalTime.parse("16:30:00"), LocalTime.parse("18:00:00")),
                        new Slot(LocalTime.parse("16:00:00"), LocalTime.parse("16:20:00"))
                ),
                "2025-09-21"
        );

        byte dayOfWeek = (byte) (LocalDate.parse("2025-09-21").getDayOfWeek().getValue() % 7);

        Mockito.when(availabilityRulesRepository.findByServiceProviderAndServiceAndDayOrdered(1L, 1L,dayOfWeek)).thenReturn(List.of(rule1, rule2));
        Mockito.when(providerExceptionsRepository.findAllByServiceProviderIdAndExceptionDateOrderByNewStartTimeAsc(1L
                , date)).thenReturn(List.of(exception1, exception2));

        Mockito.when(bookingClient.getBookedSlotsForProviderOnDate(1L, 1L, date))
                .thenReturn(response);

        AvailabilitySlotsResponse availabilitySlotsResponse = availabilityService.getAvailabilitySlots(
                serviceProviderId, serviceId, date
        );

        List<Slot> expectedSlots = List.of(
                new Slot(LocalTime.parse("18:00:00"), LocalTime.parse("20:30:00"))
        );

        // Compare expected slots with the available slots from the service response
        assertEquals(expectedSlots, availabilitySlotsResponse.availableSlots());
        assertTrue(availabilitySlotsResponse.isDayAvailable());
    }

    @Test
    void testGetAvailabilitySlots_BOOKING_EMPTY() {
        Long serviceProviderId = 1L;
        Long serviceId = 1L;
        LocalDate date = LocalDate.parse("2025-09-21");

        AvailabilityRules rule1 = new AvailabilityRules();
        rule1.setRuleId(1L);
        rule1.setServiceProviderId(1L);
        rule1.setServiceId(1L);
        rule1.setDaysOfWeek(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY});
        rule1.setStartTime(LocalTime.parse("10:00:00"));
        rule1.setEndTime(LocalTime.parse("12:00:00"));
        rule1.setCreatedAt(Instant.now());

        AvailabilityRules rule2 = new AvailabilityRules();
        rule2.setRuleId(2L);
        rule2.setServiceProviderId(1L);
        rule2.setServiceId(1L);
        rule2.setDaysOfWeek(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY});
        rule2.setStartTime(LocalTime.parse("16:00:00"));
        rule2.setEndTime(LocalTime.parse("20:00:00"));
        rule2.setCreatedAt(Instant.now());

        ProviderExceptions exception1 = new ProviderExceptions();
        exception1.setExceptionId(1L);
        exception1.setServiceProviderId(1L);
        exception1.setExceptionDate(LocalDate.parse("2025-09-21"));
        exception1.setNewStartTime(LocalTime.parse("11:30:00"));
        exception1.setNewEndTime(LocalTime.parse("12:00:00"));
        exception1.setExceptionType(ExceptionType.BLOCKED);
        exception1.setCreatedAt(Instant.now());

        ProviderExceptions exception2 = new ProviderExceptions();
        exception2.setExceptionId(2L);
        exception2.setServiceProviderId(1L);
        exception2.setExceptionDate(LocalDate.parse("2025-09-21"));
        exception2.setNewStartTime(LocalTime.parse("16:00:00"));
        exception2.setNewEndTime(LocalTime.parse("20:30:00"));
        exception2.setExceptionType(ExceptionType.OVERRIDE);
        exception2.setCreatedAt(Instant.now());

        BookedSlotsResponse response = new BookedSlotsResponse(
                1L,
                1L,
                List.of(

                ),
                "2025-09-21"
        );

        byte dayOfWeek = (byte) (LocalDate.parse("2025-09-21").getDayOfWeek().getValue() % 7);

        Mockito.when(availabilityRulesRepository.findByServiceProviderAndServiceAndDayOrdered(1L, 1L,dayOfWeek)).thenReturn(List.of(rule1, rule2));
        Mockito.when(providerExceptionsRepository.findAllByServiceProviderIdAndExceptionDateOrderByNewStartTimeAsc(1L
                , date)).thenReturn(List.of(exception1, exception2));

        Mockito.when(bookingClient.getBookedSlotsForProviderOnDate(1L, 1L, date))
                .thenReturn(response);

        AvailabilitySlotsResponse availabilitySlotsResponse = availabilityService.getAvailabilitySlots(
                serviceProviderId, serviceId, date
        );

        List<Slot> expectedSlots = List.of(
                new Slot(LocalTime.parse("10:00:00"), LocalTime.parse("11:30:00")),
                new Slot(LocalTime.parse("16:00:00"), LocalTime.parse("20:30:00"))
        );

        // Compare expected slots with the available slots from the service response
        assertEquals(expectedSlots, availabilitySlotsResponse.availableSlots());
        assertTrue(availabilitySlotsResponse.isDayAvailable());
    }



}
