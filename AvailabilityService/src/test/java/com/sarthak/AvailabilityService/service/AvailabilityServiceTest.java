package com.sarthak.AvailabilityService.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private ProviderWorkingSlotRepository workingSlotRepository;
    @Mock
    private ProviderRecurringRuleRepository recurringRuleRepository;
    @Mock
    private ProviderAvailabilityExceptionRepository exceptionRepository;

    private ProviderScheduleMapper scheduleMapper;
    private AvailabilityService availabilityService;

    private final Long PROVIDER_ID = 77L;
    private final LocalDate TEST_DATE = LocalDate.of(2025, 9, 5); // Friday

    @BeforeEach
    void setUp(){
        scheduleMapper = new ProviderScheduleMapper();
        availabilityService = new AvailabilityService(workingSlotRepository, recurringRuleRepository, exceptionRepository, scheduleMapper);
    }

    @Test
    @DisplayName("createWorkingSlot sets defaults and persists when no overlap")
    void createWorkingSlotSuccess(){
        ProviderWorkingSlotDto dto = new ProviderWorkingSlotDto();
        dto.setProviderId(PROVIDER_ID);
        dto.setDate(TEST_DATE.toString());
        dto.setStartTime("09:00");
        dto.setEndTime("11:00");
        when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, TEST_DATE)).thenReturn(List.of());
        when(workingSlotRepository.save(any())).thenAnswer(inv -> {
            ProviderWorkingSlot slot = inv.getArgument(0);
            slot.setId(100L); // simulate DB generated id
            return slot;
        });

        ProviderWorkingSlotDto saved = availabilityService.createWorkingSlot(dto);
        assertNotNull(saved.getId());
        assertEquals(WorkingSlotStatus.ACTIVE, saved.getStatus());
        assertEquals(WorkingSlotSource.RECURRING, saved.getSource());

        ArgumentCaptor<ProviderWorkingSlot> captor = ArgumentCaptor.forClass(ProviderWorkingSlot.class);
        verify(workingSlotRepository).save(captor.capture());
        ProviderWorkingSlot entity = captor.getValue();
        assertEquals(LocalTime.of(9,0), entity.getStartTime());
        assertEquals(LocalTime.of(11,0), entity.getEndTime());
    }

    @Test
    @DisplayName("createWorkingSlot rejects overlapping slot")
    void createWorkingSlotOverlap(){
        ProviderWorkingSlot existing = ProviderWorkingSlot.builder()
                .id(1L).providerId(PROVIDER_ID).date(TEST_DATE)
                .startTime(LocalTime.of(9,0)).endTime(LocalTime.of(11,0))
                .status(WorkingSlotStatus.ACTIVE).source(WorkingSlotSource.RECURRING).build();
        when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, TEST_DATE)).thenReturn(List.of(existing));

        ProviderWorkingSlotDto dto = new ProviderWorkingSlotDto();
        dto.setProviderId(PROVIDER_ID);
        dto.setDate(TEST_DATE.toString());
        dto.setStartTime("10:30");
        dto.setEndTime("12:00");

        assertThrows(IllegalArgumentException.class, () -> availabilityService.createWorkingSlot(dto));
        verify(workingSlotRepository, never()).save(any());
    }

    @Test
    @DisplayName("getEffectiveSlotsForDate returns override slot when OVERRIDE exception present")
    void effectiveSlotsOverride(){
        ProviderAvailabilityException exception = ProviderAvailabilityException.builder()
                .id(10L).providerId(PROVIDER_ID).date(TEST_DATE).status(ProviderExceptionStatus.OVERRIDE)
                .overrideStartTime(LocalTime.of(13,0)).overrideEndTime(LocalTime.of(15,0)).build();
        when(exceptionRepository.findByProviderIdAndDate(PROVIDER_ID, TEST_DATE)).thenReturn(Optional.of(exception));

        List<ProviderWorkingSlotDto> slots = availabilityService.getEffectiveSlotsForDate(PROVIDER_ID, TEST_DATE);
        assertEquals(1, slots.size());
        assertEquals("13:00", slots.get(0).getStartTime().substring(0,5));
        assertEquals("15:00", slots.get(0).getEndTime().substring(0,5));
        assertEquals(WorkingSlotSource.EXCEPTION, slots.get(0).getSource());
    }

    @Test
    @DisplayName("getEffectiveSlotsForDate returns empty when BLOCKED exception present")
    void effectiveSlotsBlocked(){
        ProviderAvailabilityException exception = ProviderAvailabilityException.builder()
                .id(11L).providerId(PROVIDER_ID).date(TEST_DATE).status(ProviderExceptionStatus.BLOCKED).build();
        when(exceptionRepository.findByProviderIdAndDate(PROVIDER_ID, TEST_DATE)).thenReturn(Optional.of(exception));

        List<ProviderWorkingSlotDto> slots = availabilityService.getEffectiveSlotsForDate(PROVIDER_ID, TEST_DATE);
        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("getEffectiveSlotsForDate uses concrete slots and merges touching ones")
    void effectiveSlotsConcreteMerge(){
        when(exceptionRepository.findByProviderIdAndDate(PROVIDER_ID, TEST_DATE)).thenReturn(Optional.empty());
        ProviderWorkingSlot slot1 = ProviderWorkingSlot.builder().id(1L).providerId(PROVIDER_ID).date(TEST_DATE)
                .startTime(LocalTime.of(9,0)).endTime(LocalTime.of(10,0)).status(WorkingSlotStatus.ACTIVE).source(WorkingSlotSource.RECURRING).build();
        ProviderWorkingSlot slot2 = ProviderWorkingSlot.builder().id(2L).providerId(PROVIDER_ID).date(TEST_DATE)
                .startTime(LocalTime.of(10,0)).endTime(LocalTime.of(11,0)).status(WorkingSlotStatus.ACTIVE).source(WorkingSlotSource.RECURRING).build();
        when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, TEST_DATE)).thenReturn(List.of(slot1, slot2));

        List<ProviderWorkingSlotDto> slots = availabilityService.getEffectiveSlotsForDate(PROVIDER_ID, TEST_DATE);
        assertEquals(1, slots.size());
        assertEquals("09:00", slots.get(0).getStartTime().substring(0,5));
        assertEquals("11:00", slots.get(0).getEndTime().substring(0,5));
    }

    @Test
    @DisplayName("getEffectiveSlotsForDate falls back to recurring rules when no exception or concrete")
    void effectiveSlotsRecurringFallback(){
        when(exceptionRepository.findByProviderIdAndDate(PROVIDER_ID, TEST_DATE)).thenReturn(Optional.empty());
        when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, TEST_DATE)).thenReturn(List.of());
        ProviderRecurringRule rule = ProviderRecurringRule.builder()
                .id(20L).providerId(PROVIDER_ID).dayOfWeek(DayOfWeek.FRIDAY)
                .startTime(LocalTime.of(14,0)).endTime(LocalTime.of(18,0)).active(true).build();
        when(recurringRuleRepository.findByProviderIdAndDayOfWeekAndActiveTrue(PROVIDER_ID, DayOfWeek.FRIDAY))
                .thenReturn(List.of(rule));

        List<ProviderWorkingSlotDto> slots = availabilityService.getEffectiveSlotsForDate(PROVIDER_ID, TEST_DATE);
        assertEquals(1, slots.size());
        assertEquals("14:00", slots.get(0).getStartTime().substring(0,5));
        assertEquals("18:00", slots.get(0).getEndTime().substring(0,5));
        assertEquals(WorkingSlotSource.RECURRING, slots.get(0).getSource());
    }

    @Nested
    @DisplayName("Validation edge cases")
    class ValidationEdgeCases {
        @Test
        @DisplayName("createWorkingSlot allows touching non-overlapping earlier slot (end == start)")
        void touchingSlotsAllowed(){
            ProviderWorkingSlot existing = ProviderWorkingSlot.builder()
                    .id(1L).providerId(PROVIDER_ID).date(TEST_DATE)
                    .startTime(LocalTime.of(9,0)).endTime(LocalTime.of(10,0))
                    .status(WorkingSlotStatus.ACTIVE).source(WorkingSlotSource.RECURRING).build();
            when(workingSlotRepository.findByProviderIdAndDate(PROVIDER_ID, TEST_DATE)).thenReturn(List.of(existing));
            when(workingSlotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ProviderWorkingSlotDto dto = new ProviderWorkingSlotDto();
            dto.setProviderId(PROVIDER_ID);
            dto.setDate(TEST_DATE.toString());
            dto.setStartTime("10:00");
            dto.setEndTime("11:00");

            ProviderWorkingSlotDto saved = availabilityService.createWorkingSlot(dto);
            assertEquals("10:00", saved.getStartTime().substring(0,5));
        }
    }
}
