package io.mazurkiewicz.gifter.draw;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DrawEditionRepository extends JpaRepository<DrawEditionEntity, Long> {

    List<DrawEditionEntity> findAllByGroupPublicId(String groupPublicId);

    List<DrawEditionEntity> findAllByStatusAndGroupPublicIdIn(EditionStatus status, Collection<String> groupPublicId);
}
