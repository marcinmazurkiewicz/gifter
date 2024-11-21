package io.mazurkiewicz.gifter.wishlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishItemRepository extends JpaRepository<WishItemEntity, Long> {

    List<WishItemEntity> findAllByUserPublicId(String userPublicId);

    Optional<WishItemEntity> findByItemPublicId(UUID itemPublicId);
}

