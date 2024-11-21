package io.mazurkiewicz.gifter.user;

import io.mazurkiewicz.gifter.error.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String userPublicId) {
        super("Nie znaleziono użytkownika %s".formatted(userPublicId), "GFTR_1005", new Object[]{userPublicId});
    }
}
