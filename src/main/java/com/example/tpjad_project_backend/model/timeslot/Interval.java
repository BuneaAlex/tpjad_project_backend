package com.example.tpjad_project_backend.model.timeslot;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Builder(setterPrefix = "with")
public class Interval {
    private Time start;
    private Time end;
}
