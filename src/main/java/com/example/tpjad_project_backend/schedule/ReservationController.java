package com.example.tpjad_project_backend.schedule;

import com.example.tpjad_project_backend.model.timeslot.AlreadyBookedException;
import com.example.tpjad_project_backend.model.timeslot.InvalidTimeslotException;
import com.example.tpjad_project_backend.model.timeslot.Timeslot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservation")
@CrossOrigin(origins = "http://localhost:3000")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/beforeDate")
    public ResponseEntity<List<Timeslot>> getBeforeDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        return ResponseEntity.ok(reservationService.getAllTimeslotsEndingFrom(date));
    }

    @GetMapping("/afterDate")
    public ResponseEntity<List<Timeslot>> getAfterDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        return ResponseEntity.ok(reservationService.getAllTimeslotsStartingFrom(date));
    }

    @GetMapping("/exactDate")
    public ResponseEntity<List<Timeslot>> getAll(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(reservationService.getAllTimeslotsForDate(date));
    }

    @PostMapping("/new")
    public ResponseEntity<?> addReservation(@RequestBody Timeslot timeslot) {
        try {
            return ResponseEntity.ok(reservationService.addTimeslot(timeslot));
        } catch (AlreadyBookedException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (InvalidTimeslotException e) {
            return ResponseEntity.badRequest().body("Timeslot contains errors!\n" + e.getMessage());
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> deleteReservation(@RequestParam String uuid, @RequestParam String userId) {
        try {
            return ResponseEntity.ok(reservationService.deleteTimeslot(uuid, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
