package io.mazurkiewicz.gifter.wishlist;

import io.mazurkiewicz.gifter.error.NotFoundException;

import java.util.UUID;

public class ItemNotFoundException extends NotFoundException {
    public ItemNotFoundException(UUID itemPublicId) {
      super("Nie znaleziono prezentu o id %s".formatted(itemPublicId), "GFTR_1006", new Object[]{itemPublicId});
    }
}
