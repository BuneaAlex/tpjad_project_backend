package com.example.tpjad_project_backend.model.timeslot;

public class AlreadyBookedException extends Exception {
    public AlreadyBookedException(String message) {
        super(message);
    }
}
