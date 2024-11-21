package io.mazurkiewicz.gifter.draw;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity(name = "draw_editions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Service
public class DrawEditionEntity {

    @Id
    @GeneratedValue
    private Long id;
    private UUID editionPublicId;
    private String name;
    private ZonedDateTime createdAt;
    private String groupPublicId;
    @OneToMany(mappedBy="drawEdition")
    private List<Assignment> assignments;
    @Enumerated(EnumType.STRING)
    private EditionStatus status;

    public DrawEditionEntity(UUID editionPublicId, String name, ZonedDateTime createdAt, String groupPublicId) {
        this.editionPublicId = editionPublicId;
        this.name = name;
        this.createdAt = createdAt;
        this.groupPublicId = groupPublicId;
        this.status = EditionStatus.ACTIVE;
    }
}
