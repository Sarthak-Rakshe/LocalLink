package com.sarthak.AvailabilityService.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceBookingIntegrationTest {

    @Mock
    private ProviderWorkingSlotRepository workingSlotRepository;
    @Mock
    private ProviderRecurringRuleRepository recurringRuleRepository;
    @Mock
    private ProviderAvailabilityExceptionRepository exceptionRepository;

    private AvailabilityService availabilityService;

    private final Long PROVIDER_ID = 99L;
    private final LocalDate DATE = LocalDate.of(2025, 9, 6);

    @BeforeEach
    void setUp(){
        availabilityService = new AvailabilityService(workingSlotRepository, recurringRuleRepository, exceptionRepository, new ProviderScheduleMapper());
    }

    @Nested
    @DisplayName("applyBooking")
    class ApplyBooking {

        @Test
        @DisplayName("Splits active slot into left-active, booked, right-active when booking in middle")
        void splitMiddle() {
            ProviderWorkingSlot active = ProviderWorkingSlot.builder()
                    .id(1L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(9,0)).endTime(LocalTime.of(12,0))
                    .status(WorkingSlotStatus.ACTIVE).source(WorkingSlotSource.RECURRING).build();
            when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, DATE)).thenReturn(List.of(active));

            ArgumentCaptor<List<ProviderWorkingSlot>> listCaptor = ArgumentCaptor.forClass(List.class);
            when(workingSlotRepository.saveAll(listCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            availabilityService.applyBooking(PROVIDER_ID, DATE, LocalTime.of(10,0), LocalTime.of(11,0));

            verify(workingSlotRepository).delete(active);
            verify(workingSlotRepository).saveAll(any());
            List<ProviderWorkingSlot> persisted = listCaptor.getValue();
            assertEquals(3, persisted.size());
            ProviderWorkingSlot left = persisted.get(0);
            ProviderWorkingSlot booked = persisted.get(1);
            ProviderWorkingSlot right = persisted.get(2);
            // Order may depend on creation order in service (left, booked, right)
            assertEquals(LocalTime.of(9,0), left.getStartTime());
            assertEquals(LocalTime.of(10,0), left.getEndTime());
            assertEquals(WorkingSlotStatus.ACTIVE, left.getStatus());

            assertEquals(LocalTime.of(10,0), booked.getStartTime());
            assertEquals(LocalTime.of(11,0), booked.getEndTime());
            assertEquals(WorkingSlotStatus.BOOKED, booked.getStatus());

            assertEquals(LocalTime.of(11,0), right.getStartTime());
            assertEquals(LocalTime.of(12,0), right.getEndTime());
            assertEquals(WorkingSlotStatus.ACTIVE, right.getStatus());
        }

        @Test
        @DisplayName("Produces booked + right-active when booking starts at slot start")
        void bookingAtStart() {
            ProviderWorkingSlot active = ProviderWorkingSlot.builder()
                    .id(2L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(9,0)).endTime(LocalTime.of(12,0))
                    .status(WorkingSlotStatus.ACTIVE).source(WorkingSlotSource.RECURRING).build();
            when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, DATE)).thenReturn(List.of(active));
            ArgumentCaptor<List<ProviderWorkingSlot>> listCaptor = ArgumentCaptor.forClass(List.class);
            when(workingSlotRepository.saveAll(listCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            availabilityService.applyBooking(PROVIDER_ID, DATE, LocalTime.of(9,0), LocalTime.of(10,0));

            List<ProviderWorkingSlot> persisted = listCaptor.getValue();
            assertEquals(2, persisted.size());
            ProviderWorkingSlot booked = persisted.get(0);
            ProviderWorkingSlot right = persisted.get(1);
            assertEquals(WorkingSlotStatus.BOOKED, booked.getStatus());
            assertEquals(LocalTime.of(9,0), booked.getStartTime());
            assertEquals(LocalTime.of(10,0), booked.getEndTime());
            assertEquals(WorkingSlotStatus.ACTIVE, right.getStatus());
            assertEquals(LocalTime.of(10,0), right.getStartTime());
            assertEquals(LocalTime.of(12,0), right.getEndTime());
        }

        @Test
        @DisplayName("Produces left-active + booked when booking ends at slot end")
        void bookingAtEnd() {
            ProviderWorkingSlot active = ProviderWorkingSlot.builder()
                    .id(3L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(9,0)).endTime(LocalTime.of(12,0))
                    .status(WorkingSlotStatus.ACTIVE).source(WorkingSlotSource.RECURRING).build();
            when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, DATE)).thenReturn(List.of(active));
            ArgumentCaptor<List<ProviderWorkingSlot>> listCaptor = ArgumentCaptor.forClass(List.class);
            when(workingSlotRepository.saveAll(listCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            availabilityService.applyBooking(PROVIDER_ID, DATE, LocalTime.of(11,0), LocalTime.of(12,0));
            List<ProviderWorkingSlot> persisted = listCaptor.getValue();
            assertEquals(2, persisted.size());
            ProviderWorkingSlot left = persisted.get(0);
            ProviderWorkingSlot booked = persisted.get(1);
            assertEquals(WorkingSlotStatus.ACTIVE, left.getStatus());
            assertEquals(LocalTime.of(9,0), left.getStartTime());
            assertEquals(LocalTime.of(11,0), left.getEndTime());
            assertEquals(WorkingSlotStatus.BOOKED, booked.getStatus());
            assertEquals(LocalTime.of(11,0), booked.getStartTime());
            assertEquals(LocalTime.of(12,0), booked.getEndTime());
        }

        @Test
        @DisplayName("Throws when no covering active slot")
        void noCoveringSlot() {
            // Active slot 9-10, booking 10-11 (edge not inside)
            ProviderWorkingSlot active = ProviderWorkingSlot.builder()
                    .id(4L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(9,0)).endTime(LocalTime.of(10,0))
                    .status(WorkingSlotStatus.ACTIVE).source(WorkingSlotSource.RECURRING).build();
            when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, DATE)).thenReturn(List.of(active));
            assertThrows(IllegalStateException.class, () -> availabilityService.applyBooking(PROVIDER_ID, DATE, LocalTime.of(10,0), LocalTime.of(11,0)));
            verify(workingSlotRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("Throws when overlaps existing booked segment")
        void overlapsBooked() {
            ProviderWorkingSlot booked = ProviderWorkingSlot.builder()
                    .id(5L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(10,0)).endTime(LocalTime.of(11,0))
                    .status(WorkingSlotStatus.BOOKED).source(WorkingSlotSource.RECURRING).build();
            ProviderWorkingSlot active = ProviderWorkingSlot.builder()
                    .id(6L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(9,0)).endTime(LocalTime.of(12,0))
                    .status(WorkingSlotStatus.ACTIVE).source(WorkingSlotSource.RECURRING).build();
            when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, DATE)).thenReturn(List.of(booked, active));
            assertThrows(IllegalStateException.class, () -> availabilityService.applyBooking(PROVIDER_ID, DATE, LocalTime.of(10,30), LocalTime.of(11,30)));
            verify(workingSlotRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("cancelBooking")
    class CancelBooking {

        @Test
        @DisplayName("Merges left + booked + right into single active slot")
        void mergeThree() {
            ProviderWorkingSlot left = ProviderWorkingSlot.builder().id(10L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(9,0)).endTime(LocalTime.of(10,0)).status(WorkingSlotStatus.ACTIVE).source(WorkingSlotSource.RECURRING).build();
            ProviderWorkingSlot booked = ProviderWorkingSlot.builder().id(11L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(10,0)).endTime(LocalTime.of(11,0)).status(WorkingSlotStatus.BOOKED).source(WorkingSlotSource.RECURRING).build();
            ProviderWorkingSlot right = ProviderWorkingSlot.builder().id(12L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(11,0)).endTime(LocalTime.of(12,0)).status(WorkingSlotStatus.ACTIVE).source(WorkingSlotSource.RECURRING).build();
            when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, DATE)).thenReturn(List.of(left, booked, right));

            ArgumentCaptor<ProviderWorkingSlot> saveCaptor = ArgumentCaptor.forClass(ProviderWorkingSlot.class);
            when(workingSlotRepository.save(saveCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            availabilityService.cancelBooking(PROVIDER_ID, DATE, LocalTime.of(10,0), LocalTime.of(11,0));

            verify(workingSlotRepository).delete(booked);
            verify(workingSlotRepository).delete(left);
            verify(workingSlotRepository).delete(right);
            ProviderWorkingSlot merged = saveCaptor.getValue();
            assertEquals(WorkingSlotStatus.ACTIVE, merged.getStatus());
            assertEquals(LocalTime.of(9,0), merged.getStartTime());
            assertEquals(LocalTime.of(12,0), merged.getEndTime());
        }

        @Test
        @DisplayName("Merges left + booked into single active slot when no right neighbor")
        void mergeLeftOnly() {
            ProviderWorkingSlot left = ProviderWorkingSlot.builder().id(20L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(9,0)).endTime(LocalTime.of(10,0)).status(WorkingSlotStatus.ACTIVE).source(WorkingSlotSource.RECURRING).build();
            ProviderWorkingSlot booked = ProviderWorkingSlot.builder().id(21L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(10,0)).endTime(LocalTime.of(11,0)).status(WorkingSlotStatus.BOOKED).source(WorkingSlotSource.RECURRING).build();
            when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, DATE)).thenReturn(List.of(left, booked));
            ArgumentCaptor<ProviderWorkingSlot> saveCaptor = ArgumentCaptor.forClass(ProviderWorkingSlot.class);
            when(workingSlotRepository.save(saveCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            availabilityService.cancelBooking(PROVIDER_ID, DATE, LocalTime.of(10,0), LocalTime.of(11,0));
            verify(workingSlotRepository).delete(booked);
            verify(workingSlotRepository).delete(left);
            ProviderWorkingSlot merged = saveCaptor.getValue();
            assertEquals(LocalTime.of(9,0), merged.getStartTime());
            assertEquals(LocalTime.of(11,0), merged.getEndTime());
        }

        @Test
        @DisplayName("Merges booked + right into single active slot when no left neighbor")
        void mergeRightOnly() {
            ProviderWorkingSlot booked = ProviderWorkingSlot.builder().id(30L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(10,0)).endTime(LocalTime.of(11,0)).status(WorkingSlotStatus.BOOKED).source(WorkingSlotSource.RECURRING).build();
            ProviderWorkingSlot right = ProviderWorkingSlot.builder().id(31L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(11,0)).endTime(LocalTime.of(12,0)).status(WorkingSlotStatus.ACTIVE).source(WorkingSlotSource.RECURRING).build();
            when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, DATE)).thenReturn(List.of(booked, right));
            ArgumentCaptor<ProviderWorkingSlot> saveCaptor = ArgumentCaptor.forClass(ProviderWorkingSlot.class);
            when(workingSlotRepository.save(saveCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            availabilityService.cancelBooking(PROVIDER_ID, DATE, LocalTime.of(10,0), LocalTime.of(11,0));
            verify(workingSlotRepository).delete(booked);
            verify(workingSlotRepository).delete(right);
            ProviderWorkingSlot merged = saveCaptor.getValue();
            assertEquals(LocalTime.of(10,0), merged.getStartTime());
            assertEquals(LocalTime.of(12,0), merged.getEndTime());
        }

        @Test
        @DisplayName("Restores a standalone active slot when no neighbors exist")
        void standaloneRestore() {
            ProviderWorkingSlot booked = ProviderWorkingSlot.builder().id(40L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(10,0)).endTime(LocalTime.of(11,0)).status(WorkingSlotStatus.BOOKED).source(WorkingSlotSource.RECURRING).build();
            when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, DATE)).thenReturn(List.of(booked));
            ArgumentCaptor<ProviderWorkingSlot> saveCaptor = ArgumentCaptor.forClass(ProviderWorkingSlot.class);
            when(workingSlotRepository.save(saveCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            availabilityService.cancelBooking(PROVIDER_ID, DATE, LocalTime.of(10,0), LocalTime.of(11,0));
            verify(workingSlotRepository).delete(booked);
            ProviderWorkingSlot restored = saveCaptor.getValue();
            assertEquals(WorkingSlotStatus.ACTIVE, restored.getStatus());
            assertEquals(LocalTime.of(10,0), restored.getStartTime());
            assertEquals(LocalTime.of(11,0), restored.getEndTime());
        }

        @Test
        @DisplayName("Throws when booked segment not found")
        void bookedNotFound() {
            ProviderWorkingSlot active = ProviderWorkingSlot.builder().id(50L).providerId(PROVIDER_ID).date(DATE)
                    .startTime(LocalTime.of(9,0)).endTime(LocalTime.of(12,0)).status(WorkingSlotStatus.ACTIVE).source(WorkingSlotSource.RECURRING).build();
            when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, DATE)).thenReturn(List.of(active));
            assertThrows(IllegalStateException.class, () -> availabilityService.cancelBooking(PROVIDER_ID, DATE, LocalTime.of(10,0), LocalTime.of(11,0)));
            verify(workingSlotRepository, never()).save(any());
        }
    }
}

