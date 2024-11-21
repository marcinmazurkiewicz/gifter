package io.mazurkiewicz.gifter.group;

import io.mazurkiewicz.gifter.error.NotFoundException;

public class GroupNotFoundException extends NotFoundException {

    public GroupNotFoundException(String userPublicId) {
        super("Nie znaleziono grupy %s".formatted(userPublicId), "GFTR_1007", new Object[]{userPublicId});
    }
}
