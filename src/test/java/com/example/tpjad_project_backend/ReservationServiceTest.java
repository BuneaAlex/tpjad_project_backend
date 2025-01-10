package com.example.tpjad_project_backend;

import com.example.tpjad_project_backend.model.timeslot.*;
import com.example.tpjad_project_backend.schedule.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class ReservationServiceTest {

    private final static String ROOM_01 = "room-01";
    private final static String ROOM_02 = "room-02";

    private final static Timeslot TIMESLOT_01 = Timeslot.builder()
            .withUserId("user-01")
            .withMeeting(MeetingDetails.builder()
                    .withName("Meeting-01")
                    .withType("Type-01")
                    .withRoom(ROOM_01)
                    .build())
            .withInterval(Interval.builder()
                    .withStart(new Time(12, 0))
                    .withEnd(new Time(13, 0))
                    .build())
            .withDate(LocalDate.now().minusDays(1))
            .build();

    private final static Timeslot TIMESLOT_02 = Timeslot.builder()
            .withUserId("user-02")
            .withMeeting(MeetingDetails.builder()
                    .withName("Meeting-02")
                    .withType("Type-02")
                    .withRoom(ROOM_02)
                    .build())
            .withInterval(Interval.builder()
                    .withStart(new Time(12, 0))
                    .withEnd(new Time(13, 0))
                    .build())
            .withDate(LocalDate.now().minusDays(1))
            .build();

    private final static Timeslot TIMESLOT_03 = Timeslot.builder()
            .withUserId("user-01")
            .withMeeting(MeetingDetails.builder()
                    .withName("Meeting-03")
                    .withType("Type-01")
                    .withRoom(ROOM_01)
                    .build())
            .withInterval(Interval.builder()
                    .withStart(new Time(11, 0))
                    .withEnd(new Time(13, 0))
                    .build())
            .withDate(LocalDate.now().minusDays(1))
            .build();

    private final static Timeslot TIMESLOT_INVALID_INTERVAL = Timeslot.builder()
            .withUserId("user-01")
            .withMeeting(MeetingDetails.builder()
                    .withName("Meeting-03")
                    .withType("Type-01")
                    .withRoom(ROOM_01)
                    .build())
            .withInterval(Interval.builder()
                    .withStart(new Time(11, 0))
                    .withEnd(new Time(10, 0))
                    .build())
            .withDate(LocalDate.now().minusDays(1))
            .build();

    @Autowired
    private ReservationService reservationService;

    @BeforeEach
    public void setup() {
        List<Timeslot> beforeTodayTimeslots = reservationService.getAllTimeslotsEndingFrom(LocalDate.now());
        List<Timeslot> afterTodayTimeslots = reservationService.getAllTimeslotsStartingFrom(LocalDate.now());
        assertEquals(0, beforeTodayTimeslots.size());
        assertEquals(0, afterTodayTimeslots.size());
    }

    @AfterEach
    public void teardown() {
        List<Timeslot> beforeTodayTimeslots = reservationService.getAllTimeslotsEndingFrom(LocalDate.now());
        List<Timeslot> afterTodayTimeslots = reservationService.getAllTimeslotsStartingFrom(LocalDate.now());
        List<Timeslot> allTimeslots = new ArrayList<>();
        allTimeslots.addAll(beforeTodayTimeslots);
        allTimeslots.addAll(afterTodayTimeslots);
        allTimeslots.forEach(timeslot -> {
            try {
                reservationService.deleteTimeslot(timeslot.getUuid(), timeslot.getUserId());
            } catch (Exception ignored) {

            }
        });
    }


    /**
     * TIMESLOT_01 and TIMESLOT_03 overlap, so it should return an error
     */
    @Test
    public void testFindTimeslotConflicts() {
        AtomicInteger errors = new AtomicInteger();
        AtomicInteger otherErrors = new AtomicInteger();
        List.of(TIMESLOT_01, TIMESLOT_03)
                .forEach(timeslot -> {
                    try {
                        reservationService.addTimeslot(timeslot);
                    } catch (AlreadyBookedException e) {
                        e.printStackTrace();
                        errors.getAndIncrement();
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                        otherErrors.getAndIncrement();
                    }
                });
        assertEquals(1, errors.get());
        assertEquals(0, otherErrors.get());
        assertConfirmedTimeslots(1);
    }

    @Test
    public void testTimeslotInvalidInterval() {
        AtomicInteger errors = new AtomicInteger();
        AtomicInteger otherErrors = new AtomicInteger();
        List.of(TIMESLOT_03, TIMESLOT_INVALID_INTERVAL)
                .forEach(timeslot -> {
                    try {
                        reservationService.addTimeslot(timeslot);
                    } catch (InvalidTimeslotException e) {
                        e.printStackTrace();
                        errors.getAndIncrement();
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                        otherErrors.getAndIncrement();
                    }
                });
        assertEquals(1, errors.get());
        assertEquals(0, otherErrors.get());
        assertConfirmedTimeslots(1);
    }

    @Test
    public void testFailToDeleteReservation() {
        AtomicInteger errors = new AtomicInteger();
        AtomicInteger otherErrors = new AtomicInteger();
        List.of(TIMESLOT_02)
                .forEach(timeslot -> {
                    try {
                        Timeslot tmpTimeslot = reservationService.addTimeslot(timeslot);
                        try {
                            //try to delete with other user
                            reservationService.deleteTimeslot(tmpTimeslot.getUuid(), "user-01");
                        } catch (AccessDeniedException e) {
                            otherErrors.getAndIncrement();
                        }
                        try {
                            reservationService.deleteTimeslot(tmpTimeslot.getUuid(), "user-02");
                        } catch (AccessDeniedException e) {
                            otherErrors.getAndIncrement();
                        }
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                        errors.getAndIncrement();
                    }
                });
        assertEquals(0, errors.get());
        assertEquals(1, otherErrors.get());
        assertConfirmedTimeslots(0);
    }

    private void assertConfirmedTimeslots(int expected) {
        List<Timeslot> beforeTodayTimeslots = reservationService.getAllTimeslotsEndingFrom(LocalDate.now());
        List<Timeslot> afterTodayTimeslots = reservationService.getAllTimeslotsStartingFrom(LocalDate.now());
        assertEquals(expected, beforeTodayTimeslots.size() + afterTodayTimeslots.size());
    }
}
