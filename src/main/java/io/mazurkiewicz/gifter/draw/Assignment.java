package io.mazurkiewicz.gifter.draw;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Entity(name = "assignments")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Service
public class Assignment {

    @Id
    @GeneratedValue
    private Long id;
    private ZonedDateTime assignedAt;
    private String donor;
    private String receiver;
    @ManyToOne
    @JoinColumn(name="edition_id", nullable=false)
    private DrawEditionEntity drawEdition;

    public Assignment(String donor, String receiver, DrawEditionEntity drawEdition) {
        this.assignedAt = ZonedDateTime.now();
        this.donor = donor;
        this.receiver = receiver;
        this.drawEdition = drawEdition;
    }
}
