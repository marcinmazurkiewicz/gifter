package io.mazurkiewicz.gifter.user;

import io.mazurkiewicz.gifter.draw.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentsRepository extends JpaRepository<Assignment, Long> {
}
