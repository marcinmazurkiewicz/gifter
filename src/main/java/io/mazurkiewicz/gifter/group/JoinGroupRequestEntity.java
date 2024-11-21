package io.mazurkiewicz.gifter.group;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "join_group_requests")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JoinGroupRequestEntity {

    @Id
    @GeneratedValue
    Long id;
    String groupPublicId;
    String userPublicId;
    @Enumerated(EnumType.STRING)
    RequestStatus status;

    public JoinGroupRequestEntity(String groupPublicId, String userPublicId) {
        this.groupPublicId = groupPublicId;
        this.userPublicId = userPublicId;
        this.status = RequestStatus.PENDING;
    }
}
