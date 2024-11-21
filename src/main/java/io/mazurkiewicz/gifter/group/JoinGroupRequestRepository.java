package io.mazurkiewicz.gifter.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JoinGroupRequestRepository extends JpaRepository<JoinGroupRequestEntity, Long> {

    List<JoinGroupRequestEntity> findAllByUserPublicIdAndStatus(String userPublicId, RequestStatus status);
    List<JoinGroupRequestEntity> findAllByStatus(RequestStatus status);

}
