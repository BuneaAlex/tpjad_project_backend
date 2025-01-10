package com.example.tpjad_project_backend.model.timeslot;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timeslot")
@Builder(setterPrefix = "with")
public class Timeslot {

    @Id
    private String uuid;

    @PrePersist
    public void generateUUID() {
        this.uuid = UUID.randomUUID().toString();
    }

    private String userId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "meeting_id", referencedColumnName = "id")
    private MeetingDetails meeting;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "start.hour", column = @Column(name = "start_hour")),
            @AttributeOverride(name = "start.minutes", column = @Column(name = "start_minutes")),
            @AttributeOverride(name = "end.hour", column = @Column(name = "end_hour")),
            @AttributeOverride(name = "end.minutes", column = @Column(name = "end_minutes"))
    })
    private Interval interval;
    private LocalDate date;
}
