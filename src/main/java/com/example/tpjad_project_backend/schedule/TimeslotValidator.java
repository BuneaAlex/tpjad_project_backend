package com.example.tpjad_project_backend.schedule;

import com.example.tpjad_project_backend.model.timeslot.Time;
import com.example.tpjad_project_backend.model.timeslot.Timeslot;
import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class TimeslotValidator {
    public static String validateTimeslot(Timeslot timeslot) {
        StringBuilder errorBuilder = new StringBuilder();
        if (Objects.isNull(timeslot.getUserId()) || timeslot.getUserId().isEmpty()) {
            errorBuilder.append("User id is required!\n");
        }
        if (Objects.isNull(timeslot.getMeeting())) {
            errorBuilder.append("Meeting is required!\n");
        } else {
            if (Objects.isNull(timeslot.getMeeting().getName()) || timeslot.getMeeting().getName().isEmpty()) {
                errorBuilder.append("Meeting name is required!\n");
            }
            if (Objects.isNull(timeslot.getMeeting().getType()) || timeslot.getMeeting().getType().isEmpty()) {
                errorBuilder.append("Meeting type is required!\n");
            }
            if (Objects.isNull(timeslot.getMeeting().getRoom()) || timeslot.getMeeting().getRoom().isEmpty()) {
                errorBuilder.append("Meeting room is required!\n");
            }
        }
        if (Objects.isNull(timeslot.getInterval())) {
            errorBuilder.append("Interval is required!\n");
        } else {
            Time startTime = timeslot.getInterval().getStart();
            Time endTime = timeslot.getInterval().getEnd();
            if (Objects.isNull(startTime) || Objects.isNull(endTime)) {
                errorBuilder.append("Interval start and end time are required!\n");
            } else {
                if (startTime.getHour() > endTime.getHour() ||
                        (startTime.getHour() == endTime.getHour() && startTime.getMinutes() > endTime.getMinutes())) {
                    errorBuilder.append("Start time must be before end time!\n");
                }
            }
        }
        if (Objects.isNull(timeslot.getDate())) {
            errorBuilder.append("Date is required!\n");
        }

        return errorBuilder.toString();
    }
}
