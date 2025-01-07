package com.example.tpjad_project_backend.schedule;

import com.example.tpjad_project_backend.model.timeslot.AlreadyBookedException;
import com.example.tpjad_project_backend.model.timeslot.InvalidTimeslotException;
import com.example.tpjad_project_backend.model.timeslot.Timeslot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ReservationService {

    @Autowired
    private TimeslotRepository timeslotRepository;

    public Timeslot addTimeslot(Timeslot timeslot) throws AlreadyBookedException, InvalidTimeslotException {
        String errors = TimeslotValidator.validateTimeslot(timeslot);
        if (!errors.isEmpty()) {
            throw new InvalidTimeslotException(errors);
        }
        List<Timeslot> timeSlotsInInterval = timeslotRepository.findTimeslotsOverlapping(
                timeslot.getMeeting().getRoom(),
                timeslot.getDate(),
                timeslot.getInterval().getStart().getHour(),
                timeslot.getInterval().getStart().getMinutes(),
                timeslot.getInterval().getEnd().getHour(),
                timeslot.getInterval().getEnd().getMinutes());
        if (timeSlotsInInterval.isEmpty()) {
            return timeslotRepository.save(timeslot);
        }
        throw new AlreadyBookedException("Unable to book meeting");
    }

    public Timeslot deleteTimeslot(String uuid, String requestingUser) throws Exception {
        AtomicReference<Timeslot> toBeDeleted = new AtomicReference<>();
        timeslotRepository.findById(uuid).ifPresent(timeslot -> {
            if (timeslot.getUserId().equals(requestingUser)) {
                timeslotRepository.deleteById(uuid);
                toBeDeleted.set(timeslot);
            } else {
                throw new AccessDeniedException("You don't have permission to delete this timeslot");
            }
        });
        if (toBeDeleted.get() != null) {
            return toBeDeleted.get();
        }
        throw new Exception("Unable to delete meeting");
    }

    public List<Timeslot> getAllTimeslotsStartingFrom(LocalDate startingDate) {
        return timeslotRepository.findByDateAfter(startingDate);
    }

    public List<Timeslot> getAllTimeslotsEndingFrom(LocalDate endingDate) {
        return timeslotRepository.findByDateBefore(endingDate);
    }

    public List<Timeslot> getAllTimeslotsForDate(LocalDate date) {
        return timeslotRepository.findAllByDate(date);
    }

}
