package com.example.tpjad_project_backend.model.timeslot;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "meeting_details")
@Builder(setterPrefix = "with")
public class MeetingDetails {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String type;
    private String room;
}
