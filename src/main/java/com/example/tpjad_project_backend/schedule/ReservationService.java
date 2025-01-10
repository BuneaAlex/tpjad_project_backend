package com.example.tpjad_project_backend.schedule;

import com.example.tpjad_project_backend.email.EmailDto;
import com.example.tpjad_project_backend.email.EmailService;
import com.example.tpjad_project_backend.model.timeslot.AlreadyBookedException;
import com.example.tpjad_project_backend.model.timeslot.InvalidTimeslotException;
import com.example.tpjad_project_backend.model.timeslot.Timeslot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ReservationService {

    @Autowired
    private TimeslotRepository timeslotRepository;

    @Autowired
    private EmailService emailService;

    public Timeslot addTimeslot(Timeslot timeslot) throws AlreadyBookedException, InvalidTimeslotException {
        String errors = TimeslotValidator.validateTimeslot(timeslot);
        if (!errors.isEmpty()) {
            throw new InvalidTimeslotException(errors);
        }

        LocalDate date = timeslot.getDate();
        int startHour = timeslot.getInterval().getStart().getHour();
        int startMinute = timeslot.getInterval().getStart().getMinutes();
        int endHour = timeslot.getInterval().getEnd().getHour();
        int endMinute = timeslot.getInterval().getEnd().getMinutes();

        List<Timeslot> timeSlotsInInterval = timeslotRepository.findTimeslotsOverlapping(
                timeslot.getMeeting().getRoom(),
                date,
                startHour,
                startMinute,
                endHour,
                endMinute);
        if (timeSlotsInInterval.isEmpty()) {
            LocalDateTime startDateTime = LocalDateTime.of(date, LocalTime.of(startHour, startMinute));
            LocalDateTime endDateTime = LocalDateTime.of(date, LocalTime.of(endHour, endMinute));
            EmailDto emailDto = EmailDto.builder().title(timeslot.getMeeting().getName()).
                    startDateTime(startDateTime).endDateTime(endDateTime).
                    createdAt(LocalDateTime.now()).
                    updatedAt(LocalDateTime.now()).build();
            String userEmail = timeslot.getUserId();
            emailService.sendReservationNotification(userEmail, emailDto);

            //sendReminder(emailDto, userEmail);
            return timeslotRepository.save(timeslot);
        }
        throw new AlreadyBookedException("Unable to book meeting");
    }

    private void sendReminder(EmailDto emailDto, String userEmail) {
        long minutesUntilReservation = LocalDateTime.now().until(emailDto.getStartDateTime(), ChronoUnit.MINUTES);
        long delay = Math.max(0, minutesUntilReservation - 1440);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> {
            emailService.sendReservationReminder(userEmail, emailDto);
            executorService.shutdown();
        }, delay, TimeUnit.MINUTES);
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
        return timeslotRepository.findByDateGreaterThanEqual(startingDate);
    }

    public List<Timeslot> getAllTimeslotsEndingFrom(LocalDate endingDate) {
        return timeslotRepository.findByDateBefore(endingDate);
    }

    public List<Timeslot> getAllTimeslotsForDate(LocalDate date) {
        return timeslotRepository.findAllByDate(date);
    }

}
