package io.mazurkiewicz.gifter.wishlist;

import io.mazurkiewicz.gifter.user.ItemForbiddenActionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishItemRepository wishItemRepository;

    public UUID addItem(String userPublicId, String itemName, String link) {
        WishItemEntity wishItemEntity = new WishItemEntity(userPublicId, itemName, link);
        wishItemRepository.save(wishItemEntity);
        return wishItemEntity.getItemPublicId();
    }

    public List<WishItem> findItemsForUser(String userPublicId) {
        return wishItemRepository.findAllByUserPublicId(userPublicId)
                .stream()
                .map(entity -> new WishItem(entity.getItemPublicId(), entity.getName(), entity.getLink()))
                .toList();
    }

    @Transactional
    public void deleteItem(String userPublicId, UUID itemPublicId) {
        WishItemEntity wishItemEntity = wishItemRepository.findByItemPublicId(itemPublicId)
                .orElseThrow(() -> new ItemNotFoundException(itemPublicId));

        if(wishItemEntity.getUserPublicId().equals(userPublicId)) {
            wishItemRepository.delete(wishItemEntity);
        } else {
            throw new ItemForbiddenActionException();
        }
    }

    public record WishItem(UUID itemPublicId, String itemName, String link) {}
}
