package com.example.tpjad_project_backend.schedule;

import com.example.tpjad_project_backend.model.timeslot.Timeslot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeslotRepository extends JpaRepository<Timeslot, String> {

    List<Timeslot> findByDateBefore(LocalDate date);

    List<Timeslot> findByDateGreaterThanEqual(LocalDate date);

    List<Timeslot> findAllByDate(LocalDate date);

    @Query("""
    SELECT t FROM Timeslot t
    WHERE t.date = :date
    AND t.meeting.room = :room
    AND (
        (t.interval.start.hour < :endHour OR\s
        (t.interval.start.hour = :endHour AND t.interval.start.minutes < :endMinute))
        AND
        (t.interval.end.hour > :startHour OR\s
        (t.interval.end.hour = :startHour AND t.interval.end.minutes > :startMinute))
    )
""")
    List<Timeslot> findTimeslotsOverlapping(
            @Param("room") String room,
            @Param("date") LocalDate date,
            @Param("startHour") int startHour,
            @Param("startMinute") int startMinute,
            @Param("endHour") int endHour,
            @Param("endMinute") int endMinute
    );
}